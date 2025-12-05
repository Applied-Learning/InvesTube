<!-- Front/src/views/HomeView.vue -->
<template>
  <div>
    <h2 class="title">추천 영상</h2>
    <div class="grid">
      <VideoCard
        v-for="video in videos"
        :key="video.id"
        v-bind="video"
        @click="goDetail(video.id)"
        @toggle-wish="toggleWish(video)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import VideoCard from '../components/video/VideoCard.vue'

const videos = ref([
  {
    // 샘플 데이터
    id: 1,
    thumbnailUrl: 'https://i.ytimg.com/vi/nqbKOvQ8x1s/hqdefault.jpg',
    title: '주린이를 위한 ETF 투자 기초',
    channelName: '인베스튜브 채널',
    uploaderName: '홍길동',
    uploaderProfileImageUrl: 'https://i.ytimg.com/vi/nqbKOvQ8x1s/hqdefault.jpg',
    views: 12345,
    createdAtText: '3시간 전',
    duration: '12:34',
    wished: true,
  },
])

const goDetail = (id) => {
  console.log('go video detail', id)
}

const toggleWish = (video) => {
  video.wished = !video.wished
}
</script>

<style scoped>
.title {
  margin: 0 0 12px;
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}
</style>
