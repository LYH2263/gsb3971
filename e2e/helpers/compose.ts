export const E2E_FRONTEND_PORT = process.env.E2E_FRONTEND_PORT || '15175';
export const E2E_BACKEND_PORT = process.env.E2E_BACKEND_PORT || '18081';
export const E2E_MYSQL_PORT = process.env.E2E_MYSQL_PORT || '13307';

export const E2E_FRONTEND_BASE_URL = `http://127.0.0.1:${E2E_FRONTEND_PORT}`;
export const E2E_BACKEND_BASE_URL = `http://127.0.0.1:${E2E_BACKEND_PORT}`;

export const createComposeEnv = (): NodeJS.ProcessEnv => ({
  ...process.env,
  FRONTEND_PORT: E2E_FRONTEND_PORT,
  BACKEND_PORT: E2E_BACKEND_PORT,
  MYSQL_PORT: E2E_MYSQL_PORT
});
