<template>
  <div class="stock-chart">
    <div class="chart-header">
      <h3>{{ title }}</h3>
      <div class="period-selector">
        <button 
          v-for="period in periods" 
          :key="period.value"
          :class="{ active: selectedPeriod === period.value }"
          @click="changePeriod(period.value)"
        >
          {{ period.label }}
        </button>
      </div>
    </div>
    <div class="chart-container">
      <canvas ref="chartCanvas"></canvas>
    </div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

export default {
  name: 'StockChart',
  props: {
    stockCode: {
      type: String,
      required: true
    },
    title: {
      type: String,
      default: '주가 차트'
    },
    priceData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      chart: null,
      selectedPeriod: '1M',
      periods: [
        { label: '1주', value: '1W' },
        { label: '1개월', value: '1M' },
        { label: '3개월', value: '3M' },
        { label: '6개월', value: '6M' },
        { label: '1년', value: '1Y' }
      ]
    }
  },
  watch: {
    priceData: {
      handler() {
        this.updateChart()
      },
      deep: true
    }
  },
  mounted() {
    this.initChart()
  },
  beforeUnmount() {
    if (this.chart) {
      this.chart.destroy()
    }
  },
  methods: {
    initChart() {
      const ctx = this.$refs.chartCanvas.getContext('2d')
      
      this.chart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: [],
          datasets: [{
            label: '종가',
            data: [],
            borderColor: '#1976d2',
            backgroundColor: 'rgba(25, 118, 210, 0.1)',
            tension: 0.1,
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              mode: 'index',
              intersect: false,
              callbacks: {
                label: (context) => {
                  return `종가: ${Number(context.parsed.y).toLocaleString('ko-KR')}원`
                }
              }
            }
          },
          scales: {
            y: {
              beginAtZero: false,
              ticks: {
                callback: (value) => {
                  return value.toLocaleString('ko-KR') + '원'
                }
              }
            },
            x: {
              ticks: {
                maxRotation: 45,
                minRotation: 45
              }
            }
          }
        }
      })
      
      this.updateChart()
    },
    updateChart() {
      if (!this.chart || !this.priceData || this.priceData.length === 0) return
      
      const filteredData = this.filterDataByPeriod(this.priceData)
      
      const labels = filteredData.map(item => {
        const date = new Date(item.tradeDate)
        return `${date.getMonth() + 1}/${date.getDate()}`
      })
      
      const data = filteredData.map(item => Number(item.closePrice))
      
      this.chart.data.labels = labels
      this.chart.data.datasets[0].data = data
      this.chart.update()
    },
    filterDataByPeriod(data) {
      if (!data || data.length === 0) return []
      
      const now = new Date()
      let startDate = new Date()
      
      switch (this.selectedPeriod) {
        case '1W':
          startDate.setDate(now.getDate() - 7)
          break
        case '1M':
          startDate.setMonth(now.getMonth() - 1)
          break
        case '3M':
          startDate.setMonth(now.getMonth() - 3)
          break
        case '6M':
          startDate.setMonth(now.getMonth() - 6)
          break
        case '1Y':
          startDate.setFullYear(now.getFullYear() - 1)
          break
        default:
          startDate.setMonth(now.getMonth() - 1)
      }
      
      return data
        .filter(item => new Date(item.tradeDate) >= startDate)
        .sort((a, b) => new Date(a.tradeDate) - new Date(b.tradeDate))
    },
    changePeriod(period) {
      this.selectedPeriod = period
      this.updateChart()
      this.$emit('period-change', period)
    }
  }
}
</script>

<style scoped>
.stock-chart {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.period-selector {
  display: flex;
  gap: 4px;
}

.period-selector button {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  background-color: white;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: #616161;
}

.period-selector button:hover {
  background-color: #f5f5f5;
}

.period-selector button.active {
  background-color: #1976d2;
  color: white;
  border-color: #1976d2;
}

.chart-container {
  height: 400px;
  position: relative;
}
</style>
