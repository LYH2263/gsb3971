import { defineStore } from 'pinia';
import type { UserInfo } from '@/types';

const TOKEN_KEY = 'elderly_token';
const USER_KEY = 'elderly_user';

interface AuthState {
  token: string;
  user: UserInfo | null;
}

function readUserFromStorage(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as UserInfo;
  } catch {
    return null;
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem(TOKEN_KEY) ?? '',
    user: readUserFromStorage()
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    setAuth(token: string, user: UserInfo) {
      this.token = token;
      this.user = user;
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    },
    clearAuth() {
      this.token = '';
      this.user = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },
    hydrate() {
      this.token = localStorage.getItem(TOKEN_KEY) ?? '';
      this.user = readUserFromStorage();
    }
  }
});

export const authStorageKeys = {
  TOKEN_KEY,
  USER_KEY
};
