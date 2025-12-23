<template>
  <div class="invest-view">
    <PageHeader title="투자 정보" :showBack="false" icon="invest" />

    <Container>
      <!-- 투자 성향 카드 -->
      <div class="profile-card">
        <div class="profile-header">
          <span class="profile-icon">📊</span>
          <span class="profile-title">나의 투자 성향</span>
        </div>
        
        <!-- 현재 성향 표시 -->
        <div v-if="currentProfile" class="current-profile">
          <div class="profile-type-badge" :class="getProfileClass(currentProfile.profileName)">
            {{ getProfileIcon(currentProfile.profileName) }} {{ currentProfile.profileName }}
          </div>
          <p class="profile-description">{{ getProfileDescription(currentProfile.profileName) }}</p>
        </div>
        <div v-else class="no-profile">
          아직 투자 성향을 설정하지 않았어요.
        </div>

        <!-- 성향 선택 버튼 -->
        <div class="profile-actions">
          <div class="profile-select-buttons">
            <button 
              v-for="type in profileTypes" 
              :key="type.name"
              class="profile-type-btn"
              :class="{ active: currentProfile?.profileName?.includes(type.name) }"
              @click="selectProfileType(type.name)"
            >
              {{ type.icon }} {{ type.name }}
            </button>
          </div>
          <button class="survey-link" @click="goToSurvey">
            설문으로 정확히 분석하기 →
          </button>
        </div>
      </div>

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
            @input="handleInputChange"
            @keyup.enter="handleSearch"
          />
          <button
            v-if="searchQuery"
            class="clear-search-btn"
            type="button"
            @click="clearSearchResults(true)"
            aria-label="검색어 지우기"
          >
            ×
          </button>
          <Button @click="handleSearch">검색</Button>
        </div>
      </div>

      <!-- 검색 결과 -->
      <div class="search-results" v-if="hasSearched">
        <div class="section-header">
          <h3>검색 결과 <span class="count">({{ searchResults.length }}개)</span></h3>
          <span class="favorite-hint" v-if="!searchResults.length">결과가 없습니다.</span>
        </div>
        <div v-if="searchResults.length" class="stock-grid">
          <StockCard
            v-for="stock in searchResults"
            :key="stock.stockCode"
            :stock="stock"
            :showWishToggle="true"
            :wished="wishedSet.has(stock.stockCode)"
            @toggleWish="handleToggleWish"
            @select="goToDetail"
          />
        </div>
      </div>

      <!-- 관심 종목 -->
      <div class="favorites-section">
        <div class="section-header">
          <h3>관심 종목</h3>
          <span class="favorite-hint" v-if="wishedLoading">불러오는 중...</span>
          <span class="favorite-hint" v-else-if="!wishedStocks.length">등록된 관심 종목이 없습니다.</span>
          <span
            class="favorite-hint"
            v-else-if="wishedStocks.length > limitedWishedStocks.length"
          >
            최근 관심 종목 6개만 표시합니다. 전체 보기에서 모두 확인하세요.
          </span>
          <RouterLink v-if="wishedStocks.length" to="/mypage/stocks/wished" class="view-all-link">전체 보기</RouterLink>
        </div>
        <div v-if="wishedStocks.length" class="stock-grid favorites-grid">
          <StockCard
            v-for="stock in limitedWishedStocks"
            :key="stock.stockCode"
            :stock="stock"
            :compact="true"
            :showWishToggle="true"
            :wished="wishedSet.has(stock.stockCode)"
            @toggleWish="handleToggleWish"
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
      본 서비스는 금융감독원(DART) 및 한국거래소(KRX)의 공개 데이터를 가공·분석하여 제공하는 참고용
      정보입니다.<br />
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
import profileApi from '@/api/profile'
import { getWishedStocks, addStockWish, removeStockWish } from '@/api/stockWish'
import { useInvestSearchStore } from '@/stores/investSearch'

export default {
  name: 'InvestView',
  components: {
    PageHeader,
    Container,
    Button,
    StockCard,
  },
  data() {
    return {
      stocks: [],
      indices: [],
      indicesLoading: false,
      wishedStocks: [],
      wishedLoading: false,
      wishedSet: new Set(),
      searchQuery: '',
      searchResults: [],
      hasSearched: false,
      searchStore: null,
      currentProfile: null,
      profileTypes: [
        { name: '안정형', icon: '🛡️' },
        { name: '균형형', icon: '⚖️' },
        { name: '공격형', icon: '🚀' },
      ],
    }
  },
  computed: {
    mainIndices() {
      // KOSPI, KOSDAQ, KRX100 등 주요 지수 필터링
      return this.indices
        .filter(
          (idx) =>
            idx.IDX_NM &&
            (idx.IDX_NM.includes('KOSPI') ||
              idx.IDX_NM.includes('KOSDAQ') ||
              idx.IDX_NM.includes('KRX')),
        )
        .slice(0, 3)
    },
    topGainers() {
      return [...this.stocks]
        .filter((s) => s.priceChangeRate > 0)
        .sort((a, b) => b.priceChangeRate - a.priceChangeRate)
        .slice(0, 5)
    },
    topLosers() {
      return [...this.stocks]
        .filter((s) => s.priceChangeRate < 0)
        .sort((a, b) => a.priceChangeRate - b.priceChangeRate)
        .slice(0, 5)
    },
    topVolume() {
      return [...this.stocks].sort((a, b) => (b.volume || 0) - (a.volume || 0)).slice(0, 5)
    },
    limitedWishedStocks() {
      return (this.wishedStocks || []).slice(0, 6)
    },
  },
  created() {
    this.searchStore = useInvestSearchStore()
    this.initPage()
  },
  methods: {
    async initPage() {
      await this.loadStocks()
      this.loadIndices()
      this.loadProfile()
      this.loadWishedStocks()

      if (this.searchStore?.searchQuery) {
        this.searchQuery = this.searchStore.searchQuery
        this.handleSearch()
      }
    },
    async loadWishedStocks() {
      this.wishedLoading = true
      try {
        const res = await getWishedStocks()
        this.wishedStocks = res.data || []
        this.wishedSet = new Set(this.wishedStocks.map((s) => s.stockCode))
      } catch (err) {
        console.error('관심 종목 조회 실패:', err)
        this.wishedStocks = []
        this.wishedSet = new Set()
      } finally {
        this.wishedLoading = false
      }
    },
    async loadProfile() {
      try {
        const response = await profileApi.getDefaultProfile()
        this.currentProfile = response.data
      } catch (err) {
        console.error('프로필 조회 실패:', err)
        this.currentProfile = null
      }
    },
    async selectProfileType(typeName) {
      try {
        // 새 프로필 생성
        const response = await profileApi.createProfile({
          profileName: typeName,
          isDefault: true
        })
        this.currentProfile = response.data
      } catch (err) {
        console.error('프로필 변경 실패:', err)
        alert('프로필 변경에 실패했습니다.')
      }
    },
    getProfileIcon(name) {
      if (!name) return '📊'
      if (name.includes('안정')) return '🛡️'
      if (name.includes('균형')) return '⚖️'
      if (name.includes('공격')) return '🚀'
      return '📊'
    },
    getProfileClass(name) {
      if (!name) return ''
      if (name.includes('안정')) return 'safe'
      if (name.includes('균형')) return 'balanced'
      if (name.includes('공격')) return 'aggressive'
      return ''
    },
    getProfileDescription(name) {
      if (!name) return ''
      if (name.includes('안정')) return '원금 보존을 최우선으로 생각하며, 안정적인 수익을 추구합니다.'
      if (name.includes('균형')) return '위험과 수익의 균형을 중시하며, 안정과 성장을 동시에 추구합니다.'
      if (name.includes('공격')) return '높은 수익을 위해 위험을 감수할 수 있는 적극적인 투자 스타일입니다.'
      return ''
    },
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
    clearSearchResults(forceClear = false) {
      // 입력 중에는 결과만 초기화, 강제(clear 버튼) 시에는 검색어/스토어도 함께 초기화
      if (forceClear) {
        this.searchQuery = ''
        this.searchStore?.clear()
      }
      this.searchResults = []
      this.hasSearched = false
    },
    handleInputChange() {
      // 입력이 바뀌면 이전 검색 결과는 즉시 지우고 검색 상태 초기화
      this.searchResults = []
      this.hasSearched = false
    },
    handleSearch() {
      if (!this.searchQuery.trim()) {
        this.searchResults = []
        this.searchStore?.clear()
        this.hasSearched = false
        return
      }

      // 공백/대소문자 무시하고 부분 매칭
      const normalize = (str) => (str || '').toLowerCase().replace(/\s+/g, '')
      const query = normalize(this.searchQuery)

      this.searchResults = this.stocks.filter(
        (stock) =>
          normalize(stock.stockName).includes(query) ||
          normalize(stock.stockCode).includes(query),
      )
      this.searchStore?.setQuery(this.searchQuery)
      this.hasSearched = true
    },
    goToDetail(stockCode) {
      this.$router.push({ name: 'stockDetail', params: { stockCode } })
    },
    formatIndexValue(value) {
      if (!value) return '-'
      return Number(value).toLocaleString('ko-KR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      })
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
    },
    goToSurvey() {
      this.$router.push({ name: 'investmentSurvey' })
    },
    async handleToggleWish(stockCode, currentlyWished) {
      try {
        if (currentlyWished) {
          await removeStockWish(stockCode)
          this.wishedSet.delete(stockCode)
          this.wishedStocks = this.wishedStocks.filter((s) => s.stockCode !== stockCode)
        } else {
          await addStockWish(stockCode)
          // 최신 목록 다시 불러와 정합성 유지
          await this.loadWishedStocks()
        }
      } catch (err) {
        console.error('관심 종목 토글 실패:', err)
        alert('관심 종목 처리에 실패했습니다.')
      }
    },
  },
}
</script>

<style scoped>
.invest-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 관심 종목 섹션 */
.favorites-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.favorites-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.favorites-header h3 {
  margin: 0;
}

.favorite-hint {
  color: #6b7280;
  font-size: 13px;
}

.favorites-grid {
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
}

.section-header .count {
  font-size: 14px;
  color: #6b7280;
}

.view-all-link {
  margin-left: auto;
  font-size: 13px;
  color: #2563eb;
  text-decoration: none;
}

.view-all-link:hover {
  text-decoration: underline;
}

/* 투자 성향 프로필 카드 */
.profile-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.profile-icon {
  font-size: 24px;
}

.profile-title {
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.current-profile {
  margin-bottom: 20px;
}

.profile-type-badge {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.profile-type-badge.safe {
  background: #e3f2fd;
  color: #1976d2;
}

.profile-type-badge.balanced {
  background: #fff3e0;
  color: #f57c00;
}

.profile-type-badge.aggressive {
  background: #ffebee;
  color: #d32f2f;
}

.profile-description {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.no-profile {
  color: #999;
  font-size: 14px;
  margin-bottom: 16px;
}

.profile-actions {
  border-top: 1px solid #e0e0e0;
  padding-top: 16px;
}

.profile-select-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.profile-type-btn {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.profile-type-btn:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.profile-type-btn.active {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.survey-link {
  width: 100%;
  padding: 10px;
  background: none;
  border: none;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  text-align: center;
}

.survey-link:hover {
  text-decoration: underline;
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

.clear-search-btn {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 16px;
  cursor: pointer;
  color: #4b5563;
}

.clear-search-btn:hover {
  background: #e5e7eb;
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
