<template>
  <div class="image-container" :class="{ 'is-loading': loading, 'has-error': error }">
    <transition name="fade" mode="out-in">
      <div v-if="loading" class="image-loading">
        <div class="loading-spinner"></div>
      </div>
      <div v-else-if="error" class="image-error">
        <div class="error-icon">!</div>
        <span class="error-text">{{ errorText }}</span>
      </div>
      <img
        v-else
        :src="src"
        :alt="alt"
        class="image"
        :class="imageClass"
        @error="handleError"
        @load="handleLoad"
      />
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  fallbackSrc: {
    type: String,
    default: '/src/assets/blank.png'
  },
  errorText: {
    type: String,
    default: '图片加载失败'
  },
  imageClass: {
    type: String,
    default: ''
  },
  lazyLoad: {
    type: Boolean,
    default: true
  }
})

const loading = ref(true)
const error = ref(false)
const currentSrc = ref('')
const observer = ref(null)

function handleError() {
  // 如果有备用图片且当前不是备用图片，则尝试加载备用图片
  if (props.fallbackSrc && currentSrc.value !== props.fallbackSrc) {
    currentSrc.value = props.fallbackSrc
    return
  }
  
  // 否则显示错误状态
  loading.value = false
  error.value = true
}

function handleLoad() {
  loading.value = false
}

onMounted(() => {
  if (props.lazyLoad) {
    // 使用 IntersectionObserver 实现懒加载
    observer.value = new IntersectionObserver((entries) => {
      const entry = entries[0]
      if (entry.isIntersecting) {
        // 当元素进入视口时加载图片
        currentSrc.value = props.src
        observer.value.unobserve(entry.target)
      }
    })
    
    // 开始观察容器元素
    const container = document.querySelector('.image-container')
    if (container) {
      observer.value.observe(container)
    }
  } else {
    // 不使用懒加载，直接设置图片源
    currentSrc.value = props.src
  }
})
</script>

<style scoped>
.image-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-container:hover .image {
  transform: scale(1.05);
}

.image-loading,
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #8f98a0;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-top-color: #67c1f5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.error-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  font-weight: bold;
}

.error-text {
  font-size: 12px;
  text-align: center;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>