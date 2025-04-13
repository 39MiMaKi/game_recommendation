<template>
  <div class="app">
    <div class="app-header">
      <div class="app-header-avatar">
        <img :src="userInfo.avatar || 'https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fdefault_avatar.jpg'" alt="用户头像">
      </div>
      {{ userInfo.nickname || userInfo.username }}
    </div>

    <div class="app-content">
      <div class="nav">
        <div class="nav-title">个人资料</div>
        <div class="nav-item" :class="{ current: true }">
          <img class="nav-item-icon" src="@/assets/iconsheet_profile.png" alt="">
          基本信息
        </div>
      </div>

      <div class="router-view-area">
        <div class="section">
          <div class="section-header">
            <h2>个人信息</h2>
            <button v-if="isOwnProfile && !isEditing" class="edit-btn" @click="startEditing">编辑资料</button>
          </div>
          
          <!-- 编辑模式 -->
          <div v-if="isOwnProfile && isEditing" class="edit-form">
            <!-- 头像上传 -->
            <div class="avatar-upload">
              <div class="avatar-preview">
                <img :src="avatarPreview || userInfo.avatar || 'https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fdefault_avatar.jpg'" alt="头像预览">
              </div>
              <label class="avatar-upload-btn">
                更换头像
                <input type="file" accept="image/*" @change="handleAvatarChange" style="display: none;">
              </label>
            </div>
            
            <!-- 昵称编辑 -->
            <div class="form-group">
              <label>昵称</label>
              <input v-model="editForm.nickname" class="form-input" placeholder="请输入昵称">
            </div>

            <!-- 邮箱显示（不可编辑） -->
            <div class="form-group">
              <label>邮箱</label>
              <div class="form-static">{{ userInfo.email }}</div>
            </div>
            
            <!-- 偏好标签编辑 -->
            <div class="form-group">
              <label>偏好标签</label>
              <div class="tag-options">
                <div 
                  v-for="tag in availableTags" 
                  :key="tag"
                  class="tag-card"
                  :class="{ selected: editForm.tags.includes(tag) }"
                  @click="toggleTag(tag)"
                >
                  {{ tag }}
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="form-actions">
              <button class="cancel-btn" @click="cancelEditing">取消</button>
              <button class="save-btn" @click="saveProfile">保存</button>
            </div>
          </div>
          
          <!-- 显示模式 -->
          <div v-else class="profile-info">
            <div class="info-item">
              <label>用户名</label>
              <div>{{ userInfo.username }}</div>
            </div>

            <div class="info-item">
              <label>昵称</label>
              <div>{{ userInfo.nickname || '未设置' }}</div>
            </div>

            <div class="info-item">
              <label>邮箱</label>
              <div>{{ userInfo.email }}</div>
            </div>

            <div class="info-item">
              <label>偏好标签</label>
              <div class="tag-list">
                <div v-for="tag in userInfo.tags" :key="tag" class="profile-tag">{{ tag }}</div>
                <div v-if="!userInfo.tags?.length" class="no-tags">暂无标签</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 游戏统计信息 -->
        <div class="section">
          <h2>游戏统计</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ gameStats.ratedGamesCount }}</div>
              <div class="stat-label">已评分游戏</div>
            </div>
          </div>
        </div>

        <!-- 游戏评分记录 -->
        <div class="section">
          <h2>评分记录</h2>
          <div v-if="gameReviews.length > 0" class="reviews-list">
            <div v-for="review in gameReviews" :key="review.appId" class="review-item">
              <div class="review-header">
                <div class="review-game">{{ review.appName }}</div>
                <div class="review-rating">
                  <span>{{ review.recommended ? '推荐' : '不推荐' }}</span>
                </div>
              </div>
              <div class="review-date">{{ formatDate(review.ratingTime) }}</div>
            </div>
            <!-- 分页控件 -->
            <div class="pagination">
              <button 
                :disabled="reviewPage === 1" 
                @click="loadGameReviews(reviewPage - 1)"
              >上一页</button>
              <span>{{ reviewPage }} / {{ Math.ceil(totalReviews / reviewPageSize) }}</span>
              <button 
                :disabled="reviewPage >= Math.ceil(totalReviews / reviewPageSize)" 
                @click="loadGameReviews(reviewPage + 1)"
              >下一页</button>
            </div>
          </div>
          <div v-else class="empty-list">暂无评分记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { getUserInfoApi, updateUserInfoApi, updateAvatarApi } from '@/api/user'
import { getUserRatingsApi } from '@/api/rating'
import { getUserGameStatsApi } from '@/api/review'

const route = useRoute()
const store = useStore()

// 当前登录用户信息
const currentUserId = computed(() => store.getters['user/userId'])

// 个人资料页面显示的用户ID
const profileUserId = computed(() => route.params.userId || currentUserId.value)

// 判断是否是查看自己的资料
const isOwnProfile = computed(() => currentUserId.value === profileUserId.value)

// 用户信息
const userInfo = ref({
  userId: '',
  username: '',
  nickname: '',
  avatar: '',
  email: '',
  tags: []
})

// 用户游戏评分历史
const gameReviews = ref([])
const reviewPage = ref(1)
const reviewPageSize = ref(10)
const totalReviews = ref(0)
const loading = ref(false)

// 用户游戏统计信息
const gameStats = ref({
  ratedGamesCount: 0
})

// 编辑模式
const isEditing = ref(false)

// 编辑表单
const editForm = ref({
  nickname: '',
  tags: []
})

// 可选标签列表
const availableTags = [
  '动作', '冒险', '角色扮演', '射击', '策略', 
  '体育', '模拟', '解谜', '竞速', '格斗'
]

// 头像文件
const avatarFile = ref(null)
const avatarPreview = ref('')

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfoApi(profileUserId.value)
    if (res.data) {
      userInfo.value = res.data
      // 将tags字符串转换为数组
      userInfo.value.tags = res.data.tags ? res.data.tags.split(',') : []
    }
    // 加载用户游戏统计信息
    const statsRes = await getUserGameStatsApi(profileUserId.value)
    if (statsRes.data) {
      gameStats.value = statsRes.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 加载游戏评分记录
const loadGameReviews = async (page = 1) => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getUserRatingsApi(profileUserId.value, page - 1, reviewPageSize.value)
    if (res.data) {
      gameReviews.value = res.data.content
      totalReviews.value = res.data.total
      reviewPage.value = page
    }
  } catch (error) {
    console.error('获取游戏评分记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 开始编辑个人资料
const startEditing = () => {
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    tags: [...(userInfo.value.tags || [])]
  }
  avatarPreview.value = ''
  isEditing.value = true
}

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false
  avatarFile.value = null
  avatarPreview.value = ''
}

// 处理头像选择
const handleAvatarChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    avatarFile.value = file
    avatarPreview.value = URL.createObjectURL(file)
  }
}

// 切换标签选择
const toggleTag = (tag) => {
  const index = editForm.value.tags.indexOf(tag)
  if (index === -1) {
    editForm.value.tags.push(tag)
  } else {
    editForm.value.tags.splice(index, 1)
  }
}

// 保存个人资料
const saveProfile = async () => {
  try {
    // 更新用户信息
    await updateUserInfoApi({
      nickname: editForm.value.nickname,
      tags: editForm.value.tags
    })
    
    // 更新头像
    if (avatarFile.value) {
      const formData = new FormData()
      formData.append('avatar', avatarFile.value)
      await updateAvatarApi(formData)
    }
    
    // 重新加载用户信息
    await loadUserInfo()
    
    // 更新Vuex中的用户信息
    store.commit('user/setUserInfo', {
      ...store.getters['user/userInfo'],
      nickname: editForm.value.nickname,
      avatar: avatarPreview.value || userInfo.value.avatar
    })
    
    // 退出编辑模式
    isEditing.value = false
    avatarFile.value = null
    avatarPreview.value = ''
  } catch (error) {
    console.error('保存个人资料失败:', error)
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(() => {
  loadUserInfo()
  loadGameReviews()
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
    border-radius: 4px;
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

.router-view-area {
  width: 100%;
  padding-top: 24px;
}

.section {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;

  h2 {
    color: #ffffff;
    margin: 0 0 20px;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.edit-btn, .save-btn, .cancel-btn {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  border: none;
}

.edit-btn, .save-btn {
  background: #67c1f5;
  color: #ffffff;

  &:hover {
    background: #4fa4d8;
  }
}

.cancel-btn {
  background: #2a475e;
  color: #ffffff;
  margin-right: 10px;

  &:hover {
    background: #1b2838;
  }
}

.form-group {
  margin-bottom: 20px;

  label {
    display: block;
    color: #8f98a0;
    margin-bottom: 8px;
  }
}

.form-input {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #2a475e;
  background: #2a475e;
  color: #ffffff;

  &:focus {
    outline: none;
    border-color: #67c1f5;
  }
}

.form-static {
  color: #8f98a0;
}

.avatar-upload {
  margin-bottom: 20px;

  .avatar-preview {
    width: 100px;
    height: 100px;
    margin-bottom: 10px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 4px;
    }
  }
}

.avatar-upload-btn {
  display: inline-block;
  padding: 8px 16px;
  background: #67c1f5;
  color: #ffffff;
  border-radius: 4px;
  cursor: pointer;

  &:hover {
    background: #4fa4d8;
  }
}

.tag-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-card {
  padding: 6px 12px;
  border-radius: 4px;
  background: #2a475e;
  color: #ffffff;
  cursor: pointer;

  &.selected {
    background: #67c1f5;
  }

  &:hover {
    background: #4fa4d8;
  }
}

.profile-info {
  .info-item {
    margin-bottom: 16px;

    label {
      color: #8f98a0;
      margin-bottom: 4px;
      display: block;
    }

    div {
      color: #ffffff;
    }
  }
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .profile-tag {
    padding: 4px 8px;
    background: #2a475e;
    border-radius: 4px;
    color: #ffffff;
  }

  .no-tags {
    color: #8f98a0;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;

  .stat-value {
    font-size: 24px;
    color: #67c1f5;
    margin-bottom: 8px;
  }

  .stat-label {
    color: #8f98a0;
  }
}

.reviews-list {
  .review-item {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 4px;
    padding: 16px;
    margin-bottom: 16px;

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .review-game {
        color: #67c1f5;
        font-weight: bold;
      }

      .review-rating {
        span {
          color: #67c1f5;
        }
      }
    }

    .review-date {
      color: #8f98a0;
      font-size: 12px;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;

  button {
    padding: 8px 16px;
    background: #2a475e;
    border: none;
    border-radius: 4px;
    color: #ffffff;
    cursor: pointer;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    &:not(:disabled):hover {
      background: #4fa4d8;
    }
  }

  span {
    color: #8f98a0;
  }
}

.empty-list {
  text-align: center;
  color: #8f98a0;
  padding: 20px;
}
</style>

