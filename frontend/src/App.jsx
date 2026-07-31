import { useEffect, useState } from 'react'

function App() {
  const [health, setHealth] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch('/api/health')
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`)
        return res.json()
      })
      .then(setHealth)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="flex min-h-svh items-center justify-center">
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-raised)] px-8 py-6 text-center shadow-sm">
        <h1 className="text-xl font-semibold text-[var(--color-ink)]">
          Customer Support System
        </h1>

        {error && (
          <p className="mt-3 text-sm text-[var(--color-status-escalated)]">
            Backend unreachable: {error}
          </p>
        )}

        {!error && !health && (
          <p className="mt-3 text-sm text-[var(--color-ink-muted)]">
            Checking backend status…
          </p>
        )}

        {health && (
          <div className="mt-3 space-y-1 text-sm text-[var(--color-ink-muted)]">
            <p>
              Backend status:{' '}
              <span className="font-medium text-[var(--color-status-auto-resolved)]">
                {health.status}
              </span>
            </p>
            <p>Service: {health.service}</p>
            <p>Checked at: {new Date(health.timestamp).toLocaleTimeString()}</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
