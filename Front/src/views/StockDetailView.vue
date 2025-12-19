<template>
  <div class="stock-detail-view">
    <PageHeader :title="stock ? stock.stockName : '주식 상세'" />
    
    <Container>
      <div v-if="loading" class="loading">
        <p>로딩 중...</p>
      </div>
      
      <div v-else-if="error" class="error">
        <p>{{ error }}</p>
        <Button @click="loadStockDetail">다시 시도</Button>
      </div>
      
      <div v-else-if="stock" class="stock-detail">
        <!-- 기본 정보 -->
        <div class="stock-info-card">
          <div class="info-header">
            <div>
              <h2>{{ stock.stockName }}</h2>
              <span class="stock-code">{{ stock.stockCode }}</span>
            </div>
            <span class="market-badge" :class="marketClass">{{ stock.market }}</span>
          </div>
          
          <div class="price-info">
            <div class="current-price">
              {{ formatPrice(stock.closePrice) }}원
            </div>
            <div class="price-change" :class="priceChangeClass">
              <span class="change-amount">{{ formatPriceChange(stock.priceChange) }}</span>
              <span class="change-rate">{{ formatRate(stock.priceChangeRate) }}%</span>
            </div>
          </div>
          
          <div class="stock-details-grid">
            <div class="detail-item">
              <span class="label">시가</span>
              <span class="value">{{ formatPrice(stock.openPrice) }}원</span>
            </div>
            <div class="detail-item">
              <span class="label">고가</span>
              <span class="value">{{ formatPrice(stock.highPrice) }}원</span>
            </div>
            <div class="detail-item">
              <span class="label">저가</span>
              <span class="value">{{ formatPrice(stock.lowPrice) }}원</span>
            </div>
            <div class="detail-item">
              <span class="label">거래량</span>
              <span class="value">{{ formatVolume(stock.volume) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">시가총액</span>
              <span class="value">{{ formatMarketCap(stock.marketCap) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">업종</span>
              <span class="value">{{ stock.industry }}</span>
            </div>
          </div>
          
          <div class="trade-date">
            기준일: {{ formatDate(stock.tradeDate) }}
          </div>
        </div>
        
        <!-- 차트 -->
        <div class="chart-section">
          <StockChart 
            :stock-code="stockCode"
            :price-data="priceHistory"
            :title="`${stock.stockName} 주가 차트`"
            @period-change="handlePeriodChange"
          />
        </div>
        
        <!-- 관련 영상 섹션 (추후 구현) -->
        <div class="related-videos">
          <h3>관련 투자 영상</h3>
          <p class="coming-soon">준비 중입니다</p>
        </div>
      </div>
    </Container>
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue'
import Container from '@/components/common/Container.vue'
import Button from '@/components/common/Button.vue'
import StockChart from '@/components/stock/StockChart.vue'
import stockApi from '@/api/stock'

export default {
  name: 'StockDetailView',
  components: {
    PageHeader,
    Container,
    Button,
    StockChart
  },
  data() {
    return {
      stock: null,
      priceHistory: [],
      loading: false,
      error: null
    }
  },
  computed: {
    stockCode() {
      return this.$route.params.stockCode
    },
    marketClass() {
      return this.stock?.market === 'KOSPI' ? 'market-kospi' : 'market-kosdaq'
    },
    priceChangeClass() {
      if (!this.stock?.priceChange) return ''
      return this.stock.priceChange > 0 ? 'price-up' : this.stock.priceChange < 0 ? 'price-down' : ''
    }
  },
  created() {
    this.loadStockDetail()
    this.loadPriceHistory()
  },
  methods: {
    async loadStockDetail() {
      this.loading = true
      this.error = null
      
      try {
        const response = await stockApi.getStock(this.stockCode)
        this.stock = response.data
      } catch (err) {
        console.error('주식 상세 조회 실패:', err)
        this.error = '주식 정보를 불러오는데 실패했습니다.'
      } finally {
        this.loading = false
      }
    },
    async loadPriceHistory() {
      try {
        const response = await stockApi.getStockPrices(this.stockCode)
        this.priceHistory = response.data
      } catch (err) {
        console.error('주가 이력 조회 실패:', err)
      }
    },
    handlePeriodChange(period) {
      console.log('기간 변경:', period)
      // 필요시 특정 기간 데이터만 다시 로드
    },
    formatPrice(price) {
      if (!price) return '-'
      return Number(price).toLocaleString('ko-KR')
    },
    formatPriceChange(change) {
      if (!change) return '-'
      const num = Number(change)
      return num > 0 ? `+${num.toLocaleString('ko-KR')}` : num.toLocaleString('ko-KR')
    },
    formatRate(rate) {
      if (!rate) return '-'
      const num = Number(rate)
      return num > 0 ? `+${num.toFixed(2)}` : num.toFixed(2)
    },
    formatVolume(volume) {
      if (!volume) return '-'
      const num = Number(volume)
      if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + '백만'
      } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + '천'
      }
      return num.toLocaleString('ko-KR')
    },
    formatMarketCap(cap) {
      if (!cap) return '-'
      const num = Number(cap)
      if (num >= 1000000000000) {
        return (num / 1000000000000).toFixed(1) + '조'
      } else if (num >= 100000000) {
        return (num / 100000000).toFixed(1) + '억'
      }
      return num.toLocaleString('ko-KR')
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleDateString('ko-KR')
    }
  }
}
</script>

<style scoped>
.stock-detail-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stock-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stock-info-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e0e0e0;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.info-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  color: #212121;
}

.stock-code {
  font-size: 14px;
  color: #757575;
}

.market-badge {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.market-kospi {
  background-color: #e3f2fd;
  color: #1976d2;
}

.market-kosdaq {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.price-info {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f5f5f5;
}

.current-price {
  font-size: 36px;
  font-weight: 700;
  color: #212121;
  margin-bottom: 8px;
}

.price-change {
  font-size: 18px;
  font-weight: 500;
}

.price-up {
  color: #d32f2f;
}

.price-down {
  color: #1976d2;
}

.change-amount {
  margin-right: 12px;
}

.stock-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 13px;
  color: #757575;
}

.detail-item .value {
  font-size: 16px;
  font-weight: 500;
  color: #424242;
}

.trade-date {
  text-align: right;
  font-size: 12px;
  color: #9e9e9e;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f5f5f5;
}

.chart-section {
  background: white;
  border-radius: 8px;
  padding: 0;
  overflow: hidden;
}

.related-videos {
  background: white;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e0e0e0;
}

.related-videos h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.coming-soon {
  text-align: center;
  padding: 40px;
  color: #9e9e9e;
}

.loading,
.error {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
}

.loading p,
.error p {
  color: #757575;
  font-size: 16px;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .info-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .current-price {
    font-size: 28px;
  }
  
  .price-change {
    font-size: 16px;
  }
  
  .stock-details-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
