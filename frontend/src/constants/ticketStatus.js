// Maps backend TicketStatus enum values to a display label and the design-system
// status token slug (see --color-status-* in index.css).
export const TICKET_STATUS = {
  NEW: { label: 'New', slug: 'new' },
  AUTO_RESOLVED: { label: 'Auto-Resolved', slug: 'auto-resolved' },
  PENDING_CUSTOMER: { label: 'Pending', slug: 'pending' },
  OPEN: { label: 'Open', slug: 'open' },
  ESCALATED: { label: 'Escalated', slug: 'escalated' },
  RESOLVED: { label: 'Resolved', slug: 'resolved' },
  CLOSED: { label: 'Closed', slug: 'closed' },
  SPAM: { label: 'Spam', slug: 'spam' },
}

// Ordered list of statuses for filter dropdowns / status pickers.
export const TICKET_STATUS_ORDER = [
  'NEW',
  'OPEN',
  'ESCALATED',
  'PENDING_CUSTOMER',
  'AUTO_RESOLVED',
  'RESOLVED',
  'CLOSED',
  'SPAM',
]

export function statusLabel(status) {
  return TICKET_STATUS[status]?.label ?? status
}
