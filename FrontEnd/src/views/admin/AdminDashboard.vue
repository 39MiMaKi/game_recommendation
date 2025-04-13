<template>
  <div class="admin-dashboard">
    <div class="admin-header">
      <h1>管理员后台</h1>
      <div class="admin-nav">
        <button 
          v-for="(tab, index) in tabs" 
          :key="index"
          :class="{ active: currentTab === tab.id }"
          @click="currentTab = tab.id"
          class="tab-button">
          {{ tab.name }}
        </button>
      </div>
    </div>
    
    <div class="admin-content">
      <!-- 系统概览 -->
      <div v-if="currentTab === 'dashboard'" class="dashboard-section">
        <h2>系统概览</h2>
        <div class="statistics-cards">
          <div class="stat-card">
            <div class="stat-icon"><i class="fas fa-users"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.userCount || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon"><i class="fas fa-gamepad"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.appCount || 0 }}</div>
              <div class="stat-label">游戏总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon"><i class="fas fa-star"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.ratingCount || 0 }}</div>
              <div class="stat-label">评分总数</div>
            </div>
          </div>
        </div>
        
        <!-- 数据可视化图表 -->
        <DataCharts :statistics="statistics" />
      </div>
      
      <!-- 数据分析 -->
      <div v-if="currentTab === 'analytics'" class="dashboard-section">
        <h2>数据分析</h2>
        <DataCharts :statistics="statistics" />
      </div>
      
      <!-- 用户管理 -->
      <!-- 在用户管理部分添加搜索框 -->
      <div v-if="currentTab === 'users'" class="dashboard-section">
        <h2>用户管理</h2>
        <div class="search-bar">
          <input v-model="searchUsername" placeholder="用户名" />
          <input v-model="searchEmail" placeholder="邮箱" />
          <button @click="searchUsers">搜索</button>
          <button @click="showCreateAdminModal = true" class="create-admin-button">创建管理员</button>
        </div>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>邮箱</th>
                <th>昵称</th>
                <th>注册时间</th>
                <th>角色</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.userId">
                <td>{{ user.userId }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.nickname }}</td>
                <td>{{ formatDate(user.createTime) }}</td>
                <td>{{ user.role === 1 ? '管理员' : '普通用户' }}</td>
                <td>
                  <button @click="editUserPreferences(user)" class="action-button">
                    设置偏好
                  </button>
                  <button @click="setUserRole(user)" class="action-button" :class="{ 'admin-button': user.role === 1 }">
                    {{ user.role === 1 ? '取消管理员' : '设为管理员' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pagination">
            <button @click="changePage('users', -1)" :disabled="userPage === 0">
              上一页
            </button>
            <span>第 {{ userPage + 1 }} 页</span>
            <button @click="changePage('users', 1)" :disabled="userPage >= userTotalPages - 1">
              下一页
            </button>
          </div>
        </div>
      </div>
      
      <!-- 游戏管理 -->
      <div v-if="currentTab === 'apps'" class="dashboard-section">
        <h2>游戏管理</h2>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>游戏名称</th>
                <th>开发商</th>
                <th>发行商</th>
                <th>价格</th>
                <th>标签</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="app in apps" :key="app.appId">
                <td>{{ app.appId }}</td>
                <td>{{ app.name }}</td>
                <td>{{ app.developer }}</td>
                <td>{{ app.publisher }}</td>
                <td>{{ getPriceStr(app.price) }}</td>
                <td>{{ app.tags }}</td>
              </tr>
            </tbody>
          </table>
          <div class="pagination">
            <button @click="changePage('apps', -1)" :disabled="appPage === 0">
              上一页
            </button>
            <span>第 {{ appPage + 1 }} 页</span>
            <button @click="changePage('apps', 1)" :disabled="appPage >= appTotalPages - 1">
              下一页
            </button>
          </div>
        </div>
      </div>
      
      <!-- 评分数据 -->
      <div v-if="currentTab === 'ratings'" class="dashboard-section">
        <h2>评分数据</h2>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>用户ID</th>
                <th>游戏ID</th>
                <th>评分</th>
                <th>评分时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="rating in ratings" :key="rating.id">
                <td>{{ rating.id }}</td>
                <td>{{ rating.userId }}</td>
                <td>{{ rating.appId }}</td>
                <td>{{ rating.rating }}</td>
                <td>{{ formatDate(rating.timestamp) }}</td>
              </tr>
            </tbody>
          </table>
          <div class="pagination">
            <button @click="changePage('ratings', -1)" :disabled="ratingPage === 0">
              上一页
            </button>
            <span>第 {{ ratingPage + 1 }} 页</span>
            <button @click="changePage('ratings', 1)" :disabled="ratingPage >= ratingTotalPages - 1">
              下一页
            </button>
          </div>
        </div>
      </div>
      
      <!-- 推荐系统设置 -->
      <div v-if="currentTab === 'recommendation'" class="dashboard-section">
        <h2>推荐系统设置</h2>
        <div class="recommendation-settings">
          <div class="setting-item">
            <label>内容推荐权重</label>
            <div class="slider-container">
              <input 
                type="range" 
                min="0" 
                max="1" 
                step="0.1" 
                v-model="recommendationParams.contentWeight"
              />
              <span>{{ recommendationParams.contentWeight }}</span>
            </div>
          </div>
          <div class="setting-item">
            <label>协同过滤权重</label>
            <div class="slider-container">
              <input 
                type="range" 
                min="0" 
                max="1" 
                step="0.1" 
                v-model="recommendationParams.collaborativeWeight"
              />
              <span>{{ recommendationParams.collaborativeWeight }}</span>
            </div>
          </div>
          <div class="setting-item">
            <label>冷启动阈值（最小评分数）</label>
            <div class="slider-container">
              <input 
                type="range" 
                min="1" 
                max="10" 
                step="1" 
                v-model="recommendationParams.coldStartThreshold"
              />
              <span>{{ recommendationParams.coldStartThreshold }}</span>
            </div>
          </div>
          <button @click="saveRecommendationParams" class="save-button">
            保存设置
          </button>
        </div>
      </div>
    </div>
    
    <!-- 用户偏好设置弹窗 -->
    <div v-if="showPreferenceModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>设置用户偏好标签</h3>
          <button @click="showPreferenceModal = false" class="close-button">&times;</button>
        </div>
        <div class="modal-body">
          <p>为用户 <strong>{{ selectedUser?.username }}</strong> 设置偏好标签：</p>
          <div class="tag-selection">
            <div v-for="(tag, index) in availableTags" :key="index" 
                 class="tag-item" 
                 :class="{ selected: selectedTags.includes(tag) }"
                 @click="toggleTag(tag)">
              {{ tag }}
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveUserPreferences" class="save-button">保存</button>
          <button @click="showPreferenceModal = false" class="cancel-button">取消</button>
        </div>
      </div>
    </div>
    
    <!-- 创建管理员弹窗 -->
    <div v-if="showCreateAdminModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>创建管理员账号</h3>
          <button @click="showCreateAdminModal = false" class="close-button">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="newAdminData.username" type="text" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="newAdminData.email" type="email" />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="newAdminData.password" type="password" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="createAdmin" class="save-button">创建</button>
          <button @click="showCreateAdminModal = false" class="cancel-button">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import adminApi from '@/api/admin'
import DataCharts from '@/components/admin/DataCharts.vue'

// 响应式数据
const currentTab = ref('dashboard')
const statistics = ref({})
const users = ref([])
const apps = ref([])
const ratings = ref([])
const recommendationParams = ref({
  contentWeight: 0.6,
  collaborativeWeight: 0.4,
  coldStartThreshold: 5
})

// 加载状态
const loading = ref({
  users: false,
  apps: false,
  ratings: false,
  statistics: false,
  recommendations: false
})

// 分页相关
const userPage = ref(0)
const appPage = ref(0)
const ratingPage = ref(0)
const pageSize = 10
const userTotalPages = ref(1)
const appTotalPages = ref(1)
const ratingTotalPages = ref(1)

// 搜索相关
const searchUsername = ref('')
const searchEmail = ref('')
const searchKeyword = ref('')
const searchTag = ref('')
const minRating = ref(0)
const maxRating = ref(5)

// 模态框相关
const showPreferenceModal = ref(false)
const showCreateAdminModal = ref(false)
const selectedUser = ref(null)
const availableTags = ref(['动作', '冒险', '角色扮演', '策略', '模拟', '体育', '竞速', '射击', '解谜', '恐怖'])
const selectedTags = ref([])
const editAppData = ref(null)
const isEditModalOpen = ref(false)
const newAdminData = ref({
  username: '',
  email: '',
  password: ''
})

const tabs = [
  { id: 'dashboard', name: '系统概览' },
  { id: 'analytics', name: '数据分析' },
  { id: 'users', name: '用户管理' },
  { id: 'apps', name: '游戏管理' },
  { id: 'ratings', name: '评分数据' },
  { id: 'recommendation', name: '推荐系统设置' }
]

// 工具函数
function formatDate(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString()
}

function getPriceStr(price) {
  if (price === 0) return '免费'
  return `¥${price.toFixed(2)}`
}

// 数据加载函数
async function loadStatistics() {
  try {
    const response = await adminApi.getStatistics()
    statistics.value = response.data.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

async function loadUsers() {
  try {
    // 显示加载状态
    loading.value.users = true
    users.value = []
    const response = await adminApi.getUsers(userPage.value, pageSize)
    // 确保响应数据格式正确
    if (response.data && response.data.data) {
      users.value = response.data.data.content || []
      userTotalPages.value = response.data.data.totalPages || 1
    } else {
      console.error('用户数据格式不正确:', response)
      alert('获取用户数据格式不正确')
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    alert('获取用户数据失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    loading.value.users = false
  }
}

async function loadApps() {
  try {
    const response = await adminApi.getApps(appPage.value, pageSize)
    apps.value = response.data.data.content
    appTotalPages.value = response.data.data.totalPages
  } catch (error) {
    console.error('加载游戏数据失败:', error)
  }
}

async function loadRatings() {
  try {
    const response = await adminApi.getRatings(ratingPage.value, pageSize)
    ratings.value = response.data.data.content
    ratingTotalPages.value = response.data.data.totalPages
  } catch (error) {
    console.error('加载评分数据失败:', error)
  }
}

async function loadRecommendationParams() {
  try {
    const response = await adminApi.getRecommendConfig()
    recommendationParams.value = response.data.data
  } catch (error) {
    console.error('加载推荐参数失败:', error)
  }
}

// 用户管理功能
async function searchUsers() {
  try {
    // 显示加载状态
    users.value = []
    const response = await adminApi.searchUsers(
      userPage.value,
      pageSize,
      searchUsername.value,
      searchEmail.value
    )
    // 确保响应数据格式正确
    if (response.data && response.data.data) {
      users.value = response.data.data.content || []
      userTotalPages.value = response.data.data.totalPages || 1
    } else {
      console.error('搜索用户数据格式不正确:', response)
      alert('搜索用户数据格式不正确')
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    alert('搜索用户失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

async function setUserRole(user) {
  const newRole = user.role === 1 ? 0 : 1
  const actionText = newRole === 1 ? '设为管理员' : '取消管理员'
  
  if (confirm(`确定要${actionText}吗？`)) {
    try {
      // 调用API设置用户角色
      const response = await adminApi.setUserRole(user.userId, newRole)
      
      // 检查响应状态
      if (response.code === 200 || response.status === 'success') {
        // 重新加载用户列表以获取最新数据
        await loadUsers()
        alert(`${actionText}成功`)
      } else {
        console.error('设置用户角色返回错误:', response)
        alert(`${actionText}失败: ${response.message || '未知错误'}`)
      }
    } catch (error) {
      console.error('设置用户角色失败:', error)
      alert(`${actionText}失败: ${error.response?.data?.message || error.message || '未知错误'}`)
    }
  }
}

// 游戏管理功能
async function searchApps() {
  try {
    const response = await adminApi.getApps(
      appPage.value, 
      pageSize,
      searchKeyword.value,
      searchTag.value
    )
    apps.value = response.data.data.content
    appTotalPages.value = response.data.data.totalPages
  } catch (error) {
    console.error('搜索游戏失败:', error)
  }
}

async function filterByRating() {
  try {
    const response = await adminApi.getAppsByRatingRange(
      appPage.value,
      pageSize,
      minRating.value,
      maxRating.value
    )
    apps.value = response.data.data.content
    appTotalPages.value = response.data.data.totalPages
  } catch (error) {
    console.error('按评分筛选失败:', error)
  }
}

async function updateApp(appId) {
  try {
    await adminApi.updateApp(appId, editAppData.value)
    isEditModalOpen.value = false
    loadApps()
  } catch (error) {
    console.error('更新游戏失败:', error)
  }
}

async function deleteApp(appId) {
  if (confirm('确定要删除这个游戏吗？')) {
    try {
      await adminApi.deleteApp(appId)
      loadApps()
    } catch (error) {
      console.error('删除游戏失败:', error)
    }
  }
}

// 用户偏好功能
function editUserPreferences(user) {
  selectedUser.value = user
  selectedTags.value = user.tags ? user.tags.split(',') : []
  showPreferenceModal.value = true
}

function toggleTag(tag) {
  const index = selectedTags.value.indexOf(tag)
  if (index === -1) {
    selectedTags.value.push(tag)
  } else {
    selectedTags.value.splice(index, 1)
  }
}

async function saveUserPreferences() {
  if (!selectedUser.value) return
  
  try {
    // 调用API更新用户偏好
    const response = await adminApi.updateUserPreferences(
      selectedUser.value.userId, 
      selectedTags.value.join(',')
    )
    
    // 检查响应状态
    if (response.code === 200 || response.status === 'success') {
      showPreferenceModal.value = false
      // 重新加载用户列表以获取最新数据
      await loadUsers()
      alert('保存成功')
    } else {
      console.error('保存用户偏好返回错误:', response)
      alert(`保存失败: ${response.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('保存用户偏好失败:', error)
    alert(`保存失败: ${error.response?.data?.message || error.message || '未知错误'}`)
  }
}

async function createAdmin() {
  try {
    // 验证输入
    if (!newAdminData.value.username || !newAdminData.value.email || !newAdminData.value.password) {
      alert('请填写完整的管理员信息')
      return
    }
    
    // 调用API创建管理员
    const response = await adminApi.createAdmin(newAdminData.value)
    
    // 检查响应状态
    if (response.code === 200 || response.status === 'success') {
      showCreateAdminModal.value = false
      // 重新加载用户列表以获取最新数据
      await loadUsers()
      alert('管理员创建成功')
      // 重置表单
      newAdminData.value = { username: '', email: '', password: '' }
    } else {
      console.error('创建管理员返回错误:', response)
      alert(`创建失败: ${response.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('创建管理员失败:', error)
    alert('创建失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 推荐系统功能
async function saveRecommendationParams() {
  try {
    await adminApi.updateRecommendConfig(recommendationParams.value)
    alert('保存成功')
  } catch (error) {
    console.error('保存推荐参数失败:', error)
    alert('保存失败')
  }
}

// 分页功能
function changePage(type, direction) {
  if (type === 'users') {
    userPage.value += direction
    loadUsers()
  } else if (type === 'apps') {
    appPage.value += direction
    loadApps()
  } else if (type === 'ratings') {
    ratingPage.value += direction
    loadRatings()
  }
}

// 初始化加载
onMounted(async () => {
  try {
    // 并行加载所有数据
    await Promise.all([
      loadStatistics(),
      loadUsers(),
      loadApps(),
      loadRatings(),
      loadRecommendationParams()
    ])
  } catch (error) {
    console.error('初始化加载数据失败:', error)
  }
})
</script>



<style scoped>
.admin-dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.admin-header {
  background-color: #2c3e50;
  color: white;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
 
}

.admin-header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.admin-nav {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.tab-button {
  background: none;
  border: none;
  color: white;
  padding: 0.5rem 1rem;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.tab-button:hover {
  background-color: rgba(255,255,255,0.1);
}

.tab-button.active {
  background-color: #42b983;
}

.admin-content {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
}

.dashboard-section {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.stat-card {
  background: #42b983;
  color: white;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 2rem;
  margin-right: 1rem;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
}

.data-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #f8f9fa;
  font-weight: 600;
}

tr:hover {
  background-color: #f5f5f5;
}

.action-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 0.5rem;
}

.create-admin-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 1rem;
}

.create-admin-button:hover {
  opacity: 0.9;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.form-group input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.action-button:hover {
  opacity: 0.9;
}

.admin-button {
  background-color: #ff6b6b;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
}

.modal-header {
  padding: 1rem;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.tag-selection {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 1rem;
}

.tag-item {
  padding: 0.5rem 1rem;
  background-color: #eee;
  border-radius: 20px;
  cursor: pointer;
}

.tag-item.selected {
  background-color: #42b983;
  color: white;
}

.modal-footer {
  padding: 1rem;
  border-top: 1px solid #ddd;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.save-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-button {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}
</style>
