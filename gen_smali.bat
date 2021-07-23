@echo off

del /f /q "ContentShellActivity.class"
del /f /q "ContentShellActivity.jar"
del /f /q "classes.dex"
del /f /q "ContentShellActivity.smali"

start "" /wait cmd /c java2class ContentShellActivity.java
start "" /wait cmd /c class2dexjar --output ContentShellActivity.jar ContentShellActivity.class
del /f /q "ContentShellActivity.class"
start "" /wait cmd /c jar -xvf "ContentShellActivity.jar"
del /f /q "ContentShellActivity.jar"
start "" /wait cmd /c dex2smali -o "./" "classes.dex"
del /f /q "classes.dex"