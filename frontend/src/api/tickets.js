import apiClient from './axios'

export function listTickets({ status, categoryId, assigneeId, page = 0, size = 20 } = {}) {
  const params = { page, size }
  if (status) params.status = status
  if (categoryId != null && categoryId !== '') params.categoryId = categoryId
  if (assigneeId != null && assigneeId !== '') params.assigneeId = assigneeId
  return apiClient.get('/tickets', { params }).then((res) => res.data)
}

export function getTicket(id) {
  return apiClient.get(`/tickets/${id}`).then((res) => res.data)
}

export function replyToTicket(id, body) {
  return apiClient.post(`/tickets/${id}/replies`, { body }).then((res) => res.data)
}

export function updateStatus(id, status) {
  return apiClient.patch(`/tickets/${id}/status`, { status }).then((res) => res.data)
}

export function updateCategory(id, categoryId) {
  return apiClient.patch(`/tickets/${id}/category`, { categoryId }).then((res) => res.data)
}

export function assignTicket(id, adminId) {
  return apiClient.patch(`/tickets/${id}/assignee`, { adminId }).then((res) => res.data)
}

export function listCategories() {
  return apiClient.get('/categories').then((res) => res.data)
}

export function listAdmins() {
  return apiClient.get('/admins').then((res) => res.data)
}
