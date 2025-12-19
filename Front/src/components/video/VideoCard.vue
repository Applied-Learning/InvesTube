<!-- Front/src/components/video/VideoCard.vue -->
<template>
  <Card :clickable="true" @click="onClick">
    <template #header>
      <div class="thumbnail-wrapper">
        <img :src="thumbnailUrl" :alt="title" class="thumbnail" />
        <span v-if="duration" class="duration">{{ duration }}</span>
      </div>
    </template>

    <div class="info-row">
      <div class="uploader">
        <img
          v-if="uploaderProfileImageUrl"
          :src="resolveImageUrl(uploaderProfileImageUrl)"
          :alt="uploaderName || channelName"
          class="avatar"
        />
        <div v-else class="avatar avatar--fallback">
          {{ uploaderInitial }}
        </div>
      </div>

      <div class="info">
        <h3 class="title">{{ title }}</h3>
        <p v-if="uploaderName" class="channel">{{ uploaderName }}</p>
        <p v-if="metaText" class="meta">{{ metaText }}</p>
      </div>

      <button
        class="wish-btn"
        :class="{ 'wish-btn--active': wished }"
        type="button"
        @click.stop="onToggleWish"
      >
        <span v-if="wished">♥</span>
        <span v-else>♡</span>
      </button>
    </div>
  </Card>
</template>

<script setup>
import { computed } from 'vue'
import Card from '../common/Card.vue'
import { resolveImageUrl } from '../../utils/image.js'

const props = defineProps({
  thumbnailUrl: { type: String, required: true },
  title: { type: String, required: true },
  channelName: { type: String, default: '' },
  uploaderName: { type: String, default: '' },
  uploaderProfileImageUrl: { type: String, default: '' },
  views: { type: Number, default: null },
  createdAtText: { type: String, default: '' },
  duration: { type: String, default: '' },
  wished: { type: Boolean, default: false },
})

const emit = defineEmits(['click', 'toggle-wish'])

const metaText = computed(() => {
  const parts = []
  if (props.views != null) parts.push(`조회수 ${props.views.toLocaleString()}회`)
  if (props.createdAtText) parts.push(props.createdAtText)
  return parts.join(' · ')
})

const uploaderInitial = computed(() => {
  const base = props.uploaderName || props.channelName || ''
  return base ? base.trim().charAt(0).toUpperCase() : '?'
})

const onClick = () => {
  emit('click')
}

const onToggleWish = () => {
  emit('toggle-wish')
}
</script>

<style scoped>
.thumbnail-wrapper {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  padding-bottom: 56.25%;
  background: #111827;
}

.thumbnail {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.duration {
  position: absolute;
  right: 6px;
  bottom: 6px;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: rgba(15, 23, 42, 0.9);
  color: #f9fafb;
  font-size: 11px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 6px;
}

.uploader {
  flex-shrink: 0;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  object-fit: cover;
  background-color: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
}

.avatar--fallback {
  text-transform: uppercase;
}

.info {
  flex: 1;
}

.title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.channel,
.meta {
  margin: 2px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.wish-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 999px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #9ca3af;
  transition:
    color 0.12s ease,
    background-color 0.12s ease;
}

.wish-btn:hover {
  background-color: #f3f4f6;
}

.wish-btn--active {
  color: #ef4444;
}
</style>
