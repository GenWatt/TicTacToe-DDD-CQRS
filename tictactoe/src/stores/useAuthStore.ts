import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    // State
    state: () => ({
        playerId: localStorage.getItem('playerId') || null as string | null,
        username: localStorage.getItem('username') || null as string | null,
        isAuthenticated: !!localStorage.getItem('playerId')
    }),

    // Actions
    actions: {
        setAuth(playerId: string, username: string) {
            this.playerId = playerId;
            this.username = username;
            this.isAuthenticated = true;

            // Save to local storage
            localStorage.setItem('playerId', playerId);
            localStorage.setItem('username', username);
        },

        clearAuth() {
            this.playerId = null;
            this.username = null;
            this.isAuthenticated = false;

            // Remove from local storage
            localStorage.removeItem('playerId');
            localStorage.removeItem('username');
        }
    },
})