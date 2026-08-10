@echo off
REM Canvas App - Start Script (Windows)

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=5174"
cd /d "%SCRIPT_DIR%"

echo.
echo ====================================================================
echo            Canvas Drawing Application - Startup
echo ====================================================================
echo.

echo ====================================================================
echo Starting Backend ^(Spring Boot on port %BACKEND_PORT%^)...
echo ====================================================================
cd /d "%SCRIPT_DIR%\canvas-backend"

if not exist "gradlew.bat" (
    echo Error: gradlew.bat not found in canvas-backend
    exit /b 1
)

echo [INFO] Running gradlew.bat clean build -x test ^(this may take a while^)...
call gradlew.bat clean build -x test > "%SCRIPT_DIR%\\backend-build.log" 2>&1
if errorlevel 1 (
    echo Error: Backend build failed. Check %SCRIPT_DIR%\backend-build.log
    pause
    exit /b 1
)
echo [OK] Backend build succeeded.

start "Canvas Backend" cmd /k call gradlew.bat bootRun
echo [OK] Backend started
echo.

echo [INFO] Waiting for backend to be fully ready...
set BACKEND_READY=0
for /L %%i in (1,1,30) do (
    for /f %%A in ('curl -s -o nul -w "%%{http_code}" http://localhost:%BACKEND_PORT%/actuator/health 2^>nul') do (
        if "%%A" == "200" (
            set BACKEND_READY=1
            echo [OK] Backend is fully ready!
            goto backend_ready_done
        )
    )
    timeout /t 1 /nobreak >nul
)

:backend_ready_done
if !BACKEND_READY! == 0 (
    echo [WARNING] Backend may still be starting. Continuing with frontend startup...
)
echo.

echo ====================================================================
echo Starting Frontend ^(React on port %FRONTEND_PORT%^)...
echo ====================================================================
cd /d "%SCRIPT_DIR%\canvas-frontend"

echo [INFO] Installing frontend dependencies...
call npm install
if errorlevel 1 (
    echo Error: Frontend dependency install failed.
    pause
    exit /b 1
)

echo [INFO] Starting frontend process...
start "Canvas Frontend" cmd /k npm start

echo ====================================================================
echo.
echo                   Canvas App Successfully Started!
echo.
echo ====================================================================
echo Frontend ^(React^):    http://localhost:%FRONTEND_PORT%
echo Backend (API):         http://localhost:%BACKEND_PORT%
echo API Docs:              http://localhost:%BACKEND_PORT%/swagger-ui/index.html
echo test coverage report:  npm run test:coverage
echo.
echo Close the command windows to stop the applications.
echo ====================================================================
echo.

pause
