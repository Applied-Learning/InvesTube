import { defineStore } from 'pinia'

export const useChatbotStore = defineStore('chatbot', {
    state: () => ({
        currentStock: null,   // 현재 보고 있는 종목 정보
        notificationMessage: '', // 알림 메시지
        showNotification: false
    }),

    actions: {
        // 종목 상세 페이지 진입 시 호출
        setCurrentStock(stock) {
            this.currentStock = stock
            if (stock) {
                this.notificationMessage = `${stock.stockName} 종목 분석에 궁금한게 있다면 물어보세요!`
                this.showNotification = true

                // 5초 후 알림 자동 숨김
                setTimeout(() => {
                    this.showNotification = false
                }, 5000)
            }
        },

        // 종목 상세 페이지 떠날 때 호출
        clearCurrentStock() {
            this.currentStock = null
            this.notificationMessage = ''
            this.showNotification = false
        },

        // 알림 닫기
        dismissNotification() {
            this.showNotification = false
        }
    }
})
