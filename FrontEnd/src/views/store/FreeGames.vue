<template>
  <div class="free-games-page">
    <div class="tag-filter">
      <span class="filter-label">标签筛选：</span>
      <select v-model="selectedTag" class="tag-select">
        <option value="">全部</option>
        <option value="动作">动作</option>
        <option value="冒险">冒险</option>
        <option value="休闲">休闲</option>
        <option value="策略">策略</option>
        <option value="模拟">模拟</option>
        <option value="角色扮演">角色扮演</option>
        <option value="独立">独立</option>
      </select>
    </div>
    <GameList
      title="免费游戏"
      :games="games"
      :loading="loading"
      :has-more="hasMore"
      @load-more="loadMore"
      @sort-change="handleSortChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import GameList from '@/components/GameList.vue'
import { getFreeGamesApi } from '@/api/app'

const games = ref([])
const loading = ref(true)
const hasMore = ref(false)
const page = ref(1)
const pageSize = 12
const sortBy = ref('popularity')
const selectedTag = ref('')

onMounted(() => {
  loadGames()
})

// 监听标签变化，重新加载游戏
watch(selectedTag, () => {
  page.value = 1
  loadGames()
})

function loadGames() {
  loading.value = true
  getFreeGamesApi({
    page: page.value,
    pageSize,
    sortBy: sortBy.value,
    tag: selectedTag.value
  }).then(({ data }) => {
    if (page.value === 1) {
      games.value = data
    } else {
      games.value = [...games.value, ...data]
    }
    hasMore.value = data.length === pageSize
    loading.value = false
  }).catch((error) => {
    console.error('加载免费游戏失败:', error)
    loading.value = false
  })
}

function loadMore() {
  page.value++
  loadGames()
}

function handleSortChange(newSortBy) {
  sortBy.value = newSortBy
  page.value = 1
  loadGames()
}
</script>

<style scoped>
.free-games-page {
  min-height: calc(100vh - 104px);
  background-color: #1b2838;
  padding-top: 20px;
}

.tag-filter {
  width: 940px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.filter-label {
  color: #c6d4df;
  font-size: 14px;
  margin-right: 10px;
}

.tag-select {
  background-color: rgba(0, 0, 0, 0.4);
  border: 1px solid #4d4b49;
  border-radius: 2px;
  color: #ffffff;
  padding: 5px 10px;
  font-size: 14px;
  cursor: pointer;
  min-width: 150px;
}

.tag-select:hover {
  border-color: #ffffff;
}
</style>