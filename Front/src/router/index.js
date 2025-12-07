import { createRouter, createWebHistory } from 'vue-router'
import VideoListView from '../views/VideoListView.vue'
import BoardListView from '../views/BoardListView.vue'
import InvestView from '../views/InvestView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: VideoListView,
    },
    {
      path: '/videos',
      redirect: '/',
    },
    {
      path: '/board',
      name: 'board',
      component: BoardListView,
    },
    {
      path: '/invest',
      name: 'invest',
      component: InvestView,
    },
  ],
})

export default router
