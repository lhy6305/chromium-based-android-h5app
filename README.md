# chromium-based-android-h5app

This is an android app based on **chromium ContentShell apk**.   

# build instruction
Put your website files into folder [**assets/www**](ContentShell_project/assets/www)  
It will automaticlly open [**assets/www/index.html**](ContentShell_project/assets/www/index.html) to display.  
Then use [**apktools**](apktool) to edit the xml files and rebuild.  

# notice
#### Due to github restrictions, I can only upload zipped files if they're too big. You should unzip the 7z files first.  
- [**res.7z**](ContentShell_project/res.7z)  
- [**lib/armeabi-v7a/libcontent_shell_content_view.7z**](ContentShell_project/lib/armeabi-v7a/libcontent_shell_content_view.7z)  
- [**lib/x86/libcontent_shell_content_view.7z**](ContentShell_project/lib/x86/libcontent_shell_content_view.7z.001)  

The file **shell_view_\*.xml** can be placed into folder [**res/layout**](ContentShell_project/res/layout) to hide or show the default toolbar.  

# License  

The part I modified is under the original license.  
