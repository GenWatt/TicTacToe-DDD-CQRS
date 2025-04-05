import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    // State
    state: () => ({
        playerId: localStorage.getItem('playerId') || null as string | null,
        username: localStorage.getItem('username') || null as string | null,
        token: localStorage.getItem('token') || null as string | null,
        isAuthenticated: !!localStorage.getItem('playerId')
    }),

    // Actions
    actions: {
        setAuth(playerId: string, username: string, token: string) {
            this.playerId = playerId;
            this.username = username;
            this.token = token;
            this.isAuthenticated = true;

            // Save to local storage
            localStorage.setItem('playerId', playerId);
            localStorage.setItem('username', username);
            localStorage.setItem('token', token);
        },

        clearAuth() {
            this.playerId = null;
            this.username = null;
            this.token = null;
            this.isAuthenticated = false;

            // Remove from local storage
            localStorage.removeItem('playerId');
            localStorage.removeItem('username');
            localStorage.removeItem('token');
        }
    },
})