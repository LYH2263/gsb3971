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

module.exports = async () => {
  try {
    execSync('docker compose down -v', {
      cwd: process.cwd(),
      stdio: 'inherit',
      env: createComposeEnv()
    });
  } catch (_) {
    // ignore teardown failure
  }
};
