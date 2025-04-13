<template>
  <div class="recommendations-page">
    <GameList
      title="新鲜推荐"
      :games="games"
      :loading="loading"
      :has-more="hasMore"
      @load-more="loadMore"
      @sort-change="handleSortChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import GameList from '@/components/GameList.vue'
import { getRecommendationsApi } from '@/api/app'

const games = ref([])
const loading = ref(true)
const hasMore = ref(false)
const page = ref(1)
const pageSize = 12
const sortBy = ref('popularity')

onMounted(() => {
  loadGames()
})

function loadGames() {
  loading.value = true
  getRecommendationsApi({
    page: page.value,
    pageSize,
    sortBy: sortBy.value
  }).then(({ data }) => {
    if (page.value === 1) {
      // 随机选取5个游戏
      const shuffled = [...data].sort(() => 0.5 - Math.random())
      games.value = shuffled.slice(0, 5)
    } else {
      games.value = [...games.value, ...data]
    }
    hasMore.value = data.length === pageSize
    loading.value = false
  }).catch(() => {
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
.recommendations-page {
  min-height: calc(100vh - 104px);
  background-color: #1b2838;
}
</style>