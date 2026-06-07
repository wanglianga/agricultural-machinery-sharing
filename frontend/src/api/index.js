import request from '@/utils/request'

export const fieldApi = {
  getAll: () => request.get('/fields'),
  getById: (id) => request.get(`/fields/${id}`),
  getByGrower: (growerId) => request.get(`/fields/grower/${growerId}`),
  create: (data) => request.post('/fields', data),
  update: (id, data) => request.put(`/fields/${id}`, data),
  delete: (id) => request.delete(`/fields/${id}`)
}

export const machineApi = {
  getAll: () => request.get('/machines'),
  getById: (id) => request.get(`/machines/${id}`),
  getByCooperative: (cooperativeId) => request.get(`/machines/cooperative/${cooperativeId}`),
  getAvailable: () => request.get('/machines/available'),
  create: (data) => request.post('/machines', data),
  update: (id, data) => request.put(`/machines/${id}`, data),
  delete: (id) => request.delete(`/machines/${id}`)
}

export const appointmentApi = {
  getAll: () => request.get('/appointments'),
  getById: (id) => request.get(`/appointments/${id}`),
  getByGrower: (growerId) => request.get(`/appointments/grower/${growerId}`),
  getByCooperative: (cooperativeId) => request.get(`/appointments/cooperative/${cooperativeId}`),
  getByDate: (date) => request.get(`/appointments/date/${date}`),
  create: (data) => request.post('/appointments', data),
  schedule: (id, data) => request.post(`/appointments/${id}/schedule`, data),
  rainDelay: (id, data) => request.post(`/appointments/${id}/rain-delay`, data),
  reschedule: (id, data) => request.post(`/appointments/${id}/reschedule`, data),
  cancel: (id, data) => request.post(`/appointments/${id}/cancel`, data)
}

export const workOrderApi = {
  getAll: () => request.get('/work-orders'),
  getById: (id) => request.get(`/work-orders/${id}`),
  getByOperator: (operatorId) => request.get(`/work-orders/operator/${operatorId}`),
  getByGrower: (growerId) => request.get(`/work-orders/grower/${growerId}`),
  getByCooperative: (cooperativeId) => request.get(`/work-orders/cooperative/${cooperativeId}`),
  createFromAppointment: (appointmentId) => request.post(`/work-orders/from-appointment/${appointmentId}`),
  reportArrival: (id) => request.post(`/work-orders/${id}/arrive`),
  startWork: (id) => request.post(`/work-orders/${id}/start`),
  completeWork: (id, data) => request.post(`/work-orders/${id}/complete`, data),
  confirmArea: (id, data) => request.post(`/work-orders/${id}/confirm-area`, data),
  resolveDispute: (id, data) => request.post(`/work-orders/${id}/resolve-dispute`, data)
}

export const fuelTicketApi = {
  getAll: () => request.get('/fuel-tickets'),
  getById: (id) => request.get(`/fuel-tickets/${id}`),
  getByWorkOrder: (workOrderId) => request.get(`/fuel-tickets/work-order/${workOrderId}`),
  getByOperator: (operatorId) => request.get(`/fuel-tickets/operator/${operatorId}`),
  getAbnormal: () => request.get('/fuel-tickets/abnormal'),
  getUnverified: () => request.get('/fuel-tickets/unverified'),
  create: (data) => request.post('/fuel-tickets', data),
  verify: (id, data) => request.post(`/fuel-tickets/${id}/verify`, data)
}

export const settlementApi = {
  getAll: () => request.get('/settlements'),
  getById: (id) => request.get(`/settlements/${id}`),
  getByGrower: (growerId) => request.get(`/settlements/grower/${growerId}`),
  getByCooperative: (cooperativeId) => request.get(`/settlements/cooperative/${cooperativeId}`),
  calculate: (workOrderId) => request.post(`/settlements/calculate/${workOrderId}`),
  approve: (id, data) => request.post(`/settlements/${id}/approve`, data),
  reject: (id, data) => request.post(`/settlements/${id}/reject`, data),
  markPaid: (id, data) => request.post(`/settlements/${id}/paid`, data)
}

export const subsidyPolicyApi = {
  getAll: () => request.get('/subsidy-policies'),
  getActive: () => request.get('/subsidy-policies/active'),
  getById: (id) => request.get(`/subsidy-policies/${id}`),
  create: (data) => request.post('/subsidy-policies', data),
  update: (id, data) => request.put(`/subsidy-policies/${id}`, data),
  delete: (id) => request.delete(`/subsidy-policies/${id}`)
}
