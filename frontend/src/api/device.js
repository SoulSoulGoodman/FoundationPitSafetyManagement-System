import request from './request'

export function getDeviceList(params) {
  return request.get('/device/list', { params })
}

export function addDevice(data) {
  return request.post('/device/add', data)
}

export function updateDevice(data) {
  return request.put(`/device/${data.id}`, data)
}

export function deleteDevice(id) {
  return request.delete(`/device/${id}`)
}

export function updateDeviceStatus(id, status) {
  return request.put('/device/status', null, { params: { id, status } })
}
