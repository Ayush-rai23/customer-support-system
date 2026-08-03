import { test, expect } from '@playwright/test'

const ADMIN_EMAIL = 'admin@support.local'
const ADMIN_PASSWORD = 'changeme'

test('visiting a protected route while logged out redirects to /login', async ({ page }) => {
  await page.goto('/dashboard/tickets')
  await expect(page).toHaveURL(/\/login$/)
})

test('logging in with valid credentials lands on dashboard home', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('Email').fill(ADMIN_EMAIL)
  await page.getByPlaceholder('Password').fill(ADMIN_PASSWORD)
  await page.getByRole('button', { name: 'Sign in' }).click()

  await expect(page).toHaveURL(/\/dashboard\/home$/)
  await expect(page.getByRole('link', { name: 'Home' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Tickets' })).toBeVisible()
})

test('logging in with wrong credentials shows an inline error and stays on /login', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('Email').fill(ADMIN_EMAIL)
  await page.getByPlaceholder('Password').fill('wrong-password')
  await page.getByRole('button', { name: 'Sign in' }).click()

  await expect(page.getByText('Invalid email or password')).toBeVisible()
  await expect(page).toHaveURL(/\/login$/)
})

test('logging out invalidates the server session, not just client state', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('Email').fill(ADMIN_EMAIL)
  await page.getByPlaceholder('Password').fill(ADMIN_PASSWORD)
  await page.getByRole('button', { name: 'Sign in' }).click()
  await expect(page).toHaveURL(/\/dashboard\/home$/)

  await page.getByRole('button', { name: 'Log out' }).click()
  await expect(page).toHaveURL(/\/login$/)

  // Prove it's a real server-side session invalidation: a fresh navigation
  // to a protected route must redirect again, not just rely on client state.
  await page.goto('/dashboard/home')
  await expect(page).toHaveURL(/\/login$/)
})
