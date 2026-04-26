<template>
  <div class="knowledge-graph">
    <van-nav-bar title="知识图谱" left-arrow @click-left="$router.back()" />

    <div v-if="loading" class="loading-container">
      <van-loading>加载中...</van-loading>
    </div>

    <div v-else-if="graphData.nodes.length === 0" class="empty-container">
      <van-empty description="暂无数据" />
    </div>

    <div v-else class="graph-container">
      <div ref="chartRef" class="chart"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const loading = ref(false)
const graphData = ref({ nodes: [], links: [] })
const chartRef = ref(null)
let chartInstance = null

const fetchGraphData = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/portal/knowledge-graph')
    console.log('知识图谱接口返回:', res)
    if (res.data) {
      graphData.value = res.data
      console.log('节点数量:', res.data.nodes?.length, '连线数量:', res.data.links?.length)

      // 先关闭 loading，确保 DOM 已渲染
      loading.value = false

      await nextTick()
      console.log('nextTick 后，chartRef:', chartRef.value)
      renderChart()
    } else {
      loading.value = false
    }
  } catch (error) {
    console.error('获取知识图谱失败:', error)
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value) {
    console.error('chartRef 不存在')
    return
  }

  if (graphData.value.nodes.length === 0) {
    console.error('节点数据为空')
    return
  }

  console.log('开始渲染图表，节点数:', graphData.value.nodes.length, '连线数:', graphData.value.links.length)

  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  // 创建 id -> name 映射
  const nodeMap = new Map()
  graphData.value.nodes.forEach(node => {
    nodeMap.set(node.id, node.name)
  })

  const nodes = graphData.value.nodes.map(node => ({
    id: String(node.id),
    name: node.name,
    symbolSize: Math.max(20, Math.min(60, node.count * 5)),
    category: 0,
    itemStyle: {
      color: getRandomColor()
    }
  }))

  const links = graphData.value.links.map(link => ({
    source: String(link.source),
    target: String(link.target),
    value: link.weight,
    lineStyle: {
      width: Math.min(5, link.weight)
    }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'node') {
          return `${params.data.name}<br/>文章数: ${Math.floor(params.data.symbolSize / 5)}`
        } else {
          return `关联强度: ${params.data.value}`
        }
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      animation: true,
      data: nodes,
      links: links,
      roam: true,
      label: {
        show: true,
        position: 'right',
        formatter: '{b}'
      },
      labelLayout: {
        hideOverlap: true
      },
      force: {
        repulsion: 500,
        gravity: 0.1,
        edgeLength: [50, 150],
        layoutAnimation: true
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 5
        }
      }
    }]
  }

  chartInstance.setOption(option)

  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
}

const getRandomColor = () => {
  const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
  return colors[Math.floor(Math.random() * colors.length)]
}

onMounted(() => {
  fetchGraphData()
})
</script>

<style lang="scss" scoped>
.knowledge-graph {
  min-height: 100vh;
  background: var(--bg-primary);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
}

.graph-container {
  height: calc(100vh - 46px);
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
