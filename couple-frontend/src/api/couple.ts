import request from './request'

export function createInvite(): Promise<any> {
  return request.post('/couple/invite')
}

export function bind(inviteCode: string): Promise<any> {
  return request.post('/couple/bind', { inviteCode })
}

export function getCoupleStatus(): Promise<any> {
  return request.get('/couple/status')
}
