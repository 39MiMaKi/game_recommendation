# steam 仿写

本项目为仿写 steam 网页版，采用前后端分离模式，前端使用Vue3框架，后端使用SpringBoot框架

已完成功能：全局顶部导航栏、商城首页（部分）、注册/登录/登出、愿望单、游戏详情、个人资料、好友系统、聊天系统；

待完成功能：搜索结果、游戏评测

## 在线演示：[steam](http://wbbb.plus/steam)

## 技术细节

### 前端：

已完成：

* 使用 Vue Router 进行路由管理
* 使用 Vuex 进行状态管理（如登录状态、当前路由）
* 封装了 Axios 的 request 方法，配置了请求拦截器和响应拦截器
* 使用 WebSocket 以及一套简单的消息规范（身份验证、获取好友列表、获取聊天记录、发送消息、已读、好友上/下线等），实现聊天系统
* 实现了愿望单的拖拽排序，以及优雅的拖拽动画
* 封装了开箱即用的 showModal 方法、轮播图组件、分页器组件
* 在 sort.js 中封装了快速排序、二分插入等常用排序算法
* 自定义 v-loading 指令用于播放加载动画，v-trunc 指令用于自动截断多行文本
* 自定义 v-lazy 指令，实现图片懒加载；封装 debounce 防抖函数，用于实时搜索

待完成：

* 封装文本编辑器组件，用于撰写评测

### 后端

已完成：

* 使用 SpringBoot 处理 HTTP 请求
* 使用 SpringDataJPA + MySQL 进行数据库的建表以及增、删、改、查
* 使用 SpringBootStarterWebSocket 搭建 WebSocket 服务，实现聊天系统
* 使用 JJWT 库进行 Token 的生成和解析
* 使用全局异常处理器捕获并处理请求异常和服务器异常；使用 @Configuration 配置全局允许跨域
* 封装了通用响应体、常用状态码等

待完成：

* 使用全局拦截器进行身份校验
* 搭建图床，实现图片上传

## 启动说明

### 后端启动

1. 确保已安装Java 8或更高版本和Maven
2. 进入BackEnd目录
   ```
   cd BackEnd
   ```
3. 使用Maven启动Spring Boot应用
   ```
   ./mvnw spring-boot:run
   ```
   Windows系统使用：
   ```
   mvnw.cmd spring-boot:run
   ```
4. 后端服务将在 http://localhost:8081 上运行

### 前端启动

1. 确保已安装Node.js (推荐v14或更高版本)
2. 进入FrontEnd目录
   ```
   cd FrontEnd
   ```
3. 安装依赖
   ```
   npm install
   ```
4. 启动开发服务器
   ```
   npm run dev
   ```
5. 前端应用将在 http://localhost:5173 (或其他Vite默认端口) 上运行

## 常见问题

### API连接被拒绝

如果看到控制台有类似 `ERR_CONNECTION_REFUSED` 的错误：

1. 确保后端服务已启动并在端口8081上运行
2. 检查前端的API配置是否正确 (在 `.env` 文件中)
3. 如果修改了后端端口，请相应更新前端的 `VITE_API_BASE_URL` 环境变量

### 图片加载失败

如果看到图片加载404错误：

1. 项目已配置默认图片，当API返回的图片URL无效时会显示默认图片
2. 如果仍有问题，请检查资源路径是否正确
