<template>
  <div class="app">
    <div class="login">
      <div class="login-title">登录</div>
      <transition name="fade" appear>
      <div class="login-content">
        <div class="login-content-left">
          <!--账户名称-->
          <div class="username">
            <div class="username-label">用账户名称登录</div>
            <input v-model="username" class="username-input" name="username" @input="debouncedClearErrMsg()" @keyup.enter="login()" placeholder="请输入账户名称">
          </div>
          <!--密码-->
          <div class="password">
            <div class="password-label">密码</div>
            <input v-model="password" class="password-input" name="password" type="password" @input="debouncedClearErrMsg()" @keyup.enter="login()" placeholder="请输入密码">
          </div>
          <!--记住我-->
          <label class="remember">
            <input v-model="rememberMe" class="remember-input" type="checkbox">
            记住我
          </label>
          <!--登录按钮-->
          <div v-loading="loading" class="login-button" @click="login()">登录</div>
          <!--错误提示-->
          <div class="login-error">{{ errMsg }}</div>
          
        </div>

        <div >
          <!--二维码登录-->
          <div class="qrcode">
            <img loading="lazy" src="@/assets/join_pc.png" alt="二维码登录">
          </div>
        </div>
      </div>
      </transition>
    </div>
    
    <div class="bottom">
      <div class="bottom-left">
        <div>加入 steam，探索数千款精彩游戏。</div>
        <RouterLink class="learn-more" to="about">了解更多</RouterLink>
      </div>
      <div class="bottom-middle">
        <img loading="lazy" src="@/assets/join_pc.png" alt="Join steam">
      </div>
      <div class="bottom-right">
        <!--注册-->
        <RouterLink class="join-button" :to="`/join?redir=${route.query.redir}`">加入 steam</RouterLink>
        <div>免费加入且简单易用。</div>
      </div>

    </div>

    </div>
</template>


<script setup>
// 导入Vue组合式API
import { ref, onMounted, nextTick } from 'vue'
// 导入Vuex store用于状态管理
import { useStore } from 'vuex'
// 导入Vue Router相关功能
import { useRoute, useRouter } from 'vue-router'
// 导入防抖函数
import { debounce } from '@/utils/debounce'

// 定义响应式变量
const username = ref('') // 用户名输入框绑定
const password = ref('') // 密码输入框绑定
const rememberMe = ref(true) // "记住我"复选框状态
const errMsg = ref('') // 错误提示信息
const loading = ref(false) // 登录按钮加载状态

// 获取Vuex store实例
const store = useStore()
// 获取当前路由信息
const route = useRoute()
// 获取路由实例用于导航
const router = useRouter()

// 使用防抖优化错误消息清除
const debouncedClearErrMsg = debounce(clearErrMsg, 300)


// 登录函数
function login() {
    // 验证用户名和密码是否为空
    if (!username.value || !password.value) return
    
    // 设置加载状态
    loading.value = true
    
    // 调用Vuex action执行登录
    store.dispatch('user/login', {
        username: username.value,
        password: password.value,
        rememberMe: rememberMe.value
    }).then(async () => {
        // 确保获取最新的用户信息
        await store.dispatch('user/getUserInfo')
        
        // 获取用户角色
        const role = store.getters['user/role']
        console.log('用户登录成功，角色:', role)
        
        // 如果是管理员(role=1)则跳转到后台
        if (role === 1) {
            console.log('检测到管理员角色，跳转到管理员后台')
            // 使用replace而不是push，避免导航历史问题
            router.replace('/admin')
            return
        }
        
        // 普通用户跳转到首页或重定向URL
        router.replace(route.query.redir || '/')
    }).catch(error => {
        // 登录失败处理
        loading.value = false
        
        // 显示错误信息，优先显示服务器返回的错误
        errMsg.value = error.response?.data?.message || 
                      error.message || 
                      '登录失败'
    })
}

// 清除错误信息函数
function clearErrMsg() {
  errMsg.value = ''
}

// 优化页面加载
onMounted(() => {
  // 延迟加载不重要的资源
  nextTick(() => {
    // 聚焦用户名输入框，提升用户体验
    const usernameInput = document.querySelector('.username-input')
    if (usernameInput) usernameInput.focus()
  })
})


</script>

<style scoped lang="scss">
.app {
  background-color: #181a21;
  min-height: calc(100vh - 104px);
  font-family: "Motiva Sans", sans-serif;
}

.login {
  display: flex;
  flex-direction: column;
  align-items: center;
  row-gap: 32px;
  padding: 80px 0 150px 0;
  background-image: url("@/assets/new_login_bg_strong_mask.jpg");
  background-position: center top;
  background-repeat: no-repeat;
}

.login-title {
  box-sizing: border-box;
  width: 700px;
  padding: 8px 16px;
  color: #ffffff;
  font-size: 32px;
  font-weight: 200;
}

.login-content {
  display: flex;
  column-gap: 40px;
  box-sizing: border-box;
  width: 700px;
  padding: 24px 32px;
  border-radius: 4px;
  background-color: #181a21;
}

.login-content-left {
  display: flex;
  flex-direction: column;
  row-gap: 12px;
  flex-grow: 1;
}

.username-label {
  margin-bottom: 2px;
  color: #1999ff;
  font-size: 12px;
  user-select: none;
}

.password-label {
  margin-bottom: 2px;
  color: #afafaf;
  font-size: 12px;
  user-select: none;
}

.username-input, .password-input {
  box-sizing: border-box;
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 2px;
  outline: none;
  color: #ffffff;
  font-size: 15px;
  font-family: Arial, sans-serif;
  background-color: #32353c;

  &:hover {
    background-color: #393c44;
  }
}

.remember {
  display: flex;
  align-items: center;
  color: #afafaf;
  font-size: 12px;
  cursor: pointer;
}

.remember-input {
  position: relative;
  box-sizing: border-box;
  width: 20px;
  height: 20px;
  padding: 10px;
  margin: 0 6px 0 0;
  border-radius: 2px;
  background-color: #32353c;
  cursor: pointer;
  appearance: none;

  &:checked::after {
    content: "✔";
    position: absolute;
    left: 0;
    top: 0;
    display: inline-block;
    width: 20px;
    height: 20px;
    color: #ffffff;
    font-size: 15px;
    line-height: 20px;
    text-align: center;
  }

  &:hover {
    background-color: #393c44;
  }
}

.login-button {
  align-self: center;
  box-sizing: border-box;
  width: 270px;
  padding: 12px;
  border-radius: 2px;
  color: #ffffff;
  font-size: 16px;
  text-align: center;
  background: linear-gradient(90deg, #06BFFF 0%, #2D73FF 100%);
  cursor: pointer;

  &:hover {
    background: linear-gradient(90deg, #06BFFF 30%, #2D73FF 100%);
  }
}

.login-error {
  align-self: center;
  height: 16px;
  color: #c15755;
  font-size: 12px;
}



.qrcode {
  width: 350px;

  margin-bottom: 8px;
  border-radius: 8px;
  overflow: hidden;
  user-select: none;

  img {
    width: 100%;
    height: 100%;
  }
}



.bottom {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-bottom: 100px;
  color: #b8b6b4;
  font-size: 14px;
}

.bottom-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 200px;
  text-align: center;
}

.learn-more {
  color: #ffffff;
  text-decoration: none;

  &:hover {
    color: #66c0f4
  }
}

.bottom-middle {
  width: 200px;

  img {
    width: 100%;
  }
}

.bottom-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 200px;
}

.join-button {
  padding: 0 15px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 2px;
  margin: 40px 0 10px 0;
  color: #ffffff;
  font-size: 15px;
  line-height: 30px;
  text-decoration: none;

  &:hover {
    border-color: #ffffff;
  }
}
/* 添加过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 优化输入框交互 */
.username-input:focus, .password-input:focus {
  background-color: #393c44;
  transition: background-color 0.3s ease;
}
</style>
