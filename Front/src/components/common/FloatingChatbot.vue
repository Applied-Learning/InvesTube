<template>
  <div class="floating-chat">
    <!-- 마스코트 상태 바인딩 -->
    <!-- 챗봇 닫혀있을 때 버튼 위의 마스코트는 floating-btn 내부에서 처리 -->
    
    <!-- 알림 토스트 -->
    <div v-if="chatbotStore.showNotification && !isOpen" class="notification-toast">
      <span class="toast-icon">💡</span>
      <span class="toast-message">{{ chatbotStore.notificationMessage }}</span>
      <button class="toast-close" @click="chatbotStore.dismissNotification">✕</button>
    </div>

    <!-- 플로팅 버튼 (닫혀있을 때) -->
    <div 
      v-if="!isOpen" 
      class="chat-toggle-btn"
      :class="{ 'has-notification': chatbotStore.showNotification || hasNewMessage }"
      @click="openChat"
    >
       <!-- 마스코트가 버튼 위에 앉아있는 느낌 -->
       <div class="floating-mascot">
          <MascotCharacter :state="mascotState" :type="selectedMascot" style="transform: scale(0.85);" />
       </div>
       <span class="chat-badge" v-if="chatbotStore.showNotification || hasNewMessage">!</span>
    </div>

    <!-- 챗봇 패널 (열려있을 때) -->
    <div v-if="isOpen" class="chat-panel">
      <div class="chat-header">
        <div class="header-left">
          <!-- 마스코트 클릭 시 캐릭터 변경 -->
          <div class="mascot-wrapper" @click="toggleMascot" title="클릭해서 캐릭터 변경하기! 🦁🐶🐱">
            <MascotCharacter :state="mascotState" :type="selectedMascot" style="transform: scale(0.6);" />
            <div class="change-hint">Click Me!</div>
          </div>
          <div class="header-info">
             <span class="header-title">Investube AI</span>
             <span class="header-subtitle" v-if="chatbotStore.currentStock">{{ chatbotStore.currentStock.stockName }} 분석 중</span>
          </div>
        </div>
        <button class="close-btn" @click="closeChat">
            <i class="fas fa-chevron-down"></i> ✕
        </button>
      </div>

      <div class="chat-messages" ref="chatMessages">
        <!-- 환영 메시지 -->
        <div v-if="chatHistory.length === 0" class="chat-welcome">
          <MascotCharacter state="greeting" :type="selectedMascot" style="transform: scale(1.5); margin-bottom: 20px;" />
          <h4>안녕하세요! 투자 비서 {{ mascotName }}입니다.</h4>
          <p>종목 분석이나 재무 데이터에 대해<br/>무엇이든 물어보세요!</p>
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
          <div class="message-avatar" v-if="msg.role === 'assistant'">
            <MascotCharacter :state="msg.isWelcome ? 'greeting' : 'idle'" :type="selectedMascot" style="transform: scale(0.4);" />
          </div>
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <!-- 인라인 추천 질문 -->
            <div v-if="msg.stockQuestions" class="inline-quick-questions">
              <button 
                v-for="q in msg.stockQuestions" 
                :key="q"
                class="inline-quick-btn"
                @click="sendQuickQuestion(q)"
              >
                {{ q }}
              </button>
            </div>
          </div>
        </div>

        <!-- 로딩 중 (Thinking) -->
        <div v-if="loading" class="message assistant">
          <div class="message-avatar">
            <MascotCharacter state="thinking" :type="selectedMascot" style="transform: scale(0.4);" />
          </div>
          <div class="message-bubble">
             <div class="message-content loading">
               <span class="typing-dots"><span></span><span></span><span></span></span>
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
import MascotCharacter from './MascotCharacter.vue'

export default {
  name: 'FloatingChatbot',
  components: {
    MascotCharacter
  },
  setup() {
    const chatbotStore = useChatbotStore()
    return { chatbotStore }
  },
  data() {
    return {
      isOpen: false,
      mascotState: 'idle', // idle, thinking, success, shock, caution, greeting
      selectedMascot: 'lion', // lion, dog, cat
      loading: false,
      userInput: '',
      chatHistory: [],
      hasNewMessage: false,
      defaultQuestions: [
        '오늘 시장 상황 요약해줘',
        '삼성전자 지금 살만해?',
        '재무제표 보는 법 알려줘'
      ],
      stockQuestions: [
        '이 종목 점수 왜 이래?',
        '재무적으로 가장 안 좋은 지표 뭐야?',
        '리스크 요약해줘'
      ]
    }
  },
  computed: {
    currentQuickQuestions() {
      return this.chatbotStore.currentStock ? this.stockQuestions : this.defaultQuestions
    },
    mascotName() {
        if (this.selectedMascot === 'dog') return '멍멍이'
        if (this.selectedMascot === 'cat') return '냥냥이'
        return '라이온'
    }
  },
  watch: {
    // 새로운 종목 진입 시 환영 메시지
    'chatbotStore.currentStock'(newStock, oldStock) {
      if (newStock && (!oldStock || newStock.stockCode !== oldStock.stockCode)) {
        this.chatHistory.push({
          role: 'assistant',
          content: `${newStock.stockName} 종목을 보고 계시군요! 재무 상태를 분석해드릴까요?`,
          isWelcome: true, // 환영 메시지 플래그
          stockQuestions: [
             '이 종목 점수 왜 이래?',
             '재무적으로 가장 안 좋은 지표 뭐야?',
             '리스크 요약해줘'
          ]
        })
        if(this.isOpen) this.scrollToBottom()
        else this.hasNewMessage = true
      }
    }
  },
  methods: {
    // ... 기존 openChat, closeChat 등 ...
    openChat() {
      this.isOpen = true
      this.hasNewMessage = false
      this.mascotState = 'greeting' 
      setTimeout(() => {
        if (this.mascotState === 'greeting') {
           this.mascotState = 'idle'
        }
      }, 1500)
      this.scrollToBottom()
    },
    closeChat() {
      this.isOpen = false
      this.mascotState = 'idle'
    },
    
    // 마스코트 변경 토글
    toggleMascot() {
      const types = ['lion', 'dog', 'cat']
      const currentIndex = types.indexOf(this.selectedMascot)
      const nextIndex = (currentIndex + 1) % types.length
      this.selectedMascot = types[nextIndex]
      
      // 변경 시 인사
      const originalState = this.mascotState
      this.mascotState = 'greeting'
      setTimeout(() => {
         this.mascotState = originalState === 'greeting' ? 'idle' : originalState
      }, 1000)
    },


    sendQuickQuestion(question) {
      this.userInput = question
      this.sendMessage()
    },
    
    async sendMessage() {
      if (!this.userInput.trim() || this.loading) return

      const message = this.userInput.trim()
      this.userInput = ''

      // 사용자 메시지
      this.chatHistory.push({
        role: 'user',
        content: message
      })

      this.scrollToBottom()
      this.loading = true
      this.mascotState = 'thinking' // 답변 생성 중 고민

      try {
        const payload = { message }
        // 현재 종목 정보가 있으면 함께 전송
        if (this.chatbotStore.currentStock) {
          payload.stockCode = this.chatbotStore.currentStock.stockCode
        }
        
        const response = await http.post('/chat/general', payload)
        
        let responseText = response.data.message
        
        // 감정 태그 파싱
        let sentiment = 'idle'
        if (responseText.includes('[[SUCCESS]]')) {
          sentiment = 'success'
          responseText = responseText.replace('[[SUCCESS]]', '').trim()
        } else if (responseText.includes('[[SHOCK]]')) {
          sentiment = 'shock'
          responseText = responseText.replace('[[SHOCK]]', '').trim()
        } else if (responseText.includes('[[CAUTION]]')) {
          sentiment = 'caution'
          responseText = responseText.replace('[[CAUTION]]', '').trim()
        }
        
        this.chatHistory.push({
          role: 'assistant',
          content: responseText
        })
        
        // 감정 반응
        if (sentiment !== 'idle') {
          this.mascotState = sentiment
          // 4초 후 복귀 (충분히 보여줌)
          setTimeout(() => {
            this.mascotState = 'idle'
          }, 4000)
        } else {
             this.mascotState = 'idle'
        }
        
      } catch (err) {
        console.error('챗봇 실패:', err)
        this.chatHistory.push({
          role: 'assistant',
          content: '죄송해요, 잠시 문제가 생겼어요. 다시 시도해주세요. 🥺'
        })
        this.mascotState = 'shock' // 에러 시 당황
        setTimeout(() => { this.mascotState = 'idle' }, 2000)
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
  bottom: 30px;
  left: 30px; /* 왼쪽 하단으로 복귀 */
  z-index: 9999;
  font-family: 'Pretendard', sans-serif;
}

/* 알림 토스트 */
.notification-toast {
  position: absolute;
  bottom: 80px;
  left: 0; /* 왼쪽 정렬 */
  background: white;
  color: #333;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 260px;
  animation: slideUp 0.3s ease-out;
  border: 1px solid #eee;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.toast-message {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
}

.toast-close {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
}

/* 플로팅 버튼 */
.chat-toggle-btn {
  width: 60px; /* 크기 축소 (80px -> 60px) */
  height: 60px;
  border-radius: 50%; /* 버튼 자체는 원형 유지하되 */
  background: white;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  transition: transform 0.2s;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-toggle-btn:hover {
  transform: scale(1.1);
}

.floating-mascot {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: visible; /* 마스코트가 튀어나오도록 허용 */
}

/* 마스코트 크기 대폭 확대 (버튼 밖으로 튀어나옴) */
/* 버튼이 작아졌으므로 마스코트 스케일도 조정이 필요할 수 있으나, '꽉 찬' 느낌을 위해 유지 */
.floating-mascot :deep(.mascot-container) {
    transform: scale(1.1) !important; 
}

.chat-badge {
  position: absolute;
  top: 0;
  right: 0;
  width: 18px; /* 배지 크기도 약간 조정 */
  height: 18px;
  background: #FF5252;
  border-radius: 50%;
  color: white;
  font-size: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  z-index: 10;
}

/* 알림 시 애니메이션(커지고 작아지는 것) 제거 요청 반영 */
.chat-toggle-btn.has-notification {
    /* animation: bounce 2s infinite; 제거 */
    border: 2px solid #FF5252; /* 대신 테두리로 빨갛게 표시 */
}

/* 챗봇 패널 */
.chat-panel {
  position: absolute;
  bottom: 95px; /* 버튼 위로 살짝 띄움 */
  left: 0; /* 왼쪽 정렬 */
  width: 380px;
  height: 600px;
  background: #fdfdfd;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eee;
}

.chat-header {
  background: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px; /* 간격 넓힘 */
}

.mascot-wrapper {
  width: 75px; /* 너비 대폭 확대 */
  height: 75px; /* 높이 대폭 확대 */
  /* border-radius 제거 (원형 제한 해제) */
  background: transparent; /* 배경 제거 */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  overflow: visible; /* 잘리지 않게 설정 */
  margin-left: -10px; /* 왼쪽 여백 조정 (크기 커져서) */
  margin-right: 5px;
}

.mascot-wrapper :deep(.mascot-container) {
    transform: scale(1.2) !important; /* 헤더 마스코트 더 크게 */
}

/* 호버 시 배경 대신 마스코트만 살짝 커지거나 효과 */
.mascot-wrapper:hover {
  transform: scale(1.1);
  background: transparent;
  border-color: transparent;
}

.change-hint {
   position: absolute;
   bottom: -15px; /* 위치 조정 */
   left: 50%;
   transform: translateX(-50%);
   background: #333;
   color: white;
   font-size: 10px;
   padding: 4px 6px;
   border-radius: 4px;
   white-space: nowrap;
   opacity: 0;
   transition: opacity 0.2s;
   pointer-events: none;
   z-index: 10;
}
.mascot-wrapper:hover .change-hint {
    opacity: 1;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.header-subtitle {
  font-size: 11px;
  color: #666;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 4px;
}
.close-btn:hover { color: #333; }

/* 채팅 메시지 영역 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-welcome {
  text-align: center;
  margin-top: 40px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chat-welcome h4 {
  font-size: 18px;
  margin: 10px 0;
  color: #333;
}
.chat-welcome p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 24px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  max-width: 90%;
}

.quick-btn {
  background: white;
  border: 1px solid #ddd;
  padding: 8px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}
.quick-btn:hover {
  background: #2196F3;
  color: white;
  border-color: #2196F3;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 42px; /* 아바타 크기 확대 */
  height: 42px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden; /* 아바타는 원형 유지 */
}

.message-avatar :deep(.mascot-container) {
    transform: scale(0.6) !important; /* 내부 마스코트 크기 확대 */
}

.message-bubble {
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-content {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.message.user .message-content {
  background: #2196F3;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  border: 1px solid #eee;
}

.inline-quick-questions {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}
.inline-quick-btn {
    font-size: 12px;
    padding: 6px 12px;
    background: #e3f2fd;
    color: #1565C0;
    border: none;
    border-radius: 12px;
    cursor: pointer;
}
.inline-quick-btn:hover {
    background: #bbdefb;
}

/* 로딩 애니메이션 */
.typing-dots {
  display: inline-flex;
  gap: 4px;
}
.typing-dots span {
  width: 6px;
  height: 6px;
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
  padding: 16px;
  background: white;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 10px;
}

.chat-input-area input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  background: #f9f9f9;
}
.chat-input-area input:focus {
  background: white;
  border-color: #2196F3;
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #2196F3;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}
.send-btn:hover:not(:disabled) {
  background: #1976D2;
}
.send-btn:disabled {
  background: #ccc;
  cursor: default;
}

@media (max-width: 480px) {
    .chat-panel {
        width: 100vw;
        height: 100vh;
        bottom: 0;
        right: 0;
        border-radius: 0;
    }
    .floating-chat {
        bottom: 20px;
        right: 20px;
    }
}
</style>
