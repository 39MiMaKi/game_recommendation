<template>
  <div class="game-list">
    <div class="game-list-header">
      <h2 class="game-list-title">{{ title }}</h2>
      <div class="game-list-filters" v-if="showFilters">
        <select v-model="sortBy" class="game-list-filter">
          <option value="popularity">按热门度</option>
          <option value="releaseDate">按发行日期</option>
          <option value="price">按价格</option>
          <option value="name">按名称</option>
        </select>
      </div>
    </div>
    
    <div class="game-list-content">
      <div v-if="loading" class="game-list-loading">
        <div class="loader"></div>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="games.length === 0" class="game-list-empty">
        没有找到符合条件的游戏
      </div>
      
      <div v-else class="game-list-grid">
        <RouterLink 
          v-for="game in games" 
          :key="game.appId" 
          :to="`/app/${game.appId}`"
          class="game-card"
        >
          <div class="game-card-image">
            <img v-lazy="game.header" alt="">
            <div v-if="game.discount > 0" class="game-card-discount">
              <span>{{ getDiscountStr(game.discount) }}</span>
            </div>
          </div>
          
          <div class="game-card-info">
            <div class="game-card-name">{{ game.name }}</div>
            
            <div class="game-card-platforms">
              <div v-if="game.win" class="game-card-platform" title="Windows">
                <img src="@/assets/icon_platform_win.png" alt="">
              </div>
              <div v-if="game.mac" class="game-card-platform" title="MacOS">
                <img src="@/assets/icon_platform_mac.png" alt="">
              </div>
              <div v-if="game.linux" class="game-card-platform" title="Linux">
                <img src="@/assets/icon_platform_linux.png" alt="">
              </div>
            </div>
            
            <div class="game-card-price-area">
              <div v-if="game.discount === 0" class="game-card-price">
                {{ getPriceStr(game.price) }}
              </div>
              <div v-else class="game-card-price-discounted">
                <span class="game-card-origin-price">{{ getPriceStr(game.price) }}</span>
                <span class="game-card-final-price">{{ getPriceStr(game.finalPrice) }}</span>
              </div>
            </div>
          </div>
        </RouterLink>
      </div>
      
      <div v-if="hasMore && !loading" class="game-list-load-more">
        <button @click="loadMore" class="load-more-button">加载更多</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getPriceStr, getDiscountStr } from '@/utils/format'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  games: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  hasMore: {
    type: Boolean,
    default: false
  },
  showFilters: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['load-more', 'sort-change'])

const sortBy = ref('popularity')

watch(sortBy, (newValue) => {
  emit('sort-change', newValue)
})

function loadMore() {
  emit('load-more')
}
</script>

<style scoped lang="scss">
.game-list {
  width: 940px;
  margin: 0 auto;
  padding: 20px 0;
}

.game-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.game-list-title {
  color: #ffffff;
  font-size: 26px;
  font-weight: normal;
  margin: 0;
}

.game-list-filters {
  display: flex;
  gap: 10px;
}

.game-list-filter {
  background-color: rgba(0, 0, 0, 0.4);
  border: 1px solid #4d4b49;
  border-radius: 2px;
  color: #ffffff;
  padding: 5px 10px;
  font-size: 12px;
  cursor: pointer;
  
  &:hover {
    border-color: #ffffff;
  }
}

.game-list-loading, .game-list-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #8f98a0;
  font-size: 18px;
}

.loader {
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  border-top: 4px solid #3498db;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.game-list-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.game-card {
  display: flex;
  flex-direction: column;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
  overflow: hidden;
  text-decoration: none;
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  }
}

.game-card-image {
  position: relative;
  width: 100%;
  height: 150px;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.game-card-discount {
  position: absolute;
  right: 0;
  top: 0;
  background-color: #4c6b22;
  color: #beee11;
  padding: 5px 8px;
  font-size: 14px;
  font-weight: bold;
}

.game-card-info {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.game-card-name {
  color: #ffffff;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.game-card-platforms {
  display: flex;
  gap: 5px;
  opacity: 0.7;
}

.game-card-platform {
  width: 20px;
  height: 20px;
  
  img {
    width: 100%;
    height: 100%;
  }
}

.game-card-price-area {
  margin-top: 5px;
}

.game-card-price {
  color: #ffffff;
  font-size: 13px;
}

.game-card-price-discounted {
  display: flex;
  align-items: center;
  gap: 5px;
}

.game-card-origin-price {
  position: relative;
  color: #738895;
  font-size: 11px;
  text-decoration: line-through;
}

.game-card-final-price {
  color: #beee11;
  font-size: 13px;
}

.game-list-load-more {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.load-more-button {
  background: linear-gradient(to right, #47bfff, #1a44c2);
  border: none;
  border-radius: 2px;
  color: white;
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
  
  &:hover {
    background: linear-gradient(to right, #61c8ff, #2a54d2);
  }
}
</style>