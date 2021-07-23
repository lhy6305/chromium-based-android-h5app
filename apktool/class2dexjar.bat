@echo off
"%~dp0build-tools 30.0.2/d8.bat" --release --lib "%~dp0android-19/android.jar" --classpath "%~dp0build-tools 30.0.2/shrinkedAndroid.jar" --intermediate %*