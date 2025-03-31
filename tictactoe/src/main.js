import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { WebSocketService } from './WebSocketService'

const pinia = createPinia()

const app = createApp(App)

const webSocketService = new WebSocketService('ws://localhost:8080/ws/game')

app.provide('webSocketService', webSocketService)
app.use(pinia)
app.use(router)
app.mount('#app')