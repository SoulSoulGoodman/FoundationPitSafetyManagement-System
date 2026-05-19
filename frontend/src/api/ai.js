import request from './request'

export function chatAi(message, history = []) {
  return request.post('/ai/chat', { message, history })
}

export function maintenanceAssistant(message, history = []) {
  return request.post('/ai/maintenance', { message, history })
}

export function healthMonitor(message, history = []) {
  return request.post('/ai/health-monitor', { message, history })
}

export function predictDevice(message, history = []) {
  return request.post('/ai/predict', { message, history })
}

export function predictAuto(params) {
  return request.get('/ai/predict-auto', { params })
}
