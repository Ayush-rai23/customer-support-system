# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An AI-assisted customer support system: customer emails become tickets, get auto-categorized, and (when the AI is confident) get auto-answered from a knowledge base. Anything the AI can't handle confidently escalates to a human admin. Customers only ever interact via email — there is no customer-facing login. Only admins/agents use the web app. Full problem statement, ticket status model, and MVP feature scope live in [PROJECT_PLAN.md](./PROJECT_PLAN.md); tech choices/rationale live in [tech-stack.md](./tech-stack.md).

This is a monorepo: `backend/` (Spring Boot API) + `frontend/` (React admin SPA). The project is an early scaffold — most business logic (tickets, email ingestion, AI categorization/auto-response, knowledge base, dashboard) has not been built yet. Build incrementally and check with the user before assuming scope beyond what's been explicitly requested.

## Commands

### Backend (`backend/`, Spring Boot 4.1.0, Java 21, Maven)

```
./mvnw.cmd spring-boot:run       # run the dev server (port 8080)
./mvnw.cmd compile               # compile only
./mvnw.cmd test                  # run all tests
./mvnw.cmd test -Dtest=ClassName # run a single test class
./mvnw.cmd test -Dtest=ClassName#methodName  # run a single test method
```

Requires a running PostgreSQL instance with a `support_system` database (see Database section below). The backend won't start without it — Hibernate fails fast on connection errors.

### Frontend (`frontend/`, React 19 + Vite 8)

```
npm run dev       # dev server on port 5173, proxies /api/* to localhost:8080
npm run build     # production build
npm run lint      # oxlint
npm run preview   # preview a production build
npm run test:e2e  # Playwright e2e tests (see Testing section below)
```

## Architecture

### Backend structure (`backend/src/main/java/com/support/backend/`)

- `BackendApplication.java` — entry point.
- `config/` — `@Configuration` classes (currently `SecurityConfig`).
- `controller/` — `@RestController` REST endpoints.
- Domain/service/repository packages don't exist yet — add them as features are built (e.g. `domain/`, `repository/`, `service/`, `dto/`).

**Security is currently wide open.** `SecurityConfig` permits all requests (`anyRequest().permitAll()`) with CSRF disabled — there is a `TODO` marking this for replacement with JWT-based admin auth once the login flow exists. `spring-boot-starter-security`, `spring-boot-starter-validation`, and the `io.jsonwebtoken:jjwt-*` (0.12.6) dependencies are already on the classpath for that future work. Don't assume any endpoint is protected until `SecurityConfig` says otherwise.

**Spring AI (2.0.0, via `spring-ai-bom`)** is wired for two roles per `tech-stack.md`: ticket categorization and knowledge-base-driven auto-response.
- `spring-ai-starter-model-openai` — chat model is `gpt-4o-mini`, embedding model is `text-embedding-3-small` (see `application.yml` under `spring.ai.openai`). Requires `OPENAI_API_KEY` env var to actually call the API — it's empty by default.
- `spring-ai-starter-vector-store-pgvector` — backs the knowledge base (PDF/doc uploads → embeddings for RAG). `spring.ai.vectorstore.pgvector.initialize-schema` is **not** set, so it defaults to `false` — the `vector` Postgres extension and `vector_store` table are not auto-created. This must be handled explicitly (enable schema init, or `CREATE EXTENSION vector` manually) before the vector store is used, since the native Windows Postgres install here doesn't have `pgvector` preinstalled the way a `pgvector/pgvector` Docker image would.
- `org.apache.pdfbox:pdfbox` (3.0.3) is available for parsing uploaded knowledge-base PDFs.

**Config** lives in `application.yml` (not `.properties`), using `${ENV_VAR:default}` placeholders throughout — mail SMTP creds, `OPENAI_API_KEY`, `JWT_SECRET`, `SUPPORT_INBOX_ADDRESS`, and `app.ai.confidence-threshold` (0.75, referenced in PROJECT_PLAN as the AI auto-response confidence gate) all have empty/dev-only defaults. `DB_PASSWORD` has **no default** (`${DB_PASSWORD}`) — it must be set as an environment variable before running the backend, since `application.yml` is not gitignored and real secrets must never be added there as literal defaults.

Actuator is exposed at `/actuator/*` with only `health` and `info` included (`management.endpoints.web.exposure.include`).

### Frontend structure (`frontend/src/`)

- `main.jsx` → mounts `App.jsx` into `#root`.
- `App.jsx` — currently a placeholder that fetches `/api/health` and renders backend status; replace as real views (inbox, ticket detail, knowledge base upload, dashboard) get built per `tech-stack.md`.
- `index.css` — Tailwind v4 entry point (`@import "tailwindcss"`, no `tailwind.config.js`/PostCSS file — v4's Vite plugin handles content scanning automatically). Defines the design system via `@theme` custom properties: neutral surface/ink/border tokens, a `brand-*` indigo accent scale, and a full `status-*` color set (new/auto-resolved/pending/open/escalated/resolved/closed/spam) matching the ticket status model in PROJECT_PLAN — use these tokens (`var(--color-status-escalated)` etc.) rather than inventing new colors when building ticket status UI. Dark mode is handled via `prefers-color-scheme` overriding the same token names.
- `vite.config.js` — dev server proxies `/api/*` to `http://localhost:8080`, so frontend code should call relative `/api/...` paths (via `fetch` or `axios`) rather than hardcoding the backend origin; this also sidesteps CORS in dev.
- `react-router-dom` and `axios` are installed but not yet wired up — no routes or API client module exist yet.

### Database

PostgreSQL, native Windows install (not Docker) — service `postgresql-x64-18`, default port 5432, database name `support_system`. Managed via pgAdmin4. `spring.jpa.hibernate.ddl-auto` is `update`, so entities auto-migrate the schema on backend startup once they exist — no migration tool (e.g. Flyway/Liquibase) is set up.

## Testing

Three tiers exist so far, covering the session-auth feature — extend the same pattern as new features get built rather than introducing new test tooling per-feature.

- **Backend unit** (`backend/src/test/java/.../service/AdminUserDetailsServiceTest.java`) — plain Mockito (`@ExtendWith(MockitoExtension.class)`), no Spring context, no DB. JUnit 5, Mockito, and AssertJ come transitively from the existing `spring-boot-starter-*-test` modular test starters in `pom.xml` — no extra test dependency needed for this tier.
- **Backend integration** (`backend/src/test/java/.../controller/AuthControllerIntegrationTest.java`) — `@SpringBootTest(webEnvironment = WebEnvironment.MOCK)` + `@AutoConfigureMockMvc`, exercising the real Spring Security filter chain via `MockMvc`. Runs against the **same local Postgres dev database** as `spring-boot:run` (no H2/Testcontainers) — deliberately simple since the app already requires Postgres to start at all. Requires `DB_PASSWORD` set in the environment, same as running the app. In Spring Boot 4's modular test-starter layout, `@AutoConfigureMockMvc` lives at `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` (not the classic `org.springframework.boot.test.autoconfigure.web.servlet` package) — easy to get wrong from memory/older docs. There is no autowired `ObjectMapper` bean available in this test context; build JSON request bodies as plain strings instead of using Jackson.
  - **IntelliJ gotcha**: run configurations don't inherit the shell's environment variables. If a backend integration test fails in IntelliJ with a Postgres `password authentication failed` error (often surfaced as `ApplicationContext failure threshold (1) exceeded` on later tests in the same class — that's just Spring's context-cache guard hiding the real first failure), set `DB_PASSWORD` under Run → Edit Configurations → (the test config, or the JUnit template so it applies to all) → Environment variables.
- **E2E** (`frontend/e2e/auth.spec.js`, config in `frontend/playwright.config.js`) — Playwright, Chromium only (`@playwright/test` devDependency). Run `npx playwright install chromium` once per machine. `playwright.config.js` has no `webServer` auto-start — both the backend (`:8080`, needs Postgres + `DB_PASSWORD`) and frontend (`:5173`) must already be running before `npm run test:e2e`, per this project's normal dev workflow. Hardcodes the seeded default admin credentials (`admin@support.local` / `changeme`, matching `application.yml`'s `app.admin.default-*` defaults).
- **No frontend unit tests** (no Vitest/RTL) by deliberate choice — thin context/hook layers like `AuthContext` are already covered by the backend integration tests + e2e; add a frontend test framework only when component logic actually warrants it, not preemptively.

## Origin / how this was scaffolded

Backend was generated via Spring Initializr (start.spring.io) — Spring Boot 4.1.0, Java 21, Maven, with web/data-jpa/postgresql/security/validation/lombok/mail/actuator, then Spring AI, JWT, and PDFBox deps were added by hand. Frontend was generated via `npm create vite@latest -- --template react`, then Tailwind v4, react-router-dom, and axios were added on top. Keep this in mind if dependency versions ever need bumping — check `spring-ai-bom` version compatibility against the Spring Boot parent version before upgrading either independently.
