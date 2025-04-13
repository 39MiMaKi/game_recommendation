<template>
  <div class="app">
    <div class="join">
      <transition name="fade" appear>
      <div class="join-content">
        <div class="join-title">创建您的账户</div>
        <!--          电子邮件地址-->
        <div class="email">
          <div class="label">电子邮件地址</div>
          <div class="input-area">
            <input v-model="email" class="input" :class="{ error: emailErrMsg }"
                   @input="debouncedValidateEmail(email)" @focus="emailErrMsg = ''">
            <div v-show="emailErrMsg" class="err-msg">{{ emailErrMsg }}</div>
          </div>
        </div>
        <!--        steam 账户名称-->
        <div class="username">
          <div class="label">steam 账户名称</div>
          <div class="input-area">
            <input v-model="username" class="input" :class="{ error: usernameErrMsg }" maxlength="64"
                   @input="debouncedValidateUsername(username)" @focus="usernameErrMsg = ''">
            <div v-show="usernameErrMsg" class="err-msg">{{ usernameErrMsg }}</div>
          </div>
        </div>
        <!--        选择密码-->
        <div class="password">
          <div class="label">选择密码</div>
          <div class="input-area">
            <input v-model="password" class="input" :class="{ error: passwordErrMsg }" type="password"
                   maxlength="64" autocomplete="new-password"
                   @blur="validatePassword(password)" @focus="passwordErrMsg = ''">
            <div v-show="passwordErrMsg" class="err-msg">{{ passwordErrMsg }}</div>
          </div>
        </div>
        <!--        确认密码-->
        <div class="confirm-password">
          <div class="label">确认密码</div>
          <div class="input-area">
            <input v-model="confirmPassword" class="input" :class="{ error: confirmPasswordErrMsg }" type="password"
                   maxlength="64" autocomplete="new-password"
                   @blur="validateConfirmPassword(password, confirmPassword)" @focus="confirmPasswordErrMsg = ''">
            <div v-show="confirmPasswordErrMsg" class="err-msg">{{ confirmPasswordErrMsg }}</div>
          </div>
        </div>
        <!--        同意条款-->
        <label class="agree">
          <input v-model="agree" class="agree-input" :class="{ error: agreeErrMsg }" type="checkbox"
                 @change="agreeErrMsg = ''">
          我已年满 13 周岁并同意<a href="" target="_blank">《GameRec 订户协议》</a>和<a href="" target="_blank">《Valve
          隐私政策》</a>的条款。
          <div v-show="agreeErrMsg" class="err-msg">{{ agreeErrMsg }}</div>
        </label>
        <!-- 游戏标签选择 -->
<div class="tags">
  <div class="label">偏好游戏标签（可选）</div>
  <div class="tag-options">
    <div 
      v-for="tag in ['动作', '冒险', '角色扮演', '射击', '策略', '体育', '模拟', '解谜', '竞速', '格斗']" 
      :key="tag"
      class="tag-card"
      :class="{ selected: selectedTags.includes(tag) }"
      @click="toggleTag(tag)"
    >
      {{ tag }}
    </div>
  </div>
</div>
<div v-loading="loading" class="join-button" @click="join()">完成</div>
      </div>
       </transition>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, nextTick } from 'vue'
import { isEmailValid, isPasswordValid } from '@/utils/validate'
import { checkUsernameAvailableApi, checkEmailAvailableApi, joinApi } from '@/api/user'
import { debounce } from '@/utils/debounce'
import { useRoute, useRouter } from 'vue-router'

const selectedTags = ref([])
const loading = ref(false)

const email = ref('')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const agree = ref(false)

const emailErrMsg = ref('')
const usernameErrMsg = ref('')
const passwordErrMsg = ref('')
const confirmPasswordErrMsg = ref('')
const agreeErrMsg = ref('')

const route = useRoute()
const router = useRouter()

// 使用防抖优化验证函数
const debouncedValidateEmail = debounce(validateEmail, 500)
const debouncedValidateUsername = debounce(validateUsername, 500)



async function validateUsername(usernameValue) {
  if (!usernameValue) {
    usernameErrMsg.value = '请输入账户名称'
    return false
  }
  try {
    const { data: valid } = await checkUsernameAvailableApi(usernameValue)
    usernameErrMsg.value = valid ? '' : '账户名称不可用'
    return valid
  } catch (error) {
    usernameErrMsg.value = '验证失败，请稍后再试'
    return false
  }
}

async function validateEmail(emailValue) {
  if (!emailValue) {
    emailErrMsg.value = '请输入电子邮件地址'
    return false
  }
  if (!isEmailValid(emailValue)) {
    emailErrMsg.value = '请输入有效的电子邮件地址'
    return false
  }
  try {
    const { data: available } = await checkEmailAvailableApi(emailValue)
    emailErrMsg.value = available ? '' : '该邮箱已被注册'
    return available
  } catch (error) {
    emailErrMsg.value = '验证失败，请稍后再试'
    return false
  }
}


function validatePassword(passwordValue) {
  const valid = isPasswordValid(passwordValue)
  passwordErrMsg.value = valid ? '' : '密码必须包含大小写字母和数字，且长度至少8位'
  return valid
}

function validateConfirmPassword(passwordValue, confirmPasswordValue) {
  const valid = (passwordValue === confirmPasswordValue)
  confirmPasswordErrMsg.value = valid ? '' : '密码不符'
  return valid
}

function validateAgree(agreeValue) {
  agreeErrMsg.value = agreeValue ? '' : '您必须同意《GameRec 订户协议》才能继续'
  return agreeValue
}

async function validateAll(email, username, password, confirmPassword, agree) {
  // 并行执行所有验证，提高性能
  const [emailValid, usernameValid, passwordValid, confirmValid, agreeValid] = await Promise.all([
    validateEmail(email),
    validateUsername(username),
    Promise.resolve(validatePassword(password)),
    Promise.resolve(validateConfirmPassword(password, confirmPassword)),
    Promise.resolve(validateAgree(agree))
  ])
  
  return emailValid && passwordValid && confirmValid && agreeValid && usernameValid
}

async function join() {
  const emailValue = email.value
  const usernameValue = username.value
  const passwordValue = password.value
  const confirmPasswordValue = confirmPassword.value
  const agreeValue = agree.value
  loading.value = true
  if (!await validateAll(emailValue, usernameValue, passwordValue, confirmPasswordValue, agreeValue)) {
    loading.value = false
    return
  }
  joinApi({ 
  email: emailValue, 
  username: usernameValue, 
  password: passwordValue,
  ...(selectedTags.value.length ? { tags: selectedTags.value.join(',') } : {})
}).then(() => {
    router.replace({
      name: 'login',
      query: { redir: route.query.redir }
    })
  }).catch((reason) => {
    loading.value = false
    if (reason.code === 400) {
      emailErrMsg.value = '请输入有效的电子邮件地址'
    } else if (reason.code === 409) {
      usernameErrMsg.value = '账户名称不可用'
    }
  })
}
const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag);
  if (index === -1) {
    selectedTags.value.push(tag);
  } else {
    selectedTags.value.splice(index, 1);
  }
}

// 优化页面加载
onMounted(() => {
  // 延迟加载不重要的资源
  nextTick(() => {
    // 预加载表单验证，减少首次验证时的延迟
    setTimeout(() => {
      isEmailValid('')
      isPasswordValid('')
    }, 1000)
  })
})
</script>


<style scoped lang="scss">
.app {
  background-color: #1f2428;
  min-height: calc(100vh - 104px);
  font-family: "Motiva Sans", sans-serif;
}

.join {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  row-gap: 32px;
  padding: 80px 0 150px 0;
  background-image: url("@/assets/acct_creation_bg.jpg");
  background-position: -150% top;
  background-repeat: no-repeat;
}

.join-content {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 25px;
  width: 800px;
  padding: 0 36px;
  margin: 0 auto;
}

.join-title {
  margin-bottom: 10px;
  color: #ffffff;
  font-size: 32px;
  font-weight: 200;
}

.label {
  margin-bottom: 2px;
  color: #b8b6b4;
  font-size: 14px;
}

.input-area {
  position: relative;
  display: inline-block;
}

.input {
  box-sizing: border-box;
  width: 300px;
  padding: 8px;
  border: none;
  border-radius: 2px;
  color: #ffffff;
  font-size: 15px;
  font-family: Arial, sans-serif;
  background-color: #32353c;

  &:hover {
    background-color: #393c44;
  }

  &.error {
    outline: 1px solid #c15755;
  }
}

.err-msg {
  position: absolute;
  left: calc(100% + 20px);
  top: 50%;
  padding: 8px;
  border-radius: 4px;
  color: #ffffff;
  background-color: #a0382b;
  font-size: 12px;
  white-space: nowrap;
  transform: translateY(-50%);

  &::before {
    content: "";
    position: absolute;
    right: 100%;
    top: 50%;
    display: inline-block;
    border-top: 8px solid transparent;
    border-right: 8px solid #a0382b;
    border-bottom: 8px solid transparent;
    transform: translate(1px, -50%);
  }
}

.agree {
  position: relative;
  display: flex;
  align-items: center;
  width: max-content;
  color: #b8b6b4;
  font-size: 14px;
  cursor: pointer;

  a {
    color: #ffffff;
    text-decoration: none;

    &:hover {
      color: #66c0f4;
    }
  }
}

.agree-input {
  position: relative;
  box-sizing: border-box;
  width: 20px;
  height: 20px;
  padding: 10px;
  margin: 0 6px 0 0;
  //border: 1px solid #;
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

  &.error {
    outline: 1px solid #c15755;
  }
}

.join-button {
  box-sizing: border-box;
  width: 130px;
  border-radius: 2px;
  margin-top: 10px;
  color: #ffffff;
  font-size: 15px;
  line-height: 36px;
  text-align: center;
  background: linear-gradient(90deg, #06BFFF 0%, #2D73FF 100%);
  cursor: pointer;

  &:hover {
    background: linear-gradient(90deg, #06BFFF 30%, #2D73FF 100%);
  }
}


.tag-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.tag-card {
  padding: 6px 12px;
  background: #32353c;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  text-align: center;
  font-size: 13px;
  margin-right: 4px;
  color: #b8b6b4;

  &:hover {
    background: #393c44;
    transform: translateY(-2px);
  }

  &.selected {
    background: linear-gradient(90deg, #06BFFF 0%, #2D73FF 100%);
    box-shadow: 0 0 5px rgba(6, 191, 255, 0.5);
    color: white;
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
</style>