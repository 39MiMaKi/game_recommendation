/**
 * 评分相关工具函数
 */

/**
 * 根据好评率获取评分CSS类名
 * @param {Number} rate 好评率
 * @returns {String} CSS类名
 */
export function getRatingClass(rate) {
  if (!rate && rate !== 0) return 'unknown'
  if (rate >= 80) return 'positive'
  if (rate >= 40) return 'mixed'
  return 'negative'
}

/**
 * 根据好评率获取评分图标
 * @param {Number} rate 好评率
 * @returns {String} 图标文本
 */
export function getRatingIcon(rate) {
  if (!rate && rate !== 0) return '?'
  if (rate >= 80) return '👍'
  if (rate >= 40) return '👌'
  return '👎'
}

/**
 * 根据好评率获取评分文本
 * @param {Number} rate 好评率
 * @returns {String} 评分文本
 */
export function getRatingText(rate) {
  if (!rate && rate !== 0) return '暂无评分'
  if (rate >= 80) return '好评如潮'
  if (rate >= 40) return '褒贬不一'
  return '差评如潮'
}