<template>
  <div class="stock-card" :class="{ compact }" @click="handleClick">
    <div class="stock-header">
      <div class="stock-info">
        <h3 class="stock-name">{{ stock.stockName }}</h3>
        <span class="stock-code">{{ stock.stockCode }}</span>
      </div>
      <div class="header-right-col">
        <div class="badge-row">
          <span v-if="score" class="score-pill">{{ score.toFixed(1) }}점</span>
          <span class="stock-market" :class="marketClass">{{ stock.market }}</span>
        </div>
        <button
          v-if="showWishToggle"
          class="wish-toggle"
          :class="{ wished }"
          @click.stop="$emit('toggleWish', stock.stockCode, wished)"
          aria-label="관심 종목 토글"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path
              :fill="wished ? 'currentColor' : '#ffffff'"
              d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
            ></path>
          </svg>
        </button>
      </div>
    </div>

      <div class="stock-body">
        <div class="price-section">
        <div class="current-price">{{ formatPrice(stock.closePrice) }}원</div>
        <div class="price-change" :class="priceChangeClass">
          <span class="change-amount">{{ formatPriceChange(stock.priceChange) }}</span>
          <span class="change-rate">{{ formatRate(stock.priceChangeRate) }}%</span>
        </div>
      </div>

      <div class="stock-details" v-if="!compact">
        <div class="detail-item">
          <span class="label">거래량</span>
          <span class="value">{{ formatVolume(stock.volume) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">시가총액</span>
          <span class="value">{{ formatMarketCap(stock.marketCap) }}</span>
        </div>
      </div>
    </div>

    <div class="stock-footer">
      <span class="industry">{{ stock.industry }}</span>
      <span class="trade-date">{{ formatDate(stock.tradeDate) }}</span>
    </div>
  </div>
</template>

<script>
import { formatKSTDate } from '../../utils/date.js'

export default {
  name: 'StockCard',
  props: {
    stock: {
      type: Object,
      required: true,
    },
    compact: {
      type: Boolean,
      default: false,
    },
    showWishToggle: {
      type: Boolean,
      default: false,
    },
    wished: {
      type: Boolean,
      default: false,
    },
    score: {
      type: Number,
      default: null,
    },
  },
  computed: {
    marketClass() {
      return this.stock.market === 'KOSPI' ? 'market-kospi' : 'market-kosdaq'
    },
    priceChangeClass() {
      if (!this.stock.priceChange) return ''
      return this.stock.priceChange > 0
        ? 'price-up'
        : this.stock.priceChange < 0
          ? 'price-down'
          : ''
    },
  },
  methods: {
    handleClick() {
      this.$emit('select', this.stock.stockCode)
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
.stock-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stock-card.compact {
  padding: 12px;
}

.stock-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stock-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 8px;
}

.header-right-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.badge-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stock-info {
  flex: 1;
}

.stock-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #212121;
}

.stock-code {
  font-size: 12px;
  color: #757575;
}

.stock-market {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.score-pill {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.wish-toggle {
  margin-left: 8px;
  background: transparent;
  border: none;
  padding: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #e53935;
  cursor: pointer;
  transition: color 0.2s ease;
}

.wish-toggle:hover {
  color: #c62828;
}

.wish-toggle.wished {
  color: #e53935;
}

.market-kospi {
  background-color: #e3f2fd;
  color: #1976d2;
}

.market-kosdaq {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.stock-body {
  margin-bottom: 16px;
}

.price-section {
  margin-bottom: 12px;
}

.current-price {
  font-size: 24px;
  font-weight: 700;
  color: #212121;
  margin-bottom: 4px;
}

.price-change {
  font-size: 14px;
  font-weight: 500;
}

.price-up {
  color: #d32f2f;
}

.price-down {
  color: #1976d2;
}

.change-amount {
  margin-right: 8px;
}

.stock-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-item .label {
  font-size: 12px;
  color: #757575;
}

.detail-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #424242;
}

.stock-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.industry {
  font-size: 12px;
  color: #616161;
  background-color: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.trade-date {
  font-size: 11px;
  color: #9e9e9e;
}
</style>
