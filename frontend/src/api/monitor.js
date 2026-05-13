import request from './request'

export function getTotalStation(params) {
  return request.get('/monitor/total-station', { params })
}

export function getAxialForce(params) {
  return request.get('/monitor/axial-force', { params })
}

export function getSteelTemperature(params) {
  return request.get('/monitor/steel-temperature', { params })
}

export function getSensors() {
  return request.get('/monitor/sensors')
}