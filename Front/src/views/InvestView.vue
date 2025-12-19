<template>
  <div class="invest-view">
    <PageHeader title="투자 정보" />
    
    <Container>
      <div class="controls">
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="종목명 또는 종목코드 검색..."
            @input="handleSearch"
          />
        </div>
        
        <div class="filter-box">
          <select v-model="selectedMarket" @change="handleFilterChange">
            <option value="">전체 시장</option>
            <option value="KOSPI">KOSPI</option>
            <option value="KOSDAQ">KOSDAQ</option>
          </select>
          
          <select v-model="sortBy" @change="handleSortChange">
            <option value="name">종목명순</option>
            <option value="priceUp">상승률순</option>
            <option value="priceDown">하락률순</option>
            <option value="volume">거래량순</option>
          </select>
        </div>
      </div>
      
      <div v-if="loading" class="loading">
        <p>로딩 중...</p>
      </div>
      
      <div v-else-if="error" class="error">
        <p>{{ error }}</p>
        <Button @click="loadStocks">다시 시도</Button>
      </div>
      
      <div v-else class="stock-grid">
        <StockCard 
          v-for="stock in filteredStocks" 
          :key="stock.stockCode"
          :stock="stock"
          @click="goToDetail"
        />
      </div>
      
      <div v-if="!loading && filteredStocks.length === 0" class="empty">
        <p>검색 결과가 없습니다.</p>
      </div>
    </Container>
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
      filteredStocks: [],
      loading: false,
      error: null,
      searchQuery: '',
      selectedMarket: '',
      sortBy: 'name'
    }
  },
  created() {
    this.loadStocks()
  },
  methods: {
    async loadStocks() {
      this.loading = true
      this.error = null
      
      try {
        const response = await stockApi.getStocks()
        this.stocks = response.data
        this.applyFilters()
      } catch (err) {
        console.error('주식 목록 조회 실패:', err)
        this.error = '주식 목록을 불러오는데 실패했습니다.'
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.applyFilters()
    },
    handleFilterChange() {
      this.applyFilters()
    },
    handleSortChange() {
      this.applyFilters()
    },
    applyFilters() {
      let result = [...this.stocks]
      
      // 검색 필터
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        result = result.filter(stock => 
          stock.stockName.toLowerCase().includes(query) ||
          stock.stockCode.toLowerCase().includes(query)
        )
      }
      
      // 시장 필터
      if (this.selectedMarket) {
        result = result.filter(stock => stock.market === this.selectedMarket)
      }
      
      // 정렬
      switch (this.sortBy) {
        case 'name':
          result.sort((a, b) => a.stockName.localeCompare(b.stockName))
          break
        case 'priceUp':
          result.sort((a, b) => (b.priceChangeRate || 0) - (a.priceChangeRate || 0))
          break
        case 'priceDown':
          result.sort((a, b) => (a.priceChangeRate || 0) - (b.priceChangeRate || 0))
          break
        case 'volume':
          result.sort((a, b) => (b.volume || 0) - (a.volume || 0))
          break
      }
      
      this.filteredStocks = result
    },
    goToDetail(stockCode) {
      this.$router.push({ name: 'StockDetail', params: { stockCode } })
    }
  }
}
</script>

<style scoped>
.invest-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.controls {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 200px;
}

.search-box input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.search-box input:focus {
  outline: none;
  border-color: #1976d2;
}

.filter-box {
  display: flex;
  gap: 8px;
}

.filter-box select {
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  background-color: white;
  cursor: pointer;
}

.filter-box select:focus {
  outline: none;
  border-color: #1976d2;
}

.stock-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
}

.loading p,
.error p,
.empty p {
  color: #757575;
  font-size: 16px;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .controls {
    flex-direction: column;
  }
  
  .filter-box {
    flex-direction: column;
  }
  
  .filter-box select {
    width: 100%;
  }
  
  .stock-grid {
    grid-template-columns: 1fr;
  }
}
</style>
