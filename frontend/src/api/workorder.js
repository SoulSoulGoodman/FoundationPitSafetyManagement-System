import request from './request'

export function getWorkOrderList(params) {
  return request.get('/work-order/list', { params })
}

export function getWorkOrderDetail(id) {
  return request.get(`/work-order/${id}`)
}

export function createWorkOrder(data) {
  return request.post('/work-order/create', data)
}

export function assignWorkOrder(id, repairerId) {
  return request.put(`/work-order/${id}/assign`, null, { params: { repairerId } })
}

export function startWorkOrder(id) {
  return request.put(`/work-order/${id}/start`)
}

export function completeWorkOrder(id, repairLog) {
  return request.put(`/work-order/${id}/complete`, null, { params: { repairLog } })
}

export function acceptWorkOrder(id) {
  return request.put(`/work-order/${id}/accept`)
}

export function cancelWorkOrder(id) {
  return request.put(`/work-order/${id}/cancel`)
}
