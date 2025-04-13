<template>
  <div class="personalized-recommendations-container">
    <div class="section-header">
      <h2>个性化推荐</h2>
      <div class="preference-settings" v-if="showPreferenceSettings">
        <button @click="openPreferenceModal" class="preference-button">
          <i class="fas fa-sliders-h"></i> 设置偏好
        </button>
      </div>
    </div>
    
    <div class="recommendations-grid" v-if="recommendations.length > 0">
      <div v-for="(item, index) in recommendations" :key="index" class="recommendation-item">
        <RouterLink :to="`/app/${item.appId}`" class="recommendation-link">
          <div class="recommendation-cover">
            <img v-lazy="item.cover" :alt="item.name">
            <WishlistButton :app-id="item.appId" :status="item.status" 
              @update:status="getRecommendations()"/>
            <div class="recommendation-on-wishlist" :class="{ show: item.status === 1 }">
              <i class="fas fa-check"></i> 已在愿望单中
            </div>
          </div>
          <div class="recommendation-info">
            <div class="recommendation-name">{{ item.name }}</div>
            <div class="recommendation-price-area">
              <div v-if="item.discount === 0" class="recommendation-price">{{ getPriceStr(item.price) }}</div>
              <div v-else class="recommendation-price_discounted">
                <span class="recommendation-discount">{{ getDiscountStr(item.discount) }}</span>
                <span class="recommendation-origin-price">{{ getPriceStr(item.price) }}</span>
                <span class="recommendation-final-price">{{ getPriceStr(item.finalPrice) }}</span>
              </div>
            </div>
            <div class="recommendation-platforms">
              <div v-if="item.win" class="recommendation-platform" title="Windows">
                <i class="fab fa-windows"></i>
              </div>
              <div v-if="item.mac" class="recommendation-platform" title="MacOS">
                <i class="fab fa-apple"></i>
              </div>
              <div v-if="item.linux" class="recommendation-platform" title="Linux">
                <i class="fab fa-linux"></i>
              </div>
            </div>
          </div>
        </RouterLink>
      </div>
    </div>
    
    <div v-else class="no-recommendations">
      <p>我们需要了解您的偏好以提供个性化推荐</p>
      <button @click="openPreferenceModal" class="preference-button-large">
        设置您的游戏偏好
      </button>
    </div>
    
    <!-- 偏好设置弹窗 -->
    <div v-if="showModal" class="preference-modal">
      <div class="preference-modal-content">
        <div class="preference-modal-header">
          <h3>设置您的游戏偏好</h3>
          <button @click="closePreferenceModal" class="close-button">&times;</button>
        </div>
        <div class="preference-modal-body">
          <p>请选择您感兴趣的游戏类型（最多选择5个）：</p>
          <div class="tag-selection">
            <div v-for="(tag, index) in availableTags" :key="index" 
                 class="tag-item" 
                 :class="{ selected: selectedTags.includes(tag) }"
                 @click="toggleTag(tag)">
              {{ tag }}
            </div>
          </div>
        </div>
        <div class="preference-modal-footer">
          <button @click="savePreferences" class="save-button">保存偏好</button>
          <button @click="closePreferenceModal" class="cancel-button">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPersonalizedRecommendationsApi, updateUserPreferencesApi } from '@/api/app'
import WishlistButton from '@/components/WishlistButton.vue'

const router = useRouter()
const recommendations = ref([])
const showPreferenceSettings = ref(true)
const showModal = ref(false)

// 可用的游戏标签
const availableTags = ref([
  '动作', '冒险', '休闲', '模拟', '策略', '角色扮演', '竞速', '体育', 
  '射击', '恐怖', '解谜', '独立', '多人', '开放世界', '剧情', '科幻', 
  '奇幻', '生存', '建造', '卡牌'
])
const selectedTags = ref([])

onMounted(() => {
  getRecommendations()
})

// 获取个性化推荐
function getRecommendations() {
  getPersonalizedRecommendationsApi().then(({ data }) => {
    recommendations.value = [...data]
    // 如果没有推荐结果，可能是冷启动问题
    if (data.length === 0) {
      showPreferenceSettings.value = true
    }
  }).catch(error => {
    console.error('获取个性化推荐失败', error)
  })
}

// 打开偏好设置弹窗
function openPreferenceModal() {
  showModal.value = true
}

// 关闭偏好设置弹窗
function closePreferenceModal() {
  showModal.value = false
}

// 切换标签选择状态
function toggleTag(tag) {
  const index = selectedTags.value.indexOf(tag)
  if (index === -1) {
    // 最多选择5个标签
    if (selectedTags.value.length < 5) {
      selectedTags.value.push(tag)
    }
  } else {
    selectedTags.value.splice(index, 1)
  }
}

// 保存用户偏好
function savePreferences() {
  if (selectedTags.value.length > 0) {
    const tags = selectedTags.value.join(',')
    updateUserPreferencesApi(tags).then(() => {
      closePreferenceModal()
      getRecommendations() // 更新推荐结果
    }).catch(error => {
      console.error('保存偏好失败', error)
    })
  } else {
    alert('请至少选择一个游戏类型')
  }
}

// 格式化价格
function getPriceStr(price) {
  if (price === 0) return '免费'
  return '¥' + price.toFixed(2)
}

// 格式化折扣
function getDiscountStr(discount) {
  return '-' + (discount * 100).toFixed(0) + '%'
}
</script>

<style scoped>
.personalized-recommendations-container {
  width: 100%;
  padding: 20px;
  background-color: #1b2838;
  border-radius: 4px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  color: #ffffff;
  font-size: 24px;
  margin: 0;
}

.preference-button {
  background-color: #1a9fff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preference-button:hover {
  background-color: #0d8ae6;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.recommendation-item {
  background-color: #16202d;
  border-radius: 4px;
  overflow: hidden;
  transition: transform 0.2s;
}

.recommendation-item:hover {
  transform: translateY(-5px);
}

.recommendation-link {
  text-decoration: none;
  color: inherit;
}

.recommendation-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 比例 */
  overflow: hidden;
}

.recommendation-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommendation-on-wishlist {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  display: none;
}

.recommendation-on-wishlist.show {
  display: block;
}

.recommendation-info {
  padding: 10px;
}

.recommendation-name {
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recommendation-price-area {
  margin-bottom: 8px;
}

.recommendation-price {
  color: #b8b6b4;
}

.recommendation-price_discounted {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommendation-discount {
  background-color: #4c6b22;
  color: #a4d007;
  padding: 2px 4px;
  border-radius: 2px;
  font-size: 12px;
}

.recommendation-origin-price {
  color: #7a7a7a;
  text-decoration: line-through;
  font-size: 12px;
}

.recommendation-final-price {
  color: #b8b6b4;
}

.recommendation-platforms {
  display: flex;
  gap: 8px;
}

.recommendation-platform {
  color: #7a7a7a;
  font-size: 14px;
}

.no-recommendations {
  text-align: center;
  padding: 40px 0;
  color: #b8b6b4;
}

.preference-button-large {
  background-color: #1a9fff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 20px;
}

.preference-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.preference-modal-content {
  background-color: #1b2838;
  border-radius: 4px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.preference-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #2a3f5f;
}

.preference-modal-header h3 {
  margin: 0;
  color: #ffffff;
}

.close-button {
  background: none;
  border: none;
  color: #b8b6b4;
  font-size: 24px;
  cursor: pointer;
}

.preference-modal-body {
  padding: 20px;
}

.tag-selection {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 15px;
}

.tag-item {
  background-color: #2a3f5f;
  color: #b8b6b4;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.tag-item:hover {
  background-color: #3a5275;
}

.tag-item.selected {
  background-color: #1a9fff;
  color: white;
}

.preference-modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #2a3f5f;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.save-button {
  background-color: #1a9fff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-button {
  background-color: #32435f;
  color: #b8b6b4;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
</style>