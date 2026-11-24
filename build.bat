@echo off
echo ===================================================================
echo Compiling Smart Campus Lost and Found Management System...
echo ===================================================================

if not exist "bin" mkdir bin

javac -cp ".;lib\sqlite-jdbc-3.49.1.0.jar" -d bin src\com\campus\lostfound\*.java src\com\campus\lostfound\config\*.java src\com\campus\lostfound\model\*.java src\com\campus\lostfound\exception\*.java src\com\campus\lostfound\util\*.java src\com\campus\lostfound\dao\*.java src\com\campus\lostfound\dao\impl\*.java src\com\campus\lostfound\service\*.java src\com\campus\lostfound\ui\theme\*.java src\com\campus\lostfound\ui\components\*.java src\com\campus\lostfound\ui\auth\*.java src\com\campus\lostfound\ui\panels\*.java src\com\campus\lostfound\ui\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Build succeeded! Classes compiled into \bin.
) else (
    echo.
    echo [ERROR] Build failed.
)
pause
