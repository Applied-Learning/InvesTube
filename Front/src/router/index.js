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
      path: '/board/create',
      name: 'boardCreate',
      component: () => import('../views/BoardCreateView.vue'),
    },
    {
      path: '/board/:id/edit',
      name: 'boardEdit',
      component: () => import('../views/BoardCreateView.vue'),
    },
    {
      path: '/board/:id',
      name: 'boardDetail',
      component: () => import('../views/BoardDetailView.vue'),
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
      path: '/mypage/videos',
      name: 'myVideos',
      component: () => import('../views/MyVideosView.vue'),
    },
    {
      path: '/mypage/boards',
      name: 'myBoards',
      component: () => import('../views/MyBoardsView.vue'),
    },
    {
      path: '/mypage/boards/commented',
      name: 'myCommentedBoards',
      component: () => import('../views/MyCommentedBoardsView.vue'),
    },
    {
      path: '/mypage/reviews',
      name: 'myReviewVideos',
      component: () => import('../views/MyReviewedVideosView.vue'),
    },
    {
      path: '/users/:userId/boards',
      name: 'userBoards',
      component: () => import('../views/UserBoardsView.vue'),
    },
    {
      path: '/users/:userId/videos',
      name: 'userVideos',
      component: () => import('../views/UserVideosView.vue'),
    },
    {
      path: '/users/:userId',
      name: 'userProfile',
      component: () => import('../views/UserProfileView.vue'),
    },
    {
      path: '/video/:id',
      name: 'videoDetail',
      component: () => import('../views/VideoDetailView.vue'),
    },
  ],
})

export default router
