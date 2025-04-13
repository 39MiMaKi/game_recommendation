import request from '@/utils/request'

// 获取用户游戏评分记录
export function getUserReviewsApi(userId, page = 1, pageSize = 10) {
  return request({
    url: '/user/ratings',
    method: 'get',
    params: {
      page,
      size: pageSize
    },
    headers: {
      'Authorization': localStorage.getItem('token')
    }
  })
}

// 获取用户游戏统计信息
export function getUserGameStatsApi(userId) {
  return request({
    url: '/user/stats',
    method: 'get',
    headers: {
      'Authorization': localStorage.getItem('token')
    }
  })
}