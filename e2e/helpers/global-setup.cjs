const { execSync } = require('node:child_process');

const frontendPort = process.env.E2E_FRONTEND_PORT || '15175';
const backendPort = process.env.E2E_BACKEND_PORT || '18081';
const mysqlPort = process.env.E2E_MYSQL_PORT || '13307';

const createComposeEnv = () => ({
  ...process.env,
  FRONTEND_PORT: frontendPort,
  BACKEND_PORT: backendPort,
  MYSQL_PORT: mysqlPort
});

const sleep = (ms) => new Promise((resolve) => {
  setTimeout(resolve, ms);
});

const waitForHttp = async (url, timeoutMs = 180000) => {
  const start = Date.now();
  let lastError = 'unknown';
  while (Date.now() - start < timeoutMs) {
    try {
      const response = await fetch(url, { method: 'GET' });
      if (response.status >= 200 && response.status < 500) {
        return;
      }
      lastError = `status=${response.status}`;
    } catch (error) {
      lastError = error instanceof Error ? error.message : String(error);
    }
    await sleep(1500);
  }
  throw new Error(`Timed out waiting for ${url}, last error: ${lastError}`);
};

module.exports = async () => {
  const env = createComposeEnv();
  try {
    execSync('docker compose down -v', { cwd: process.cwd(), stdio: 'inherit', env });
  } catch (_) {
    // ignore cleanup failure
  }

  try {
    execSync('docker compose up --build -d', {
      cwd: process.cwd(),
      stdio: 'inherit',
      env
    });
  } catch (error) {
    console.warn('docker compose --build 失败，尝试直接启动：', error.message);
    execSync('docker compose up -d', {
      cwd: process.cwd(),
      stdio: 'inherit',
      env
    });
  }

  await waitForHttp(`http://127.0.0.1:${backendPort}/api/health`);
  await waitForHttp(`http://127.0.0.1:${frontendPort}`);
};
