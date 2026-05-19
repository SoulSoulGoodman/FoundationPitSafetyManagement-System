import request from './request'

export function login(username, password) {
  return request.post('/auth/login', { username, password })
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function getUsers() {
  return request.get('/auth/users')
}

export function createUser(data) {
  return request.post('/user/add', data)
}

export function updateUser(data) {
  return request.put('/user/update', data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}