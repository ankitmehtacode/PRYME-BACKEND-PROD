import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig } from "axios";

export type ApiErrorPayload = {
  code?: string;
  message?: string;
};

export class ApiClientError extends Error {
  status?: number;
  code?: string;
  details?: unknown;

  constructor(message: string, status?: number, code?: string, details?: unknown) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api";

const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 20000,
  withCredentials: false,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem("pryme_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  if (config.url?.includes("/api/v1/public/leads") && config.method?.toLowerCase() === "post") {
    config.headers["Idempotency-Key"] = crypto.randomUUID();
  }

  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError<ApiErrorPayload>) => {
    const status = error.response?.status;
    const payload = error.response?.data;

    if (status === 401 || status === 403) {
      localStorage.removeItem("pryme_token");

      window.dispatchEvent(
        new CustomEvent("pryme:session-expired", {
          detail: { message: "Session expired. Please login again." },
        })
      );
      if (window.location.pathname !== "/auth") {
        window.location.href = "/auth";
      }
    }

    throw new ApiClientError(
      payload?.message ?? error.message ?? "API request failed",
      status,
      payload?.code,
      payload
    );
  }
);

export const fetchWithAuth = async <T>(config: AxiosRequestConfig): Promise<T> => {
  const response = await api.request<T>(config);
  return response.data;
};

export type ApplicationResponse = {
  id: string;
  applicationId: string;
  applicant: { name: string };
  loanType: string;
  requestedAmount: number;
  declaredCibilScore: number;
  status: string;
  assignee: string;
  createdAt: string;
  version?: number;
};

export type AuthUserResponse = {
  id: string;
  email: string;
  role: "SUPER_ADMIN" | "ADMIN" | "EMPLOYEE" | "USER";
  fullName: string;
  phone?: string | null;
};

export type DocumentUploadResponse = {
  status: "STORED";
  message: string;
  document: {
    id: string;
    applicationId: string;
    docType: string;
    originalFilename: string;
    contentType: string;
    fileSize: number;
    storagePath: string;
    createdAt: string;
  };
};

export const PrymeAPI = {
  login: async (email: string, password: string, deviceId = "web") => {
    return fetchWithAuth<{ token: string; role: string; name: string; expiresAt: string; message: string }>({
      method: "POST",
      url: "/api/v1/auth/login",
      data: { email, password, deviceId },
    });
  },

  getMe: async (): Promise<AuthUserResponse> => {
    return fetchWithAuth<AuthUserResponse>({ method: "GET", url: "/api/v1/auth/me" });
  },

  logout: async () => {
    return fetchWithAuth<{ message: string }>({ method: "POST", url: "/api/v1/auth/logout", data: {} });
  },

  getApplications: async (): Promise<ApplicationResponse[]> => {
    return fetchWithAuth<ApplicationResponse[]>({ method: "GET", url: "/api/v1/admin/applications" });
  },

  getMyApplications: async (): Promise<ApplicationResponse[]> => {
    return fetchWithAuth<ApplicationResponse[]>({ method: "GET", url: "/api/v1/applications/me" });
  },

  assignLead: async (applicationId: string, assigneeId: string, version?: number) => {
    return fetchWithAuth<{ message: string }>({
      method: "PATCH",
      url: `/api/v1/admin/applications/${applicationId}/assign`,
      data: { assigneeId, version },
    });
  },

  updateStatus: async (applicationId: string, status: string, version?: number) => {
    return fetchWithAuth<{ message: string }>({
      method: "PATCH",
      url: `/api/v1/admin/applications/${applicationId}/status`,
      data: { status, version },
    });
  },

  uploadDocument: async (file: File, applicationId: string, docType: string) => {
    const formData = new FormData();
    formData.append("applicationId", applicationId);
    formData.append("docType", docType);
    formData.append("file", file);

    return fetchWithAuth<DocumentUploadResponse>({
      method: "POST",
      url: "/api/v1/documents/upload",
      data: formData,
    });
  },
};

export default api;
