import request from './request'

export function getStats() {
  return request.get('/dashboard/stats')
}

export function getAlertTrend() {
  return request.get('/dashboard/alert-trend')
}

export function getRecentAlerts() {
  return request.get('/dashboard/recent-alerts')
}

export function getPendingOrders() {
  return request.get('/dashboard/pending-orders')
}