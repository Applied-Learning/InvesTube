<template>
  <div class="invest-view">
    <PageHeader title="투자 정보" :showBack="false" icon="invest" />
    
    <Container>
      <!-- 지수 카드 섹션 -->
      <div class="indices-section">
        <div v-if="indicesLoading" class="loading-small">지수 정보 로딩 중...</div>
        <div v-else class="indices-grid">
          <div v-for="index in mainIndices" :key="index.IDX_NM" class="index-card">
            <div class="index-name">{{ index.IDX_NM }}</div>
            <div class="index-value">{{ formatIndexValue(index.CLSPRC_IDX) }}</div>
            <div class="index-change" :class="getChangeClass(index.FLUC_RT)">
              <span>{{ formatChange(index.CMPPREVDD_IDX) }}</span>
              <span>{{ formatRate(index.FLUC_RT) }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 검색 섹션 -->
      <div class="search-section">
        <h3>종목 검색</h3>
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="종목명 또는 종목코드를 입력하세요..."
            @input="clearSearchResults"
            @keyup.enter="handleSearch"
          />
          <Button @click="handleSearch">검색</Button>
        </div>
      </div>

      <!-- 검색 결과가 있을 때만 표시 -->
      <div v-if="searchQuery && searchResults.length > 0" class="search-results">
        <h3>검색 결과 ({{ searchResults.length }}개)</h3>
        <div class="stock-grid">
          <StockCard 
            v-for="stock in searchResults" 
            :key="stock.stockCode"
            :stock="stock"
            @select="goToDetail"
          />
        </div>
      </div>

      <!-- 상위 종목 섹션 -->
      <div class="top-stocks-section">
        <div class="top-section">
          <h3>상승률 TOP 5</h3>
          <div class="stock-list-small">
            <div 
              v-for="stock in topGainers" 
              :key="stock.stockCode"
              class="stock-item-small"
              @click="goToDetail(stock.stockCode)"
            >
              <div class="stock-info-small">
                <span class="stock-name-small">{{ stock.stockName }}</span>
                <span class="stock-code-small">{{ stock.stockCode }}</span>
              </div>
              <div class="stock-change-small price-up">
                {{ formatRate(stock.priceChangeRate) }}%
              </div>
            </div>
          </div>
        </div>

        <div class="top-section">
          <h3>하락률 TOP 5</h3>
          <div class="stock-list-small">
            <div 
              v-for="stock in topLosers" 
              :key="stock.stockCode"
              class="stock-item-small"
              @click="goToDetail(stock.stockCode)"
            >
              <div class="stock-info-small">
                <span class="stock-name-small">{{ stock.stockName }}</span>
                <span class="stock-code-small">{{ stock.stockCode }}</span>
              </div>
              <div class="stock-change-small price-down">
                {{ formatRate(stock.priceChangeRate) }}%
              </div>
            </div>
          </div>
        </div>

        <div class="top-section">
          <h3>거래량 TOP 5</h3>
          <div class="stock-list-small">
            <div 
              v-for="stock in topVolume" 
              :key="stock.stockCode"
              class="stock-item-small"
              @click="goToDetail(stock.stockCode)"
            >
              <div class="stock-info-small">
                <span class="stock-name-small">{{ stock.stockName }}</span>
                <span class="stock-code-small">{{ stock.stockCode }}</span>
              </div>
              <div class="stock-volume-small">
                {{ formatVolume(stock.volume) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </Container>
    <!-- Disclaimer -->
    <div class="invest-disclaimer">
      본 서비스는 금융감독원(DART) 및 한국거래소(KRX)의 공개 데이터를 가공·분석하여 제공하는 참고용 정보입니다.<br />
      투자 판단의 책임은 이용자 본인에게 있습니다.
    </div>
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue'
import Container from '@/components/common/Container.vue'
import Button from '@/components/common/Button.vue'
import StockCard from '@/components/stock/StockCard.vue'
import stockApi from '@/api/stock'

export default {
  name: 'InvestView',
  components: {
    PageHeader,
    Container,
    Button,
    StockCard
  },
  data() {
    return {
      stocks: [],
      indices: [],
      indicesLoading: false,
      searchQuery: '',
      searchResults: []
    }
  },
  computed: {
    mainIndices() {
      // KOSPI, KOSDAQ, KRX100 등 주요 지수 필터링
      return this.indices.filter(idx => 
        idx.IDX_NM && (
          idx.IDX_NM.includes('KOSPI') || 
          idx.IDX_NM.includes('KOSDAQ') || 
          idx.IDX_NM.includes('KRX')
        )
      ).slice(0, 3)
    },
    topGainers() {
      return [...this.stocks]
        .filter(s => s.priceChangeRate > 0)
        .sort((a, b) => b.priceChangeRate - a.priceChangeRate)
        .slice(0, 5)
    },
    topLosers() {
      return [...this.stocks]
        .filter(s => s.priceChangeRate < 0)
        .sort((a, b) => a.priceChangeRate - b.priceChangeRate)
        .slice(0, 5)
    },
    topVolume() {
      return [...this.stocks]
        .sort((a, b) => (b.volume || 0) - (a.volume || 0))
        .slice(0, 5)
    }
  },
  created() {
    this.loadStocks()
    this.loadIndices()
  },
  methods: {
    async loadStocks() {
      try {
        const response = await stockApi.getStocks()
        this.stocks = response.data
      } catch (err) {
        console.error('주식 목록 조회 실패:', err)
      }
    },
    async loadIndices() {
      this.indicesLoading = true
      try {
        const response = await stockApi.getIndices()
        this.indices = response.data
      } catch (err) {
        console.error('지수 정보 조회 실패:', err)
      } finally {
        this.indicesLoading = false
      }
    },
    clearSearchResults() {
      // 검색어를 수정할 때 이전 검색 결과 초기화
      this.searchResults = []
    },
    handleSearch() {
      if (!this.searchQuery.trim()) {
        this.searchResults = []
        return
      }
      
      const query = this.searchQuery.toLowerCase().trim()
      this.searchResults = this.stocks.filter(stock => 
        stock.stockName.toLowerCase().includes(query) ||
        stock.stockCode.toLowerCase().includes(query)
      )
    },
    goToDetail(stockCode) {
      this.$router.push({ name: 'StockDetail', params: { stockCode } })
    },
    formatIndexValue(value) {
      if (!value) return '-'
      return Number(value).toLocaleString('ko-KR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    formatChange(change) {
      if (!change) return '-'
      const num = Number(change)
      return num > 0 ? `+${num.toFixed(2)}` : num.toFixed(2)
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
    getChangeClass(rate) {
      if (!rate) return ''
      const num = Number(rate)
      return num > 0 ? 'price-up' : num < 0 ? 'price-down' : ''
    }
  }
}
</script>

<style scoped>
.invest-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 지수 섹션 */
.indices-section {
  margin-bottom: 32px;
}

.loading-small {
  text-align: center;
  padding: 20px;
  color: #757575;
}

.indices-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.index-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e0e0e0;
}

.index-name {
  font-size: 14px;
  color: #757575;
  margin-bottom: 8px;
}

.index-value {
  font-size: 28px;
  font-weight: 700;
  color: #212121;
  margin-bottom: 8px;
}

.index-change {
  display: flex;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

/* 검색 섹션 */
.search-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 32px;
  border: 1px solid #e0e0e0;
}

.search-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.search-box {
  display: flex;
  gap: 12px;
}

.search-box input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.search-box input:focus {
  outline: none;
  border-color: #1976d2;
}

/* 검색 결과 */
.search-results {
  margin-bottom: 32px;
}

.search-results h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.stock-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

/* 상위 종목 섹션 */
.top-stocks-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.top-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e0e0e0;
}

.top-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #212121;
}

.stock-list-small {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stock-item-small {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  background: #f9f9f9;
  cursor: pointer;
  transition: all 0.2s;
}

.stock-item-small:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.stock-info-small {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stock-name-small {
  font-size: 14px;
  font-weight: 500;
  color: #212121;
}

.stock-code-small {
  font-size: 12px;
  color: #757575;
}

.invest-disclaimer {
  max-width: 900px;
  margin: 24px auto 48px auto;
  padding: 12px 16px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e6edf3;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.6;
  text-align: center;
}

.stock-change-small,
.stock-volume-small {
  font-size: 14px;
  font-weight: 600;
}

/* 가격 변동 색상 */
.price-up {
  color: #d32f2f;
}

.price-down {
  color: #1976d2;
}

@media (max-width: 768px) {
  .indices-grid {
    grid-template-columns: 1fr;
  }
  
  .top-stocks-section {
    grid-template-columns: 1fr;
  }
  
  .stock-grid {
    grid-template-columns: 1fr;
  }
}
</style>
