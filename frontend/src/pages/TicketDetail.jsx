import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  getTicket,
  replyToTicket,
  updateStatus,
  updateCategory,
  assignTicket,
  listCategories,
  listAdmins,
} from '../api/tickets'
import { TICKET_STATUS_ORDER, statusLabel } from '../constants/ticketStatus'
import StatusBadge from '../components/StatusBadge'

const UNASSIGNED = '-1'
const REPLY_MIN_LENGTH = 20
const REPLY_MAX_LENGTH = 700

export default function TicketDetail() {
  const { id } = useParams()
  const [ticket, setTicket] = useState(null)
  const [categories, setCategories] = useState([])
  const [admins, setAdmins] = useState([])
  const [reply, setReply] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    getTicket(id)
      .then(setTicket)
      .catch(() => setError('Ticket not found'))
  }, [id])

  useEffect(() => {
    Promise.all([listCategories(), listAdmins()])
      .then(([cats, adm]) => {
        setCategories(cats)
        setAdmins(adm)
      })
      .catch(() => {})
  }, [])

  async function run(action) {
    setBusy(true)
    setError(null)
    try {
      setTicket(await action())
    } catch (e) {
      setError(e.response?.data?.error ?? 'Something went wrong')
    } finally {
      setBusy(false)
    }
  }

  const replyLength = reply.trim().length
  const replyTooShort = replyLength > 0 && replyLength < REPLY_MIN_LENGTH
  const replyValid = replyLength >= REPLY_MIN_LENGTH && replyLength <= REPLY_MAX_LENGTH

  async function handleReply(e) {
    e.preventDefault()
    if (!replyValid) return
    await run(() => replyToTicket(id, reply.trim()))
    setReply('')
  }

  if (error && !ticket) {
    return <p className="text-sm text-[var(--color-status-escalated)]">{error}</p>
  }
  if (!ticket) {
    return <p className="text-sm text-[var(--color-ink-muted)]">Loading…</p>
  }

  return (
    <div>
      <Link to="/dashboard/tickets" className="text-sm text-[var(--color-ink-muted)] hover:text-[var(--color-ink)]">
        ← Back to tickets
      </Link>

      <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-lg font-semibold text-[var(--color-ink)]">{ticket.subject}</h1>
          <p className="text-sm text-[var(--color-ink-muted)]">
            {ticket.customerName ? `${ticket.customerName} · ` : ''}
            {ticket.customerEmail}
          </p>
        </div>
        <StatusBadge status={ticket.status} />
      </div>

      {error && <p className="mt-3 text-sm text-[var(--color-status-escalated)]">{error}</p>}

      <div className="mt-4 grid gap-6 lg:grid-cols-[1fr_18rem]">
        {/* Conversation thread */}
        <div className="space-y-3">
          {ticket.messages.map((m) => {
            const outbound = m.direction === 'OUTBOUND'
            return (
              <div key={m.id} className={`flex ${outbound ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-[80%] rounded-lg border px-4 py-2.5 ${
                    outbound
                      ? 'border-[var(--color-brand-100)] bg-[var(--color-brand-50)]'
                      : 'border-[var(--color-border)] bg-[var(--color-surface-raised)]'
                  }`}
                >
                  <div className="mb-1 flex items-center gap-2 text-xs text-[var(--color-ink-muted)]">
                    <span className="font-medium">
                      {m.authorType === 'CUSTOMER'
                        ? ticket.customerName || ticket.customerEmail
                        : m.authorType === 'AI'
                          ? 'AI Assistant'
                          : m.authorEmail || 'Admin'}
                    </span>
                    <span>· {new Date(m.createdAt).toLocaleString()}</span>
                  </div>
                  <p className="whitespace-pre-wrap text-sm text-[var(--color-ink)]">{m.body}</p>
                  {m.attachments.length > 0 && (
                    <div className="mt-2 flex flex-wrap gap-2">
                      {m.attachments.map((a) => (
                        <a
                          key={a.id}
                          href={`/api/attachments/${a.id}`}
                          className="rounded border border-[var(--color-border)] px-2 py-0.5 text-xs text-[var(--color-brand-600)]"
                        >
                          {a.filename}
                        </a>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )
          })}

          <form onSubmit={handleReply} className="mt-4">
            <textarea
              value={reply}
              onChange={(e) => setReply(e.target.value)}
              placeholder="Write a reply to the customer… (min 20 characters)"
              rows={4}
              maxLength={REPLY_MAX_LENGTH}
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-2 text-sm text-[var(--color-ink)]"
            />
            <div className="mt-2 flex items-center justify-between">
              <span
                className={`text-xs ${
                  replyTooShort ? 'text-[var(--color-status-escalated)]' : 'text-[var(--color-ink-muted)]'
                }`}
              >
                {replyTooShort
                  ? `${REPLY_MIN_LENGTH - replyLength} more characters needed`
                  : `${replyLength}/${REPLY_MAX_LENGTH}`}
              </span>
              <button
                type="submit"
                disabled={busy || !replyValid}
                className="rounded-md bg-[var(--color-brand-600)] px-4 py-2 text-sm font-medium text-white hover:bg-[var(--color-brand-700)] disabled:opacity-60"
              >
                Send reply
              </button>
            </div>
          </form>
        </div>

        {/* Action panel */}
        <aside className="space-y-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-raised)] p-4">
          <label className="block">
            <span className="text-xs font-medium uppercase tracking-wide text-[var(--color-ink-muted)]">Status</span>
            <select
              value={ticket.status}
              disabled={busy}
              onChange={(e) => run(() => updateStatus(id, e.target.value))}
              className="mt-1 w-full rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-1.5 text-sm text-[var(--color-ink)]"
            >
              {TICKET_STATUS_ORDER.map((s) => (
                <option key={s} value={s}>
                  {statusLabel(s)}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="text-xs font-medium uppercase tracking-wide text-[var(--color-ink-muted)]">Category</span>
            <select
              value={ticket.category?.id ?? ''}
              disabled={busy}
              onChange={(e) => run(() => updateCategory(id, e.target.value === '' ? null : Number(e.target.value)))}
              className="mt-1 w-full rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-1.5 text-sm text-[var(--color-ink)]"
            >
              <option value="">Uncategorized</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="text-xs font-medium uppercase tracking-wide text-[var(--color-ink-muted)]">Assignee</span>
            <select
              value={ticket.assignee?.id ?? UNASSIGNED}
              disabled={busy}
              onChange={(e) => run(() => assignTicket(id, e.target.value === UNASSIGNED ? null : Number(e.target.value)))}
              className="mt-1 w-full rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-1.5 text-sm text-[var(--color-ink)]"
            >
              <option value={UNASSIGNED}>Unassigned</option>
              {admins.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.email}
                </option>
              ))}
            </select>
          </label>
        </aside>
      </div>
    </div>
  )
}
