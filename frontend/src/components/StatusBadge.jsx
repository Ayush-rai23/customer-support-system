import { TICKET_STATUS } from '../constants/ticketStatus'

// Small colored pill for a ticket status. Colors come from the --color-status-*
// tokens in index.css; we read them as inline CSS vars since the slug is dynamic.
export default function StatusBadge({ status }) {
  const meta = TICKET_STATUS[status] ?? { label: status, slug: 'closed' }
  return (
    <span
      className="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
      style={{
        color: `var(--color-status-${meta.slug})`,
        backgroundColor: `var(--color-status-${meta.slug}-bg)`,
      }}
    >
      {meta.label}
    </span>
  )
}
