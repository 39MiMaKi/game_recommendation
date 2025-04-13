<template>
  <div class="rating-history">
    <h3 class="section-title">我的评分记录</h3>
    
    <div v-if="isLoading" class="loading">
      <div class="spinner"></div>
      <div>加载中...</div>
    </div>
    
    <div v-else-if="ratings.length === 0" class="empty-state">
      <p>您还没有对任何游戏进行评分</p>
      <button class="browse-games-btn" @click="router.push('/')">浏览游戏</button>
    </div>
    
    <div v-else class="ratings-list">
      <div v-for="rating in ratings" :key="rating.id" class="rating-item">
        <div class="rating-game" @click="router.push(`/app/${rating.appId}`)">
          <img :src="rating.appCover || 'https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fblank.png'" alt="游戏封面" class="game-cover">
          <div class="game-info">
            <div class="game-name">{{ rating.appName }}</div>
            <div class="rating-date">评分于 {{ formatDate(rating.createTime) }}</div>
          </div>
        </div>
        
        <div class="rating-content">
          <div class="rating-score">
            <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= rating.score }">
              ★
            </span>
            <span class="score-text">{{ rating.score }}/5</span>
          </div>
          
          <div v-if="rating.comment" class="rating-comment">
            <p>{{ rating.comment }}</p>
          </div>
          
          <div class="rating-actions">
            <button class="edit-btn" @click="editRating(rating)">编辑</button>
            <button class="delete-btn" @click="deleteRating(rating.id)">删除</button>
          </div>
        </div>
      </div>
      
      <div v-if="hasMoreRatings" class="load-more">
        <button 
          class="load-more-btn" 
          :disabled="isLoadingMore" 
          @click="loadMoreRatings"
        >
          {{ isLoadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
    
    <!-- 编辑评分弹窗 -->
    <div v-if="showEditModal" class="edit-modal-overlay" @click="closeEditModal">
      <div class="edit-modal" @click.stop>
        <h3>编辑评分</h3>
        
        <div class="stars-input">
          <span v-for="i in 5" :key="i" class="star" 
                :class="{ filled: i <= editForm.score, hovered: i <= hoverScore }" 
                @mouseenter="hoverScore = i"
                @mouseleave="hoverScore = 0"
                @click="editForm.score = i">
            ★
          </span>
          <span class="score-text">{{ editForm.score }}/5</span>
        </div>
        
        <div class="comment-input">
          <textarea 
            v-model="editForm.comment" 
            placeholder="分享您对这款游戏的看法（可选）" 
            rows="4"
          ></textarea>
        </div>
        
        <div class="modal-actions">
          <button 
            class="submit-btn" 
            :disabled="!editForm.score || isSubmitting" 
            @click="submitEdit"
          >
            保存修改
          </button>
          <button class="cancel-btn" @click="closeEditModal">
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { getUserRatingsApi, submitRatingApi, deleteRatingApi } from '@/api/rating'

const props = defineProps({
  userId: {
    type: [Number, String],
    default: null
  }
})

const router = useRouter()
const store = useStore()

// 状态变量
const ratings = ref([])
const isLoading = ref(false)
const isLoadingMore = ref(false)
const isSubmitting = ref(false)
const currentPage = ref(1)
const hasMoreRatings = ref(false)
const pageSize = 5

// 编辑相关状态
const showEditModal = ref(false)
const editForm = ref({ id: null, appId: null, score: 0, comment: '' })
const hoverScore = ref(0)

// 获取用户评分历史
const fetchRatings = async (page = 1) => {
  const userId = props.userId || store.getters['user/userId']
  if (!userId) return
  
  try {
    isLoading.value = page === 1 ? true : false
    isLoadingMore.value = page > 1 ? true : false
    
    const { data } = await getUserRatingsApi(userId, page, pageSize)
    
    if (page === 1) {
      ratings.value = data.records || []
    } else {
      ratings.value = [...ratings.value, ...(data.records || [])]
    }
    
    currentPage.value = page
    hasMoreRatings.value = data.hasNext || false
  } catch (error) {
    console.error('获取评分历史失败:', error)
  } finally {
    isLoading.value = false
    isLoadingMore.value = false
  }
}

// 加载更多评分
const loadMoreRatings = () => {
  if (isLoadingMore.value || !hasMoreRatings.value) return
  fetchRatings(currentPage.value + 1)
}

// 编辑评分
const editRating = (rating) => {
  editForm.value = {
    id: rating.id,
    appId: rating.appId,
    score: rating.score,
    comment: rating.comment || ''
  }
  showEditModal.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!editForm.value.score) return
  
  try {
    isSubmitting.value = true
    await submitRatingApi(
      editForm.value.appId, 
      editForm.value.score, 
      editForm.value.comment
    )
    
    // 更新本地数据
    const index = ratings.value.findIndex(r => r.id === editForm.value.id)
    if (index !== -1) {
      ratings.value[index].score = editForm.value.score
      ratings.value[index].comment = editForm.value.comment
    }
    
    closeEditModal()
  } catch (error) {
    console.error('更新评分失败:', error)
  } finally {
    isSubmitting.value = false
  }
}

// 关闭编辑弹窗
const closeEditModal = () => {
  showEditModal.value = false
  editForm.value = { id: null, appId: null, score: 0, comment: '' }
  hoverScore.value = 0
}

// 删除评分
const deleteRating = async (ratingId) => {
  if (!confirm('确定要删除这条评分记录吗？')) return
  
  try {
    await deleteRatingApi(ratingId)
    // 从列表中移除
    ratings.value = ratings.value.filter(r => r.id !== ratingId)
  } catch (error) {
    console.error('删除评分失败:', error)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

onMounted(() => {
  fetchRatings()
})
</script>

<style scoped lang="scss">
.rating-history {
  width: 100%;
  color: #acb2b8;
}

.section-title {
  color: #ffffff;
  font-size: 18px;
  margin-bottom: 15px;
  padding-bottom: 5px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  color: #8f98a0;
  
  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    border-top-color: #66c0f4;
    animation: spin 1s ease-in-out infinite;
    margin-bottom: 10px;
  }
}

.empty-state {
  text-align: center;
  padding: 30px 0;
  color: #8f98a0;
  
  p {
    margin-bottom: 15px;
  }
  
  .browse-games-btn {
    background-color: #1a9fff;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 2px;
    cursor: pointer;
    transition: background-color 0.2s;
    
    &:hover {
      background-color: #0b78c9;
    }
  }
}

.ratings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rating-item {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  overflow: hidden;
}

.rating-game {
  display: flex;
  padding: 12px;
  background-color: rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: rgba(103, 193, 245, 0.1);
  }
  
  .game-cover {
    width: 120px;
    height: 45px;
    object-fit: cover;
    margin-right: 12px;
    border-radius: 2px;
  }
  
  .game-info {
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  
  .game-name {
    color: #ffffff;
    font-size: 16px;
    margin-bottom: 4px;
  }
  
  .rating-date {
    color: #8f98a0;
    font-size: 12px;
  }
}

.rating-content {
  padding: 12px;
}

.rating-score {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  
  .star {
    font-size: 20px;
    color: #626366;
    
    &.filled {
      color: #66c0f4;
    }
  }
  
  .score-text {
    margin-left: 10px;
    font-size: 14px;
    color: #8f98a0;
  }
}

.rating-comment {
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #c6d4df;
}

.rating-actions {
  display: flex;
  gap: 10px;
}

.edit-btn, .delete-btn {
  background-color: rgba(103, 193, 245, 0.2);
  border: none;
  border-radius: 2px;
  color: #66c0f4;
  padding: 5px 10px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: rgba(103, 193, 245, 0.4);
  }
}

.delete-btn {
  background-color: rgba(171, 63, 63, 0.2);
  color: #e47c7c;
  
  &:hover {
    background-color: rgba(171, 63, 63, 0.4);
  }
}

.load-more {
  text-align: center;
  margin-top: 15px;
  
  .load-more-btn {
    background-color: rgba(103, 193, 245, 0.2);
    border: none;
    border-radius: 2px;
    color: #66c0f4;
    padding: 8px 16px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.2s;
    
    &:hover:not(:disabled) {
      background-color: rgba(103, 193, 245, 0.4);
    }
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.edit-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.edit-modal {
  background-color: #1b2838;
  border-radius: 4px;
  padding: 20px;
  width: 90%;
  max-width: 500px;
  
  h3 {
    color: #ffffff;
    margin-top: 0;
    margin-bottom: 15px;
  }
  
  .stars-input {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .star {
      font-size: 24px;
      color: #626366;
      cursor: pointer;
      transition: color 0.2s;
      
      &.filled {
        color: #66c0f4;
      }
      
      &.hovered {
        color: #1a9fff;
      }
    }
    
    .score-text {
      margin-left: 10px;
      font-size: 16px;
      color: #8f98a0;
    }
  }
  
  .comment-input textarea {
    width: 100%;
    background-color: #222b35;
    border: 1px solid #4b5c6e;
    border-radius: 3px;
    color: #c6d4df;
    padding: 10px;
    font-size: 14px;
    resize: vertical;
    margin-bottom: 15px;
    
    &:focus {
      outline: none;
      border-color: #66c0f4;
    }
  }
  
  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    
    .submit-btn, .cancel-btn {
      padding: 8px 16px;
      border-radius: 2px;
      font-size: 14px;
      cursor: pointer;
      transition: background-color 0.2s;
    }
    
    .submit-btn {
      background-color: #1a9fff;
      color: white;
      border: none;
      
      &:hover:not(:disabled) {
        background-color: #0b78c9;
      }
      
      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
    
    .cancel-btn {
      background-color: rgba(103, 193, 245, 0.2);
      border: none;
      color: #66c0f4;
      
      &:hover {
        background-color: rgba(103, 193, 245, 0.4);
      }
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>