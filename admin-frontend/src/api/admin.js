import request from './request'

// ========== 认证 ==========
export function adminLogin(data) {
  return request.post('/admin/login', data)
}
export function changePassword(data) {
  return request.put('/admin/password/change', data)
}

// ========== 数据看板 ==========
export function getOverview() {
  return request.get('/admin/stats/overview')
}

// ========== 用户管理 ==========
export function listUsers(params) {
  return request.get('/admin/users', { params })
}
export function setUserStatus(id, enabled) {
  return request.put(`/admin/users/${id}/status`, { enabled })
}
export function resetUserPassword(id, newPassword) {
  return request.put(`/admin/users/${id}/password`, { newPassword })
}

// ========== 标签管理 ==========
export function listTags(type) {
  return request.get('/admin/tags', { params: type ? { type } : {} })
}
export function deleteTag(id) {
  return request.delete(`/admin/tags/${id}`)
}

// ========== 管理员管理 ==========
export function listAdmins() {
  return request.get('/admin/admins')
}
export function grantAdmin(id) {
  return request.put(`/admin/admins/${id}/grant`)
}
export function revokeAdmin(id) {
  return request.put(`/admin/admins/${id}/revoke`)
}

// ========== 数据导出 ==========
export function exportUsers() {
  return request.get('/admin/export/users', { responseType: 'blob' })
}
export function exportRecords() {
  return request.get('/admin/export/records', { responseType: 'blob' })
}
