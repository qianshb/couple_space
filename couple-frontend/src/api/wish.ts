import request from './request'

export function createWish(data: { title: string; description?: string }): Promise<any> {
  return request.post('/wish/create', data)
}

export function getWishList(): Promise<any> {
  return request.get('/wish/list')
}

export function toggleWish(id: number): Promise<any> {
  return request.put(`/wish/toggle/${id}`)
}

export function deleteWish(id: number): Promise<any> {
  return request.delete(`/wish/${id}`)
}
