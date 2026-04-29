import api from './index'

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  getUserInfo: () => api.get('/auth/userinfo'),
  logout: () => api.post('/auth/logout')
}

export const userApi = {
  list: (params) => api.get('/system/user/list', { params }),
  getById: (id) => api.get(`/system/user/${id}`),
  add: (data) => api.post('/system/user', data),
  update: (data) => api.put('/system/user', data),
  delete: (id) => api.delete(`/system/user/${id}`),
  batchDelete: (ids) => api.delete('/system/user/batch', { data: { ids } }),
  assignRoles: (id, roleIds) => api.put(`/system/user/${id}/roles`, { roleIds }),
  resetPassword: (id) => api.put(`/system/user/${id}/reset-password`),
  updatePassword: (data) => api.put('/system/user/password', data)
}

export const roleApi = {
  list: (params) => api.get('/system/role/list', { params }),
  listAll: () => api.get('/system/role/all'),
  getById: (id) => api.get(`/system/role/${id}`),
  add: (data) => api.post('/system/role', data),
  update: (data) => api.put('/system/role', data),
  delete: (id) => api.delete(`/system/role/${id}`),
  getMenus: (id) => api.get(`/system/role/${id}/menus`),
  assignMenus: (id, menuIds) => api.put(`/system/role/${id}/menus`, { menuIds })
}

export const menuApi = {
  getTree: () => api.get('/system/menu/tree'),
  getUserTree: () => api.get('/system/menu/user-tree'),
  getById: (id) => api.get(`/system/menu/${id}`),
  add: (data) => api.post('/system/menu', data),
  update: (data) => api.put('/system/menu', data),
  delete: (id) => api.delete(`/system/menu/${id}`),
  updateSort: (data) => api.put('/system/menu/sort', data)
}
