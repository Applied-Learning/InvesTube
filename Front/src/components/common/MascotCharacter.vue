<template>
  <div class="mascot-container" :class="[state, type]">
    <svg class="mascot-svg" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
      <!-- 그림자 -->
      <ellipse cx="60" cy="110" rx="30" ry="5" fill="rgba(0,0,0,0.1)" />

      <!-- 꼬리 (캐릭터별 다름) -->
      <g class="tail-group">
        <!-- 사자: 끝이 둥근 꼬리 -->
        <template v-if="type === 'lion'">
          <path class="tail" d="M35 85 Q20 85 25 70" stroke="#CD853F" stroke-width="4" fill="none" stroke-linecap="round">
            <animate v-if="state !== 'shock'" attributeName="d" values="M35 85 Q20 85 25 70; M35 85 Q15 95 20 60; M35 85 Q20 85 25 70" dur="2s" repeatCount="indefinite" />
            <animate v-if="state === 'shock'" attributeName="d" values="M35 85 Q40 95 45 90; M35 85 Q38 96 43 91; M35 85 Q40 95 45 90" dur="0.2s" repeatCount="indefinite" />
          </path>
          <circle cx="25" cy="70" r="4" fill="#8B4513">
             <animate v-if="state !== 'shock'" attributeName="cy" values="70; 60; 70" dur="2s" repeatCount="indefinite" />
             <animate v-if="state !== 'shock'" attributeName="cx" values="25; 20; 25" dur="2s" repeatCount="indefinite" />
          </circle>
        </template>

        <!-- 강아지: 짧고 통통한 꼬리 (기분 좋으면 파닥파닥) -->
        <template v-else-if="type === 'dog'">
          <path d="M35 85 Q25 80 25 70" stroke="#C19A6B" stroke-width="6" stroke-linecap="round" fill="none">
             <animate v-if="state === 'success' || state === 'greeting'" attributeName="d" values="M35 85 Q25 80 25 70; M35 85 Q15 85 15 75; M35 85 Q25 80 25 70" dur="0.3s" repeatCount="indefinite" />
          </path>
        </template>

        <!-- 고양이: 길고 우아한 꼬리 -->
        <template v-else-if="type === 'cat'">
          <path d="M35 85 Q15 85 15 60 Q15 40 25 35" stroke="#95A5A6" stroke-width="4" stroke-linecap="round" fill="none">
             <animate v-if="state !== 'shock'" attributeName="d" values="M35 85 Q15 85 15 60 Q15 40 25 35; M35 85 Q15 85 20 60 Q25 40 35 45; M35 85 Q15 85 15 60 Q15 40 25 35" dur="4s" repeatCount="indefinite" />
          </path>
        </template>
      </g>

      <!-- 몸통 (공통 정장) -->
      <g class="body-group">
        <!-- 다리 색상 (캐릭터별) -->
        <path d="M45 100 L45 80 L55 80 L55 100 A3 3 0 0 1 45 100" :fill="bodyColor"/>
        <path d="M65 100 L65 80 L75 80 L75 100 A3 3 0 0 1 65 100" :fill="bodyColor"/>
        
        <path d="M40 90 L80 90 L75 60 L45 60 Z" fill="#2C3E50"/>
        <rect x="38" y="55" width="44" height="40" rx="8" fill="#2C3E50"/>
        <path d="M60 55 L60 85" stroke="#1a2530" stroke-width="1"/>
        
        <path d="M50 55 L60 70 L70 55 Z" fill="white"/>
        <path d="M60 55 L57 60 L60 72 L63 60 Z" fill="#FF6B6B"/> 
      </g>

      <!-- 머리 -->
      <g class="head-group">
        <!-- 1. 사자 머리 -->
        <template v-if="type === 'lion'">
          <!-- 갈기 -->
          <circle cx="60" cy="40" r="32" fill="#D35400" />
          <circle cx="60" cy="40" r="32" fill="none" stroke="#A04000" stroke-width="3" stroke-dasharray="6,6"/>
          <!-- 귀 -->
          <g class="ears">
              <circle cx="35" cy="20" r="9" fill="#E6A800" stroke="#D35400" stroke-width="2"/>
              <circle cx="85" cy="20" r="9" fill="#E6A800" stroke="#D35400" stroke-width="2"/>
              <circle cx="35" cy="20" r="5" fill="#FFE082" />
              <circle cx="85" cy="20" r="5" fill="#FFE082" />
          </g>
          <!-- 얼굴 -->
          <circle cx="60" cy="42" r="22" fill="#F4D03F" />
        </template>

        <!-- 2. 강아지 머리 (누렁이) -->
        <template v-else-if="type === 'dog'">
           <!-- 귀 (축 처진 귀 - 머리에 딱 붙임) -->
           <g class="ears">
              <!-- 왼쪽 귀: 머리 중심(60,40) 기준으로 위치 수정 -->
              <path d="M40 30 Q30 30 25 45 Q30 55 45 45" fill="#8D6E63" stroke="#5D4037" stroke-width="2" />
              <!-- 오른쪽 귀 -->
              <path d="M80 30 Q90 30 95 45 Q90 55 75 45" fill="#8D6E63" stroke="#5D4037" stroke-width="2" />
           </g>
           <!-- 머리 (귀 위로 덮어서 연결 부위 자연스럽게) -->
           <circle cx="60" cy="40" r="28" fill="#E6C288" stroke="#D7CCC8" stroke-width="1"/>
           <ellipse cx="60" cy="50" rx="12" ry="9" fill="#FFF3E0" /> <!-- 주둥이 주변 -->
        </template>

        <!-- 3. 고양이 머리 (회색냥) -->
        <!-- 3. 고양이 머리 (회색냥) -->
        <!-- 3. 고양이 머리 (회색냥 - 최종 수정) -->
        <template v-else-if="type === 'cat'">
           <!-- 귀 (정석적인 삼각형 귀) -->
           <g class="ears">
              <path d="M35 35 L28 10 L55 25 Z" fill="#95A5A6" stroke="#7F8C8D" stroke-width="2" />
              <path d="M85 35 L92 10 L65 25 Z" fill="#95A5A6" stroke="#7F8C8D" stroke-width="2" />
              <!-- 귓속 -->
              <path d="M36 32 L32 16 L48 24 Z" fill="#FFAB91" />
              <path d="M84 32 L88 16 L72 24 Z" fill="#FFAB91" />
           </g>
           
           <!-- 머리 (약간 둥글넙적한 귀여운 형태) -->
           <ellipse cx="60" cy="45" rx="32" ry="26" fill="#ECF0F1" stroke="#BDC3C7" stroke-width="1.5" />
           
           <!-- 이마 무늬 (심플한 줄무늬) -->
           <path d="M60 22 L60 30" stroke="#BDC3C7" stroke-width="2" stroke-linecap="round" />
           <path d="M54 24 L54 28" stroke="#BDC3C7" stroke-width="2" stroke-linecap="round" />
           <path d="M66 24 L66 28" stroke="#BDC3C7" stroke-width="2" stroke-linecap="round" />

           <!-- 수염 (옆으로 시원하게 뻗음) -->
           <g stroke="#95A5A6" stroke-width="1.5" stroke-linecap="round">
              <!-- 왼쪽 -->
              <line x1="28" y1="46" x2="15" y2="44" />
              <line x1="28" y1="50" x2="18" y2="50" />
              <!-- 오른쪽 -->
              <line x1="92" y1="46" x2="105" y2="44" />
              <line x1="92" y1="50" x2="102" y2="50" />
           </g>
        </template>

        <!-- 공통 요소 (표정, 눈, 코, 소품 효과) -->
        
        <!-- 식은땀 (충격 시) -->
        <g v-if="state === 'shock'" class="sweat-drops">
            <path d="M80 30 Q83 25 86 30 Q89 35 83 38 Q80 35 80 30" fill="#81D4FA" opacity="0.8">
                <animateTransform attributeName="transform" type="translate" values="0 0; 0 10; 0 0" dur="1s" repeatCount="indefinite"/>
                <animate attributeName="opacity" values="0; 1; 0" dur="1s" repeatCount="indefinite"/>
            </path>
            <path d="M35 35 Q38 30 41 35 Q44 40 38 43 Q35 40 35 35" fill="#81D4FA" opacity="0.8">
                <animateTransform attributeName="transform" type="translate" values="0 0; 0 8; 0 0" dur="1.2s" repeatCount="indefinite"/>
                <animate attributeName="opacity" values="0; 1; 0" dur="1.2s" repeatCount="indefinite"/>
            </path>
        </g>
        
        <!-- 물음표 (신중 시) -->
        <g v-if="state === 'caution'" class="question-mark">
            <text x="75" y="30" font-size="24" fill="#E6A800" font-weight="bold" transform="rotate(15 75 30)">?</text>
            <animateTransform attributeName="transform" type="scale" values="1; 1.2; 1" dur="2s" repeatCount="indefinite"/>
        </g>

        <!-- 볼터치 (충격 아닐 때 공통) -->
        <template v-if="state !== 'shock'">
           <circle cx="46" cy="50" r="5" fill="#FF8A80" opacity="0.5" />
           <circle cx="74" cy="50" r="5" fill="#FF8A80" opacity="0.5" />
        </template>
        <template v-else>
           <circle cx="46" cy="50" r="4" fill="#64B5F6" opacity="0.4" /> 
           <circle cx="74" cy="50" r="4" fill="#64B5F6" opacity="0.4" />
        </template>
        
        <!-- 주둥이/코/입 -->
        <template v-if="type === 'lion'">
           <ellipse cx="60" cy="48" rx="9" ry="7" fill="#FFF8DC" />
           <path d="M57 46 Q60 48 63 46 Q60 51 57 46" fill="#4E342E" />
        </template>
        <template v-else-if="type === 'dog'">
           <ellipse cx="60" cy="48" rx="8" ry="6" fill="#3E2723" /> <!-- 개코 -->
        </template>
        <template v-else-if="type === 'cat'">
           <!-- 고양이 코 (아주 작게) -->
           <path d="M58.5 48 Q60 47 61.5 48 Q60 49.5 58.5 48" fill="#FFAB91" /> 
           <!-- 고양이 입 (작게) -->
           <path d="M60 49 Q58 51 56 50 M60 49 Q62 51 64 50" stroke="#7F8C8D" stroke-width="1" fill="none" />
        </template>

        <!-- 눈 (공통 로직 사용) -->
        <g class="eyes">
          <template v-if="state === 'thinking'">
             <!-- 고민 -->
             <circle cx="53" cy="38" r="3" fill="#333" />
             <rect x="65" y="37" width="8" height="2" rx="1" fill="#333" />
          </template>
          <template v-else-if="state === 'success'">
             <!-- 성공 -->
             <path d="M48 38 Q53 34 58 38" stroke="#333" stroke-width="3" fill="none" stroke-linecap="round"/>
             <path d="M62 38 Q67 34 72 38" stroke="#333" stroke-width="3" fill="none" stroke-linecap="round"/>
          </template>
          <template v-else-if="state === 'shock'">
             <!-- 충격 -->
             <path d="M48 36 L56 42 M56 36 L48 42" stroke="#333" stroke-width="3" stroke-linecap="round" />
             <path d="M64 36 L72 42 M72 36 L64 42" stroke="#333" stroke-width="3" stroke-linecap="round" />
             <circle cx="60" cy="55" r="4" fill="#333" /> 
          </template>
          <template v-else-if="state === 'caution'">
             <!-- 신중 -->
             <circle cx="54" cy="38" r="4" fill="#333" />
             <rect x="65" y="38" width="8" height="2" rx="1" fill="#333" />
          </template>
          <template v-else>
             <!-- 기본 - 모든 캐릭터 공통적으로 초롱초롱한 눈 사용 -->
             <g>
                <circle cx="52" cy="38" r="4.5" fill="#333" />
                <circle cx="53.5" cy="36.5" r="1.5" fill="white" />
             </g>
             <g>
                <circle cx="68" cy="38" r="4.5" fill="#333" />
                <circle cx="69.5" cy="36.5" r="1.5" fill="white" />
             </g>
          </template>
        </g>
        
        <!-- 입모양 (신중 시 삐죽 - 개, 고양이 공통 처리) -->
        <g v-if="state === 'caution' && type !== 'cat'"> <!-- 고양이는 이미 입 그림 -->
           <path d="M58 53 L64 51" stroke="#4E342E" stroke-width="2" stroke-linecap="round" />
        </g>
      </g>

      <!-- 팔 & 소품 (상태별) -->
      <g class="arms">
        <!-- 1. 기본/인사 -->
        <template v-if="state === 'idle' || state === 'greeting'">
          <path d="M40 65 Q35 75 35 85" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
          <rect x="25" y="80" width="18" height="14" rx="2" fill="#5D4037" stroke="#3E2723" />
          <path d="M31 80 Q34 75 37 80" stroke="#3E2723" stroke-width="2" fill="none"/>
          
          <g class="right-arm">
            <template v-if="state === 'greeting'">
              <path d="M80 65 Q95 55 95 30" stroke="#2C3E50" stroke-width="7" stroke-linecap="round">
                <animate attributeName="d" values="M80 65 Q95 55 95 30; M80 65 Q98 55 98 25; M80 65 Q95 55 95 30" dur="0.8s" repeatCount="indefinite" />
              </path>
              <circle cx="95" cy="30" r="6" :fill="handColor">
                 <animate attributeName="cx" values="95; 98; 95" dur="0.8s" repeatCount="indefinite" />
                 <animate attributeName="cy" values="30; 25; 30" dur="0.8s" repeatCount="indefinite" />
              </circle>
            </template>
            <template v-else>
               <path d="M80 65 Q90 75 90 85" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
               <circle cx="90" cy="85" r="5" :fill="handColor" />
            </template>
          </g>
        </template>

        <!-- 2. 고민 -->
        <template v-if="state === 'thinking'">
           <path d="M40 65 Q38 75 48 80" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
           <path d="M80 65 Q82 75 72 80" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
           <rect x="45" y="70" width="30" height="22" rx="2" fill="#333" transform="rotate(-5 60 81)" />
           <rect x="47" y="72" width="26" height="18" rx="1" fill="#81D4FA" transform="rotate(-5 60 81)" />
           <polyline points="48,85 55,80 62,82 70,75" fill="none" stroke="red" stroke-width="2" transform="rotate(-5 60 81)" />
        </template>

        <!-- 3. 성공 -->
        <template v-if="state === 'success'">
           <path d="M40 65 Q30 55 25 35" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
           <path d="M80 65 Q90 55 95 35" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
           <circle cx="25" cy="35" r="12" fill="#F1C40F" stroke="#F39C12" />
           <text x="20" y="40" font-size="14" fill="#D35400">$</text>
           <circle cx="95" cy="35" r="12" fill="#F1C40F" stroke="#F39C12" />
           <text x="90" y="40" font-size="14" fill="#D35400">$</text>
        </template>

        <!-- 4. 충격 -->
        <template v-if="state === 'shock'">
           <path d="M40 70 Q30 50 45 35" stroke="#2C3E50" stroke-width="7" stroke-linecap="round">
            <animate attributeName="d" values="M40 70 Q30 50 45 35; M40 70 Q28 52 43 37; M40 70 Q30 50 45 35" dur="0.2s" repeatCount="indefinite" />
           </path>
           <path d="M80 70 Q90 50 75 35" stroke="#2C3E50" stroke-width="7" stroke-linecap="round">
            <animate attributeName="d" values="M80 70 Q90 50 75 35; M80 70 Q92 52 77 37; M80 70 Q90 50 75 35" dur="0.2s" repeatCount="indefinite" />
           </path>
           <circle cx="45" cy="35" r="6" :fill="handColor" />
           <circle cx="75" cy="35" r="6" :fill="handColor" />
        </template>

        <!-- 5. 신중/애매 -->
        <template v-if="state === 'caution'">
           <path d="M40 65 Q35 75 35 85" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
           <circle cx="35" cy="85" r="5" :fill="handColor" />
           <path d="M80 65 Q100 50 85 25" stroke="#2C3E50" stroke-width="7" stroke-linecap="round">
              <animate attributeName="d" values="M80 65 Q100 50 85 25; M80 65 Q102 52 87 27; M80 65 Q100 50 85 25" dur="1s" repeatCount="indefinite" />
           </path>
           <circle cx="85" cy="25" r="5" :fill="handColor" />
        </template>
      </g>
    </svg>
  </div>
</template>

<script>
export default {
  name: 'MascotCharacter',
  props: {
    state: {
      type: String,
      default: 'idle',
      validator: (value) => ['idle', 'thinking', 'success', 'greeting', 'shock', 'caution'].includes(value)
    },
    type: {
      type: String,
      default: 'lion',
      validator: (value) => ['lion', 'dog', 'cat'].includes(value)
    }
  },
  computed: {
    bodyColor() {
       if (this.type === 'dog') return '#E6C288' // 베이지
       if (this.type === 'cat') return '#BDC3C7' // 회색
       return '#E6A800' // 사자 (노랑)
    },
    handColor() {
       // 손 색깔은 몸 색깔과 맞춤
       if (this.type === 'dog') return '#E6C288'
       if (this.type === 'cat') return '#BDC3C7'
       return '#E6A800'
    }
  }
}
</script>

<style scoped>
.mascot-container {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 90px;
  height: 90px;
  cursor: default;
  user-select: none;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.mascot-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
}

/* 애니메이션 */
.mascot-container.idle .head-group,
.mascot-container.idle .body-group,
.mascot-container.idle .arms {
  animation: breathe 3s ease-in-out infinite;
}
@keyframes breathe {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.03); }
}

.mascot-container.thinking .head-group {
  animation: lookDown 2s ease-in-out infinite;
  transform-origin: 60px 50px;
}
@keyframes lookDown {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(8deg) translateY(3px); }
  60% { transform: rotate(8deg) translateY(3px); }
}

.mascot-container.success {
  animation: jump 0.6s cubic-bezier(0.25, 1.5, 0.5, 1) infinite alternate;
}
@keyframes jump {
  0% { transform: translateY(0); }
  100% { transform: translateY(-18px) rotate(-5deg); }
}

.mascot-container.greeting {
  animation: slightWiggle 2s ease-in-out infinite;
}
@keyframes slightWiggle {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(3deg); }
}

.mascot-container.shock {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both infinite;
} 
@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

.mascot-container.caution .head-group {
  animation: tiltHead 2s ease-in-out infinite;
  transform-origin: 60px 80px; 
}
@keyframes tiltHead {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(-5deg); }
}
</style>
