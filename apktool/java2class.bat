@echo off
javac -bootclasspath "%~dp0android-19/android.jar" -classpath "%~dp0build-tools 30.0.2/shrinkedAndroid.jar" %*