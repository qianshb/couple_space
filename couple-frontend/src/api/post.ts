import request from './request'

export function createPost(data: { postType: string; content?: string; files: string[] }): Promise<any> {
  return request.post('/post/create', data)
}

export function getPostList(page: number = 1, size: number = 20): Promise<any> {
  return request.get('/post/list', { params: { page, size } })
}

export function deletePost(id: number): Promise<any> {
  return request.delete(`/post/${id}`)
}
