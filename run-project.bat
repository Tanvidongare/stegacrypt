@echo off
setlocal

set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"

echo ========================================
echo StegaCrypt Project Launcher
echo ========================================
echo.

where java >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Java is not installed or not available in PATH.
  echo Install Java 17 and try again.
  exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Maven is not installed or not available in PATH.
  echo Install Maven 3.9+ and try again.
  exit /b 1
)

where node >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Node.js is not installed or not available in PATH.
  echo Install Node.js 18+ and try again.
  exit /b 1
)

where npm >nul 2>&1
if errorlevel 1 (
  echo [ERROR] npm is not installed or not available in PATH.
  echo Install npm and try again.
  exit /b 1
)

if not exist "frontend\node_modules" (
  echo Frontend dependencies not found. Running npm install...
  pushd "frontend"
  call npm install
  if errorlevel 1 (
    popd
    echo [ERROR] npm install failed.
    exit /b 1
  )
  popd
)

echo Starting backend on http://localhost:8080 ...
start "StegaCrypt Backend" cmd /k "cd /d ""%ROOT_DIR%backend"" && mvn spring-boot:run"

echo Starting frontend on http://localhost:5573 ...
start "StegaCrypt Frontend" cmd /k "cd /d ""%ROOT_DIR%frontend"" && npm run dev"

echo.
echo Backend and frontend are launching in separate windows.
echo Frontend: http://localhost:5573
echo Backend health: http://localhost:8080/api/health
echo.
echo Close the launched terminal windows to stop the project.

endlocal
