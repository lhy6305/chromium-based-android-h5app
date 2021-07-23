@echo off
setlocal
chcp 936 2>nul >nul

set java_exe=java.exe

if defined JAVA_HOME (
set java_exe="%JAVA_HOME%\bin\java.exe"
)

%java_exe% -jar "%~dp0baksmali.jar" disassemble --bootclasspath "%~dp0android-19/android.jar" --classpath "%~dp0build-tools 30.0.2/shrinkedAndroid.jar" %*