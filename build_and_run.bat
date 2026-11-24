@echo off
setlocal
echo ===================================================================
echo Building and Launching Smart Campus Lost and Found System...
echo ===================================================================

if not exist "bin" mkdir bin

set "JAVA_BIN=java"
set "JAVAC_BIN=javac"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_BIN=%JAVA_HOME%\bin\javac.exe"
if "%JAVA_BIN%"=="java" for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~fD\bin\java.exe" set "JAVA_BIN=%%~fD\bin\java.exe"
if "%JAVAC_BIN%"=="javac" for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~fD\bin\javac.exe" set "JAVAC_BIN=%%~fD\bin\javac.exe"

"%JAVAC_BIN%" -cp ".;lib\sqlite-jdbc-3.49.1.0.jar" -d bin src\com\campus\lostfound\*.java src\com\campus\lostfound\config\*.java src\com\campus\lostfound\model\*.java src\com\campus\lostfound\exception\*.java src\com\campus\lostfound\util\*.java src\com\campus\lostfound\dao\*.java src\com\campus\lostfound\dao\impl\*.java src\com\campus\lostfound\service\*.java src\com\campus\lostfound\ui\theme\*.java src\com\campus\lostfound\ui\components\*.java src\com\campus\lostfound\ui\auth\*.java src\com\campus\lostfound\ui\panels\*.java src\com\campus\lostfound\ui\*.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation succeeded! Launching desktop GUI...
"%JAVA_BIN%" --enable-native-access=ALL-UNNAMED -cp "bin;lib\sqlite-jdbc-3.49.1.0.jar" com.campus.lostfound.Main
