@echo off
setlocal
echo ===================================================================
echo Starting Smart Campus Lost and Found Management System...
echo ===================================================================

set "JAVA_BIN=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
if "%JAVA_BIN%"=="java" for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~fD\bin\java.exe" set "JAVA_BIN=%%~fD\bin\java.exe"

"%JAVA_BIN%" --enable-native-access=ALL-UNNAMED -cp "bin;lib\sqlite-jdbc-3.49.1.0.jar" com.campus.lostfound.Main
