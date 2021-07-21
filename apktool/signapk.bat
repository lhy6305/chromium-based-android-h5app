@echo off
setlocal
chcp 936 2>nul >nul

set java_exe=java.exe

if defined JAVA_HOME (
set java_exe="%JAVA_HOME%\bin\java.exe"
)

%java_exe% -jar "%~dp0\signapk\%~n0.jar" "%~dp0\signapk\certificate.pem" "%~dp0\signapk\key.pk8" %*