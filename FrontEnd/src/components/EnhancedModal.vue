<template>
  <transition name="modal-fade" appear>
    <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
      <transition name="modal-bounce">
        <div class="modal-container" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">{{ title }}</h3>
            <button v-if="showClose" class="modal-close" @click="handleClose">
              <span>×</span>
            </button>
          </div>
          
          <div class="modal-body">
            <slot>
              <p v-if="content">{{ content }}</p>
            </slot>
          </div>
          
          <div class="modal-footer">
            <button 
              v-if="showCancel" 
              class="modal-btn modal-cancel" 
              @click="handleCancel"
            >
              {{ cancelText }}
            </button>
            <button 
              class="modal-btn modal-confirm" 
              @click="handleConfirm"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '提示'
  },
  content: {
    type: String,
    default: ''
  },
  showClose: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: true
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  closeOnClickOverlay: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'close'])

function handleConfirm() {
  emit('confirm')
  emit('update:visible', false)
}

function handleCancel() {
  emit('cancel')
  emit('update:visible', false)
}

function handleClose() {
  emit('close')
  emit('update:visible', false)
}

function handleOverlayClick() {
  if (props.closeOnClickOverlay) {
    handleClose()
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-container {
  background: radial-gradient(circle at top left, rgba(74, 81, 92, 0.4) 0%, rgba(75, 81, 92, 0) 60%), #25282e;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 500px;
  width: 90%;
  padding: 0;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-title {
  margin: 0;
  font-size: 18px;
  color: #ffffff;
  font-weight: 500;
}

.modal-close {
  background: transparent;
  border: none;
  font-size: 22px;
  color: #8f98a0;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  transition: color 0.2s;
}

.modal-close:hover {
  color: #ffffff;
}

.modal-body {
  padding: 20px;
  color: #acb2b8;
  font-size: 14px;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  gap: 12px;
}

.modal-btn {
  padding: 8px 16px;
  border-radius: 2px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.modal-cancel {
  background-color: rgba(255, 255, 255, 0.1);
  color: #acb2b8;
}

.modal-cancel:hover {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.modal-confirm {
  background: linear-gradient(to right, #47bfff 0%, #1a44c2 100%);
  color: white;
}

.modal-confirm:hover {
  background: linear-gradient(to right, #66ccff 0%, #3355d8 100%);
  transform: translateY(-1px);
}

/* 过渡动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-bounce-enter-active {
  animation: modal-bounce-in 0.3s ease-out;
}

.modal-bounce-leave-active {
  animation: modal-bounce-in 0.3s ease-in reverse;
}

@keyframes modal-bounce-in {
  0% {
    transform: scale(0.9);
    opacity: 0;
  }
  70% {
    transform: scale(1.03);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>