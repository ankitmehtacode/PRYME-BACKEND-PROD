# GoPryme Step 1 — De-Gimmickification Audit (Kill List)

## Scope executed
- Backend repository audit (`/workspace/PRYME-BACKEND-PROD`) for concurrency, DB pooling, JPA query patterns, and infrastructure runtime defaults.
- Frontend findings are limited to the integration template in this repo + your external frontend `package.json`/Vite config snapshot.

---

## Kill List (rip these out now)

## 1) `LifecycleConfig` manual mega-threadpool is a liability
**Why this is terrible:** Java 21 virtual threads are already enabled (`spring.threads.virtual.enabled=true`), but `LifecycleConfig` still provisions a 20→500 platform-thread pool + queue capacity 4000. That is an old pre-Loom tuning model and creates latency cliffs under burst load.

**Evidence:** `LifecycleConfig` defines `ThreadPoolTaskExecutor` with `core=20`, `max=500`, queue `4000`, caller-runs fallback. There are zero `@Async` method usages in the main codebase.

**Kill/Replace:**
- Delete the custom `AsyncConfigurer` executor.
- If async work is needed, use virtual-thread-per-task executor directly.
- Keep a tiny scheduler pool only for true periodic jobs.

```java
@Configuration
@EnableScheduling
public class LifecycleConfig {

    @Bean(destroyMethod = "close")
    ExecutorService applicationExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    TaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2); // scheduled jobs only
        scheduler.setThreadNamePrefix("pryme-sched-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }
}
```

---

## 2) Hikari sizing is not Neon/PgBouncer-native
**Why this is terrible:** `maximum-pool-size=150` in `application.yml` is a connection-storm generator for Neon serverless when multiplied by pods/instances. On serverless Postgres + PgBouncer, large JVM pools are anti-pattern.

**Kill/Replace:**
- Use Neon's pooled connection string (`-pooler` endpoint).
- Shrink JVM-side pool aggressively.
- Fail fast and recycle connections before server-side idle eviction.

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL} # must be Neon pooler host
    hikari:
      maximum-pool-size: 12
      minimum-idle: 0
      connection-timeout: 2500
      validation-timeout: 1000
      idle-timeout: 30000
      max-lifetime: 240000
      keepalive-time: 0
      leak-detection-threshold: 0
      pool-name: pryme-neon-pool
```

If you run >1 backend replica, cap total active connections globally (e.g., 3 replicas × 12 = 36 total).

---

## 3) Tomcat thread tuning contradicts virtual-thread strategy
**Why this is terrible:** `server.tomcat.threads.max=1000` and `max-connections=8000` with Loom enabled is mixed-model tuning. You are paying context-switch and memory overhead while pretending Loom is doing the heavy lifting.

**Kill/Replace:**
- Reduce platform thread max to a sane ceiling.
- Let virtual threads absorb blocking request concurrency.

```yaml
server:
  tomcat:
    threads:
      max: 80
      min-spare: 10
    accept-count: 512
    max-connections: 2048
```

---

## 4) KVM-in-Docker claim is currently theater (no `/dev/kvm` passthrough)
**Why this is terrible:** `docker-compose.prod.yml` has no KVM device mount, no cgroup/device rule for KVM, no CPU pinning policy. If this is supposed to be KVM-accelerated runtime, current config does not prove it.

**Kill/Replace:**
Use explicit KVM pass-through + conservative host tuning:

```yaml
services:
  pryme-backend:
    devices:
      - /dev/kvm:/dev/kvm
    device_cgroup_rules:
      - 'c 10:232 rwm'
    ulimits:
      nofile:
        soft: 65535
        hard: 65535
    tmpfs:
      - /tmp:size=512m
```

And for direct run mode:

```bash
docker run --rm \
  --device /dev/kvm \
  --cpuset-cpus="0-3" \
  --memory="2g" \
  --ulimit nofile=65535:65535 \
  --tmpfs /tmp:rw,noexec,nosuid,size=512m \
  pryme-backend:latest
```

---

## 5) Frontend dependency surface is bloated for fintech critical path
**Why this is terrible:** External frontend package manifest shows a very wide UI/animation dependency set (many Radix packages + GSAP + Framer + Lenis + Recharts + Zustand + React Query). This is typical template carryover and creates startup and parse cost.

**Kill/Replace:**
- Keep one animation stack (GSAP **or** Framer, not both).
- Keep one global state strategy (React Query + local state; drop Zustand unless truly needed).
- Lazy-load dashboard/chart routes.
- Strip unused Radix primitives.

```tsx
const DashboardPage = lazy(() => import("@/pages/DashboardPage"));
const CalculatorPage = lazy(() => import("@/pages/CalculatorPage"));

<Route
  path="/dashboard"
  element={
    <Suspense fallback={<PageShellSkeleton />}>
      <DashboardPage />
    </Suspense>
  }
/>
```

---

## 6) Session token mismatch in frontend integration template
**Why this is terrible:** Integration template `AuthContext.tsx` stores token in `localStorage` (`pryme_token`) while backend architecture docs/config emphasize HttpOnly cookie session strategy. This split is security and consistency debt.

**Kill/Replace:**
- Remove token persistence from localStorage.
- Use `withCredentials` cookie flow only.

```ts
// axios setup
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});

// login: no localStorage writes
await api.post("/api/v1/auth/login", { email, password, deviceId: "web" });
await refreshUser();
```

---

## Immediate high-risk bugs to check next (Step 2 queue)
1. **Virtual-thread pinning scan:** no obvious `synchronized` blocks found in core modules, but lock-heavy native/driver paths still need JFR pinning event verification under load.
2. **N+1 regression under admin endpoints:** entity graphs exist in `LoanApplicationRepository`, but unbounded `findAll()` usage exists in several services/controllers and can become memory bombs.
3. **Outbox/cron overlap contention:** multiple scheduled jobs run on shared scheduler; verify no long blocking DB transactions starve scheduled tasks.

