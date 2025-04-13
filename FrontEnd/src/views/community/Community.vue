<template>
<div class="app">
    <div class="app-header">
      <div class="app-header-avatar">
        <img src="https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fdefault_avatar.jpg" alt="">
      </div>
      社区中心
    </div>

    <div class="app-content">
      <div class="nav">
        <div class="nav-title">社区</div>
        <div class="nav-item current">
          <img class="nav-item-icon" src="@/assets/iconsheet_friends_list.png" alt="">
          评分记录
        </div>
      </div>
      <div class="router-view-area">
        <div class="search-box">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索用户..." 
            @input="searchUsers"
          >
          <div v-if="searchResults.length > 0" class="search-results">
            <div 
              v-for="user in searchResults" 
              :key="user.userId" 
              class="search-result-item"
              @click="goToProfile(user.userId)"
            >
              <img :src="user.avatar || 'https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fdefault_avatar.jpg'" alt="用户头像">
              <span>{{ user.nickname || user.username }}</span>
            </div>
          </div>
        </div>
        
        <div class="section">
          <h2>评分记录</h2>
          <div class="rating-records-container">
            <div class="tabs">
              <div 
                class="tab" 
                :class="{ active: activeTab === 'my' }" 
                @click="activeTab = 'my'"
                v-if="token"
              >
                我的评分
              </div>
              
              <div 
                class="tab" 
                :class="{ active: activeTab === 'recent' }" 
                @click="activeTab = 'recent'"
              >
                最新评分
              </div>
            </div>
            
            <!-- 我的评分 -->
            <div v-if="activeTab === 'my' && token" class="tab-content">
              <RatingHistory :userId="userId" />
            </div>
            
            <!-- 最新评分 -->
            <div v-else-if="activeTab === 'recent'" class="tab-content">
              <div v-if="isLoadingRecent" class="loading">
                <div class="spinner"></div>
                <div>加载中...</div>
              </div>
              
              <div v-else-if="recentRatings.length === 0" class="empty-state">
                <p>暂无最新评分记录</p>
              </div>
              
              <div v-else class="recent-ratings">
                <div v-for="rating in recentRatings" :key="rating.id" class="rating-item">
                  <div class="rating-user" @click="router.push(`/profile/${rating.userId}`)">
                    <img :src="rating.userAvatar || 'https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fdefault_avatar.jpg'" alt="用户头像" class="user-avatar">
                    <span class="username">{{ rating.nickname || rating.username }}</span>
                  </div>
                  
                  <div class="rating-game" @click="router.push(`/app/${rating.appId}`)">
                    <img :src="rating.appCover || 'https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fblank.png'" alt="游戏封面" class="game-cover">
                    <div class="game-info">
                      <div class="game-name">{{ rating.appName }}</div>
                      <div class="rating-score">
                        <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= rating.score }">
                          ★
                        </span>
                        <span class="score-text">{{ rating.score }}/5</span>
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="rating.comment" class="rating-comment">
                    <p>{{ rating.comment }}</p>
                  </div>
                  
                  <div class="rating-date">{{ formatDate(rating.createTime) }}</div>
                </div>
                
                <div v-if="hasMoreRecentRatings" class="load-more">
                  <button 
                    class="load-more-btn" 
                    :disabled="isLoadingMoreRecent" 
                    @click="loadMoreRecentRatings"
                  >
                    {{ isLoadingMoreRecent ? '加载中...' : '加载更多' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

      <div class="community-right">
        <div class="section">
          <h2>推荐游戏</h2>
          <div class="game-grid">
            <div v-for="game in recommendedGames" :key="game.appId" class="game-card" @click="router.push(`/app/${game.appId}`)">
              <img :src="game.header" alt="游戏封面">
              <div class="game-info">
                <div class="game-name">{{ game.name }}</div>
                <div class="game-price">{{ game.finalPrice === 0 ? '免费' : `¥${game.finalPrice}` }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="section">
          <h2>即将举行的活动</h2>
          <div class="event-list">
            <div class="event-item">
              <div class="event-title">夏季特卖</div>
              <div class="event-time">6月22日 - 7月6日</div>
            </div>
            <div class="event-item">
              <div class="event-title">独立游戏展示会</div>
              <div class="event-time">8月1日 - 8月7日</div>
            </div>
            <div class="event-item">
              <div class="event-title">游戏开发者直播</div>
              <div class="event-time">每周五 20:00</div>
            </div>
          </div>
        </div>

        <div v-if="token" class="section">
          <h2>个人中心</h2>
          <div class="profile-shortcuts">
            <div class="shortcut-item" @click="router.push(`/profile/${userId}`)">
              <span>我的资料</span>
            </div>
            <div class="shortcut-item" @click="router.push('/friends')">
              <span>我的好友</span>
            </div>
            <div class="shortcut-item" @click="router.push('/wishlist')">
              <span>我的愿望单</span>
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
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { getPersonalizedRecommendationsApi } from '@/api/app.js'
import { searchUsersApi } from '@/api/user.js'
import { getRecentRatingsApi } from '@/api/rating'
import RatingHistory from '@/components/RatingHistory.vue'

const router = useRouter()
const store = useStore()

// 用户登录状态
const token = store.getters.token
const userId = store.getters.userId

// 标签页状态
const activeTab = ref(token ? 'my' : 'recent')

// 最新评分状态
const recentRatings = ref([])
const isLoadingRecent = ref(false)
const isLoadingMoreRecent = ref(false)
const recentCurrentPage = ref(1)
const hasMoreRecentRatings = ref(false)

const pageSize = 5



// 获取最新评分
const fetchRecentRatings = async (page = 1) => {
  try {
    isLoadingRecent.value = page === 1 ? true : false
    isLoadingMoreRecent.value = page > 1 ? true : false
    
    const { data } = await getRecentRatingsApi(page, pageSize)
    
    if (page === 1) {
      recentRatings.value = data.records || []
    } else {
      recentRatings.value = [...recentRatings.value, ...(data.records || [])]
    }
    
    recentCurrentPage.value = page
    hasMoreRecentRatings.value = data.hasNext || false
  } catch (error) {
    console.error('获取最新评分失败:', error)
  } finally {
    isLoadingRecent.value = false
    isLoadingMoreRecent.value = false
  }
}

// 加载更多最新评分
const loadMoreRecentRatings = () => {
  if (isLoadingMoreRecent.value || !hasMoreRecentRatings.value) return
  fetchRecentRatings(recentCurrentPage.value + 1)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffSec = Math.floor(diffMs / 1000)
  const diffMin = Math.floor(diffSec / 60)
  const diffHour = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHour / 24)
  
  if (diffDay > 30) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  } else if (diffDay > 0) {
    return `${diffDay}天前`
  } else if (diffHour > 0) {
    return `${diffHour}小时前`
  } else if (diffMin > 0) {
    return `${diffMin}分钟前`
  } else {
    return '刚刚'
  }
}

// 推荐游戏
const recommendedGames = ref([])

// 搜索用户
const searchKeyword = ref('')
const searchResults = ref([])
const isSearching = ref(false)

const searchUsers = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  isSearching.value = true
  try {
    const res = await searchUsersApi(searchKeyword.value, 1, 5)
    searchResults.value = res.data.records || []
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    isSearching.value = false
  }
}

const goToProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

// 获取推荐游戏
const getRecommendedGames = async () => {
  try {
    const res = await getPersonalizedRecommendationsApi(6)
    recommendedGames.value = res.data || []
  } catch (error) {
    console.error('获取推荐游戏失败:', error)
  }
}

// 初始化加载数据
onMounted(() => {
  getRecommendedGames()
  
  
  
  if (activeTab.value === 'recent') {
    fetchRecentRatings()
  }
})

onMounted(() => {
  getRecommendedGames()
})

</script>
<style scoped lang="scss">
.app {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: calc(100vh - 104px);
  background: #1b2838 url("@/assets/colored_body_top2.png") center -104px no-repeat;
}

.app-header {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 940px;
  padding: 25px 0;
  color: #ffffff;
  background: url("@/assets/bg_highlight.png") center;
  font-size: 26px;
}

.app-header-avatar {
  width: 68px;
  height: 68px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.app-content {
  display: flex;
  width: 940px;
  gap: 20px;
}

.nav {
  display: flex;
  flex-direction: column;
}

.nav-title {
  padding: 12px 12px 0;
}

.nav-title, .nav-item-num {
  color: #7092a5;
  font-size: 11px;
}

.nav-item {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 20px;
  width: 230px;
  height: 40px;
  padding: 10px;
  color: #ebebeb;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;

  &:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }

  &.current {
    background-color: rgba(255, 255, 255, 0.1);
  }
}

.nav-item-icon {
  width: 16px;
  height: 16px;
}

.nav-item-num {
  margin-left: auto;
}

.router-view-area {
  width: 100%;
  padding-top: 24px;
}

.search-box {
  position: relative;
  width: 300px;
  margin-bottom: 20px;
  
  input {
    width: 100%;
    padding: 10px 15px;
    background-color: #316282;
    border: none;
    border-radius: 3px;
    color: #ffffff;
    font-size: 14px;
    
    &::placeholder {
      color: #8ba6b6;
    }
  }
  
  .search-results {
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    background-color: #1b2838;
    border: 1px solid #316282;
    border-radius: 0 0 3px 3px;
    z-index: 10;
    max-height: 300px;
    overflow-y: auto;
    
    .search-result-item {
      display: flex;
      align-items: center;
      padding: 10px;
      cursor: pointer;
      
      &:hover {
        background-color: #316282;
      }
      
      img {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        margin-right: 10px;
      }
    }
  }
}

.section {
  background-color: rgba(23, 35, 55, 0.8);
  border-radius: 3px;
  padding: 15px;
  margin-bottom: 20px;
  
  h2 {
    color: #ffffff;
    font-size: 18px;
    margin-top: 0;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #316282;
  }
}

.activity-list {
  .activity-item {
    display: flex;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(49, 98, 130, 0.2);
    
    &:last-child {
      border-bottom: none;
      margin-bottom: 0;
      padding-bottom: 0;
    }
    
    .activity-user {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-right: 15px;
      cursor: pointer;
      
      img {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        margin-bottom: 5px;
      }
      
      .username {
        font-size: 12px;
        text-align: center;
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    
    .activity-content {
      flex: 1;
      
      .activity-action {
        color: #66c0f4;
        margin-right: 5px;
      }
      
      .activity-game {
        color: #ffffff;
      }
      
      .activity-detail {
        margin: 5px 0;
        color: #acb2b8;
      }
      
      .activity-time {
        font-size: 12px;
        color: #8ba6b6;
      }
    }
  }
}

.discussion-list {
  .discussion-item {
    padding: 10px;
    margin-bottom: 10px;
    background-color: rgba(49, 98, 130, 0.2);
    border-radius: 3px;
    cursor: pointer;
    
    &:hover {
      background-color: rgba(49, 98, 130, 0.4);
    }
    
    .discussion-title {
      color: #ffffff;
      margin-bottom: 5px;
    }
    
    .discussion-info {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #8ba6b6;
    }
  }
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  
  .game-card {
    cursor: pointer;
    transition: transform 0.2s;
    
    &:hover {
      transform: translateY(-3px);
    }
    
    img {
      width: 100%;
      border-radius: 3px;
    }
    
    .game-info {
      margin-top: 5px;
      
      .game-name {
        color: #ffffff;
        font-size: 14px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .game-price {
        color: #a4d007;
        font-size: 13px;
      }
    }
  }
}

.event-list {
  .event-item {
    padding: 10px;
    margin-bottom: 10px;
    background-color: rgba(49, 98, 130, 0.2);
    border-radius: 3px;
    
    .event-title {
      color: #ffffff;
      margin-bottom: 5px;
    }
    
    .event-time {
      font-size: 12px;
      color: #8ba6b6;
    }
  }
}

.profile-shortcuts {
  .shortcut-item {
    padding: 10px;
    margin-bottom: 10px;
    background-color: #316282;
    border-radius: 3px;
    text-align: center;
    cursor: pointer;
    transition: background-color 0.2s;
    
    &:hover {
      background-color: #427aa8;
    }
    
    span {
      color: #ffffff;
    }
  }
}
</style>
