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
            <div class="header-actions">
              <span class="market-badge" :class="marketClass">{{ stock.market }}</span>
              <button 
                class="wish-button" 
                :class="{ 'wished': isWished }"
                @click="toggleWish"
                :disabled="wishLoading"
              >
                <svg 
                  width="24" 
                  height="24" 
                  viewBox="0 0 24 24" 
                  fill="none" 
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path 
                    d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                    :fill="isWished ? 'currentColor' : 'none'"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                </svg>
                <span>{{ isWished ? '찜 취소' : '찜하기' }}</span>
              </button>
            </div>
          </div>

          <div class="price-info">
            <div class="current-price">{{ formatPrice(stock.closePrice) }}원</div>
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

          <div class="trade-date">기준일: {{ formatDate(stock.tradeDate) }}</div>
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

        <!-- 재무 지표 섹션 -->
        <div class="financial-section" v-if="financialData">
          <div class="financial-header">
            <h3>재무 지표 <span class="fiscal-year">({{ financialData.fiscalYear }}년)</span></h3>
            <button 
              class="sync-button" 
              @click="syncFinancialData"
              :disabled="syncLoading"
            >
              {{ syncLoading ? '동기화 중...' : '데이터 갱신' }}
            </button>
          </div>
          
          <div v-if="financialData.totalScore" class="score-card">
            <div class="score-label">투자 점수</div>
            <div class="score-value" :class="getScoreClass(financialData.totalScore)">
              {{ financialData.totalScore.toFixed(1) }}
            </div>
            <div class="score-description">{{ getScoreDescription(financialData.totalScore) }}</div>
          </div>

          <div class="metrics-grid">
            <div class="metric-item" v-if="financialData.revenueGrowthRate">
              <div class="metric-label">매출 성장률</div>
              <div class="metric-value" :class="getGrowthClass(financialData.revenueGrowthRate)">
                {{ financialData.revenueGrowthRate.toFixed(2) }}%
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.operatingMargin">
              <div class="metric-label">영업이익률</div>
              <div class="metric-value" :class="getGrowthClass(financialData.operatingMargin)">
                {{ financialData.operatingMargin.toFixed(2) }}%
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.roe">
              <div class="metric-label">ROE</div>
              <div class="metric-value" :class="getGrowthClass(financialData.roe)">
                {{ financialData.roe.toFixed(2) }}%
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.debtRatio">
              <div class="metric-label">부채비율</div>
              <div class="metric-value" :class="getDebtClass(financialData.debtRatio)">
                {{ financialData.debtRatio.toFixed(2) }}%
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.fcf">
              <div class="metric-label">잉여현금흐름 (FCF)</div>
              <div class="metric-value">
                {{ formatBigNumber(financialData.fcf) }}원
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.perRatio">
              <div class="metric-label">PER</div>
              <div class="metric-value">
                {{ financialData.perRatio.toFixed(2) }}
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.pbrRatio">
              <div class="metric-label">PBR</div>
              <div class="metric-value">
                {{ financialData.pbrRatio.toFixed(2) }}
              </div>
            </div>
          </div>
          
          <div class="data-source">
            데이터 출처: {{ financialData.dataSource || 'DART' }}
          </div>
        </div>
        
        <div class="financial-section" v-else>
          <div class="financial-header">
            <h3>재무 지표</h3>
            <button 
              class="sync-button primary" 
              @click="syncFinancialData"
              :disabled="syncLoading"
            >
              {{ syncLoading ? '동기화 중...' : 'DART 데이터 가져오기' }}
            </button>
          </div>
          <div class="no-financial-data">
            <p>재무 데이터가 없습니다.</p>
            <p class="hint">위 버튼을 클릭하여 DART API에서 최신 재무 데이터를 가져올 수 있습니다.</p>
          </div>
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
import { formatKSTDate } from '@/utils/date.js'
import Container from '@/components/common/Container.vue'
import Button from '@/components/common/Button.vue'
import StockChart from '@/components/stock/StockChart.vue'
import stockApi from '@/api/stock'
import { isStockWished, addStockWish, removeStockWish } from '@/api/stockWish'
import { getFinancialData, syncFinancialData as syncFinancialDataAPI } from '@/api/financial'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'StockDetailView',
  components: {
    PageHeader,
    Container,
    Button,
    StockChart,
  },
  setup() {
    const authStore = useAuthStore()
    return { authStore }
  },
  data() {
    return {
      stock: null,
      priceHistory: [],
      financialData: null,
      loading: false,
      error: null,
      isWished: false,
      wishLoading: false,
      syncLoading: false,
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
      return this.stock.priceChange > 0
        ? 'price-up'
        : this.stock.priceChange < 0
          ? 'price-down'
          : ''
    },
  },
  created() {
    this.loadStockDetail()
    this.loadPriceHistory()
    this.loadFinancialData()
    this.checkWishStatus()
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
    async loadFinancialData() {
      try {
        const response = await getFinancialData(this.stockCode)
        this.financialData = response.data
      } catch (err) {
        console.error('재무 데이터 조회 실패:', err)
        this.financialData = null
      }
    },
    async syncFinancialData() {
      this.syncLoading = true
      try {
        const currentYear = new Date().getFullYear() - 1 // 작년 데이터 사용 (2024년)
        await syncFinancialDataAPI(this.stockCode, currentYear)
        alert('재무 데이터 동기화가 완료되었습니다.')
        // 동기화 후 데이터 다시 로드
        await this.loadFinancialData()
      } catch (err) {
        console.error('재무 데이터 동기화 실패:', err)
        const errorMsg = err.response?.data?.error || '재무 데이터 동기화에 실패했습니다.'
        alert(`${errorMsg}\n\nDart에 해당 기업의 재무제표가 없거나, 아직 공시되지 않았을 수 있습니다.`)
      } finally {
        this.syncLoading = false
      }
    },
    async checkWishStatus() {
      if (!this.authStore.isAuthenticated) {
        return
      }
      
      try {
        const response = await isStockWished(this.stockCode)
        this.isWished = response.data
      } catch (err) {
        console.error('찜 상태 확인 실패:', err)
        // 인증 에러여도 페이지는 유지
        this.isWished = false
      }
    },
    async toggleWish() {
      if (!this.authStore.isAuthenticated) {
        alert('로그인이 필요합니다.')
        this.$router.push('/login')
        return
      }

      this.wishLoading = true
      try {
        if (this.isWished) {
          await removeStockWish(this.stockCode)
          this.isWished = false
        } else {
          await addStockWish(this.stockCode)
          this.isWished = true
        }
      } catch (err) {
        console.error('찜 처리 실패:', err)
        alert('찜 처리에 실패했습니다.')
      } finally {
        this.wishLoading = false
      }
    },
    handlePeriodChange(period) {
      console.log('기간 변경:', period)
      // 필요시 특정 기간 데이터만 다시 로드
    },
    getScoreClass(score) {
      if (score >= 80) return 'excellent'
      if (score >= 60) return 'good'
      if (score >= 40) return 'average'
      return 'poor'
    },
    getScoreDescription(score) {
      if (score >= 80) return '매우 우수'
      if (score >= 60) return '양호'
      if (score >= 40) return '보통'
      return '미흡'
    },
    getGrowthClass(value) {
      if (value >= 10) return 'positive'
      if (value >= 0) return 'neutral'
      return 'negative'
    },
    getDebtClass(ratio) {
      if (ratio <= 100) return 'good'
      if (ratio <= 200) return 'average'
      return 'poor'
    },
    formatBigNumber(num) {
      if (num >= 100000000) {
        return (num / 100000000).toFixed(0) + '억'
      }
      if (num >= 10000) {
        return (num / 10000).toFixed(0) + '만'
      }
      return num.toLocaleString()
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
      return formatKSTDate(date)
    },
  },
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.wish-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  color: #757575;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
}

.wish-button:hover {
  border-color: #1976d2;
  color: #1976d2;
}

.wish-button.wished {
  background: #e3f2fd;
  border-color: #1976d2;
  color: #1976d2;
}

.wish-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.wish-button svg {
  width: 20px;
  height: 20px;
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

/* 재무 지표 섹션 */
.financial-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e0e0e0;
  margin-top: 24px;
}

.financial-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.financial-section h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.sync-button {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.sync-button:hover:not(:disabled) {
  background: #f5f5f5;
  border-color: #1976d2;
  color: #1976d2;
}

.sync-button.primary {
  background: #1976d2;
  color: white;
  border-color: #1976d2;
}

.sync-button.primary:hover:not(:disabled) {
  background: #1565c0;
}

.sync-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.fiscal-year {
  font-size: 14px;
  color: #757575;
  font-weight: 400;
}

.score-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  color: white;
  margin-bottom: 24px;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.score-value {
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.score-description {
  font-size: 16px;
  opacity: 0.9;
}

.score-value.excellent {
  color: #fff;
}

.score-value.good {
  color: #f0f0f0;
}

.score-value.average {
  color: #e0e0e0;
}

.score-value.poor {
  color: #d0d0d0;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.metric-item {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.metric-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.metric-value.positive {
  color: #dc2626;
}

.metric-value.negative {
  color: #2563eb;
}

.metric-value.neutral {
  color: #6b7280;
}

.metric-value.good {
  color: #16a34a;
}

.metric-value.average {
  color: #ea580c;
}

.metric-value.poor {
  color: #dc2626;
}

.data-source {
  text-align: right;
  font-size: 12px;
  color: #9ca3af;
  margin-top: 16px;
}

.no-financial-data {
  text-align: center;
  padding: 40px;
  color: #9e9e9e;
}

.no-financial-data p {
  margin: 0 0 8px 0;
}

.no-financial-data .hint {
  font-size: 13px;
  color: #bdbdbd;
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
