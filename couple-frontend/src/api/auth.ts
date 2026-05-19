import request from './request'

export function login(data: { username: string; password: string }): Promise<any> {
  return request.post('/auth/login', data)
}

export function register(data: { username: string; password: string }): Promise<any> {
  return request.post('/auth/register', data)
}
