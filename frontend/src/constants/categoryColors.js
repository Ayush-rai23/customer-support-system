// Fixed name -> CSS var mapping for the dashboard's category stacked-bar chart.
// Order/hues are validated (see dataviz skill) for CVD-safe adjacency; do not
// reassign slots without re-validating. Unknown/uncategorized falls back to
// the neutral muted-ink token rather than consuming a categorical slot.
const CATEGORY_CHART_COLORS = {
  Billing: 'var(--color-chart-billing)',
  Technical: 'var(--color-chart-technical)',
  Account: 'var(--color-chart-account)',
  General: 'var(--color-chart-general)',
  Feedback: 'var(--color-chart-feedback)',
}

export function categoryColor(name) {
  return CATEGORY_CHART_COLORS[name] ?? 'var(--color-ink-muted)'
}
