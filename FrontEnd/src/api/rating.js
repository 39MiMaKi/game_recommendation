import request from '@/utils/request'

/**
 * 提交游戏评价
 * @param {Number} appId 游戏ID
 * @param {Boolean} recommended 是否推荐（true为推荐，false为不推荐）
 * @param {String} comment 评价内容
 * @returns {Promise} 请求Promise
 */
export function submitRatingApi(appId, recommended, comment) {
  return request({
    url: '/rating/submit',
    method: 'post',
    data: { appId, recommended, comment }
  })
}

/**
 * 获取用户对特定游戏的评分
 * @param {Number} appId 游戏ID
 * @returns {Promise} 请求Promise
 */
export function getUserRatingApi(appId) {
  return request({
    url: '/rating/user',
    method: 'get',
    params: { appId }
  })
}

/**
 * 获取游戏的所有评分
 * @param {Number} appId 游戏ID
 * @param {Number} pageIndex 页码
 * @param {Number} pageSize 每页数量
 * @returns {Promise} 请求Promise
 */
export function getAppRatingsApi(appId, pageIndex = 1, pageSize = 10) {
  return request({
    url: '/rating/app',
    method: 'get',
    params: { appId, pageIndex, pageSize }
  })
}

/**
 * 获取用户的所有评分记录
 * @param {Number} userId 用户ID
 * @param {Number} pageIndex 页码
 * @param {Number} pageSize 每页数量
 * @returns {Promise} 请求Promise
 */
export function getUserRatingsApi(userId, pageIndex = 1, pageSize = 10) {
  return request({
    url: '/rating/user/history',
    method: 'get',
    params: { userId, pageIndex, pageSize }
  })
}

/**
 * 获取好友的评分记录
 * @param {Number} pageIndex 页码
 * @param {Number} pageSize 每页数量
 * @returns {Promise} 请求Promise
 */
export function getFriendRatingsApi(pageIndex = 1, pageSize = 10) {
  return request({
    url: '/rating/friends',
    method: 'get',
    params: { pageIndex, pageSize }
  })
}

/**
 * 获取最新的评分记录
 * @param {Number} pageIndex 页码
 * @param {Number} pageSize 每页数量
 * @returns {Promise} 请求Promise
 */
export function getRecentRatingsApi(pageIndex = 1, pageSize = 10) {
  return request({
    url: '/rating/recent',
    method: 'get',
    params: { pageIndex, pageSize }
  })
}

/**
 * 删除评分
 * @param {Number} ratingId 评分ID
 * @returns {Promise} 请求Promise
 */
export function deleteRatingApi(ratingId) {
  return request({
    url: '/rating/delete',
    method: 'post',
    data: { ratingId }
  })
}