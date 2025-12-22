<template>
  <div class="survey-view">
    <PageHeader title="투자 성향 분석" :showBack="true" icon="invest" />

    <Container>
      <!-- 시작 화면 -->
      <div v-if="!started && !completed" class="survey-intro">
        <div class="intro-card">
          <div class="intro-icon">📊</div>
          <h2>나의 투자 성향 알아보기</h2>
          <p>
            8개의 간단한 질문에 답하고<br />
            나에게 맞는 투자 스타일을 찾아보세요!
          </p>
          <div class="profile-types">
            <div class="type-badge safe">🛡️ 안정형</div>
            <div class="type-badge balanced">⚖️ 균형형</div>
            <div class="type-badge aggressive">🚀 공격형</div>
          </div>
          <Button @click="startSurvey" size="large">시작하기</Button>
        </div>
      </div>

      <!-- 설문 진행 중 -->
      <div v-if="started && !completed" class="survey-progress">
        <!-- 진행률 바 -->
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
        <div class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</div>

        <!-- 질문 카드 -->
        <div class="question-card" v-if="currentQuestion">
          <h3 class="question-text">{{ currentQuestion.question }}</h3>
          
          <div class="options">
            <div 
              v-for="option in currentQuestion.options" 
              :key="option.value"
              class="option-item"
              :class="{ selected: answers[currentQuestion.id] === option.value }"
              @click="selectOption(currentQuestion.id, option.value)"
            >
              <span class="option-text">{{ option.text }}</span>
              <span class="option-check" v-if="answers[currentQuestion.id] === option.value">✓</span>
            </div>
          </div>

          <!-- 네비게이션 버튼 -->
          <div class="nav-buttons">
            <Button 
              v-if="currentIndex > 0" 
              @click="prevQuestion" 
              variant="secondary"
            >
              이전
            </Button>
            <div v-else></div>
            
            <Button 
              v-if="currentIndex < questions.length - 1"
              @click="nextQuestion"
              :disabled="!answers[currentQuestion.id]"
            >
              다음
            </Button>
            <Button 
              v-else
              @click="submitSurvey"
              :disabled="!isAllAnswered"
              variant="primary"
            >
              결과 보기
            </Button>
          </div>
        </div>
      </div>

      <!-- 결과 화면 -->
      <div v-if="completed" class="survey-result">
        <div class="result-card">
          <div class="result-icon" :class="resultType.toLowerCase()">
            {{ resultIcon }}
          </div>
          <h2 class="result-title">{{ result.profileType }}</h2>
          <p class="result-score">총점: {{ result.totalScore }}점 / 24점</p>
          
          <div class="result-description">
            <p v-if="result.profileType === '안정형'">
              원금 보존을 최우선으로 생각하며, 안정적인 수익을 추구하는 스타일입니다.
              변동성이 낮은 우량주나 배당주에 관심을 가져보세요.
            </p>
            <p v-else-if="result.profileType === '균형형'">
              위험과 수익의 균형을 중시하는 스타일입니다.
              성장주와 가치주를 적절히 배분하여 투자해보세요.
            </p>
            <p v-else>
              높은 수익을 위해 위험을 감수할 수 있는 스타일입니다.
              성장 잠재력이 높은 종목에 투자를 고려해보세요.
            </p>
          </div>

          <div class="result-actions">
            <Button @click="goToInvest" variant="primary" size="large">
              투자 정보 보러가기
            </Button>
            <Button @click="retakeSurvey" variant="secondary">
              다시 테스트하기
            </Button>
          </div>
        </div>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <p>분석 중...</p>
      </div>
    </Container>
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue'
import Container from '@/components/common/Container.vue'
import Button from '@/components/common/Button.vue'
import profileApi from '@/api/profile'

export default {
  name: 'InvestmentSurveyView',
  components: {
    PageHeader,
    Container,
    Button
  },
  data() {
    return {
      started: false,
      completed: false,
      loading: false,
      questions: [],
      currentIndex: 0,
      answers: {},
      result: null
    }
  },
  computed: {
    currentQuestion() {
      return this.questions[this.currentIndex] || null
    },
    progressPercent() {
      if (!this.questions.length) return 0
      return ((this.currentIndex + 1) / this.questions.length) * 100
    },
    isAllAnswered() {
      return this.questions.every(q => this.answers[q.id] !== undefined)
    },
    resultType() {
      return this.result?.profileType || ''
    },
    resultIcon() {
      const type = this.result?.profileType
      if (type === '안정형') return '🛡️'
      if (type === '균형형') return '⚖️'
      if (type === '공격형') return '🚀'
      return '📊'
    }
  },
  created() {
    this.loadQuestions()
  },
  methods: {
    async loadQuestions() {
      try {
        const response = await profileApi.getSurveyQuestions()
        this.questions = response.data
      } catch (err) {
        console.error('설문 질문 로딩 실패:', err)
      }
    },
    startSurvey() {
      this.started = true
      this.completed = false
      this.currentIndex = 0
      this.answers = {}
    },
    selectOption(questionId, value) {
      this.answers = { ...this.answers, [questionId]: value }
    },
    prevQuestion() {
      if (this.currentIndex > 0) {
        this.currentIndex--
      }
    },
    nextQuestion() {
      if (this.currentIndex < this.questions.length - 1) {
        this.currentIndex++
      }
    },
    async submitSurvey() {
      if (!this.isAllAnswered) return
      
      this.loading = true
      try {
        const survey = {
          investmentPeriod: this.answers[1],
          riskTolerance: this.answers[2],
          investmentGoal: this.answers[3],
          investmentExperience: this.answers[4],
          volatilityResponse: this.answers[5],
          stockRatio: this.answers[6],
          informationSource: this.answers[7],
          expectedReturn: this.answers[8]
        }
        
        const response = await profileApi.submitSurvey(survey)
        this.result = response.data
        this.completed = true
      } catch (err) {
        console.error('설문 제출 실패:', err)
        alert('설문 제출에 실패했습니다. 다시 시도해주세요.')
      } finally {
        this.loading = false
      }
    },
    retakeSurvey() {
      this.started = false
      this.completed = false
      this.currentIndex = 0
      this.answers = {}
      this.result = null
    },
    goToInvest() {
      this.$router.push({ name: 'invest' })
    }
  }
}
</script>

<style scoped>
.survey-view {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 시작 화면 */
.survey-intro {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.intro-card {
  background: white;
  border-radius: 20px;
  padding: 48px;
  text-align: center;
  max-width: 480px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.intro-icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.intro-card h2 {
  font-size: 28px;
  font-weight: 700;
  color: #212121;
  margin-bottom: 16px;
}

.intro-card p {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 32px;
}

.profile-types {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
}

.type-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.type-badge.safe {
  background: #e3f2fd;
  color: #1976d2;
}

.type-badge.balanced {
  background: #fff3e0;
  color: #f57c00;
}

.type-badge.aggressive {
  background: #ffebee;
  color: #d32f2f;
}

/* 진행 화면 */
.survey-progress {
  max-width: 600px;
  margin: 0 auto;
  padding: 40px 0;
}

.progress-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 24px;
}

.question-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.question-text {
  font-size: 22px;
  font-weight: 600;
  color: #212121;
  margin-bottom: 32px;
  text-align: center;
  line-height: 1.4;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.option-item.selected {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.option-text {
  font-size: 16px;
}

.option-check {
  font-size: 20px;
  font-weight: bold;
}

.nav-buttons {
  display: flex;
  justify-content: space-between;
}

/* 결과 화면 */
.survey-result {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.result-card {
  background: white;
  border-radius: 20px;
  padding: 48px;
  text-align: center;
  max-width: 520px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.result-icon {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  margin: 0 auto 24px;
}

.result-icon.안정형 {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
}

.result-icon.균형형 {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
}

.result-icon.공격형 {
  background: linear-gradient(135deg, #ffebee, #ffcdd2);
}

.result-title {
  font-size: 32px;
  font-weight: 700;
  color: #212121;
  margin-bottom: 8px;
}

.result-score {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.result-description {
  background: #f5f5f5;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
}

.result-description p {
  font-size: 15px;
  color: #424242;
  line-height: 1.7;
  margin: 0;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 로딩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-overlay p {
  color: white;
  margin-top: 16px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .intro-card,
  .question-card,
  .result-card {
    padding: 32px 24px;
  }

  .intro-card h2 {
    font-size: 24px;
  }

  .question-text {
    font-size: 18px;
  }

  .profile-types {
    flex-direction: column;
  }
}
</style>
