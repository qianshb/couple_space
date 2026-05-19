import request from './request'

export function createMemorial(data: { title: string; eventDate: string; description?: string }): Promise<any> {
  return request.post('/memorial/create', data)
}

export function getMemorialList(): Promise<any> {
  return request.get('/memorial/list')
}

export function deleteMemorial(id: number): Promise<any> {
  return request.delete(`/memorial/${id}`)
}
