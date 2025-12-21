<template>
  <div class="rte-wrapper">
    <div class="rte-toolbar">
      <button
        type="button"
        class="rte-btn"
        :class="{ 'rte-btn--active': isBold }"
        @click="exec('bold')"
      >
        B
      </button>
      <button
        type="button"
        class="rte-btn"
        :class="{ 'rte-btn--active': isItalic }"
        @click="exec('italic')"
      >
        I
      </button>
      <button type="button" class="rte-btn" @click="triggerImageSelect">Img</button>
    </div>
    <div
      ref="editorRef"
      class="rte-content"
      contenteditable="true"
      :placeholder="placeholder"
      @input="handleInput"
    ></div>
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      class="rte-file-input"
      @change="handleImageChange"
    />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { uploadBoardImage } from '../../api/board.js'
import { resolveImageUrl } from '../../utils/image.js'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const fileInputRef = ref(null)

const isBold = ref(false)
const isItalic = ref(false)

const updateSelectionState = () => {
  const sel = window.getSelection()
  if (!sel || sel.rangeCount === 0) return
  const range = sel.getRangeAt(0)
  const node = range.startContainer
  if (!editorRef.value || !editorRef.value.contains(node)) return

  try {
    isBold.value = document.queryCommandState('bold')
    isItalic.value = document.queryCommandState('italic')
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  if (editorRef.value && props.modelValue) {
    editorRef.value.innerHTML = props.modelValue
  }
  document.addEventListener('selectionchange', updateSelectionState)
})

onUnmounted(() => {
  document.removeEventListener('selectionchange', updateSelectionState)
})

watch(
  () => props.modelValue,
  (val) => {
    if (!editorRef.value) return
    if (editorRef.value.innerHTML !== val) {
      editorRef.value.innerHTML = val || ''
    }
  },
)

const handleInput = () => {
  if (!editorRef.value) return
  emit('update:modelValue', editorRef.value.innerHTML)
}

const exec = (command) => {
  document.execCommand(command, false, null)
  handleInput()
  updateSelectionState()
}

const triggerImageSelect = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

const uploadImage = async (file) => {
  const resp = await uploadBoardImage(file)
  const url = resp.data?.url
  if (url) {
    return resolveImageUrl(url)
  }

  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

const handleImageChange = async (event) => {
  const files = Array.from(event.target.files || [])
  if (files.length === 0) return

  const file = files[0]
  if (!file.type.startsWith('image/')) return

  try {
    const url = await uploadImage(file)
    if (editorRef.value && url) {
      document.execCommand('insertImage', false, url)
      handleInput()
      updateSelectionState()
    }
  } catch (err) {
    console.error('이미지 처리 오류:', err)
    alert('이미지 업로드 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.rte-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.rte-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.rte-btn {
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  font-size: 12px;
  cursor: pointer;
}

.rte-btn:hover {
  background: #f3f4f6;
}

.rte-btn--active {
  background: #e5edff;
  border-color: #2563eb;
  color: #2563eb;
}

.rte-content {
  min-height: 200px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
}

.rte-content:empty:before {
  content: attr(placeholder);
  color: #9ca3af;
}

.rte-file-input {
  display: none;
}
</style>
