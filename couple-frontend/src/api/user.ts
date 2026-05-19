import request from './request'

export function getMe(): Promise<any> {
  return request.get('/user/me')
}

export function updateMe(data: {
  nickname?: string
  avatar?: string
  birthday?: string
  city?: string
}): Promise<any> {
  return request.put('/user/me', data)
}

export function getPartner(): Promise<any> {
  return request.get('/user/partner')
}
