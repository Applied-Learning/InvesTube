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
                <span>{{ isWished ? '관심 해제' : '관심 종목' }}</span>
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
            <!-- 기본 재무 지표 (항상 표시) -->
            <div class="metric-item" v-if="financialData.revenue">
              <div class="metric-label">매출액</div>
              <div class="metric-value">
                {{ formatBigNumber(financialData.revenue) }}원
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
            
            <div class="metric-item" v-if="financialData.revenueGrowthRate">
              <div class="metric-label">매출 성장률</div>
              <div class="metric-value" :class="getGrowthClass(financialData.revenueGrowthRate)">
                {{ financialData.revenueGrowthRate.toFixed(2) }}%
              </div>
            </div>
            
            <!-- Pre-revenue 기업 안내 (매출 성장률 없고 매출 1억 미만) -->
            <div class="metric-item pre-revenue" v-else-if="!financialData.revenueGrowthRate && financialData.revenue != null && financialData.revenue < 100000000">
              <div class="metric-label">매출 성장률</div>
              <div class="metric-value pre-revenue-badge">
                📊 Pre-revenue
              </div>
              <div class="metric-hint">매출 1억 미만으로 성장률 분석 불가</div>
            </div>
            
            <div class="metric-item" v-if="financialData.debtRatio">
              <div class="metric-label">부채비율</div>
              <div class="metric-value" :class="getDebtClass(financialData.debtRatio)">
                {{ financialData.debtRatio.toFixed(2) }}%
              </div>
            </div>
          </div>
          
          <!-- 상세 보기 토글 버튼 -->
          <button class="toggle-detail-btn" @click="showDetailMetrics = !showDetailMetrics">
            <span>{{ showDetailMetrics ? '상세 지표 접기' : '상세 지표 보기' }}</span>
            <svg 
              :class="{ 'rotated': showDetailMetrics }" 
              width="16" 
              height="16" 
              viewBox="0 0 16 16" 
              fill="currentColor"
            >
              <path d="M8 10.5l-4-4h8l-4 4z"></path>
            </svg>
          </button>
          
          <!-- 상세 재무 지표 (접기/펼치기) -->
          <div v-show="showDetailMetrics" class="metrics-grid detail-metrics">
            <div class="metric-item" v-if="financialData.operatingProfit">
              <div class="metric-label">영업이익</div>
              <div class="metric-value" :class="getProfitClass(financialData.operatingProfit)">
                {{ formatBigNumber(financialData.operatingProfit) }}원
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.netIncome">
              <div class="metric-label">당기순이익</div>
              <div class="metric-value" :class="getProfitClass(financialData.netIncome)">
                {{ formatBigNumber(financialData.netIncome) }}원
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.totalAssets">
              <div class="metric-label">자산총계</div>
              <div class="metric-value">
                {{ formatBigNumber(financialData.totalAssets) }}원
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.totalEquity">
              <div class="metric-label">자본총계</div>
              <div class="metric-value">
                {{ formatBigNumber(financialData.totalEquity) }}원
              </div>
            </div>
            
            <div class="metric-item" v-if="financialData.operatingProfitGrowthRate">
              <div class="metric-label">영업이익 성장률</div>
              <div class="metric-value" :class="getGrowthClass(financialData.operatingProfitGrowthRate)">
                {{ financialData.operatingProfitGrowthRate.toFixed(2) }}%
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

        <!-- AI 분석 섹션 -->
        <div v-if="financialData" class="ai-analysis-section">
          <div class="ai-header">
            <h3>🤖 AI 투자 분석</h3>
            <button 
              class="ai-analyze-button" 
              @click="runAiAnalysis"
              :disabled="aiLoading"
            >
              {{ aiLoading ? 'AI 분석 중...' : 'AI 분석 실행' }}
            </button>
          </div>
          
          <div v-if="aiResult" class="ai-result">
            <div class="ai-score-section">
              <div class="score-comparison">
                <div class="score-item">
                  <div class="score-label">기본 점수</div>
                  <div class="score-value">{{ aiResult.baseScore.toFixed(1) }}</div>
                </div>
                <div class="score-arrow">→</div>
                <div class="score-item final">
                  <div class="score-label">AI 보정 점수</div>
                  <div class="score-value" :class="getScoreClass(aiResult.finalScore)">
                    {{ aiResult.finalScore.toFixed(1) }}
                  </div>
                  <div class="score-adjustment" :class="getAdjustmentClass(aiResult.scoreAdjustment)">
                    {{ formatAdjustment(aiResult.scoreAdjustment) }}
                  </div>
                </div>
              </div>
              
              <div class="risk-level" :class="getRiskClass(aiResult.riskLevel)">
                <span class="risk-label">리스크 레벨:</span>
                <span class="risk-value">{{ getRiskLevelText(aiResult.riskLevel) }}</span>
              </div>
            </div>
            
            <div class="ai-summary">
              <h4>AI 분석 요약</h4>
              <p>{{ aiResult.summary }}</p>
            </div>
            
            <div v-if="hasWeightAdjustments(aiResult.weightAdjustment)" class="weight-adjustments">
              <h4>가중치 보정</h4>
              <div class="adjustment-grid">
                <div 
                  v-for="(value, key) in aiResult.weightAdjustment" 
                  :key="key"
                  v-show="value !== 0"
                  class="adjustment-item"
                >
                  <span class="adjustment-label">{{ getWeightLabel(key) }}</span>
                  <span class="adjustment-value" :class="getAdjustmentClass(value)">
                    {{ formatAdjustment(value) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 동종업계 비교 섹션 -->
            <div v-if="aiResult.peerStats && aiResult.peerStats.peerCount >= 2" class="peer-comparison-section">
              <h4>📊 동종업계 비교 <span class="industry-badge">{{ aiResult.peerStats.industry }} ({{ aiResult.peerStats.peerCount }}개 기업)</span></h4>
              <div class="peer-stats-grid">
                <div class="peer-stat-item" v-if="aiResult.peerStats.avgOperatingMargin">
                  <div class="stat-header">
                    <span class="stat-label">영업이익률</span>
                    <span class="percentile-badge" v-if="aiResult.peerStats.operatingMarginPercentile" :class="getPercentileClass(aiResult.peerStats.operatingMarginPercentile, true)">
                      상위 {{ aiResult.peerStats.operatingMarginPercentile }}%
                    </span>
                  </div>
                  <div class="stat-comparison">
                    <div class="stat-row">
                      <span class="compare-label">업계 평균</span>
                      <span class="compare-value">{{ aiResult.peerStats.avgOperatingMargin.toFixed(1) }}%</span>
                    </div>
                    <div class="stat-row current" v-if="financialData.operatingMargin">
                      <span class="compare-label">현재 기업</span>
                      <span class="compare-value" :class="getCompareClass(financialData.operatingMargin, aiResult.peerStats.avgOperatingMargin, true)">
                        {{ financialData.operatingMargin.toFixed(1) }}%
                      </span>
                    </div>
                  </div>
                </div>
                
                <div class="peer-stat-item" v-if="aiResult.peerStats.avgRoe">
                  <div class="stat-header">
                    <span class="stat-label">ROE</span>
                    <span class="percentile-badge" v-if="aiResult.peerStats.roePercentile" :class="getPercentileClass(aiResult.peerStats.roePercentile, true)">
                      상위 {{ aiResult.peerStats.roePercentile }}%
                    </span>
                  </div>
                  <div class="stat-comparison">
                    <div class="stat-row">
                      <span class="compare-label">업계 평균</span>
                      <span class="compare-value">{{ aiResult.peerStats.avgRoe.toFixed(1) }}%</span>
                    </div>
                    <div class="stat-row current" v-if="financialData.roe">
                      <span class="compare-label">현재 기업</span>
                      <span class="compare-value" :class="getCompareClass(financialData.roe, aiResult.peerStats.avgRoe, true)">
                        {{ financialData.roe.toFixed(1) }}%
                      </span>
                    </div>
                  </div>
                </div>
                
                <div class="peer-stat-item" v-if="aiResult.peerStats.avgDebtRatio">
                  <div class="stat-header">
                    <span class="stat-label">부채비율</span>
                    <span class="percentile-badge" v-if="aiResult.peerStats.debtRatioPercentile" :class="getPercentileClass(aiResult.peerStats.debtRatioPercentile, false)">
                      상위 {{ aiResult.peerStats.debtRatioPercentile }}%
                    </span>
                  </div>
                  <div class="stat-comparison">
                    <div class="stat-row">
                      <span class="compare-label">업계 평균</span>
                      <span class="compare-value">{{ aiResult.peerStats.avgDebtRatio.toFixed(1) }}%</span>
                    </div>
                    <div class="stat-row current" v-if="financialData.debtRatio">
                      <span class="compare-label">현재 기업</span>
                      <span class="compare-value" :class="getCompareClass(financialData.debtRatio, aiResult.peerStats.avgDebtRatio, false)">
                        {{ financialData.debtRatio.toFixed(1) }}%
                      </span>
                    </div>
                  </div>
                </div>
                
                <div class="peer-stat-item" v-if="aiResult.peerStats.avgPer">
                  <div class="stat-header">
                    <span class="stat-label">PER</span>
                    <span class="percentile-badge" v-if="aiResult.peerStats.perPercentile" :class="getPercentileClass(aiResult.peerStats.perPercentile, false)">
                      상위 {{ aiResult.peerStats.perPercentile }}%
                    </span>
                  </div>
                  <div class="stat-comparison">
                    <div class="stat-row">
                      <span class="compare-label">업계 평균</span>
                      <span class="compare-value">{{ aiResult.peerStats.avgPer.toFixed(1) }}배</span>
                    </div>
                    <div class="stat-row current" v-if="financialData.perRatio">
                      <span class="compare-label">현재 기업</span>
                      <span class="compare-value" :class="getCompareClass(financialData.perRatio, aiResult.peerStats.avgPer, false)">
                        {{ financialData.perRatio.toFixed(1) }}배
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="ai-prompt">
            <p>AI를 활용하여 재무 데이터를 심층 분석하고 투자 인사이트를 얻어보세요.</p>
          </div>
        </div>



        <!-- 관련 기사 -->
        <div class="related-news">
          <div class="related-news-header">
            <h3>관련 기사</h3>
            <span v-if="stock" class="related-news-subtitle">"{{ stock.stockName }}" 관련 뉴스</span>
            <div class="news-actions">
              <button class="news-action-btn" :disabled="newsLoading" @click="refreshNews">새로고침</button>
              <button
                v-if="stock"
                class="news-action-btn"
                type="button"
                @click="openNaverNews"
              >
                더 보기 (네이버)
              </button>
            </div>
          </div>

          <div v-if="newsLoading" class="news-placeholder">
            <p>관련 기사를 불러오는 중입니다...</p>
          </div>
          <div v-else-if="newsError" class="news-placeholder error">
            <p>{{ newsError }}</p>
            <Button @click="loadStockNews">다시 불러오기</Button>
          </div>
          <div v-else-if="newsArticles.length" class="news-list">
            <article
              v-for="article in newsArticles"
              :key="article.link || article.title"
              class="news-card"
            >
              <a
                :href="article.link || article.originalLink"
                target="_blank"
                rel="noopener noreferrer"
              >
                <div class="news-meta">
                  <span class="news-source">{{ article.source || 'Naver News' }}</span>
                  <span v-if="article.publishedAt" class="news-date">{{ formatNewsDate(article.publishedAt) }}</span>
                </div>
                <h4 class="news-title">{{ article.title }}</h4>
                <p class="news-summary">{{ article.summary }}</p>
              </a>
            </article>
          </div>
          <div v-else class="news-placeholder">
            <p>안내 가능한 관련 기사가 없어요.</p>
            <Button @click="loadStockNews">새로고침</Button>
          </div>
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
import { getFinancialData, syncFinancialData as syncFinancialDataAPI, getAiAnalysis } from '@/api/financial'
import profileApi from '@/api/profile'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { useChatbotStore } from '@/stores/chatbot'

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
    const toastStore = useToastStore()
    const chatbotStore = useChatbotStore()
    return { authStore, toastStore, chatbotStore }
  },
  data() {
    return {
      stock: null,
      priceHistory: [],
      financialData: null,
      aiResult: null,
      currentProfile: null,
      loading: false,
      error: null,
      isWished: false,
      wishLoading: false,
      syncLoading: false,
      aiLoading: false,
      newsArticles: [],
      newsLoading: false,
      newsError: null,
      newsLimit: 6,
      showDetailMetrics: false,
      // 챗봇 관련
      chatHistory: [],
      chatInput: '',
      chatLoading: false,
      exampleQuestions: [
        '이 종목 점수 왜 이래?',
        '재무적으로 가장 안 좋은 지표 뭐야?',
        '안정형 투자자한테 괜찮아?',
        '리스크 요약해줘'
      ]
    }
  },
  computed: {
    stockCode() {
      return this.$route.params.stockCode
    },
    naverNewsUrl() {
      if (!this.stock?.stockName) return '#'
      const query = encodeURIComponent(`${this.stock.stockName}`)
      // 네이버 기본 검색
      return `https://search.naver.com/search.naver?where=news&query=${query}`
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
    this.loadProfile() // 프로필 먼저 로드 후 재무 데이터 로드
    this.loadFinancialData()
    this.loadStockNews()
    this.checkWishStatus()
  },
  mounted() {
    // 종목 정보가 로드되면 챗봇 스토어에 알림
    this.$watch('stock', (newStock) => {
      if (newStock) {
        this.chatbotStore.setCurrentStock(newStock)
      }
    })
  },
  beforeUnmount() {
    // 페이지 떠날 때 챗봇 스토어 초기화
    this.chatbotStore.clearCurrentStock()
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
    async loadProfile() {
      try {
        const response = await profileApi.getDefaultProfile()
        this.currentProfile = response.data
        // 프로필 로드 후 재무 데이터 로드
        this.loadFinancialData()
      } catch (err) {
        console.error('프로필 조회 실패:', err)
        this.currentProfile = null
        // 프로필 없어도 재무 데이터 로드 (기본값 사용)
        this.loadFinancialData()
      }
    },
    async loadFinancialData() {
      try {
        const profileId = this.currentProfile?.profileId || null
        const response = await getFinancialData(this.stockCode, profileId)
        this.financialData = response.data
      } catch (err) {
        console.error('재무 데이터 조회 실패:', err)
        this.financialData = null
      }
    },
    async loadStockNews(limit = this.newsLimit) {
      this.newsLimit = limit
      this.newsLoading = true
      this.newsError = null

      try {
        const response = await stockApi.getStockNews(this.stockCode, this.newsLimit)
        this.newsArticles = response.data || []
      } catch (err) {
        console.error('관련 기사 조회 실패:', err)
        this.newsArticles = []
        this.newsError = '관련 기사를 불러오지 못했습니다.'
      } finally {
        this.newsLoading = false
      }
    },
    refreshNews() {
      this.loadStockNews(this.newsLimit)
    },
    openNaverNews() {
      if (this.naverNewsUrl && this.naverNewsUrl !== '#') {
        window.open(this.naverNewsUrl, '_blank', 'noopener')
      }
    },
    async syncFinancialData() {
      this.syncLoading = true
      try {
        const currentYear = new Date().getFullYear() - 1 // 작년 데이터 사용 (2024년)
        
        // 매출 성장률 계산을 위해 전년도 먼저 동기화
        try {
          await syncFinancialDataAPI(this.stockCode, currentYear - 1)
        } catch (prevYearErr) {
          console.warn('전년도 데이터 동기화 실패 (매출 성장률은 계산되지 않을 수 있습니다):', prevYearErr)
        }
        
        // 현재 연도 동기화 (전년도 데이터가 있으면 성장률 자동 계산)
        await syncFinancialDataAPI(this.stockCode, currentYear)
        
        this.toastStore.show('재무 데이터 동기화가 완료되었습니다.', { type: 'success' })
        // 동기화 후 데이터 다시 로드
        await this.loadFinancialData()
      } catch (err) {
        console.error('재무 데이터 동기화 실패:', err)
        const errorMsg = err.response?.data?.error || '재무 데이터 동기화에 실패했습니다.'
        const hint = 'DART에 재무제표가 없거나 아직 공시되지 않았습니다.'
        // 상세 오류는 콘솔로, 사용자에게는 간략 힌트만 노출
        this.toastStore.show(hint, { type: 'error', duration: 5000 })
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
        console.error('관심 상태 확인 실패:', err)
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
        console.error('관심 종목 처리 실패:', err)
        alert('관심 종목 처리에 실패했습니다.')
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
    getProfitClass(value) {
      if (value > 0) return 'positive'
      if (value < 0) return 'negative'
      return 'neutral'
    },
    formatBigNumber(num) {
      const absNum = Math.abs(num)
      const sign = num < 0 ? '-' : ''
      
      if (absNum >= 1000000000000) {  // 1조 이상
        return sign + (absNum / 1000000000000).toFixed(2) + '조'
      }
      if (absNum >= 100000000) {  // 1억 이상
        return sign + (absNum / 100000000).toFixed(0) + '억'
      }
      if (absNum >= 10000) {  // 1만 이상
        return sign + (absNum / 10000).toFixed(0) + '만'
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
    formatNewsDate(value) {
      if (!value) return ''
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      return date.toLocaleString('ko-KR', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    },
    async runAiAnalysis() {
      if (!this.financialData) {
        alert('재무 데이터를 먼저 불러와주세요.')
        return
      }
      
      this.aiLoading = true
      try {
        const response = await getAiAnalysis(this.stockCode)
        this.aiResult = response.data
      } catch (err) {
        console.error('AI 분석 실패:', err)
        alert('AI 분석에 실패했습니다. 잠시 후 다시 시도해주세요.')
      } finally {
        this.aiLoading = false
      }
    },
    formatAdjustment(value) {
      if (!value) return '0'
      const num = Number(value)
      return num > 0 ? '+' + num.toFixed(1) : num.toFixed(1)
    },
    getAdjustmentClass(value) {
      if (value > 0) return 'positive'
      if (value < 0) return 'negative'
      return 'neutral'
    },
    getRiskClass(level) {
      return 'risk-' + level.toLowerCase()
    },
    getRiskLevelText(level) {
      const levels = {
        'LOW': '낮음',
        'MEDIUM': '보통',
        'HIGH': '높음'
      }
      return levels[level] || level
    },
    hasWeightAdjustments(weightAdj) {
      if (!weightAdj) return false
      return Object.values(weightAdj).some(v => v !== 0)
    },
    getWeightLabel(key) {
      const labels = {
        'revenueGrowth': '매출 성장',
        'operatingMargin': '영업이익률',
        'roe': 'ROE',
        'debtRatio': '부채비율',
        'per': 'PER',
        'pbr': 'PBR'
      }
      return labels[key] || key
    },
    // 동종업계 비교 관련 메서드
    getPercentileClass(percentile, higherIsBetter) {
      // higherIsBetter = true면 상위 %, false면 하위 %
      if (percentile <= 20) return 'percentile-excellent'
      if (percentile <= 40) return 'percentile-good'
      if (percentile <= 60) return 'percentile-average'
      return 'percentile-poor'
    },
    getCompareClass(current, average, higherIsBetter) {
      if (current == null || average == null) return ''
      const diff = current - average
      if (higherIsBetter) {
        // 높을수록 좋음 (영업이익률, ROE)
        if (diff > 0) return 'above-average'
        if (diff < 0) return 'below-average'
      } else {
        // 낮을수록 좋음 (부채비율, PER)
        if (diff < 0) return 'above-average'
        if (diff > 0) return 'below-average'
      }
      return ''
    },
    // 챗봇 관련 메서드
    async sendMessage() {
      if (!this.chatInput.trim()) return
      
      const userMessage = this.chatInput.trim()
      this.chatInput = ''
      
      // 사용자 메시지 추가
      this.chatHistory.push({
        role: 'user',
        content: userMessage
      })
      
      this.chatLoading = true
      this.scrollToBottom()
      
      try {
        const response = await chatAboutStock(this.stockCode, userMessage)
        
        // AI 응답 추가
        this.chatHistory.push({
          role: 'assistant',
          content: response.data.message
        })
      } catch (err) {
        console.error('챗봇 응답 실패:', err)
        let errorMessage = '죄송합니다. 응답 생성에 실패했습니다.'
        
        // 상세 에러 메시지 표시
        if (err.response?.data?.message) {
          errorMessage = err.response.data.message
        } else if (err.message) {
          errorMessage += ' (' + err.message + ')'
        }
        
        this.chatHistory.push({
          role: 'assistant',
          content: errorMessage
        })
      } finally {
        this.chatLoading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },
    askQuestion(question) {
      this.chatInput = question
      this.sendMessage()
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const chatMessages = this.$refs.chatMessages
        if (chatMessages) {
          chatMessages.scrollTop = chatMessages.scrollHeight
        }
      })
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
  background: #fff;
  border: 2px solid #e5e5e5;
  border-radius: 6px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
}

.wish-button:hover {
  border-color: #e53935;
  background: #fff4f4;
  color: #c62828;
}

.wish-button.wished {
  background: #ef5350;
  border-color: #c62828;
  color: #fff;
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

.related-news {
  background: white;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e0e0e0;
}

.related-news-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  }

.related-news h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.related-news-subtitle {
  color: #6b7280;
  font-size: 14px;
}

.news-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-wrap: wrap;
}

.news-action-btn {
  padding: 8px 14px;
  border: 1px solid #dbeafe;
  background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%);
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.15);
}

.news-action-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.2);
}

.news-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.news-more-link,
.news-more-link:visited,
.news-more-link:hover,
.news-more-link:focus,
.news-more-link:active {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #1d4ed8;
  text-decoration: none !important;
  text-decoration-line: none !important;
  text-decoration-color: transparent !important;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.news-more-link:hover,
.news-more-link:focus,
.news-more-link:active {
  background: #e0f2fe;
  border-color: #bfdbfe;
}

.news-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.news-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.2s;
}

.news-card a {
  display: block;
  padding: 16px;
  text-decoration: none;
  color: inherit;
  height: 100%;
}

.news-card:hover {
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.news-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  gap: 10px;
}

.news-source {
  background: #eef2ff;
  color: #4338ca;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 700;
}

.news-date {
  color: #9ca3af;
}

.news-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  line-height: 1.4;
}

.news-summary {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #4b5563;
}

.news-placeholder {
  text-align: center;
  padding: 32px 16px;
  color: #6b7280;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
}

.news-placeholder.error {
  color: #b91c1c;
  background: #fef2f2;
  border-color: #fecdd3;
}

.news-placeholder p {
  margin: 0 0 12px 0;
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

/* Pre-revenue 기업 스타일 */
.metric-item.pre-revenue {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border: 1px solid #f59e0b;
}

.metric-value.pre-revenue-badge {
  font-size: 16px;
  color: #92400e;
  font-weight: 600;
}

.metric-hint {
  font-size: 11px;
  color: #b45309;
  margin-top: 4px;
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

/* AI 분석 스타일 */
.ai-analysis-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-top: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.ai-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.ai-analyze-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-analyze-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.ai-analyze-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-result {
  margin-top: 20px;
}

.ai-score-section {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.score-comparison {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 16px;
}

.score-item {
  text-align: center;
  color: white;
}

.score-item.final {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 16px;
}

.score-item .score-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.score-item .score-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.score-adjustment {
  font-size: 16px;
  font-weight: 600;
  margin-top: 4px;
}

.score-adjustment.positive {
  color: #4ade80;
}

.score-adjustment.negative {
  color: #fca5a5;
}

.score-arrow {
  font-size: 32px;
  color: white;
  opacity: 0.8;
}

.risk-level {
  text-align: center;
  padding: 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 14px;
}

.risk-level .risk-label {
  opacity: 0.9;
  margin-right: 8px;
}

.risk-level .risk-value {
  font-weight: 600;
  font-size: 16px;
}

.risk-level.risk-low {
  background: rgba(74, 222, 128, 0.2);
}

.risk-level.risk-high {
  background: rgba(239, 68, 68, 0.2);
}

.ai-summary {
  background: #f9fafb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border-left: 4px solid #667eea;
}

.ai-summary h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.ai-summary p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

.weight-adjustments {
  background: #f3f4f6;
  border-radius: 8px;
  padding: 20px;
}

.weight-adjustments h4 {
  margin: 0 0 16px 0;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.adjustment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.adjustment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 12px;
  border-radius: 6px;
  font-size: 13px;
}

.adjustment-label {
  color: #6b7280;
  font-weight: 500;
}

.adjustment-value {
  font-weight: 600;
  font-size: 14px;
}

.adjustment-value.positive {
  color: #16a34a;
}

.adjustment-value.negative {
  color: #dc2626;
}

.adjustment-value.neutral {
  color: #6b7280;
}

/* 동종업계 비교 스타일 */
.peer-comparison-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  margin-top: 16px;
}

.peer-comparison-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
}

.industry-badge {
  font-size: 12px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 10px;
  border-radius: 20px;
}

.peer-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.peer-stat-item {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 14px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.percentile-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 12px;
}

.percentile-excellent {
  background: #dcfce7;
  color: #166534;
}

.percentile-good {
  background: #dbeafe;
  color: #1e40af;
}

.percentile-average {
  background: #fef3c7;
  color: #92400e;
}

.percentile-poor {
  background: #fee2e2;
  color: #991b1b;
}

.stat-comparison {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.stat-row.current {
  padding-top: 6px;
  border-top: 1px solid #e5e7eb;
}

.compare-label {
  color: #6b7280;
}

.compare-value {
  font-weight: 600;
  color: #374151;
}

.compare-value.above-average {
  color: #16a34a;
}

.compare-value.below-average {
  color: #dc2626;
}

.ai-prompt {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 14px;
}

/* 상세 지표 토글 스타일 */
.toggle-detail-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  margin: 16px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-detail-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.toggle-detail-btn svg {
  transition: transform 0.3s ease;
}

.toggle-detail-btn svg.rotated {
  transform: rotate(180deg);
}

/* 상세 지표 애니메이션 */
.detail-metrics {
  animation: slideDown 0.3s ease-out;
  margin-top: 0;
  padding-top: 0;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 상세 지표 스타일 강조 */
.detail-metrics .metric-item {
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.detail-metrics .metric-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-color: #d1d5db;
}

/* 챗봇 섹션 스타일 */
.chatbot-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-top: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chatbot-header {
  margin-bottom: 20px;
}

.chatbot-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.chatbot-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.chat-container {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.chat-messages {
  height: 400px;
  overflow-y: auto;
  padding: 20px;
  background: #f9fafb;
}

.chat-welcome {
  text-align: center;
  color: #6b7280;
  padding: 20px;
}

.chat-welcome p {
  margin-bottom: 20px;
  font-size: 15px;
}

.chat-examples {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
}

.examples-title {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 12px;
  text-align: left;
}

.example-btn {
  display: block;
  width: 100%;
  text-align: left;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.example-btn:hover {
  background: #f3f4f6;
  border-color: #667eea;
  color: #667eea;
}

.example-btn:last-child {
  margin-bottom: 0;
}

.chat-message {
  margin-bottom: 16px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-content {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-avatar {
  font-size: 24px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-text {
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  max-width: 80%;
  word-wrap: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  line-height: 1.6;
  color: #374151;
}

.chat-message.user .message-content {
  flex-direction: row-reverse;
}

.chat-message.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.chat-message.assistant .message-text {
  background: white;
  border: 1px solid #e5e7eb;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input-container {
  display: flex;
  gap: 8px;
  padding: 16px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.chat-input:focus {
  border-color: #667eea;
}

.chat-input:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
}

.send-button {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
