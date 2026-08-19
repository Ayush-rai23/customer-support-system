import apiClient from './axios'

export function getMetrics(days = 30) {
  return apiClient.get('/dashboard/metrics', { params: { days } }).then((res) => res.data)
}
