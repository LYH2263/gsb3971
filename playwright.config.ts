import { defineConfig } from '@playwright/test';
import { E2E_FRONTEND_BASE_URL } from './e2e/helpers/compose';

export default defineConfig({
  testDir: './e2e/specs',
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  timeout: 90_000,
  globalTimeout: 40 * 60 * 1000,
  expect: {
    timeout: 10_000
  },
  globalSetup: './e2e/helpers/global-setup.cjs',
  globalTeardown: './e2e/helpers/global-teardown.cjs',
  use: {
    baseURL: E2E_FRONTEND_BASE_URL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },
  reporter: [['list'], ['html', { open: 'never' }]],
  projects: [
    {
      name: 'smoke',
      grep: /@smoke/
    },
    {
      name: 'full',
      grep: /@full/
    }
  ]
});
