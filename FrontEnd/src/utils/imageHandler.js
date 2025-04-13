/**
 * 统一的图片加载错误处理工具
 */

// 默认图片路径
const DEFAULT_IMAGE = '/src/assets/blank.png'

/**
 * 处理图片加载错误
 * @param {Event} event - 图片加载错误事件
 * @param {Object} options - 配置选项
 * @param {string} options.fallbackImage - 备用图片路径
 * @param {string} options.errorText - 错误提示文本
 * @param {boolean} options.hideOnError - 是否在错误时隐藏图片
 */
export function handleImageError(event, options = {}) {
  const {
    fallbackImage = DEFAULT_IMAGE,
    errorText = '图片暂不可用',
    hideOnError = false
  } = options

  const imgElement = event.target
  console.error('图片加载失败:', imgElement.src)

  // 如果提供了备用图片，先尝试加载备用图片
  if (fallbackImage) {
    imgElement.src = fallbackImage
    // 防止备用图片也加载失败导致的无限循环
    imgElement.onerror = (e) => handleFallbackError(e, errorText, hideOnError)
  } else {
    handleFallbackError(event, errorText, hideOnError)
  }
}

/**
 * 处理备用图片加载失败的情况
 * @param {Event} event - 图片加载错误事件
 * @param {string} errorText - 错误提示文本
 * @param {boolean} hideOnError - 是否在错误时隐藏图片
 */
function handleFallbackError(event, errorText, hideOnError) {
  const imgElement = event.target
  const container = imgElement.parentElement

  if (hideOnError) {
    imgElement.style.display = 'none'
  }

  // 设置容器背景色
  if (container) {
    container.style.backgroundColor = '#1b2838'

    // 添加错误提示文本
    if (!container.querySelector('.image-error-text')) {
      const placeholder = document.createElement('div')
      placeholder.className = 'image-error-text'
      placeholder.textContent = errorText
      placeholder.style.cssText = `
        color: #8f98a0;
        font-size: 14px;
        text-align: center;
        width: 100%;
        padding: 10px;
      `
      container.appendChild(placeholder)
    }
  }
}