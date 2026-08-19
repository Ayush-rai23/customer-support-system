// Headline-number tile for a KPI row. No delta/sparkline: we don't have a
// real prior-period figure to compare against, and a fabricated trend would
// be misleading.
export default function StatTile({ label, value }) {
  return (
    <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-4 py-3">
      <p className="text-xs font-medium text-[var(--color-ink-muted)]">{label}</p>
      <p className="mt-1 text-2xl font-semibold text-[var(--color-ink)]">{value}</p>
    </div>
  )
}
