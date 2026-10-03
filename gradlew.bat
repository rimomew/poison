@echo off
setlocal EnableExtensions
set "SCRIPT_DIR=%~dp0"
set "GRADLE_VERSION=9.7.1"
set "DIST_DIR=%SCRIPT_DIR%.gradle-dist"
set "GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%"

if exist "%GRADLE_HOME%\bin\gradle.bat" goto runGradle

where gradle >nul 2>&1
if %ERRORLEVEL%==0 goto runSystemGradle

echo Gradle %GRADLE_VERSION% was not found locally. Downloading the pinned distribution...
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $ErrorActionPreference='Stop'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-9.7.1-bin.zip' -OutFile '%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip'; Expand-Archive -Force '%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip' '%DIST_DIR%'; Remove-Item '%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip' }"
if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Failed to install Gradle %GRADLE_VERSION%.
  exit /b 1
)

:runGradle
call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%

:runSystemGradle
call gradle %*
exit /b %ERRORLEVEL%
