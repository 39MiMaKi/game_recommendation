<template>
  <div class="data-charts">
    <div class="chart-container">
      <div ref="userGrowthChart" class="chart"></div>
      <div ref="gamePopularityChart" class="chart"></div>
      <div ref="userActivityChart" class="chart"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { onMounted, onBeforeUnmount, ref } from 'vue'

export default {
  name: 'DataCharts',
  props: {
    statistics: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const userGrowthChart = ref(null)
    const gamePopularityChart = ref(null)
    const userActivityChart = ref(null)
    
    let charts = []
    
    // 模拟数据 - 实际应用中应从后端获取
    const mockUserGrowthData = () => {
      const dates = []
      const userData = []
      const now = new Date()
      
      for (let i = 30; i >= 0; i--) {
        const date = new Date(now)
        date.setDate(date.getDate() - i)
        dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
        
        // 模拟增长数据
        const baseValue = props.statistics.userCount || 1000
        const growthRate = Math.random() * 0.05 + 0.01 // 1% - 6%的增长率
        userData.push(Math.floor(baseValue * (1 - (i * growthRate))))
      }
      
      return { dates, userData }
    }
    
    const mockGamePopularityData = () => {
      // 模拟游戏受欢迎程度数据
      return [
        { value: 40, name: '动作游戏' },
        { value: 25, name: '角色扮演' },
        { value: 15, name: '策略游戏' },
        { value: 10, name: '模拟游戏' },
        { value: 10, name: '其他类型' }
      ]
    }
    
    const mockUserActivityData = () => {
      // 模拟用户活跃度数据
      const hours = []
      const activityData = []
      
      for (let i = 0; i < 24; i++) {
        hours.push(`${i}:00`)
        // 模拟一天中不同时段的活跃度
        let activity = 0
        if (i >= 9 && i <= 12) { // 上午高峰
          activity = Math.floor(Math.random() * 300) + 700
        } else if (i >= 19 && i <= 23) { // 晚上高峰
          activity = Math.floor(Math.random() * 400) + 800
        } else if (i >= 13 && i <= 18) { // 下午中等
          activity = Math.floor(Math.random() * 300) + 500
        } else { // 深夜/凌晨低谷
          activity = Math.floor(Math.random() * 200) + 100
        }
        activityData.push(activity)
      }
      
      return { hours, activityData }
    }
    
    const initUserGrowthChart = () => {
      if (!userGrowthChart.value) return
      
      const chart = echarts.init(userGrowthChart.value)
      charts.push(chart)
      
      const { dates, userData } = mockUserGrowthData()
      
      const option = {
        title: {
          text: '用户增长趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: userData,
          type: 'line',
          smooth: true,
          areaStyle: {}
        }]
      }
      
      chart.setOption(option)
    }
    
    const initGamePopularityChart = () => {
      if (!gamePopularityChart.value) return
      
      const chart = echarts.init(gamePopularityChart.value)
      charts.push(chart)
      
      const data = mockGamePopularityData()
      
      const option = {
        title: {
          text: '游戏类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 10,
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '游戏类型',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: data
          }
        ]
      }
      
      chart.setOption(option)
    }
    
    const initUserActivityChart = () => {
      if (!userActivityChart.value) return
      
      const chart = echarts.init(userActivityChart.value)
      charts.push(chart)
      
      const { hours, activityData } = mockUserActivityData()
      
      const option = {
        title: {
          text: '用户活跃度分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: hours
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: activityData,
          type: 'bar',
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(180, 180, 180, 0.2)'
          }
        }]
      }
      
      chart.setOption(option)
    }
    
    const handleResize = () => {
      charts.forEach(chart => chart.resize())
    }
    
    onMounted(() => {
      initUserGrowthChart()
      initGamePopularityChart()
      initUserActivityChart()
      
      window.addEventListener('resize', handleResize)
    })
    
    onBeforeUnmount(() => {
      window.removeEventListener('resize', handleResize)
      charts.forEach(chart => chart.dispose())
    })
    
    return {
      userGrowthChart,
      gamePopularityChart,
      userActivityChart
    }
  }
}
</script>

<style scoped>
.data-charts {
  width: 100%;
  margin-top: 20px;
}

.chart-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.chart {
  height: 300px;
  width: 100%;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
  box-sizing: border-box;
}

@media (max-width: 1200px) {
  .chart-container {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .chart-container {
    grid-template-columns: 1fr;
  }
}
</style>

