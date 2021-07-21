# chromium-based-android-h5app

This is an android app based on **chromium ContentShell apk**.   

# build instruction
Put your website files into folder [**assets/www**](ContentShell_project/assets/www).  
Then use [**apktools**](apktool) to edit the xml files and rebuild.  
It will automaticlly open [**assets/www/index.html**](ContentShell_project/assets/www/index.html) to display.  

# notice
Due to github restrictions, I can only upload zipped files if they're too big. You should unzip the **7z** files first.  
- [**res.7z**](ContentShell_project/res.7z)  
- [**lib/armeabi-v7a/libcontent_shell_content_view.7z**](ContentShell_project/lib/armeabi-v7a/libcontent_shell_content_view.7z)  
- [**lib/x86/libcontent_shell_content_view.7z**](ContentShell_project/lib/x86/libcontent_shell_content_view.7z.001)  

The file **shell_view_\*.xml** can be placed into folder [**res/layout**](ContentShell_project/res/layout) to hide or show the default toolbar.  

You can remove one of the folders in the [**lib**](ContentShell_project/lib) folder before rebuilding if you don't need them.  

It will release the [**assets/www**](ContentShell_project/assets/www) folder to **/data/data/{packagename}/android_assets/www/** after every update or reinstall and create an empty file named **{versionName}{versionCode}** to prevent repeated release of files, so don't forget to change **versionCode (manifest->versionCode)** or/and **versionName (manifest->versionName)** in the file [**AndroidManifest.xml**](ContentShell_project/AndroidManifest.xml) every update, or the files may not update.  

You can change the **packageName** (manifest->package) in the file [**AndroidManifest.xml**](ContentShell_project/AndroidManifest.xml) and the **appName** (manifest->application->android:label) to whatever you like.

Don't forget to change the app-icon in the file(s) **res/mipmap-\*dpi-\*/app-icon**. They are in **png** format.

If you come into trouble while trying to install your app, the probable reason is the signature cannot be verified by your system. Try to change the **targetSdkVersion (manifest->uses-sdk->android:targetSdkVersion)** to a lower value or find a suitable signature method (jar,v2,v3,v4) for the **target sdk version** and rebuild. (e.g. For target sdk version **30**, you should use **jar,v2 and v3**.) You can use the **apksigner** tool to automaticly decide and sign.  

# License  

The part I modified is under the original license.  

# used project links  
https://github.com/techexpertize/SignApk  
https://ibotpeaches.github.io/Apktool  
https://github.com/hzw1199/xml2axml  
