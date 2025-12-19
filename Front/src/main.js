import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './main.css'

// lucide icons will be imported per-component where needed

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
