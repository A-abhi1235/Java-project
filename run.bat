@echo off
echo ===================================================================
echo Starting Smart Campus Lost and Found Management System...
echo ===================================================================

java --enable-native-access=ALL-UNNAMED -cp "bin;lib\sqlite-jdbc-3.49.1.0.jar" com.campus.lostfound.Main
