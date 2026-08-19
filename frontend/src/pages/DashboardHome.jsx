import { useEffect, useMemo, useState } from 'react'
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts'
import { getMetrics } from '../api/dashboard'
import { TICKET_STATUS_ORDER, statusLabel, TICKET_STATUS } from '../constants/ticketStatus'
import { categoryColor } from '../constants/categoryColors'
import StatTile from '../components/StatTile'

const RANGE_OPTIONS = [7, 30, 90]
const CATEGORY_ORDER = ['Billing', 'Technical', 'Account', 'General', 'Feedback']
const MONTH_ABBR = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

function formatDateLabel(iso) {
  const [, m, d] = iso.split('-').map(Number)
  return `${MONTH_ABBR[m - 1]} ${d}`
}

function formatMinutes(mins) {
  if (mins == null) return '—'
  if (mins < 60) return `${Math.round(mins)}m`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h ${Math.round(mins % 60)}m`
  const days = Math.floor(hours / 24)
  return `${days}d ${hours % 24}h`
}

// UTC-day range so keys line up with the backend's UTC-bucketed dates,
// regardless of the browser's local timezone offset.
function utcDateRange(days) {
  const now = new Date()
  const todayUtc = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate())
  const dates = []
  for (let i = days - 1; i >= 0; i--) {
    dates.push(new Date(todayUtc - i * 86400000).toISOString().slice(0, 10))
  }
  return dates
}

function orderCategories(names) {
  const known = CATEGORY_ORDER.filter((n) => names.has(n))
  const other = [...names].filter((n) => !CATEGORY_ORDER.includes(n) && n !== 'Uncategorized').sort()
  return [...known, ...other, ...(names.has('Uncategorized') ? ['Uncategorized'] : [])]
}

function buildSeries(byCategoryPerDay, days) {
  const dates = utcDateRange(days)
  const rows = new Map(dates.map((d) => [d, { date: d, total: 0 }]))
  const names = new Set()

  for (const row of byCategoryPerDay) {
    names.add(row.categoryName)
    const entry = rows.get(row.date)
    if (entry) {
      entry[row.categoryName] = (entry[row.categoryName] ?? 0) + row.count
      entry.total += row.count
    }
  }

  return { rows: Array.from(rows.values()), categories: orderCategories(names) }
}

function ChartTooltip({ active, payload, label, valueSuffix = '' }) {
  if (!active || !payload?.length) return null
  return (
    <div className="rounded-md border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-3 py-2 text-xs shadow-sm">
      <p className="mb-1 font-medium text-[var(--color-ink-muted)]">{formatDateLabel(label)}</p>
      {payload
        .filter((p) => p.value > 0)
        .map((p) => (
          <p key={p.dataKey} className="flex items-center gap-1.5">
            <span className="inline-block h-2 w-2 rounded-full" style={{ backgroundColor: p.color }} />
            <span className="font-semibold text-[var(--color-ink)]">{p.value}</span>
            <span className="text-[var(--color-ink-muted)]">{p.name}{valueSuffix}</span>
          </p>
        ))}
    </div>
  )
}

export default function DashboardHome() {
  const [days, setDays] = useState(30)
  const [metrics, setMetrics] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    getMetrics(days)
      .then(setMetrics)
      .catch(() => setError('Could not load dashboard metrics'))
  }, [days])

  const { rows, categories } = useMemo(
    () => (metrics ? buildSeries(metrics.ticketsByCategoryPerDay, days) : { rows: [], categories: [] }),
    [metrics, days],
  )

  const escalatedNow = metrics?.ticketsByStatus.find((s) => s.status === 'ESCALATED')?.count ?? 0
  const maxStatusCount = Math.max(1, ...(metrics?.ticketsByStatus.map((s) => s.count) ?? [1]))

  if (error) {
    return <p className="text-sm text-[var(--color-status-escalated)]">{error}</p>
  }
  if (!metrics) {
    return <p className="text-sm text-[var(--color-ink-muted)]">Loading…</p>
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-lg font-semibold text-[var(--color-ink)]">Dashboard</h1>
        <div className="flex items-center gap-1">
          {RANGE_OPTIONS.map((opt) => (
            <button
              key={opt}
              onClick={() => setDays(opt)}
              className={`rounded-md px-3 py-1.5 text-sm font-medium ${
                days === opt
                  ? 'bg-[var(--color-brand-50)] text-[var(--color-brand-700)]'
                  : 'text-[var(--color-ink-muted)] hover:text-[var(--color-ink)]'
              }`}
            >
              Last {opt}d
            </button>
          ))}
        </div>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
        <StatTile label={`Tickets in last ${days} days`} value={metrics.ticketsInRange} />
        <StatTile label="Avg first response time" value={formatMinutes(metrics.avgResponseTimeMinutes)} />
        <StatTile label="Currently escalated" value={escalatedNow} />
      </div>

      <div className="mt-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-raised)] p-4">
        <h2 className="text-sm font-medium text-[var(--color-ink)]">Tickets per day</h2>
        <ResponsiveContainer width="100%" height={220}>
          <AreaChart data={rows} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            <CartesianGrid stroke="var(--color-border)" vertical={false} />
            <XAxis
              dataKey="date"
              tickFormatter={formatDateLabel}
              stroke="var(--color-ink-muted)"
              fontSize={11}
              tickLine={false}
              axisLine={false}
              minTickGap={24}
            />
            <YAxis
              allowDecimals={false}
              stroke="var(--color-ink-muted)"
              fontSize={11}
              tickLine={false}
              axisLine={false}
              width={28}
            />
            <Tooltip
              cursor={{ stroke: 'var(--color-border)' }}
              content={<ChartTooltip valueSuffix=" tickets" />}
            />
            <Area
              type="monotone"
              dataKey="total"
              name="Tickets"
              stroke="var(--color-brand-600)"
              strokeWidth={2}
              fill="var(--color-brand-500)"
              fillOpacity={0.1}
              dot={{ r: 2, fill: 'var(--color-brand-600)' }}
              activeDot={{ r: 4, stroke: 'var(--color-surface-raised)', strokeWidth: 2 }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="mt-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-raised)] p-4">
        <h2 className="text-sm font-medium text-[var(--color-ink)]">Tickets by category, per day</h2>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={rows} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            <CartesianGrid stroke="var(--color-border)" vertical={false} />
            <XAxis
              dataKey="date"
              tickFormatter={formatDateLabel}
              stroke="var(--color-ink-muted)"
              fontSize={11}
              tickLine={false}
              axisLine={false}
              minTickGap={24}
            />
            <YAxis
              allowDecimals={false}
              stroke="var(--color-ink-muted)"
              fontSize={11}
              tickLine={false}
              axisLine={false}
              width={28}
            />
            <Tooltip content={<ChartTooltip />} cursor={{ fill: 'var(--color-surface)' }} />
            <Legend wrapperStyle={{ fontSize: 12, color: 'var(--color-ink-muted)' }} />
            {categories.map((name, i) => (
              <Bar
                key={name}
                dataKey={name}
                name={name}
                stackId="category"
                fill={categoryColor(name)}
                stroke="var(--color-surface-raised)"
                strokeWidth={2}
                barSize={24}
                radius={i === categories.length - 1 ? [4, 4, 0, 0] : 0}
              />
            ))}
          </BarChart>
        </ResponsiveContainer>

        <details className="mt-3">
          <summary className="cursor-pointer text-xs font-medium text-[var(--color-ink-muted)] hover:text-[var(--color-ink)]">
            Show as table
          </summary>
          <div className="mt-2 max-h-64 overflow-y-auto overflow-x-auto rounded border border-[var(--color-border)]">
            <table className="w-full text-left text-xs">
              <thead className="sticky top-0 border-b border-[var(--color-border)] bg-[var(--color-surface-raised)] text-[var(--color-ink-muted)]">
                <tr>
                  <th className="px-3 py-1.5 font-medium">Date</th>
                  {categories.map((name) => (
                    <th key={name} className="px-3 py-1.5 font-medium">{name}</th>
                  ))}
                  <th className="px-3 py-1.5 font-medium">Total</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.date} className="border-b border-[var(--color-border)] last:border-0">
                    <td className="px-3 py-1.5 text-[var(--color-ink)]">{formatDateLabel(row.date)}</td>
                    {categories.map((name) => (
                      <td key={name} className="px-3 py-1.5 text-[var(--color-ink-muted)]">{row[name] ?? 0}</td>
                    ))}
                    <td className="px-3 py-1.5 font-medium text-[var(--color-ink)]">{row.total}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </details>
      </div>

      <div className="mt-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-raised)] p-4">
        <h2 className="text-sm font-medium text-[var(--color-ink)]">Current backlog by status</h2>
        <p className="text-xs text-[var(--color-ink-muted)]">All-time snapshot, independent of the date range above.</p>
        <div className="mt-3 space-y-2">
          {TICKET_STATUS_ORDER.map((status) => {
            const count = metrics.ticketsByStatus.find((s) => s.status === status)?.count ?? 0
            const pct = Math.round((count / maxStatusCount) * 100)
            return (
              <div key={status} className="flex items-center gap-3">
                <span className="w-28 shrink-0 text-xs font-medium text-[var(--color-ink-muted)]">
                  {statusLabel(status)}
                </span>
                <div className="h-2 flex-1 rounded-full bg-[var(--color-surface)]">
                  <div
                    className="h-2 rounded-full"
                    style={{ width: `${pct}%`, backgroundColor: `var(--color-status-${TICKET_STATUS[status].slug})` }}
                  />
                </div>
                <span className="w-6 shrink-0 text-right text-xs font-semibold text-[var(--color-ink)]">{count}</span>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
