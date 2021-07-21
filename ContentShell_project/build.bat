@echo off
start /wait cmd /c apktool b
rmdir /s /q "build"
start /wait cmd /c zipalign -f -v -p 4 "./dist/ContentShell.apk" "./dist/ContentShell_aligned.apk"
start /wait cmd /c apksigner sign --ks "PUT YOUR KEYSTORE PATH HERE" --ks-key-alias key0 --ks-pass pass:Lhy12345678 --out "ContentShell_signed.apk" "./dist/ContentShell_aligned.apk"
rmdir /s /q "dist"
copy /b "ContentShell_signed.apk" "ContentShell_signed_aligned.apk"
del /f /q "ContentShell_signed.apk"
rename "ContentShell_signed_aligned.apk" "ContentShell_signed.apk"