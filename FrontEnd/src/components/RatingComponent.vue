<template>
  <div class="rating-component">
    <div class="rating-header">
  <h3 class="rating-title">游戏评价</h3>
  <div class="rating-summary" v-if="ratingStats">
    <span class="positive-percentage">{{ positivePercentage }}%</span>
    <span class="total-ratings">({{ ratingStats.total }} 条评价)</span>
  </div>
</div>
    
    <!-- 用户已评价显示 -->
    <div v-if="userRating && !isEditing" class="user-rating">
      <div class="rating-header">
        <div class="recommendation-display">
          <div class="recommendation-icon" :class="{ 'positive': userRating.recommended, 'negative': !userRating.recommended }">
            <i class="icon" :class="userRating.recommended ? 'thumbs-up' : 'thumbs-down'"></i>
          </div>
          <span class="recommendation-text">{{ userRating.recommended ? '推荐' : '不推荐' }}</span>
        </div>
        <div class="rating-actions">
          <button class="edit-btn" @click="startEdit">编辑</button>
          <button class="delete-btn" @click="confirmDelete">删除</button>
        </div>
      </div>
      <div class="rating-comment" v-if="userRating.comment">
        <p>{{ userRating.comment }}</p>
      </div>
      <div class="rating-date">
        <small>评价于 {{ formatDate(userRating.createTime) }}</small>
      </div>
    </div>
    
    <!-- 评价表单 -->
    <div v-else class="rating-form">
      <div class="recommendation-input">
        <div class="recommendation-options">
          <button 
            class="recommendation-btn positive" 
            :class="{ 'selected': isRecommended === true }" 
            @click="isRecommended = true"
          >
            <i class="icon thumbs-up"></i>
            推荐
          </button>
          <button 
            class="recommendation-btn negative" 
            :class="{ 'selected': isRecommended === false }" 
            @click="isRecommended = false"
          >
            <i class="icon thumbs-down"></i>
            不推荐
          </button>
        </div>
      </div>
      
      <div class="comment-input">
        <textarea 
          v-model="comment" 
          placeholder="分享您对这款游戏的看法（可选）" 
          rows="4"
        ></textarea>
      </div>
      
      <div class="form-actions">
        <button 
          class="submit-btn" 
          :disabled="isRecommended === null || isSubmitting" 
          @click="submitRating"
        >
          {{ isEditing ? '更新评价' : '提交评价' }}
        </button>
        <button 
          v-if="isEditing" 
          class="cancel-btn" 
          @click="cancelEdit"
        >
          取消
        </button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { submitRatingApi, getUserRatingApi, deleteRatingApi } from '@/api/rating'

const props = defineProps({
  appId: {
    type: [Number, String],
    required: true
  },
  ratingStats: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['rating-updated', 'rating-deleted'])

const store = useStore()

// 计算好评率
const positivePercentage = computed(() => {
  if (!ratingStats.value || ratingStats.value.total === 0) return 0
  return Math.round((ratingStats.value.positive / ratingStats.value.total) * 100)
})
const token = computed(() => store.getters['user/token'])

// 状态变量
const userRating = ref(null)
const isLoading = ref(false)
const isSubmitting = ref(false)
const isEditing = ref(false)

// 表单数据
const isRecommended = ref(null)
const comment = ref('')

// 获取用户对当前游戏的评价
const getUserRating = async () => {
  if (!token.value) return
  
  try {
    isLoading.value = true
    const { data } = await getUserRatingApi(props.appId)
    if (data) {
      userRating.value = data
    }
  } catch (error) {
    console.error('获取用户评价失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 提交评价
const submitRating = async () => {
  if (isRecommended.value === null) return
  
  try {
    isSubmitting.value = true
    await submitRatingApi(props.appId, isRecommended.value, comment.value)
    
    // 更新本地数据
    userRating.value = {
      appId: props.appId,
      recommended: isRecommended.value,
      comment: comment.value,
      createTime: new Date().toISOString()
    }
    
    isEditing.value = false
    emit('rating-updated', userRating.value)
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    isSubmitting.value = false
  }
}

// 开始编辑评价
const startEdit = () => {
  isEditing.value = true
  isRecommended.value = userRating.value.recommended
  comment.value = userRating.value.comment || ''
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
}

// 确认删除评价
const confirmDelete = async () => {
  if (!confirm('确定要删除您的评价吗？')) return
  
  try {
    isLoading.value = true
    await deleteRatingApi(userRating.value.id)
    userRating.value = null
    isRecommended.value = null
    comment.value = ''
    emit('rating-deleted')
  } catch (error) {
    console.error('删除评价失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  getUserRating()
})
</script>

<style scoped lang="scss">
.rating-component {
  position: relative;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 15px;
  margin: 20px 0;
  color: #acb2b8;
}

.rating-title {
  color: #ffffff;
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
}

.user-rating {
  background-color: rgba(62, 126, 167, 0.1);
  border-radius: 3px;
  padding: 12px;
}

.rating-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.recommendation-display {
  display: flex;
  align-items: center;
}

.recommendation-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 3px;
  margin-right: 10px;
  
  &.positive {
    background-color: rgba(164, 208, 7, 0.2);
    
    .icon {
      color: #a4d007;
    }
  }
  
  &.negative {
    background-color: rgba(171, 63, 63, 0.2);
    
    .icon {
      color: #ff9999;
    }
  }
}

.icon {
  font-size: 24px;
  
  &.thumbs-up:before {
    content: '👍';
  }
  
  &.thumbs-down:before {
    content: '👎';
  }
}

.recommendation-text {
  font-size: 16px;
  font-weight: bold;
  color: #ffffff;
}

.rating-actions {
  display: flex;
  gap: 10px;
}

.edit-btn, .delete-btn, .submit-btn, .cancel-btn {
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
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.delete-btn {
  background-color: rgba(171, 63, 63, 0.2);
  color: #e47c7c;
  
  &:hover {
    background-color: rgba(171, 63, 63, 0.4);
  }
}

.rating-form {
  .recommendation-input {
    margin-bottom: 15px;
    
    .recommendation-options {
      display: flex;
      gap: 10px;
      
      .recommendation-btn {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        padding: 10px;
        border: none;
        border-radius: 3px;
        font-size: 16px;
        cursor: pointer;
        transition: all 0.2s;
        
        &.positive {
          background-color: rgba(164, 208, 7, 0.1);
          color: #a4d007;
          
          &:hover, &.selected {
            background-color: rgba(164, 208, 7, 0.3);
          }
          
          &.selected {
            box-shadow: 0 0 0 2px #a4d007;
          }
        }
        
        &.negative {
          background-color: rgba(171, 63, 63, 0.1);
          color: #ff9999;
          
          &:hover, &.selected {
            background-color: rgba(171, 63, 63, 0.3);
          }
          
          &.selected {
            box-shadow: 0 0 0 2px #ff9999;
          }
        }
      }
    }
  }
}

.rating-comment {
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.5;
  color: #c6d4df;
}

.rating-date {
  font-size: 12px;
  color: #8f98a0;
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
  
  &:focus {
    outline: none;
    border-color: #66c0f4;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  z-index: 10;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(103, 193, 245, 0.2);
  border-top-color: #66c0f4;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>