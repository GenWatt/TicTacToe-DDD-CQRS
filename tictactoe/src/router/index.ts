// filepath: c:\Users\Adrian\AdrianekApps\ddd\tictactoe\src\router\index.js
import { createRouter, createWebHistory } from 'vue-router'
import Register from '../pages/Register.vue'
import Game from '../pages/Game.vue'
import Login from '../pages/Login.vue'

const routes = [
    { path: '/', component: Game },
    { path: '/register', component: Register },
    { path: '/login', component: Login },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router