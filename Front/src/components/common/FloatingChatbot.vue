<template>
  <div class="floating-chat">
    <!-- 알림 토스트 -->
    <div v-if="chatbotStore.showNotification && !isOpen" class="notification-toast">
      <span class="toast-icon">💡</span>
      <span class="toast-message">{{ chatbotStore.notificationMessage }}</span>
      <button class="toast-close" @click="chatbotStore.dismissNotification">✕</button>
    </div>

    <!-- 플로팅 버튼 -->
    <button 
      v-if="!isOpen" 
      class="chat-toggle-btn"
      :class="{ 'has-notification': chatbotStore.showNotification }"
      @click="openChat"
    >
      <span class="chat-icon">💬</span>
      <span class="chat-badge" v-if="chatbotStore.showNotification || hasNewMessage">!</span>
    </button>

    <!-- 챗봇 패널 -->
    <div v-if="isOpen" class="chat-panel">
      <div class="chat-header">
        <div class="header-info">
          <span class="header-icon">🤖</span>
          <span class="header-title">
            {{ chatbotStore.currentStock ? chatbotStore.currentStock.stockName + ' 분석' : '투자 AI 챗봇' }}
          </span>
        </div>
        <button class="close-btn" @click="closeChat">✕</button>
      </div>

      <div class="chat-messages" ref="chatMessages">
        <div v-if="chatHistory.length === 0" class="chat-welcome">
          <div class="welcome-icon">📊</div>
          <h4 v-if="chatbotStore.currentStock">{{ chatbotStore.currentStock.stockName }} 분석</h4>
          <h4 v-else>안녕하세요!</h4>
          <p v-if="chatbotStore.currentStock">이 종목에 대해 궁금한 점을 물어보세요.</p>
          <p v-else>투자 관련 질문을 해보세요.</p>
          <div class="quick-questions">
            <button 
              v-for="q in currentQuickQuestions" 
              :key="q"
              class="quick-btn"
              @click="sendQuickQuestion(q)"
            >
              {{ q }}
            </button>
          </div>
        </div>

        <div 
          v-for="(msg, index) in chatHistory" 
          :key="index"
          class="message"
          :class="msg.role"
        >
          <div class="message-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="message-content">{{ msg.content }}</div>
        </div>

        <div v-if="loading" class="message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="typing-dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <input 
          v-model="userInput"
          type="text"
          placeholder="질문을 입력하세요..."
          @keypress.enter="sendMessage"
          :disabled="loading"
        />
        <button 
          class="send-btn"
          @click="sendMessage"
          :disabled="loading || !userInput.trim()"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M2 21l21-9L2 3v7l15 2-15 2v7z"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import http from '@/api/http'
import { useChatbotStore } from '@/stores/chatbot'

export default {
  name: 'FloatingChatbot',
  setup() {
    const chatbotStore = useChatbotStore()
    return { chatbotStore }
  },
  data() {
    return {
      isOpen: false,
      loading: false,
      userInput: '',
      chatHistory: [],
      hasNewMessage: false,
      defaultQuestions: [
        '오늘 시장 상황은?',
        '삼성전자 어때?',
        '초보자 추천 종목',
        '투자 성향 알려줘'
      ],
      stockQuestions: [
        '이 종목 투자 점수는?',
        '영업이익률 알려줘',
        '안정형 투자자한테 괜찮아?',
        '리스크 요약해줘'
      ]
    }
  },
  computed: {
    currentQuickQuestions() {
      return this.chatbotStore.currentStock ? this.stockQuestions : this.defaultQuestions
    }
  },
  methods: {
    openChat() {
      this.isOpen = true
      this.hasNewMessage = false
    },
    closeChat() {
      this.isOpen = false
    },
    sendQuickQuestion(question) {
      this.userInput = question
      this.sendMessage()
    },
    async sendMessage() {
      if (!this.userInput.trim() || this.loading) return

      const message = this.userInput.trim()
      this.userInput = ''

      // 사용자 메시지 추가
      this.chatHistory.push({
        role: 'user',
        content: message
      })

      this.scrollToBottom()
      this.loading = true

      try {
        // 일반 챗봇 API 호출
        const response = await http.post('/chat/general', { message })
        
        this.chatHistory.push({
          role: 'assistant',
          content: response.data.message
        })
      } catch (err) {
        console.error('챗봇 응답 실패:', err)
        this.chatHistory.push({
          role: 'assistant',
          content: '죄송합니다. 잠시 후 다시 시도해주세요.'
        })
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.chatMessages
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    }
  }
}
</script>

<style scoped>
.floating-chat {
  position: fixed;
  bottom: 24px;
  left: 24px;
  z-index: 1000;
}

/* 알림 토스트 */
.notification-toast {
  position: absolute;
  bottom: 70px;
  left: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 280px;
  max-width: 320px;
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

.toast-icon {
  font-size: 20px;
}

.toast-message {
  flex: 1;
  font-size: 13px;
  line-height: 1.4;
}

.toast-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toast-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 알림 있을 때 버튼 펄스 효과 */
.chat-toggle-btn.has-notification {
  animation: pulse 2s infinite;
}

/* 플로팅 버튼 */
.chat-toggle-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.chat-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

.chat-icon {
  font-size: 28px;
}

.chat-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  background: #ef4444;
  border-radius: 50%;
  color: white;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 챗봇 패널 */
.chat-panel {
  width: 380px;
  height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
}

.header-icon {
  font-size: 24px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 메시지 영역 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
}

.chat-welcome {
  text-align: center;
  padding: 24px 16px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.chat-welcome h4 {
  font-size: 18px;
  color: #212121;
  margin-bottom: 8px;
}

.chat-welcome p {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
}

.quick-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-btn {
  padding: 10px 16px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 13px;
  color: #424242;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* 메시지 */
.message {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.message.user .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: white;
  color: #212121;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 타이핑 애니메이션 */
.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: #999;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 입력 영역 */
.chat-input-area {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.chat-input-area input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.chat-input-area input:focus {
  border-color: #667eea;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #667eea;
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #5a6fd6;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 모바일 반응형 */
@media (max-width: 480px) {
  .floating-chat {
    bottom: 16px;
    left: 16px;
  }

  .chat-panel {
    width: calc(100vw - 32px);
    height: calc(100vh - 100px);
    max-height: 600px;
  }

  .chat-toggle-btn {
    width: 56px;
    height: 56px;
  }
}
</style>
