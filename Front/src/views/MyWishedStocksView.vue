<template>
  <div class="wished-stocks-view">
    <PageHeader title="관심 종목" />

    <Container>
      <div v-if="loading" class="loading">
        <p>로딩 중...</p>
      </div>

      <div v-else-if="error" class="error">
        <p>{{ error }}</p>
        <Button @click="loadWishedStocks">다시 시도</Button>
      </div>

      <div v-else-if="wishedStocks.length === 0" class="empty">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" 
                stroke="#ccc" 
                stroke-width="2" 
                fill="none"/>
        </svg>
        <p>관심 종목이 없습니다</p>
        <router-link to="/invest">
          <Button>둘러보기</Button>
        </router-link>
      </div>

      <div v-else>
        <div v-if="hasMissingScore" class="missing-score-alert">
          DART에 재무제표가 없는 일부 종목은 점수를 제공하지 않습니다.
        </div>
        <div class="controls">
          <div class="sort-buttons">
            <button
              type="button"
              class="sort-btn"
              :class="{ active: sortBy === 'recent' }"
              @click="sortBy = 'recent'"
            >
              최근 관심 순
            </button>
            <button
              type="button"
              class="sort-btn"
              :class="{ active: sortBy === 'scoreHigh' }"
              @click="sortBy = 'scoreHigh'"
            >
              점수 높은 순
            </button>
            <button
              type="button"
              class="sort-btn"
              :class="{ active: sortBy === 'scoreLow' }"
              @click="sortBy = 'scoreLow'"
            >
              점수 낮은 순
            </button>
            <button
              type="button"
              class="sort-btn"
              :class="{ active: sortBy === 'name' }"
              @click="sortBy = 'name'"
            >
              이름 순
            </button>
          </div>
        </div>

        <div class="wished-stocks-grid">
          <StockCard
            v-for="stock in sortedStocks"
            :key="stock.stockCode"
            :stock="stock"
            :score="stock.totalScore"
            :showWishToggle="true"
            :wished="true"
            @toggleWish="() => removeWish(stock.stockCode)"
            @select="(code) => goToStockDetail(code)"
          />
        </div>
      </div>
    </Container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import Container from '@/components/common/Container.vue'
import Button from '@/components/common/Button.vue'
import StockCard from '@/components/stock/StockCard.vue'
import { getWishedStocks, removeStockWish } from '@/api/stockWish'
import { getWishedStocksWithScores, syncFinancialData } from '@/api/financial'
import { formatKSTDate } from '@/utils/date'

const router = useRouter()

const wishedStocks = ref([])
const financialLoading = ref(false)
const scoresLoaded = ref(false)
const sortBy = ref('recent')
const loading = ref(false)
const error = ref(null)
const hasMissingScore = computed(() =>
  wishedStocks.value.some((s) => s.totalScore === null || s.totalScore === undefined),
)

const sortedStocks = computed(() => {
  const stocks = [...wishedStocks.value]
  
  switch (sortBy.value) {
    case 'scoreHigh':
      return stocks.sort((a, b) => {
        const as = a.totalScore ?? -Infinity
        const bs = b.totalScore ?? -Infinity
        return bs - as
      })
    case 'scoreLow':
      return stocks.sort((a, b) => {
        const as = a.totalScore ?? Infinity
        const bs = b.totalScore ?? Infinity
        return as - bs
      })
    case 'name':
      return stocks.sort((a, b) => a.stockName.localeCompare(b.stockName))
    case 'recent':
    default:
      return stocks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  }
})

const loadWishedStocks = async () => {
  loading.value = true
  error.value = null

  try {
    const response = await getWishedStocks()
    wishedStocks.value = response.data
    // 관심 종목 로드 후 점수도 함께 조회
    await loadFinancialScores()
  } catch (err) {
    console.error('찜한 기업 목록 조회 실패:', err)
    error.value = '찜한 기업 목록을 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const loadFinancialScores = async () => {
  if (financialLoading.value) return
  financialLoading.value = true

  try {
    // 1차: 점수 있는 것들 병합
    const response = await getWishedStocksWithScores()
    const financialDataMap = new Map()
    response.data.forEach(fd => {
      financialDataMap.set(fd.stockCode, fd)
    })

    wishedStocks.value = wishedStocks.value.map(stock => {
      const financial = financialDataMap.get(stock.stockCode)
      if (financial) {
        return {
          ...stock,
          totalScore: financial.totalScore,
          fiscalYear: financial.fiscalYear
        }
      }
      return stock
    })

    // 점수가 없는 종목은 자동으로 동기화 후 다시 불러오기
    const missing = wishedStocks.value.filter(s => !s.totalScore)
    if (missing.length) {
      const currentYear = new Date().getFullYear() - 1
      for (const s of missing) {
        try {
          await syncFinancialData(s.stockCode, currentYear)
        } catch (err) {
          console.warn('재무 동기화 실패 (무시):', s.stockCode, err)
        }
      }
      // 동기화 후 다시 점수 병합
      const res2 = await getWishedStocksWithScores()
      const map2 = new Map()
      res2.data.forEach(fd => map2.set(fd.stockCode, fd))
      wishedStocks.value = wishedStocks.value.map(stock => {
        const financial = map2.get(stock.stockCode)
        if (financial) {
          return {
            ...stock,
            totalScore: financial.totalScore,
            fiscalYear: financial.fiscalYear
          }
        }
        return stock
      })
    }
    scoresLoaded.value = true
  } catch (err) {
    console.error('재무 점수 조회 실패:', err)
    alert('재무 점수를 불러오는데 실패했습니다.')
  } finally {
    financialLoading.value = false
  }
}

const sortStocks = () => {
  // computed가 자동으로 재계산함
}

const removeWish = async (stockCode) => {
  if (!confirm('이 기업의 찜을 취소하시겠습니까?')) {
    return
  }

  try {
    await removeStockWish(stockCode)
    wishedStocks.value = wishedStocks.value.filter((s) => s.stockCode !== stockCode)
  } catch (err) {
    console.error('찜 취소 실패:', err)
    alert('찜 취소에 실패했습니다.')
  }
}

const goToStockDetail = (stockCode) => {
  router.push(`/invest/${stockCode}`)
}

const getMarketClass = (market) => {
  return market === 'KOSPI' ? 'market-kospi' : 'market-kosdaq'
}

const getChangeClass = (change) => {
  if (!change) return ''
  const num = Number(change)
  return num > 0 ? 'price-up' : num < 0 ? 'price-down' : ''
}

const formatPrice = (price) => {
  if (price === null || price === undefined) return '-'
  return Number(price).toLocaleString('ko-KR') + '원'
}

const formatChange = (change) => {
  if (change === null || change === undefined) return '-'
  const num = Number(change)
  return (num > 0 ? '+' : '') + num.toLocaleString('ko-KR')
}

const formatRate = (rate) => {
  if (rate === null || rate === undefined) return '-'
  const num = Number(rate)
  return (num > 0 ? '+' : '') + num.toFixed(2)
}

const formatMarketCap = (cap) => {
  if (!cap) return '-'
  const num = Number(cap)
  if (num >= 1_000_000_000_000) return (num / 1_000_000_000_000).toFixed(1) + '조'
  if (num >= 100_000_000) return (num / 100_000_000).toFixed(1) + '억'
  return num.toLocaleString('ko-KR')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return formatKSTDate(dateStr)
}

const getScoreClass = (score) => {
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  if (score >= 40) return 'average'
  return 'poor'
}

onMounted(() => {
  loadWishedStocks()
})
</script>

<style scoped>
.wished-stocks-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 60px 20px;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.empty svg {
  margin-bottom: 12px;
}

.empty p {
  color: #757575;
  font-size: 16px;
  margin: 0;
}

.controls {
  margin-bottom: 16px;
}

.missing-score-alert {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #9a3412;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 13px;
}

.sort-buttons {
  display: flex;
  gap: 8px;
}

.sort-btn {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  font-size: 13px;
  cursor: pointer;
  color: #4b5563;
  transition: all 0.15s ease;
}

.sort-btn:hover {
  border-color: #d1d5db;
}

.sort-btn.active {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
  font-weight: 600;
}
.wished-stocks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.stock-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.2s;
}

.stock-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.card-header h3 {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.header-right {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.score-badge {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.score-badge.excellent {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.score-badge.good {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.score-badge.average {
  background: linear-gradient(135deg, #fccb90 0%, #d57eeb 100%);
}

.score-badge.poor {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #666;
}

.stock-code {
  font-size: 13px;
  color: #757575;
}

.market-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.wish-toggle {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #e53935;
  cursor: pointer;
  transition: all 0.2s;
}

.wish-toggle:hover {
  background: #ffebee;
  border-color: #ef5350;
}

.market-kospi {
  background-color: #e3f2fd;
  color: #1976d2;
}

.market-kosdaq {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.price-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: #212121;
}

.change {
  font-size: 13px;
  font-weight: 600;
}

.price-up {
  color: #d32f2f;
}

.price-down {
  color: #1976d2;
}

.metric {
  text-align: right;
}

.metric .label {
  display: block;
  font-size: 12px;
  color: #757575;
}

.metric .value {
  font-size: 13px;
  font-weight: 600;
  color: #212121;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.info-item .label {
  color: #757575;
}

.info-item .value {
  color: #212121;
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.remove-button {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  color: #757575;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.remove-button:hover {
  background: #f5f5f5;
  border-color: #d32f2f;
  color: #d32f2f;
}

@media (max-width: 768px) {
  .wished-stocks-grid {
    grid-template-columns: 1fr;
  }
}
</style>
