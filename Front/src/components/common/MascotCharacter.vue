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

        <!-- 고양이: 통통하고 부드러운 꼬리 -->
        <template v-else-if="type === 'cat'">
          <path d="M35 85 Q18 90 15 70 Q12 55 20 50" stroke="#F5A623" stroke-width="6" stroke-linecap="round" fill="none">
             <animate v-if="state !== 'shock'" attributeName="d" values="M35 85 Q18 90 15 70 Q12 55 20 50; M35 85 Q18 88 18 68 Q18 52 28 50; M35 85 Q18 90 15 70 Q12 55 20 50" dur="3s" repeatCount="indefinite" />
          </path>
          <!-- 꼬리 끝 오렌지 무늬 -->
          <circle cx="20" cy="50" r="4" fill="#E8912D" />
        </template>
      </g>

      <!-- 몸통 -->
      <g class="body-group">
        <!-- 다리 색상 (캐릭터별) -->
        <path d="M45 100 L45 80 L55 80 L55 100 A3 3 0 0 1 45 100" :fill="bodyColor"/>
        <path d="M65 100 L65 80 L75 80 L75 100 A3 3 0 0 1 65 100" :fill="bodyColor"/>
        
        <!-- 고양이: 귀여운 크리스마스 스웨터 -->
        <template v-if="type === 'cat'">
          <!-- 스웨터 몸통 (빨간 니트) -->
          <rect x="38" y="55" width="44" height="40" rx="10" fill="#E74C3C"/>
          <!-- 니트 질감 패턴 -->
          <path d="M40 62 L80 62" stroke="#C0392B" stroke-width="1.5" opacity="0.5"/>
          <path d="M40 68 L80 68" stroke="#C0392B" stroke-width="1.5" opacity="0.5"/>
          <path d="M40 74 L80 74" stroke="#C0392B" stroke-width="1.5" opacity="0.5"/>
          <path d="M40 80 L80 80" stroke="#C0392B" stroke-width="1.5" opacity="0.5"/>
          <path d="M40 86 L80 86" stroke="#C0392B" stroke-width="1.5" opacity="0.5"/>
          
          <!-- 눈꽃 무늬 장식 -->
          <g fill="white" opacity="0.9">
            <!-- 왼쪽 눈꽃 -->
            <circle cx="48" cy="70" r="1.5"/>
            <line x1="48" y1="66" x2="48" y2="74" stroke="white" stroke-width="1"/>
            <line x1="44" y1="70" x2="52" y2="70" stroke="white" stroke-width="1"/>
            <line x1="45" y1="67" x2="51" y2="73" stroke="white" stroke-width="0.8"/>
            <line x1="51" y1="67" x2="45" y2="73" stroke="white" stroke-width="0.8"/>
            
            <!-- 오른쪽 눈꽃 -->
            <circle cx="72" cy="70" r="1.5"/>
            <line x1="72" y1="66" x2="72" y2="74" stroke="white" stroke-width="1"/>
            <line x1="68" y1="70" x2="76" y2="70" stroke="white" stroke-width="1"/>
            <line x1="69" y1="67" x2="75" y2="73" stroke="white" stroke-width="0.8"/>
            <line x1="75" y1="67" x2="69" y2="73" stroke="white" stroke-width="0.8"/>
          </g>
          
          <!-- 중앙 하트 무늬 -->
          <path d="M57 78 Q57 75 60 77 Q63 75 63 78 Q63 81 60 84 Q57 81 57 78" fill="#FFD93D"/>
          
          <!-- 목 부분 (터틀넥 스타일) -->
          <ellipse cx="60" cy="56" rx="14" ry="5" fill="#C0392B"/>
          <path d="M46 56 Q60 52 74 56" stroke="#A93226" stroke-width="1" fill="none"/>
          
          <!-- 귀여운 방울 장식 -->
          <circle cx="60" cy="58" r="3" fill="#F1C40F" stroke="#D4AC0D" stroke-width="0.5"/>
          <ellipse cx="60" cy="57" rx="1" ry="0.5" fill="white" opacity="0.6"/>
        </template>
        
        <!-- 강아지: 귀여운 크리스마스 후드티 -->
        <template v-else-if="type === 'dog'">
          <!-- 후드티 몸통 (초록색) -->
          <rect x="38" y="55" width="44" height="40" rx="10" fill="#27AE60"/>
          <!-- 후드티 질감 패턴 -->
          <path d="M40 62 L80 62" stroke="#1E8449" stroke-width="1.5" opacity="0.5"/>
          <path d="M40 70 L80 70" stroke="#1E8449" stroke-width="1.5" opacity="0.5"/>
          <path d="M40 78 L80 78" stroke="#1E8449" stroke-width="1.5" opacity="0.5"/>
          <path d="M40 86 L80 86" stroke="#1E8449" stroke-width="1.5" opacity="0.5"/>
          
          <!-- 중앙 눈사람 무늬 -->
          <circle cx="60" cy="70" r="4" fill="white"/>
          <circle cx="60" cy="78" r="5" fill="white"/>
          <circle cx="58" cy="69" r="0.8" fill="#333"/>
          <circle cx="62" cy="69" r="0.8" fill="#333"/>
          <path d="M59 71 L61 71" stroke="#E67E22" stroke-width="1.5" stroke-linecap="round"/>
          
          <!-- 캥거루 포켓 -->
          <path d="M45 82 Q60 88 75 82" stroke="#1E8449" stroke-width="2" fill="none"/>
          
          <!-- 후드 칼라 부분 -->
          <ellipse cx="60" cy="57" rx="16" ry="6" fill="#1E8449"/>
          <path d="M44 57 Q60 50 76 57" stroke="#145A32" stroke-width="1.5" fill="none"/>
          
          <!-- 후드 끈 -->
          <line x1="52" y1="58" x2="52" y2="68" stroke="#F9E79F" stroke-width="2"/>
          <line x1="68" y1="58" x2="68" y2="68" stroke="#F9E79F" stroke-width="2"/>
          <circle cx="52" cy="68" r="2" fill="#F1C40F"/>
          <circle cx="68" cy="68" r="2" fill="#F1C40F"/>
        </template>
        
        <!-- 사자: 원래 정장 -->
        <template v-else>
          <path d="M40 90 L80 90 L75 60 L45 60 Z" fill="#2C3E50"/>
          <rect x="38" y="55" width="44" height="40" rx="8" fill="#2C3E50"/>
          <path d="M60 55 L60 85" stroke="#1a2530" stroke-width="1"/>
          
          <path d="M50 55 L60 70 L70 55 Z" fill="white"/>
          <path d="M60 55 L57 60 L60 72 L63 60 Z" fill="#FF6B6B"/>
        </template>
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

        <!-- 2. 강아지 머리 (귀여운 시바견 스타일) -->
        <template v-else-if="type === 'dog'">
           <!-- 귀 (작고 통통한 삼각 귀) -->
           <g class="ears">
              <!-- 왼쪽 귀 -->
              <path d="M36 32 Q32 15 41 12 Q50 15 46 32 Z" fill="#D4A574" stroke="#B8956E" stroke-width="1.5" />
              <path d="M38 29 Q35 18 42 16 Q49 18 45 29 Z" fill="#FFE4C4" />
              <!-- 오른쪽 귀 -->
              <path d="M84 32 Q88 15 79 12 Q70 15 74 32 Z" fill="#D4A574" stroke="#B8956E" stroke-width="1.5" />
              <path d="M82 29 Q85 18 78 16 Q71 18 75 29 Z" fill="#FFE4C4" />
           </g>
           
           <!-- 머리 (도톰하고 귀여운 형태) -->
           <ellipse cx="60" cy="42" rx="28" ry="26" fill="#F5D5A0" stroke="#D4A574" stroke-width="1.5"/>
           
           <!-- 볼 마킹 (시바 특유의 흰색 마킹) -->
           <ellipse cx="45" cy="48" rx="10" ry="8" fill="#FFF8EE" />
           <ellipse cx="75" cy="48" rx="10" ry="8" fill="#FFF8EE" />
           
           <!-- 주둥이 (도톰하고 귀여운) -->
           <ellipse cx="60" cy="52" rx="14" ry="10" fill="#FFF3E0" />
           
           <!-- 눈썹 느낌의 마킹 -->
           <ellipse cx="48" cy="34" rx="4" ry="2" fill="#D4A574" opacity="0.6" />
           <ellipse cx="72" cy="34" rx="4" ry="2" fill="#D4A574" opacity="0.6" />
        </template>

        <!-- 3. 고양이 머리 (오렌지 치즈냥) -->
        <template v-else-if="type === 'cat'">
           <!-- 귀 (적당한 크기의 귀여운 귀) -->
           <g class="ears">
              <path d="M38 32 L32 12 L50 25 Z" fill="#F5A623" stroke="#E8912D" stroke-width="2" />
              <path d="M82 32 L88 12 L70 25 Z" fill="#F5A623" stroke="#E8912D" stroke-width="2" />
              <!-- 귓속 (분홍) -->
              <path d="M39 28 L35 16 L46 24 Z" fill="#FFB7B2" />
              <path d="M81 28 L85 16 L74 24 Z" fill="#FFB7B2" />
           </g>
           
           <!-- 머리 (통통하고 귀여운 형태) -->
           <ellipse cx="60" cy="44" rx="30" ry="27" fill="#FFCC80" stroke="#F5A623" stroke-width="1.5" />
           
           <!-- 이마 치즈 무늬 (M자 무늬) -->
           <path d="M50 20 L53 28 L60 22 L67 28 L70 20" stroke="#E8912D" stroke-width="2.5" stroke-linecap="round" fill="none" />
           <path d="M60 22 L60 30" stroke="#E8912D" stroke-width="2" stroke-linecap="round" />

           <!-- 수염 (부드러운 수염) -->
           <g stroke="#A1887F" stroke-width="1.5" stroke-linecap="round">
              <!-- 왼쪽 -->
              <line x1="30" y1="44" x2="12" y2="40" />
              <line x1="30" y1="48" x2="10" y2="48" />
              <line x1="30" y1="52" x2="12" y2="56" />
              <!-- 오른쪽 -->
              <line x1="90" y1="44" x2="108" y2="40" />
              <line x1="90" y1="48" x2="110" y2="48" />
              <line x1="90" y1="52" x2="108" y2="56" />
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
           <!-- 고양이 코 (하트 모양) -->
           <path d="M56 46 Q60 44 64 46 Q60 50 56 46" fill="#FF8A80" />
           <!-- 고양이 입 (w 모양) -->
           <path d="M54 52 Q57 55 60 52 Q63 55 66 52" stroke="#8D6E63" stroke-width="1.5" fill="none" stroke-linecap="round" />
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
          <path d="M40 65 Q35 75 35 85" :stroke="armColor" stroke-width="7" stroke-linecap="round" />
          <rect x="25" y="80" width="18" height="14" rx="2" fill="#5D4037" stroke="#3E2723" />
          <path d="M31 80 Q34 75 37 80" stroke="#3E2723" stroke-width="2" fill="none"/>
          
          <g class="right-arm">
            <template v-if="state === 'greeting'">
              <path d="M80 65 Q95 55 95 30" :stroke="armColor" stroke-width="7" stroke-linecap="round">
                <animate attributeName="d" values="M80 65 Q95 55 95 30; M80 65 Q98 55 98 25; M80 65 Q95 55 95 30" dur="0.8s" repeatCount="indefinite" />
              </path>
              <circle cx="95" cy="30" r="6" :fill="handColor">
                 <animate attributeName="cx" values="95; 98; 95" dur="0.8s" repeatCount="indefinite" />
                 <animate attributeName="cy" values="30; 25; 30" dur="0.8s" repeatCount="indefinite" />
              </circle>
            </template>
            <template v-else>
               <path d="M80 65 Q90 75 90 85" :stroke="armColor" stroke-width="7" stroke-linecap="round" />
               <circle cx="90" cy="85" r="5" :fill="handColor" />
            </template>
          </g>
        </template>

        <!-- 2. 고민 -->
        <template v-if="state === 'thinking'">
           <path d="M40 65 Q38 75 48 80" :stroke="armColor" stroke-width="7" stroke-linecap="round" />
           <path d="M80 65 Q82 75 72 80" :stroke="armColor" stroke-width="7" stroke-linecap="round" />
           <rect x="45" y="70" width="30" height="22" rx="2" fill="#333" transform="rotate(-5 60 81)" />
           <rect x="47" y="72" width="26" height="18" rx="1" fill="#81D4FA" transform="rotate(-5 60 81)" />
           <polyline points="48,85 55,80 62,82 70,75" fill="none" stroke="red" stroke-width="2" transform="rotate(-5 60 81)" />
        </template>

        <!-- 3. 성공 -->
        <template v-if="state === 'success'">
           <path d="M40 65 Q30 55 25 35" :stroke="armColor" stroke-width="7" stroke-linecap="round" />
           <path d="M80 65 Q90 55 95 35" :stroke="armColor" stroke-width="7" stroke-linecap="round" />
           <circle cx="25" cy="35" r="12" fill="#F1C40F" stroke="#F39C12" />
           <text x="20" y="40" font-size="14" fill="#D35400">$</text>
           <circle cx="95" cy="35" r="12" fill="#F1C40F" stroke="#F39C12" />
           <text x="90" y="40" font-size="14" fill="#D35400">$</text>
        </template>

        <!-- 4. 충격 -->
        <template v-if="state === 'shock'">
           <path d="M40 70 Q30 50 45 35" :stroke="armColor" stroke-width="7" stroke-linecap="round">
            <animate attributeName="d" values="M40 70 Q30 50 45 35; M40 70 Q28 52 43 37; M40 70 Q30 50 45 35" dur="0.2s" repeatCount="indefinite" />
           </path>
           <path d="M80 70 Q90 50 75 35" :stroke="armColor" stroke-width="7" stroke-linecap="round">
            <animate attributeName="d" values="M80 70 Q90 50 75 35; M80 70 Q92 52 77 37; M80 70 Q90 50 75 35" dur="0.2s" repeatCount="indefinite" />
           </path>
           <circle cx="45" cy="35" r="6" :fill="handColor" />
           <circle cx="75" cy="35" r="6" :fill="handColor" />
        </template>

        <!-- 5. 신중/애매 -->
        <template v-if="state === 'caution'">
           <path d="M40 65 Q35 75 35 85" :stroke="armColor" stroke-width="7" stroke-linecap="round" />
           <circle cx="35" cy="85" r="5" :fill="handColor" />
           <path d="M80 65 Q100 50 85 25" :stroke="armColor" stroke-width="7" stroke-linecap="round">
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
       if (this.type === 'dog') return '#F5D5A0' // 시바견 (황금빛)
       if (this.type === 'cat') return '#FFCC80' // 오렌지
       return '#E6A800' // 사자 (노랑)
    },
    handColor() {
       // 손 색깔은 몸 색깔과 맞춤
       if (this.type === 'dog') return '#F5D5A0'
       if (this.type === 'cat') return '#FFCC80'
       return '#E6A800'
    },
    armColor() {
       // 고양이는 빨간 스웨터, 강아지는 초록 후드티, 사자는 네이비 정장
       if (this.type === 'cat') return '#E74C3C'
       if (this.type === 'dog') return '#27AE60'
       return '#2C3E50'
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
