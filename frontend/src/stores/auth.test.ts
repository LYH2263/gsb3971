import { beforeEach, describe, expect, it } from 'vitest';
import { setActivePinia } from 'pinia';
import { pinia } from '@/stores/pinia';
import { authStorageKeys, useAuthStore } from '@/stores/auth';

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear();
    setActivePinia(pinia);
  });

  it('should persist token and user', () => {
    const store = useAuthStore();
    store.setAuth('token-1', {
      id: 1,
      phone: '13800000001',
      realName: '管理员',
      age: 30,
      gender: 1,
      role: 'ADMIN',
      status: 1
    });

    expect(localStorage.getItem(authStorageKeys.TOKEN_KEY)).toBe('token-1');
    expect(store.isAuthenticated).toBe(true);
    expect(store.isAdmin).toBe(true);
  });

  it('should clear token and user', () => {
    const store = useAuthStore();
    store.setAuth('token-1', {
      id: 1,
      phone: '13800000001',
      realName: '管理员',
      age: 30,
      gender: 1,
      role: 'ADMIN',
      status: 1
    });

    store.clearAuth();

    expect(store.token).toBe('');
    expect(store.user).toBeNull();
    expect(localStorage.getItem(authStorageKeys.TOKEN_KEY)).toBeNull();
  });
});
