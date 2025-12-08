import { createRouter, createWebHistory } from 'vue-router'
import VideoListView from '../views/VideoListView.vue'
import BoardListView from '../views/BoardListView.vue'
import InvestView from '../views/InvestView.vue'
import LoginView from '../views/LoginView.vue'

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
    {
      path: '/wishlist',
      name: 'wishlist',
      component: VideoListView,
      meta: { isWishlist: true },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignupView.vue'),
    },
    {
      path: '/video/create',
      name: 'videoCreate',
      component: () => import('../views/VideoCreateView.vue'),
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/MyPageView.vue'),
    },
    {
      path: '/video/:id',
      name: 'videoDetail',
      component: () => import('../views/VideoDetailView.vue'),
    },
  ],
})

export default router
