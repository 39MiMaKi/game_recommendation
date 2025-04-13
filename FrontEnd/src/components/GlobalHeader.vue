<template>
  <div class="global-header">
    <div class="content">
      <RouterLink
        class="logo"
        :class="{ current: current === 0 }"
        to="/"
        @click="storeMenuLocked = true"
        @mouseenter="storeMenuLocked = false"
      >
        GameRec
      </RouterLink>

      <!-- 操作部分 -->
      <div class="actions">
        <div class="account-pulldown" v-if="token" @mouseenter="actionMenuLocked = false">
          {{ nickname }}
          <img src="@/assets/btn_arrow_down_padded.png" alt="" />
          <div v-show="!actionMenuLocked" class="account-pulldown-menu">
            <RouterLink
              class="account-pulldown-menu-item"
              :to="`/profile/${userId}`"
              @click="handleProfileClick"
            >
              查看个人资料
            </RouterLink>
            <div
              class="account-pulldown-menu-item"
              @click="handleLogout"
            >
              登出：<span>{{ username }}</span>
            </div>
          </div>
        </div>

        <!-- 显示用户头像或者登录链接 -->
        <RouterLink v-if="token" class="user-avatar" :to="`/profile/${userId}`">
          <img
            v-lazy="
              avatar ||
              'https://steam-1314488277.cos.ap-guangzhou.myqcloud.com/assets%2Fdefault_avatar.jpg'
            "
            alt="User Avatar"
          />
        </RouterLink>
        <RouterLink v-else class="login" to="/login">登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'

const storeMenuLocked = ref(false)
const actionMenuLocked = ref(false)

const store = useStore()
const token = computed(() => store.getters['user/token'])
const userId = computed(() => store.getters['user/userId'])
const username = computed(() => store.getters['user/username'])
const nickname = computed(() => store.getters['user/nickname'])
const avatar = computed(() => store.getters['user/avatar'])

const route = useRoute()
const current = computed(() => {
  if (['store', 'wishlist'].includes(route.name)) return 0
  else if (['community', 'profile'].includes(route.name))
    return ['profile'].includes(route.name) &&
      token &&
      store.getters['user/userId'].toString() === route.params.userId
      ? 2
      : 1
  if (['friends', 'friendList', 'friendAdd', 'friendPending'].includes(route.name)) return 2
  if (['about'].includes(route.name)) return 3
  if (['chat'].includes(route.name)) return 4
  return -1
})

function handleProfileClick() {
  actionMenuLocked.value = true
}

function handleLogout() {
  logout()
  actionMenuLocked.value = true
}

function logout() {
  store.dispatch('user/logout')
}
</script>

<style scoped lang="scss">
.global-header {
  position: relative;
  z-index: 1000;
  display: flex;
  justify-content: center;
  width: 100%;
  min-width: 940px;
  background-color: $global-header-bg-color;
  font-family: 'Motiva Sans', sans-serif;
}

.content {
  display: flex;
  align-items: center;
  width: 940px;
  height: 104px;
}

.logo {
  width: 200px;
  height: 44px;
  margin-right: 24px;
  color: #ffffff;
  font-size: 180%;
  font-weight: 500;
  line-height: 44px;
  text-decoration: none;

  &:hover {
    color: #ffffff;
  }
}

.actions {
  display: flex;
  align-items: center;
  height: 32px;
  margin-left: auto;
  right: 0;
  top: 6px;
}

.account-pulldown {
  position: relative;
  display: flex;
  align-items: center;
  height: 24px;
  color: #b8b6b4;
  font-size: 15px;
  cursor: pointer;

  &:hover {
    color: #ffffff;

    & > .account-pulldown-menu {
      opacity: 1;
      pointer-events: auto;
    }
  }
}

.account-pulldown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  border: 1px solid #3d4450;
  box-shadow: 0 0 12px #000000;
  background-color: #3d4450;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.account-pulldown-menu-item {
  display: inline-block;
  width: 148px;
  padding: 5px 12px;
  color: #b8b6b4;
  font-size: 12px;
  font-family: Motiva Sans, Arial, Helvetica, Verdana, sans-serif;
  text-decoration: none;

  &:hover {
    color: #171d25;
    background-color: #dcdedf;
  }

  span {
    color: #57cbde;
  }
}

.user-avatar {
  display: inline-block;
  width: 32px;
  height: 32px;
  border: 2px solid #474747;

  img {
    width: 32px;
    height: 32px;
    background: linear-gradient(to bottom, #41778f 5%, #3d697b 95%);
  }
}

.login {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 15px;
  color: #ffffff;
  font-size: 14px;
  background: linear-gradient(90deg, #06bfff 0%, #2d73ff 100%);
  border-radius: 2px;
  text-decoration: none;
  transition: all 0.2s;

  &:hover {
    filter: brightness(1.1);
  }
}
</style>
