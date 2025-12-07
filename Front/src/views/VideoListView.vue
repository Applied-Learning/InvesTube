<template>
  <div>
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
    id: 1,
    youtubeVideoId: 'nqbKOvQ8x1s',
    title: '초등학생도 이해하는 주식투자 기초 개념',
    thumbnailUrl: 'https://i.ytimg.com/vi/nqbKOvQ8x1s/hqdefault.jpg',
    uploaderName: '유저 A',
    uploaderProfileImageUrl: '',
    views: 0,
    createdAtText: '',
    duration: '',
    wished: false,
  },
  {
    id: 2,
    youtubeVideoId: 'ROLPR_eIrVg',
    title: '잘 나가던 나스닥 수직낙하한 진짜 이유',
    thumbnailUrl: 'https://i.ytimg.com/vi/ROLPR_eIrVg/hqdefault.jpg',
    uploaderName: '유저 B',
    uploaderProfileImageUrl: '',
    views: 0,
    createdAtText: '',
    duration: '',
    wished: false,
  },
  {
    id: 3,
    youtubeVideoId: 'hm-tW-O4YXw',
    title:
      '[#프리한닥터W] 주식 투자하기 전에 OO를 꼭 확인하세요🔍 알짜배기 주식 알아보는 방법 | #티전드',
    thumbnailUrl: 'https://i.ytimg.com/vi/hm-tW-O4YXw/hqdefault.jpg',
    uploaderName: '유저 C',
    uploaderProfileImageUrl: '',
    views: 0,
    createdAtText: '',
    duration: '',
    wished: false,
  },
  {
    id: 4,
    youtubeVideoId: 'YWgI4_Az7f4',
    title: "주식시장 조정이 오든 말든 '이 주식만' 계속 사모으세요 (신동준 교수)",
    thumbnailUrl: 'https://i.ytimg.com/vi/YWgI4_Az7f4/hqdefault.jpg',
    uploaderName: '유저 D',
    uploaderProfileImageUrl: '',
    views: 0,
    createdAtText: '',
    duration: '',
    wished: false,
  },
  {
    id: 5,
    youtubeVideoId: '8H1B836CAcI',
    title: '돈은 없고 돈 빌리려는 사람만 많다',
    thumbnailUrl: 'https://i.ytimg.com/vi/8H1B836CAcI/hqdefault.jpg',
    uploaderName: '유저 E',
    uploaderProfileImageUrl: '',
    views: 0,
    createdAtText: '',
    duration: '',
    wished: false,
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

