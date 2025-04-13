import request from '@/utils/request'

/**
 * 获取新鲜推荐游戏列表
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getRecommendationsApi(params) {
  return request({
    url: '/app/recommendations',
    method: 'get',
    params
  })
}

/**
 * 获取新品速递游戏列表
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getNewReleasesApi(params) {
  return request({
    url: '/app/new-releases',
    method: 'get',
    params
  })
}

/**
 * 获取特别优惠游戏列表
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getSpecialsApi(params) {
  return request({
    url: '/app/specials',
    method: 'get',
    params
  })
}

/**
 * 获取免费游戏列表
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getFreeGamesApi(params) {
  return request({
    url: '/app/free-games',
    method: 'get',
    params
  })
}

// 获取混合推荐列表
export function getHybridRecommendationsApi() {
  return request({
    url: '/app/hybrid-recommendations',
    method: 'get'
  })
}

// 获取个性化推荐列表
export function getPersonalizedRecommendationsApi(limit = 12) {
  return request({
    url: '/app/personalized-recommendations',
    method: 'get',
    params: { limit }
  })
}

// 更新用户偏好标签
export function updateUserPreferencesApi(tags) {
  return request({
    url: '/app/user-preferences',
    method: 'post',
    params: { tags }
  })
}

// 获取搜索建议并直接返回游戏列表
export function getSearchSuggestionsApi(keyword) {
  return request({
    url: '/app/search/suggestions',
    method: 'GET',
    params: { 
      keyword,
      pageIndex: 0,
      pageSize: 50,
      returnGames: true
    }
  })
}

// 获取游戏详情
export function getAppApi(appId) {
  return request({
    url: '/app/' + appId,
    method: 'GET'
  })
}

// 获取游戏列表（可选按标签筛选）
export function getGamesApi(tag = '') {
  return request({
    url: tag && tag.trim() !== '' ? `/app/tag/${tag}` : '/app',
    method: 'GET',
    params: {
      pageIndex: 0,
      pageSize: 50,
      tag: tag
    }
  })
}