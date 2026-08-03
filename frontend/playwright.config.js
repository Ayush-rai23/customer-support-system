import { defineConfig, devices } from '@playwright/test'

// Assumes the backend (:8080, requires local Postgres) and the frontend
// dev server (:5173) are already running, per this project's normal dev
// workflow. No `webServer` auto-start here — orchestrating a
// Postgres-dependent Spring Boot process from Playwright would be
// unnecessary complexity at this project's current stage.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  use: {
    baseURL: 'http://localhost:5173',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
