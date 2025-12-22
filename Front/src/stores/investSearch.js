import { defineStore } from 'pinia'

export const useInvestSearchStore = defineStore('investSearch', {
  state: () => ({
    searchQuery: '',
  }),
  actions: {
    setQuery(query) {
      this.searchQuery = query || ''
    },
    clear() {
      this.searchQuery = ''
    },
  },
})
