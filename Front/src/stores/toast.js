import { defineStore } from 'pinia'

let toastId = 0

export const useToastStore = defineStore('toast', {
  state: () => ({
    toasts: [],
  }),
  actions: {
    show(message, { type = 'info', duration = 4000 } = {}) {
      const id = ++toastId
      this.toasts.push({ id, message, type })

      setTimeout(() => {
        this.remove(id)
      }, duration)
    },
    remove(id) {
      this.toasts = this.toasts.filter((t) => t.id !== id)
    },
  },
})
