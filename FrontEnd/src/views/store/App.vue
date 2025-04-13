

<template>
  <div class="app-detail"> 
    <div class="app-background"></div>
    
    <!-- 加载中 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <div>加载中...</div>
    </div>
    
    <!-- 错误提示 -->
    <div v-else-if="error" class="error">
      <div>获取游戏信息失败</div>
      <button @click="getAppDetail">重试</button>
    </div>
    
    <!-- 游戏详情 -->
    <div v-else-if="app" class="app-content steam-style">
      <!-- 游戏标题和基本信息 -->
      <div class="app-header">
        <div class="app-header-info">
          <div class="app-title">{{ app.name }}</div>
          <div class="app-meta">
            <div class="app-developer">开发商: {{ app.developer }}</div>
            <div class="app-publisher">发行商: {{ app.publisher }}</div>
            <div class="app-release-date">发行日期: {{ new Date(app.createTime).toLocaleDateString() }}</div>
            <div v-if="app.reviewCount > 0" class="app-rating">
              <div class="rating-summary" :class="getRatingClass(app.positiveRate)">
                <span class="rating-icon">{{ getRatingIcon(app.positiveRate) }}</span>
                <span class="rating-text">{{ getRatingText(app.positiveRate) }}</span>
                <span class="rating-percent">({{ app.positiveRate.toFixed(0) }}% 好评)</span>
                <span class="rating-count">({{ app.reviewCount }}条评测)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 游戏截图轮播 - 移到顶部 -->
      <div class="app-screenshots top-screenshots">
        <div v-if="getImageList(app.images).length === 0" class="no-screenshots">
          暂无游戏截图
        </div>
        <div v-else class="screenshots-carousel">
          <div class="carousel-container">
            <img 
              :src="getImageList(app.images)[currentImageIndex]" 
              alt="游戏截图" 
              @error="handleImageError"
              class="carousel-image"
            >
            <div class="carousel-controls">
              <button class="carousel-btn prev" @click="prevImage">❮</button>
              <button class="carousel-btn next" @click="nextImage">❯</button>
            </div>
          </div>
          
          <div class="carousel-thumbnails">
            <div 
              v-for="(img, index) in getImageList(app.images).slice(0, 5)" 
              :key="index"
              :class="['thumbnail', { active: index === currentImageIndex }]"
              @click="currentImageIndex = index"
            >
              <img :src="img" alt="缩略图" @error="handleImageError">
            </div>
            <div v-if="getImageList(app.images).length > 5" class="thumbnail more">
              +{{ getImageList(app.images).length - 5 }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 游戏主要信息 - 介绍和购买信息并列 -->
      <div class="app-main">
        <!-- 左侧：游戏信息 -->
        <div class="app-info-container">
          <div class="app-info">
            <div class="app-description-short">{{ app.description ? app.description.substring(0, 300) + '...' : '暂无游戏描述' }}</div>
            
            <div class="app-meta">
              <div><span>开发商:</span> {{ app.developer }}</div>
              <div><span>发行商:</span> {{ app.publisher }}</div>
              <div><span>发行日期:</span> {{ new Date(app.createTime).toLocaleDateString() }}</div>
            </div>
            
            <div class="app-tags">
              <div class="tag-title">标签:</div>
              <div class="tag-list">
                <span v-for="(tag, index) in getTagList(app.tags)" :key="index" class="tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧：购买信息 -->
        <div class="app-purchase-container">
          <div class="app-purchase">
            <div class="app-platforms">
              <div v-if="app.win" class="platform" title="Windows">
                <img src="@/assets/icon_platform_win.png" alt="Windows">
              </div>
              <div v-if="app.mac" class="platform" title="MacOS">
                <img src="@/assets/icon_platform_mac.png" alt="MacOS">
              </div>
              <div v-if="app.linux" class="platform" title="Linux">
                <img src="@/assets/icon_platform_linux.png" alt="Linux">
              </div>
            </div>
            
            <div class="app-price-area">
              <div v-if="app.discount === 0" class="app-price">{{ getPriceStr(app.price) }}</div>
              <div v-else class="app-price-discounted">
                <span class="app-discount">{{ getDiscountStr(app.discount) }}</span>
                <span class="app-origin-price">{{ getPriceStr(app.price) }}</span>
                <span class="app-final-price">{{ getPriceStr(app.finalPrice) }}</span>
              </div>
            </div>
            
            <div class="app-actions">
              <button 
                class="btn-wishlist" 
                @click="addToWishlist" 
                :disabled="isInWishlist"
              >
                {{ isInWishlist ? '已加入愿望单' : '加入愿望单' }}
              </button>
              <ExpandButton :status="app?.status || 0" @update:status="val => app.status = val" :app-id="Number(app?.appId) || 0" />
            </div>
          </div>
        </div>
      </div>
      
      <!-- 愿望单提示 -->
      <div class="wishlist-toast" v-if="showWishlistToast">
        <div class="toast-content">
          <i class="toast-icon">✓</i>
          <span>已成功添加到您的愿望单</span>
        </div>
      </div>
      
      <!-- 游戏评价区域 -->
      <div class="app-ratings steam-style">
        <h2>用户评价</h2>
        
        <!-- 评分统计 -->
        <div v-if="app.reviewCount > 0" class="rating-summary-section">
          <div class="rating-stats">
            <div class="rating-percentage" :class="getRatingClass(app.positiveRate)">
              {{ app.positiveRate.toFixed(0) }}%
            </div>
            <div class="rating-text">
              {{ getRatingText(app.positiveRate) }}
            </div>
            <div class="rating-count">
              基于 {{ app.reviewCount }} 条评价
            </div>
          </div>
        </div>

        <!-- 提交评价 -->
        <div v-if="token" class="submit-rating steam-style">
          <h3>评价这款游戏</h3>
          <div class="rating-buttons">
            <button 
              class="recommend-btn steam-btn" 
              :class="{ active: userRating?.recommended === true }"
              @click="submitRating(true)"
            >
              <i class="fas fa-thumbs-up"></i> 推荐
            </button>
            <button 
              class="not-recommend-btn steam-btn" 
              :class="{ active: userRating?.recommended === false }"
              @click="submitRating(false)"
            >
              <i class="fas fa-thumbs-down"></i> 不推荐
            </button>
          </div>
        </div>
        
        <!-- 登录提示 -->
        <div v-else class="login-to-rate steam-style">
          <p>登录以评价这款游戏</p>
          <button class="login-btn steam-btn" @click="$router.push('/login')">登录</button>
        </div>

        <!-- 评价列表 -->
        <div class="ratings-list steam-style">
          <div v-if="ratingsLoading" class="loading-ratings">
            <div class="spinner"></div>
            <div>加载评价中...</div>
          </div>
          <div v-else-if="ratings.length === 0" class="no-ratings">
            还没有用户评价，来做第一个评价这款游戏的玩家吧！
          </div>
          <div v-else class="rating-item steam-style" v-for="rating in ratings" :key="rating.id">
            <div class="rating-header">
              <div class="rating-user">
                <img :src="rating.userAvatar || 'default-avatar.png'" alt="用户头像" class="user-avatar">
                <span>{{ rating.userName || '匿名用户' }}</span>
              </div>
              <div class="rating-recommendation" :class="rating.recommended ? 'positive' : 'negative'">
                <div class="recommendation-icon">
                  <i class="fas" :class="rating.recommended ? 'fa-thumbs-up' : 'fa-thumbs-down'"></i>
                </div>
                <span>{{ rating.recommended ? '推荐' : '不推荐' }}</span>
              </div>
            </div>
            <div class="rating-time">发布于 {{ formatDate(rating.ratingTime) }}</div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="ratings.length > 0 && hasMoreRatings" class="load-more steam-style">
          <button class="load-more-btn steam-btn" @click="loadMoreRatings" :disabled="ratingsLoading">
            {{ ratingsLoading ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </div>
      
      <!-- 相似游戏推荐 -->
      <div class="app-recommendations">
        <h2>您可能也喜欢</h2>
        <div class="recommendation-list">
          <div v-if="recommendationsLoading" class="recommendation-placeholder">加载推荐中...</div>
          <div v-else-if="recommendations.length === 0" class="recommendation-placeholder">
            暂无推荐游戏
          </div>
          <div v-else class="recommendation-grid">
            <div v-for="game in recommendations" :key="game.appId" class="recommendation-item" @click="$router.push(`/steam/app/${game.appId}`)">
              <img :src="game.cover" :alt="game.name" class="game-cover" @error="handleImageError">
              <div class="game-info">
                <div class="game-name">{{ game.name }}</div>
                <div :class="['game-price', { 'free': game.finalPrice === 0, 'discounted': game.discount > 0 }]">
                  <template v-if="game.discount > 0">
                    <span class="discount-tag">-{{ (game.discount * 100).toFixed(0) }}%</span>
                    <span class="original-price">¥{{ game.price?.toFixed(2) }}</span>
                    <span class="final-price">¥{{ game.finalPrice?.toFixed(2) }}</span>
                  </template>
                  <template v-else>
                    {{ getPriceStr(game.finalPrice) }}
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import ExpandButton from '@/components/ExpandButton.vue'
import { getAppApi } from '@/api/app'
import { getRecommendationsApi } from '@/api/app'
import { getRatingClass, getRatingIcon, getRatingText } from '@/utils'
import { getAppRatingsApi, getUserRatingApi, submitRatingApi } from '@/api/rating'

const route = useRoute()
const store = useStore()
const appId = route.params.appId

const app = ref(null)
const loading = ref(true)
const error = ref(false)
const token = ref(store.getters['user/token'])
const recommendations = ref([])
const recommendationsLoading = ref(true)

// 评价相关的状态
const ratings = ref([])
const ratingsLoading = ref(false)
const userRating = ref(null)
const hasMoreRatings = ref(true)
const currentPage = ref(1)
const pageSize = 10

// 格式化日期
const formatDate = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取用户对当前游戏的评价
const getUserRating = async () => {
  if (!token.value) return
  try {
    const { data } = await getUserRatingApi(appId)
    userRating.value = data
  } catch (err) {
    console.error('获取用户评价失败:', err)
  }
}

// 获取游戏评价列表
const getAppRatings = async (page = 1) => {
  try {
    ratingsLoading.value = true
    const { data } = await getAppRatingsApi(appId, page, pageSize)
    if (page === 1) {
      ratings.value = data.content
    } else {
      ratings.value = [...ratings.value, ...data.content]
    }
    hasMoreRatings.value = data.content.length === pageSize
    currentPage.value = page
  } catch (err) {
    console.error('获取评价列表失败:', err)
  } finally {
    ratingsLoading.value = false
  }
}

// 加载更多评价
const loadMoreRatings = () => {
  if (!hasMoreRatings.value || ratingsLoading.value) return
  getAppRatings(currentPage.value + 1)
}

// 提交评价
const submitRating = async (recommended) => {
  if (!token.value) return
  try {
    await submitRatingApi(appId, recommended)
    // 更新用户评价状态
    await getUserRating()
    // 刷新评价列表
    await getAppRatings(1)
    // 刷新游戏详情以更新评价统计
    await getAppDetail()
  } catch (err) {
    console.error('提交评价失败:', err)
  }
}

// 获取游戏详情
const getAppDetail = async () => {
  try {
    loading.value = true
    error.value = false
    const { data } = await getAppApi(appId)
    if (data) {
      // 确保数据处理正确
      app.value = data
      
      // 处理图片数据格式 - 不再转换为字符串，保持数组格式
      if (app.value.images) {
        // 如果images是字符串，转换为数组
        if (typeof app.value.images === 'string') {
          if (app.value.images.includes(',')) {
            app.value.images = app.value.images.split(',').filter(img => img && img.trim() !== '')
          } else if (app.value.images.includes(';')) {
            app.value.images = app.value.images.split(';').filter(img => img && img.trim() !== '')
          } else {
            app.value.images = [app.value.images]
          }
        }
        // 如果images是数组，且第一个元素包含逗号（旧格式的数组包装）
        else if (Array.isArray(app.value.images) && app.value.images.length === 1 && typeof app.value.images[0] === 'string' && app.value.images[0].includes(',')) {
          app.value.images = app.value.images[0].split(',').filter(img => img && img.trim() !== '')
        }
      }
      
      // 使用 JSON 转换后再打印，可以看到更清晰的数据结构
      console.log('获取到的游戏数据:', JSON.parse(JSON.stringify(app.value)))
    } else {
      error.value = true
    }
  } catch (err) {
    console.error('获取游戏详情失败:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

// 格式化价格
const getPriceStr = (price) => {
  if (price === null || price === undefined || typeof price !== 'number') return '价格未知'
  return price === 0 ? '免费开玩' : '¥' + price.toFixed(2)
}

// 格式化折扣
const getDiscountStr = (discount) => {
  return '-' + (discount * 100).toFixed(0) + '%'
}

// 解析图片列表
const getImageList = (imagesStr) => {
  if (!imagesStr) return []
  
  let imageList = []
  
  // 如果已经是数组，直接使用
  if (Array.isArray(imagesStr)) {
    imageList = imagesStr.map(item => {
      if (typeof item === 'string') {
        return item.trim()
      }
      return ''
    }).filter(item => item !== '')
  }
  // 处理字符串情况
  else if (typeof imagesStr === 'string') {
    if (imagesStr.includes(',')) {
      imageList = imagesStr.split(',').filter(img => img && img.trim() !== '')
    } else if (imagesStr.includes(';')) {
      imageList = imagesStr.split(';').filter(img => img && img.trim() !== '')
    } else if (imagesStr.trim() !== '') {
      imageList = [imagesStr]
    }
  }
  
  // 确保所有URL都是绝对URL并且移除引号
  return imageList.map(url => {
    // 移除可能的引号
    url = url.replace(/['"]/g, '')
    
    // 确保URL是绝对URL
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    } else {
      // 如果是相对URL，转换为绝对URL
      return `https://cdn.akamai.steamstatic.com${url.startsWith('/') ? '' : '/'}${url}`
    }
  })
}

// 解析标签列表
const getTagList = (tagsStr) => {
  // 添加类型检查
  if (!tagsStr) return []
  if (Array.isArray(tagsStr)) return tagsStr
  if (typeof tagsStr === 'string') return tagsStr.split(',').filter(tag => tag && tag.trim() !== '')
  return []
}





onMounted(() => {
  getAppDetail()
  getRecommendations()
})




// 获取推荐游戏
const getRecommendations = async () => {
  try {
    recommendationsLoading.value = true
    const { data } = await getRecommendationsApi({ appId: appId, limit: 12 })
    
    // 确保数据是数组并且每个项目都有必要的属性
    if (Array.isArray(data)) {
      // 随机选择5个游戏
      const randomGames = data.sort(() => Math.random() - 0.5).slice(0, 5)
      recommendations.value = randomGames.map(game => ({
        ...game,
        // 确保必要的属性存在，避免undefined错误
        appId: game.appId || 0,
        name: game.name || '未知游戏',
        cover: game.cover || '',
        price: typeof game.price === 'number' ? game.price : 0,
        discount: typeof game.discount === 'number' ? game.discount : 0,
        finalPrice: typeof game.finalPrice === 'number' ? game.finalPrice : 0
      }))
      console.log('处理后的推荐游戏数据:', recommendations.value)
    } else {
      console.error('推荐游戏数据格式错误:', data)
      recommendations.value = []
    }
  } catch (err) {
    console.error('获取推荐游戏失败:', err)
    recommendations.value = []
  } finally {
    recommendationsLoading.value = false
  }
}

// 处理图片加载错误
const handleImageError = (event) => {
  console.error('图片加载失败:', event.target.src)
  
  // 尝试使用游戏封面图作为备用
  if (app.value && app.value.cover) {
    event.target.src = app.value.cover
    // 防止无限循环
    event.target.onerror = (e) => {
      e.target.style.display = 'none'
      const container = e.target.parentElement
      container.style.backgroundColor = '#1b2838'
      
      if (!container.querySelector('.image-error-text')) {
        const placeholder = document.createElement('div')
        placeholder.className = 'image-error-text'
        placeholder.textContent = '游戏截图暂不可用'
        placeholder.style.color = '#8f98a0'
        placeholder.style.fontSize = '14px'
        placeholder.style.textAlign = 'center'
        placeholder.style.width = '100%'
        container.appendChild(placeholder)
      }
    }
    return
  }
  
  // 如果没有备用图片，显示错误提示
  event.target.style.display = 'none'
  const container = event.target.parentElement
  container.style.backgroundColor = '#1b2838'
  
  if (!container.querySelector('.image-error-text')) {
    const placeholder = document.createElement('div')
    placeholder.className = 'image-error-text'
    placeholder.textContent = '游戏截图暂不可用'
    placeholder.style.color = '#8f98a0'
    placeholder.style.fontSize = '14px'
    placeholder.style.textAlign = 'center'
    placeholder.style.width = '100%'
    container.appendChild(placeholder)
  }
}

// 添加轮播图相关变量和方法
const currentImageIndex = ref(0)
const showAllImages = ref(false)

// 切换到下一张图片
const nextImage = () => {
  const imageList = getImageList(app.value.images)
  if (imageList.length > 0) {
    currentImageIndex.value = (currentImageIndex.value + 1) % imageList.length
  }
}

// 切换到上一张图片
const prevImage = () => {
  const imageList = getImageList(app.value.images)
  if (imageList.length > 0) {
    currentImageIndex.value = (currentImageIndex.value - 1 + imageList.length) % imageList.length
  }
}

   // 添加愿望单相关状态
   const isInWishlist = ref(false)
            const showWishlistToast = ref(false)
            
            // 添加愿望单功能
            const addToWishlist = () => {
            if (!isInWishlist.value) {
            isInWishlist.value = true
            showWishlistToast.value = true
            
            // 3秒后自动关闭提示
            setTimeout(() => {
            showWishlistToast.value = false
            }, 3000)
            }
            }
</script>

<style scoped lang="scss">
.app-detail {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  color: #c6d4df;
  position: relative;
}

.app-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #1b2838;
  z-index: -1;
}

.loading, .error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  width: 100%;
  
  .spinner {
    width: 50px;
    height: 50px;
    border: 5px solid rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    border-top-color: #66c0f4;
    animation: spin 1s ease-in-out infinite;
    margin-bottom: 20px;
  }
  
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  
  button {
    background-color: #66c0f4;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 2px;
    cursor: pointer;
    margin-top: 20px;
    
    &:hover {
      background-color: #1a9fff;
    }
  }
}

.app-header {
  margin-bottom: 20px;
  
  .app-header-info {
    .app-title {
      color: #ffffff;
      font-size: 26px;
      margin: 0 0 10px 0;
    }
    
    .app-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      font-size: 14px;
      color: #8f98a0;
      
      .app-rating {
        margin-top: 5px;
        
        .rating-summary {
          display: inline-flex;
          align-items: center;
          padding: 5px 10px;
          border-radius: 3px;
          
          &.positive {
            background-color: rgba(164, 208, 7, 0.2);
            color: #a4d007;
          }
          
          &.mixed {
            background-color: rgba(247, 154, 72, 0.2);
            color: #f79a48;
          }
          
          &.negative {
            background-color: rgba(171, 63, 63, 0.2);
            color: #ff9999;
          }
          
          .rating-icon {
            font-size: 18px;
            margin-right: 5px;
          }
          
          .rating-text {
            font-weight: bold;
            margin-right: 5px;
          }
          
          .rating-percent, .rating-count {
            font-size: 12px;
            margin-left: 5px;
            opacity: 0.8;
          }
        }
      }
    }
  }
}

.app-main {
  display: flex;
  gap: 20px;
  margin-bottom: 60px;
  
  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.app-info-container, .app-purchase-container {
  flex: 1;
  
  @media (max-width: 768px) {
    width: 100%;
  }
}

.app-info, .app-purchase {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 15px;
  height: 100%;
}

/* 顶部截图轮播样式 */
.top-screenshots {
  margin-bottom: 20px;
  
  .screenshots-carousel {
    width: 100%;
    
    .carousel-container {
      width: 100%;
      height: 400px;
      background-color: rgba(0, 0, 0, 0.2);
      border-radius: 4px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      
      .carousel-image {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
      }
      
      .carousel-controls {
        position: absolute;
        width: 100%;
        display: flex;
        justify-content: space-between;
        padding: 0 20px;
        
        .carousel-btn {
          background-color: rgba(0, 0, 0, 0.5);
          color: white;
          border: none;
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          font-size: 18px;
          
          &:hover {
            background-color: rgba(103, 193, 245, 0.6);
          }
        }
      }
    }
    
    .carousel-thumbnails {
      display: flex;
      gap: 10px;
      margin-top: 10px;
      overflow-x: auto;
      padding-bottom: 5px;
      
      .thumbnail {
        width: 120px;
        height: 68px;
        border-radius: 2px;
        overflow: hidden;
        cursor: pointer;
        opacity: 0.6;
        transition: opacity 0.2s;
        flex-shrink: 0;
        
        &:hover, &.active {
          opacity: 1;
        }
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        &.more {
          background-color: rgba(0, 0, 0, 0.4);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #8f98a0;
          font-size: 12px;
        }
      }
    }
  }
}

.app-details {
  width: 100%;
  margin-bottom: 40px;
}

/* 评分区域样式 */
.app-ratings {
  margin-top: 30px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 20px;
}

.app-ratings h2 {
  color: #ffffff;
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 10px;
}

.app-ratings h3 {
  color: #c6d4df;
  font-size: 16px;
  margin-top: 25px;
  margin-bottom: 15px;
}

.login-to-rate {
  background-color: rgba(62, 126, 167, 0.1);
  border-radius: 3px;
  padding: 15px;
  text-align: center;
  margin-bottom: 20px;
}

.login-to-rate p {
  margin-bottom: 10px;
  color: #8f98a0;
}

.login-btn {
  background-color: #1a9fff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 2px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-btn:hover {
  background-color: #0b78c9;
}

.login-to-rate.steam-style {
  background: #1b2838;
  border-radius: 4px;
  padding: 20px;
  margin-top: 20px;
  
  h2 {
    color: #fff;
    font-size: 24px;
    margin-bottom: 20px;
  }
}

.submit-rating.steam-style {
  background: #16202d;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  
  h3 {
    color: #fff;
    margin-bottom: 15px;
  }
  
  .rating-buttons {
    display: flex;
    gap: 10px;
  }
}

.steam-btn {
  padding: 10px 20px;
  border-radius: 2px;
  border: none;
  cursor: pointer;
  font-size: 15px;
  transition: background-color 0.2s;
  
  &.recommend-btn {
    background: #67c1f5;
    color: #fff;
    
    &:hover {
      background: #52b9f5;
    }
    
    &.active {
      background: #2e89c9;
    }
  }
  
  &.not-recommend-btn {
    background: #c94f4f;
    color: #fff;
    
    &:hover {
      background: #b94141;
    }
    
    &.active {
      background: #a53d3d;
    }
  }
}

.login-to-rate.steam-style {
  background: #16202d;
  padding: 20px;
  border-radius: 4px;
  text-align: center;
  
  p {
    color: #8f98a0;
    margin-bottom: 15px;
  }
  
  .login-btn {
    background: #67c1f5;
    color: #fff;
    
    &:hover {
      background: #52b9f5;
    }
  }
}

.ratings-list.steam-style {
  .rating-item {
    background: #16202d;
    padding: 15px;
    border-radius: 4px;
    margin-bottom: 10px;
    
    .rating-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .rating-user {
        display: flex;
        align-items: center;
        gap: 10px;
        
        .user-avatar {
          width: 32px;
          height: 32px;
          border-radius: 50%;
        }
        
        span {
          color: #c1dbf4;
        }
      }
      
      .rating-recommendation {
        padding: 5px 10px;
        border-radius: 2px;
        font-size: 14px;
        
        &.positive {
          background: #66c0f4;
          color: #fff;
        }
        
        &.negative {
          background: #c94f4f;
          color: #fff;
        }
      }
    }
    
    .rating-time {
      color: #8f98a0;
      font-size: 12px;
    }
  }
}

.load-more.steam-style {
  text-align: center;
  margin-top: 20px;
  
  .load-more-btn {
    background: #67c1f5;
    color: #fff;
    
    &:hover {
      background: #52b9f5;
    }
    
    &:disabled {
      background: #2e5470;
      cursor: not-allowed;
    }
  }
}
.load-more-btn {
  background-color: rgba(103, 193, 245, 0.2);
  border: none;
  border-radius: 2px;
  color: #66c0f4;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.load-more-btn:hover:not(:disabled) {
  background-color: rgba(103, 193, 245, 0.4);
}

.load-more-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.app-description-short {
  margin-bottom: 15px;
  font-size: 14px;
  line-height: 1.5;
  color: #acb2b8;
}

.app-meta {
  margin-bottom: 15px;
  font-size: 12px;
  
  div {
    margin-bottom: 5px;
  }
  
  span {
    color: #8f98a0;
    margin-right: 5px;
  }
}

.app-tags {
  .tag-title {
    font-size: 12px;
    color: #8f98a0;
    margin-bottom: 5px;
  }
  
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
  }
  
  .tag {
    background-color: rgba(103, 193, 245, 0.2);
    color: #67c1f5;
    padding: 2px 8px;
    border-radius: 2px;
    font-size: 11px;
    cursor: pointer;
    
    &:hover {
      background-color: rgba(103, 193, 245, 0.4);
    }
  }
}

.app-platforms {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  
  .platform {
    width: 20px;
    height: 20px;
    
    img {
      width: 100%;
      height: 100%;
    }
  }
}

.app-price-area {
  margin-bottom: 15px;
}

.app-price {
  font-size: 24px;
  color: #ffffff;
}

.app-price-discounted {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  
  .app-discount {
    background-color: #4c6b22;
    color: #a4d007;
    padding: 2px 5px;
    border-radius: 2px;
    font-size: 14px;
  }
  
  .app-origin-price {
    color: #738895;
    text-decoration: line-through;
    font-size: 14px;
  }
  
  .app-final-price {
    font-size: 24px;
    color: #ffffff;
  }
}

.app-actions {
  display: flex;
  
  .btn-wishlist {
    flex: 1;
    background: linear-gradient(to right, #417a9b, #305d7a);
    color: white;
    border: none;
    padding: 10px;
    border-radius: 2px;
    cursor: pointer;
    font-size: 15px;
    margin-top: 80px;
    
    &:hover {
      background: linear-gradient(to right, #5b9eca, #417a9b);
    }
    
    &:disabled {
      background: linear-gradient(to right, #5b9eca, #417a9b);
      opacity: 0.8;
      cursor: default;
    }
  }
}

.wishlist-toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 15px 20px;
  border-radius: 4px;
  z-index: 1000;
  animation: fadeIn 0.3s, fadeOut 0.3s 2.7s;
  
  .toast-content {
    display: flex;
    align-items: center;
    
    .toast-icon {
      background-color: #5ba32b;
      color: white;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 10px;
      font-size: 12px;
    }
  }
  
  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
  }
  
  @keyframes fadeOut {
    from { opacity: 1; transform: translateY(0); }
    to { opacity: 0; transform: translateY(20px); }
  }
}

 .app-requirements {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  
  h2 {
    font-size: 18px;
    color: #ffffff;
    margin-bottom: 15px;
  }
}

.description-content {
  font-size: 14px;
  line-height: 1.6;
  color: #acb2b8;
  white-space: pre-line;
}

.requirements-content {
  display: flex;
  gap: 20px;
  
  @media (max-width: 768px) {
    flex-direction: column;
  }
  
  .requirements-section {
    flex: 1;
    
    h3 {
      font-size: 16px;
      color: #ffffff;
      margin-bottom: 10px;
    }
    
    ul {
      list-style: none;
      padding: 0;
      
      li {
        margin-bottom: 5px;
        font-size: 13px;
        color: #acb2b8;
        
        strong {
          color: #8f98a0;
        }
      }
    }
  }
}

.app-recommendations {
  margin-top: 30px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 20px;
  
  h2 {
    font-size: 18px;
    color: #ffffff;
    margin-bottom: 15px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding-bottom: 10px;
  }
  
  .recommendation-placeholder {
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 4px;
    padding: 40px;
    text-align: center;
    color: #8f98a0;
  }
  
  .recommendation-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-top: 20px;
    
    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .recommendation-item {
      background-color: rgba(0, 0, 0, 0.2);
      border-radius: 4px;
      overflow: hidden;
      transition: transform 0.2s, box-shadow 0.2s;
      cursor: pointer;
      position: relative;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
        
        .game-info {
          background-color: rgba(103, 193, 245, 0.2);
        }
        
        .game-cover {
          transform: scale(1.05);
        }
      }
      
      .game-cover {
        width: 100%;
        aspect-ratio: 16/9;
        object-fit: cover;
        transition: transform 0.3s ease;
      }
      
      .game-info {
        padding: 10px;
        transition: background-color 0.2s;
        
        .game-name {
          color: #ffffff;
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 5px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .game-price {
          color: #acb2b8;
          font-size: 13px;
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 5px;
          
          &.free {
            color: #a4d007;
          }
          
          &.discounted {
            color: #a4d007;
          }
          
          .discount-tag {
            background-color: #4c6b22;
            color: #a4d007;
            padding: 1px 4px;
            border-radius: 2px;
            font-size: 12px;
          }
          
          .original-price {
            color: #738895;
            text-decoration: line-through;
            font-size: 12px;
          }
          
          .final-price {
            font-weight: 500;
          }
        }
      }
    }
  }
}
</style>