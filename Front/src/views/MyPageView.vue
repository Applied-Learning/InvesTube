<template>
  <div class="mypage-container">
    <PageHeader title="마이페이지" />
    
    <div class="mypage-content">
      <div class="profile-section">
        <div class="profile-header">
          <div class="profile-avatar">
            {{ authStore.nickname?.charAt(0).toUpperCase() }}
          </div>
          <div class="profile-info">
            <h2>{{ authStore.nickname }}님</h2>
            <p class="user-id">{{ authStore.id }}</p>
          </div>
        </div>
      </div>

      <div class="menu-section">
        <h3 class="section-title">내 활동</h3>
        <div class="menu-list">
          <RouterLink to="/wishlist" class="menu-item">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10 17.5L8.825 16.4417C4.5 12.5583 1.66667 10.0167 1.66667 6.91667C1.66667 4.375 3.54167 2.5 6.08333 2.5C7.51667 2.5 8.89167 3.175 10 4.24167C11.1083 3.175 12.4833 2.5 13.9167 2.5C16.4583 2.5 18.3333 4.375 18.3333 6.91667C18.3333 10.0167 15.5 12.5583 11.175 16.45L10 17.5Z" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            <span>찜한 영상</span>
            <svg class="arrow" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7.5 15L12.5 10L7.5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </RouterLink>
          
          <button class="menu-item" disabled>
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10 1.66667C5.39763 1.66667 1.66667 5.39763 1.66667 10C1.66667 14.6024 5.39763 18.3333 10 18.3333C14.6024 18.3333 18.3333 14.6024 18.3333 10C18.3333 5.39763 14.6024 1.66667 10 1.66667Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M10 10V6.66667M10 13.3333H10.0083" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>내가 등록한 영상</span>
            <svg class="arrow" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7.5 15L12.5 10L7.5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          
          <button class="menu-item" disabled>
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M2.5 5.83333H17.5M5.83333 5.83333V3.33333C5.83333 2.8731 6.20643 2.5 6.66667 2.5H13.3333C13.7936 2.5 14.1667 2.8731 14.1667 3.33333V5.83333M7.5 9.16667V14.1667M12.5 9.16667V14.1667M3.33333 5.83333H16.6667V16.6667C16.6667 17.1269 16.2936 17.5 15.8333 17.5H4.16667C3.70643 17.5 3.33333 17.1269 3.33333 16.6667V5.83333Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>내가 작성한 게시글</span>
            <svg class="arrow" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7.5 15L12.5 10L7.5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="menu-section">
        <h3 class="section-title">설정</h3>
        <div class="menu-list">
          <button class="menu-item" disabled>
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10 12.5C11.3807 12.5 12.5 11.3807 12.5 10C12.5 8.61929 11.3807 7.5 10 7.5C8.61929 7.5 7.5 8.61929 7.5 10C7.5 11.3807 8.61929 12.5 10 12.5Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M16.1667 12.5C16.0556 12.7569 16.0417 13.0451 16.1285 13.3118C16.2153 13.5784 16.3967 13.8056 16.6417 13.95L16.7 14C16.8933 14.1411 17.0533 14.3214 17.1694 14.5287C17.2856 14.7361 17.3553 14.9657 17.3736 15.2021C17.392 15.4385 17.3587 15.6763 17.2758 15.8989C17.1929 16.1215 17.0623 16.3238 16.8933 16.4917C16.7243 16.6596 16.521 16.7893 16.2977 16.8713C16.0744 16.9533 15.836 16.9857 15.5993 16.9664C15.3627 16.9471 15.1331 16.8765 14.926 16.7596C14.7189 16.6427 14.5393 16.4821 14.4 16.2883L14.35 16.23C14.2056 15.985 13.9784 15.8036 13.7118 15.7168C13.4451 15.63 13.1569 15.6439 12.9 15.755C12.6486 15.8611 12.4388 16.047 12.3017 16.2843C12.1646 16.5215 12.1074 16.7978 12.1389 17.0717V17.2222C12.1389 17.7025 11.9473 18.1632 11.6051 18.5054C11.2629 18.8476 10.8022 19.0392 10.3219 19.0392C9.84167 19.0392 9.38097 18.8476 9.03877 18.5054C8.69658 18.1632 8.50498 17.7025 8.50498 17.2222V17.15C8.46665 16.8652 8.33577 16.6004 8.13076 16.3954C7.92575 16.1905 7.65693 16.0563 7.36665 16.0117C7.10972 15.9249 6.82154 15.9388 6.55486 16.0256C6.28817 16.1124 6.06097 16.2772 5.91665 16.4933L5.86665 16.55C5.72281 16.7439 5.54323 16.9044 5.33613 17.0214C5.12903 17.1383 4.89942 17.2089 4.66278 17.2282C4.42614 17.2475 4.18836 17.2151 3.96506 17.1331C3.74175 17.0511 3.53847 16.9214 3.36945 16.7524C3.20044 16.5834 3.06988 16.3791 2.98695 16.1558C2.90401 15.9325 2.87069 15.6947 2.88904 15.4581C2.90739 15.2214 2.97705 14.9918 3.094 14.7847C3.21094 14.5776 3.37152 14.398 3.56498 14.2567L3.62332 14.2067C3.86834 14.0623 4.04968 13.8351 4.13651 13.5684C4.22334 13.3017 4.2095 13.0136 4.09832 12.7567C3.99219 12.5052 3.80629 12.2955 3.56905 12.1584C3.33182 12.0213 3.05551 11.9641 2.78165 11.9956H2.63109C2.15081 11.9956 1.6901 11.804 1.3479 11.4618C1.0057 11.1196 0.814102 10.6589 0.814102 10.1786C0.814102 9.69835 1.0057 9.23764 1.3479 8.89545C1.6901 8.55325 2.15081 8.36165 2.63109 8.36165H2.70332C2.98808 8.32332 3.25291 8.19244 3.45787 7.98743C3.66283 7.78242 3.79697 7.5136 3.84165 7.22332C3.92846 6.96639 3.91462 6.6782 3.82781 6.41152C3.741 6.14484 3.57623 5.91764 3.35998 5.77332L3.30165 5.72332C3.10818 5.57948 2.94761 5.3999 2.83066 5.1928C2.71371 4.9857 2.64405 4.75609 2.6257 4.51945C2.60734 4.28281 2.64066 4.04503 2.7226 3.82172C2.80455 3.59842 2.93425 3.39513 3.10327 3.22612C3.27228 3.0571 3.47657 2.92655 3.69987 2.84361C3.92318 2.76067 4.16096 2.72735 4.3976 2.7457C4.63424 2.76405 4.86384 2.83371 5.07095 2.95065C5.27805 3.0676 5.45763 3.22818 5.60165 3.42165L5.65165 3.47998C5.79596 3.72499 6.02316 3.90633 6.28984 3.99316C6.55652 4.07999 6.84471 4.06615 7.10165 3.95498H7.17387C7.42539 3.84885 7.63507 3.66295 7.77218 3.42571C7.90929 3.18847 7.96651 2.91217 7.93498 2.6383V2.48776C7.93498 2.00748 8.12659 1.54677 8.46878 1.20457C8.81098 0.862379 9.27169 0.670776 9.75196 0.670776C10.2322 0.670776 10.693 0.862379 11.0351 1.20457C11.3773 1.54677 11.5689 2.00748 11.5689 2.48776V2.55998C11.6005 2.83386 11.7346 3.08841 11.9467 3.27052C12.1588 3.45264 12.4333 3.55003 12.7167 3.54498C12.9736 3.6318 13.2617 3.64564 13.5284 3.55883C13.7951 3.47202 14.0223 3.29069 14.1667 3.04498L14.2167 2.98665C14.3605 2.79318 14.5401 2.6326 14.7472 2.51566C14.9543 2.39871 15.1839 2.32905 15.4205 2.3107C15.6572 2.29235 15.8949 2.32567 16.1183 2.40761C16.3416 2.48955 16.5453 2.61925 16.7143 2.78827C16.8833 2.95728 17.0139 3.16157 17.0968 3.38487C17.1797 3.60818 17.213 3.84596 17.1947 4.0826C17.1763 4.31924 17.1067 4.54884 16.9897 4.75595C16.8728 4.96305 16.7122 5.14263 16.5187 5.28665L16.4604 5.33665C16.2154 5.48096 16.0341 5.70816 15.9472 5.97484C15.8604 6.24152 15.8743 6.52971 15.9854 6.78665V6.85887C16.0915 7.11039 16.2774 7.32007 16.5147 7.45718C16.7519 7.59429 17.0282 7.65151 17.3021 7.61998H17.4526C17.9329 7.61998 18.3936 7.81159 18.7358 8.15378C19.078 8.49598 19.2696 8.95669 19.2696 9.43696C19.2696 9.91724 19.078 10.3779 18.7358 10.7201C18.3936 11.0623 17.9329 11.2539 17.4526 11.2539H17.3804C17.0956 11.2923 16.8308 11.4231 16.6258 11.6281C16.4208 11.8332 16.2866 12.102 16.2421 12.3923" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            <span>프로필 수정</span>
            <svg class="arrow" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7.5 15L12.5 10L7.5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth.js'
import PageHeader from '../components/common/PageHeader.vue'

const authStore = useAuthStore()
</script>

<style scoped>
.mypage-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.mypage-content {
  margin-top: 20px;
}

.profile-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
}

.profile-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.user-id {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.menu-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 700;
  color: #374151;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: none;
  border: none;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover:not(:disabled) {
  background: #f9fafb;
}

.menu-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.menu-item svg {
  flex-shrink: 0;
  color: #6b7280;
}

.menu-item span {
  flex: 1;
  text-align: left;
}

.menu-item .arrow {
  margin-left: auto;
  color: #9ca3af;
}
</style>
