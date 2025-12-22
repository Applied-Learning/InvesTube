<template>
  <div class="mascot-container" :class="state">
    <svg class="mascot-svg" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
      <!-- 그림자 -->
      <ellipse cx="60" cy="110" rx="30" ry="5" fill="rgba(0,0,0,0.1)" />

      <!-- 꼬리 (신중: 살짝만 흔들림) -->
      <path class="tail" d="M35 85 Q20 85 25 70" stroke="#CD853F" stroke-width="4" fill="none" stroke-linecap="round">
        <animate v-if="state !== 'shock'" attributeName="d" values="M35 85 Q20 85 25 70; M35 85 Q15 95 20 60; M35 85 Q20 85 25 70" dur="2s" repeatCount="indefinite" />
        <animate v-if="state === 'shock'" attributeName="d" values="M35 85 Q40 95 45 90; M35 85 Q38 96 43 91; M35 85 Q40 95 45 90" dur="0.2s" repeatCount="indefinite" />
      </path>

      <!-- 몸통 (정장) -->
      <g class="body-group">
        <path d="M45 100 L45 80 L55 80 L55 100 A3 3 0 0 1 45 100" fill="#E6A800"/>
        <path d="M65 100 L65 80 L75 80 L75 100 A3 3 0 0 1 65 100" fill="#E6A800"/>
        
        <path d="M40 90 L80 90 L75 60 L45 60 Z" fill="#2C3E50"/>
        <rect x="38" y="55" width="44" height="40" rx="8" fill="#2C3E50"/>
        <path d="M60 55 L60 85" stroke="#1a2530" stroke-width="1"/>
        
        <path d="M50 55 L60 70 L70 55 Z" fill="white"/>
        <path d="M60 55 L57 60 L60 72 L63 60 Z" fill="#FF6B6B"/> 
      </g>

      <!-- 머리 -->
      <g class="head-group">
        <!-- 갈기 -->
        <circle cx="60" cy="40" r="32" fill="#D35400" />
        <circle cx="60" cy="40" r="32" fill="none" stroke="#A04000" stroke-width="3" stroke-dasharray="6,6"/>
        
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
        
        <!-- 물음표 (신중 시) ❓ -->
        <g v-if="state === 'caution'" class="question-mark">
            <text x="75" y="30" font-size="24" fill="#E6A800" font-weight="bold" transform="rotate(15 75 30)">?</text>
            <animateTransform attributeName="transform" type="scale" values="1; 1.2; 1" dur="2s" repeatCount="indefinite"/>
        </g>

        <!-- 귀 -->
        <g class="ears">
            <circle cx="35" cy="20" r="9" fill="#E6A800" stroke="#D35400" stroke-width="2"/>
            <circle cx="85" cy="20" r="9" fill="#E6A800" stroke="#D35400" stroke-width="2"/>
            <circle cx="35" cy="20" r="5" fill="#FFE082" />
            <circle cx="85" cy="20" r="5" fill="#FFE082" />
            <animateTransform v-if="state === 'shock'" attributeName="transform" type="rotate" values="5 60 40; -5 60 40; 5 60 40" dur="0.2s" repeatCount="indefinite"/>
            <animateTransform v-else attributeName="transform" type="rotate" values="0 60 40; 2 60 40; 0 60 40; -2 60 40; 0 60 40" dur="3s" repeatCount="indefinite"/>
        </g>

        <!-- 얼굴 -->
        <circle cx="60" cy="42" r="22" fill="#F4D03F" />
        
        <!-- 볼터치 (충격 시 창백) -->
        <circle v-if="state !== 'shock'" cx="46" cy="48" r="4" fill="#FF8A80" opacity="0.6" />
        <circle v-if="state !== 'shock'" cx="74" cy="48" r="4" fill="#FF8A80" opacity="0.6" />
        <circle v-if="state === 'shock'" cx="46" cy="48" r="4" fill="#64B5F6" opacity="0.4" /> 
        <circle v-if="state === 'shock'" cx="74" cy="48" r="4" fill="#64B5F6" opacity="0.4" />
        
        <!-- 주둥이 -->
        <ellipse cx="60" cy="48" rx="9" ry="7" fill="#FFF8DC" />
        
        <!-- 코 -->
        <path d="M57 46 Q60 48 63 46 Q60 51 57 46" fill="#4E342E" />

        <!-- 눈 -->
        <g class="eyes">
          <template v-if="state === 'thinking'">
             <!-- 고민 -->
             <circle cx="53" cy="38" r="4" fill="#333" />
             <rect x="65" y="37" width="8" height="2" rx="1" fill="#333" />
          </template>
          <template v-else-if="state === 'success'">
             <!-- 성공 -->
             <path d="M48 38 Q53 32 58 38" stroke="#333" stroke-width="3" fill="none" stroke-linecap="round"/>
             <path d="M62 38 Q67 32 72 38" stroke="#333" stroke-width="3" fill="none" stroke-linecap="round"/>
          </template>
          <template v-else-if="state === 'shock'">
             <!-- 충격 -->
             <path d="M48 35 L56 41 M56 35 L48 41" stroke="#333" stroke-width="3" stroke-linecap="round" />
             <path d="M64 35 L72 41 M72 35 L64 41" stroke="#333" stroke-width="3" stroke-linecap="round" />
             <circle cx="60" cy="55" r="4" fill="#333" /> 
          </template>
          <template v-else-if="state === 'caution'">
             <!-- 신중: 비대칭 눈 (의심/고민) -->
             <circle cx="54" cy="38" r="4" fill="#333" /> <!-- 왼쪽: 동그란 눈 -->
             <rect x="65" y="38" width="8" height="2" rx="1" fill="#333" /> <!-- 오른쪽: 가늘게 뜬 눈 -->
          </template>
          <template v-else>
             <!-- 기본 -->
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
        
        <!-- 입모양 (신중 시 삐죽) -->
        <g v-if="state === 'caution'">
           <path d="M58 53 L64 51" stroke="#4E342E" stroke-width="2" stroke-linecap="round" />
        </g>
      </g>

      <!-- 팔 & 소품 -->
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
              <circle cx="95" cy="30" r="6" fill="#E6A800">
                 <animate attributeName="cx" values="95; 98; 95" dur="0.8s" repeatCount="indefinite" />
                 <animate attributeName="cy" values="30; 25; 30" dur="0.8s" repeatCount="indefinite" />
              </circle>
            </template>
            <template v-else>
               <path d="M80 65 Q90 75 90 85" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
               <circle cx="90" cy="85" r="5" fill="#E6A800" />
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
           <circle cx="45" cy="35" r="6" fill="#E6A800" />
           <circle cx="75" cy="35" r="6" fill="#E6A800" />
        </template>

        <!-- 5. 신중/애매 (추가됨!) -->
        <template v-if="state === 'caution'">
           <!-- 왼쪽 팔: 평범 -->
           <path d="M40 65 Q35 75 35 85" stroke="#2C3E50" stroke-width="7" stroke-linecap="round" />
           <circle cx="35" cy="85" r="5" fill="#E6A800" />
           <!-- 오른쪽 팔: 머리 긁적임 -->
           <path d="M80 65 Q100 50 85 25" stroke="#2C3E50" stroke-width="7" stroke-linecap="round">
              <animate attributeName="d" values="M80 65 Q100 50 85 25; M80 65 Q102 52 87 27; M80 65 Q100 50 85 25" dur="1s" repeatCount="indefinite" />
           </path>
           <circle cx="85" cy="25" r="5" fill="#E6A800" />
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

/* 5. 신중/애매 애니메이션 (추가됨!) */
.mascot-container.caution .head-group {
  animation: tiltHead 2s ease-in-out infinite;
  transform-origin: 60px 80px; 
}
@keyframes tiltHead {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(-5deg); }
}
</style>
