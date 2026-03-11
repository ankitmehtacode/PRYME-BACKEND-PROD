import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import api from "@/api";

export type AppRole = "SUPER_ADMIN" | "ADMIN" | "EMPLOYEE" | "USER";

type AuthUser = {
  id: string;
  email: string;
  role: AppRole;
  fullName: string;
  phone?: string | null;
};

type LoginResponse = {
  token: string;
  role: AppRole;
  name: string;
  expiresAt: string;
  message: string;
};

type AuthContextValue = {
  user: AuthUser | null;
  isLoading: boolean;
  toastMessage: string | null;
  login: (email: string, password: string, deviceId?: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  clearToast: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const clearToast = useCallback(() => setToastMessage(null), []);

  const refreshUser = useCallback(async () => {
    const token = localStorage.getItem("pryme_token");
    if (!token) {
      setUser(null);
      return;
    }

    const { data } = await api.get<AuthUser>("/api/v1/auth/me");
    setUser(data);
  }, []);

  const login = useCallback(
    async (email: string, password: string, deviceId = "web") => {
      const { data } = await api.post<LoginResponse>("/api/v1/auth/login", { email, password, deviceId });
      localStorage.setItem("pryme_token", data.token);
      await refreshUser();
      setToastMessage(`Welcome back, ${data.name}`);
      window.dispatchEvent(new CustomEvent("pryme:login-success", { detail: { name: data.name } }));
    },
    [refreshUser]
  );

  const logout = useCallback(async () => {
    try {
      await api.post("/api/v1/auth/logout", {});
    } finally {
      localStorage.removeItem("pryme_token");
      setUser(null);
    }
  }, []);

  useEffect(() => {
    const onSessionExpired = (event: Event) => {
      const detail = (event as CustomEvent<{ message?: string }>).detail;
      setToastMessage(detail?.message ?? "Session expired. Please login again.");
    };

    window.addEventListener("pryme:session-expired", onSessionExpired as EventListener);
    return () => window.removeEventListener("pryme:session-expired", onSessionExpired as EventListener);
  }, []);

  useEffect(() => {
    refreshUser()
      .catch(() => {
        localStorage.removeItem("pryme_token");
        setUser(null);
      })
      .finally(() => setIsLoading(false));
  }, [refreshUser]);

  const value = useMemo<AuthContextValue>(
    () => ({ user, isLoading, toastMessage, login, logout, refreshUser, clearToast }),
    [user, isLoading, toastMessage, login, logout, refreshUser, clearToast]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};
