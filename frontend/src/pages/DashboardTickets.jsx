import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { listTickets, listCategories, listAdmins } from '../api/tickets'
import { TICKET_STATUS_ORDER, statusLabel } from '../constants/ticketStatus'
import StatusBadge from '../components/StatusBadge'

const UNASSIGNED = '-1'

export default function DashboardTickets() {
  const navigate = useNavigate()
  const [filters, setFilters] = useState({ status: '', categoryId: '', assigneeId: '' })
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [categories, setCategories] = useState([])
  const [admins, setAdmins] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([listCategories(), listAdmins()])
      .then(([cats, adm]) => {
        setCategories(cats)
        setAdmins(adm)
      })
      .catch(() => {})
  }, [])

  useEffect(() => {
    setLoading(true)
    listTickets({ ...filters, page })
      .then(setData)
      .catch(() => setData(null))
      .finally(() => setLoading(false))
  }, [filters, page])

  function updateFilter(key, value) {
    setPage(0)
    setFilters((f) => ({ ...f, [key]: value }))
  }

  function clearFilters() {
    setPage(0)
    setFilters({ status: '', categoryId: '', assigneeId: '' })
  }

  const hasFilters = filters.status || filters.categoryId || filters.assigneeId
  const tickets = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  return (
    <div>
      <h1 className="text-lg font-semibold text-[var(--color-ink)]">Tickets</h1>

      <div className="mt-4 flex flex-wrap items-center gap-3">
        <select
          value={filters.status}
          onChange={(e) => updateFilter('status', e.target.value)}
          className="rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-1.5 text-sm text-[var(--color-ink)]"
        >
          <option value="">All statuses</option>
          {TICKET_STATUS_ORDER.map((s) => (
            <option key={s} value={s}>
              {statusLabel(s)}
            </option>
          ))}
        </select>

        <select
          value={filters.categoryId}
          onChange={(e) => updateFilter('categoryId', e.target.value)}
          className="rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-1.5 text-sm text-[var(--color-ink)]"
        >
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>

        <select
          value={filters.assigneeId}
          onChange={(e) => updateFilter('assigneeId', e.target.value)}
          className="rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-1.5 text-sm text-[var(--color-ink)]"
        >
          <option value="">All assignees</option>
          <option value={UNASSIGNED}>Unassigned</option>
          {admins.map((a) => (
            <option key={a.id} value={a.id}>
              {a.email}
            </option>
          ))}
        </select>

        {hasFilters && (
          <button
            onClick={clearFilters}
            className="text-sm text-[var(--color-ink-muted)] hover:text-[var(--color-ink)]"
          >
            Clear
          </button>
        )}
      </div>

      <div className="mt-4 overflow-x-auto rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-raised)]">
        <table className="w-full text-left text-sm">
          <thead className="border-b border-[var(--color-border)] text-xs uppercase tracking-wide text-[var(--color-ink-muted)]">
            <tr>
              <th className="px-4 py-2.5 font-medium">Subject</th>
              <th className="px-4 py-2.5 font-medium">Customer</th>
              <th className="px-4 py-2.5 font-medium">Status</th>
              <th className="px-4 py-2.5 font-medium">Category</th>
              <th className="px-4 py-2.5 font-medium">Assigned</th>
              <th className="px-4 py-2.5 font-medium">Updated</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-[var(--color-ink-muted)]">
                  Loading…
                </td>
              </tr>
            ) : tickets.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-[var(--color-ink-muted)]">
                  No tickets match these filters.
                </td>
              </tr>
            ) : (
              tickets.map((t) => (
                <tr
                  key={t.id}
                  onClick={() => navigate(`/dashboard/tickets/${t.id}`)}
                  className="cursor-pointer border-b border-[var(--color-border)] last:border-0 hover:bg-[var(--color-surface)]"
                >
                  <td className="px-4 py-3 font-medium text-[var(--color-ink)]">
                    {t.subject}
                    <span className="ml-2 text-xs text-[var(--color-ink-muted)]">
                      {t.messageCount} msg
                    </span>
                  </td>
                  <td className="px-4 py-3 text-[var(--color-ink-muted)]">
                    {t.customerName || t.customerEmail}
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={t.status} />
                  </td>
                  <td className="px-4 py-3 text-[var(--color-ink-muted)]">
                    {t.category?.name ?? '—'}
                  </td>
                  <td className="px-4 py-3 text-[var(--color-ink-muted)]">
                    {t.assignee?.email ?? 'Unassigned'}
                  </td>
                  <td className="px-4 py-3 text-[var(--color-ink-muted)]">
                    {new Date(t.updatedAt).toLocaleDateString()}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="mt-3 flex items-center justify-end gap-3 text-sm">
          <button
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
            className="rounded-md border border-[var(--color-border)] px-3 py-1 disabled:opacity-40"
          >
            Prev
          </button>
          <span className="text-[var(--color-ink-muted)]">
            Page {page + 1} of {totalPages}
          </span>
          <button
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-md border border-[var(--color-border)] px-3 py-1 disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
