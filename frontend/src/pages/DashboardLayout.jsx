import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function DashboardLayout() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  const linkClass = ({ isActive }) =>
    `rounded-md px-3 py-1.5 text-sm font-medium ${
      isActive
        ? 'bg-[var(--color-brand-50)] text-[var(--color-brand-700)]'
        : 'text-[var(--color-ink-muted)] hover:text-[var(--color-ink)]'
    }`

  return (
    <div className="min-h-svh">
      <nav className="flex items-center justify-between border-b border-[var(--color-border)] bg-[var(--color-surface-raised)] px-6 py-3">
        <div className="flex items-center gap-2">
          <NavLink to="/dashboard/home" className={linkClass}>
            Home
          </NavLink>
          <NavLink to="/dashboard/tickets" className={linkClass}>
            Tickets
          </NavLink>
        </div>
        <button
          onClick={handleLogout}
          className="rounded-md border border-[var(--color-border)] px-3 py-1.5 text-sm text-[var(--color-ink-muted)] hover:text-[var(--color-ink)]"
        >
          Log out
        </button>
      </nav>
      <main className="p-6">
        <Outlet />
      </main>
    </div>
  )
}
