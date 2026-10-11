@echo off
cd /d "%~dp0"
java -jar dist\GUISE-Band-A.jar
if errorlevel 1 pause
