// Copyright 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.content_shell_apk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import org.chromium.base.CommandLine;
import org.chromium.base.MemoryPressureListener;
import org.chromium.base.library_loader.LibraryLoader;
import org.chromium.base.library_loader.LibraryProcessType;
import org.chromium.content_public.browser.BrowserStartupController;
import org.chromium.content_public.browser.DeviceUtils;
import org.chromium.content_public.browser.WebContents;
import org.chromium.content_shell.Shell;
import org.chromium.content_shell.ShellManager;
import org.chromium.ui.base.ActivityWindowAndroid;
import org.chromium.ui.base.IntentRequestTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Activity for managing the Content Shell.
 */
public class ContentShellActivity extends Activity {

    private static final String TAG = "ContentShellActivity";

    private static final String ACTIVE_SHELL_URL_KEY = "activeUrl";
    public static final String COMMAND_LINE_ARGS_KEY = "commandLineArgs";

    // Native switch - shell_switches::kRunWebTests
    private static final String RUN_WEB_TESTS_SWITCH = "run-web-tests";

    private ShellManager mShellManager;
    private ActivityWindowAndroid mWindowAndroid;
    private Intent mLastSentIntent;
    private String mStartupUrl;
    private IntentRequestTracker mIntentRequestTracker;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing the command line must occur before loading the library.
        if (!CommandLine.isInitialized()) {
            ((ContentShellApplication) getApplication()).initCommandLine();
            String[] commandLineParams = getCommandLineParamsFromIntent(getIntent());

            if (commandLineParams == null) {
                commandLineParams = new String[0];
            }
            int size = commandLineParams.length;
            String[] tmp = new String[size + 1];
            for (int i = 0; i < size; i++) {
                tmp[i] = commandLineParams[i];
            }
            tmp[size] = "--disable-web-security";
            commandLineParams = tmp;

            CommandLine.getInstance().appendSwitchesAndArguments(commandLineParams);
        }
        //mStartupUrl = getUrlFromIntent(getIntent());

        DeviceUtils.addDeviceSpecificUserAgentSwitch();

        LibraryLoader.getInstance().ensureInitialized();

        setContentView(R.layout.content_shell_activity);
        mShellManager = findViewById(R.id.shell_container);
        final boolean listenToActivityState = true;
        mIntentRequestTracker = IntentRequestTracker.createFromActivity(this);
        mWindowAndroid =
                new ActivityWindowAndroid(this, listenToActivityState, mIntentRequestTracker);
        mIntentRequestTracker.restoreInstanceState(savedInstanceState);
        mShellManager.setWindow(mWindowAndroid);
        // Set up the animation placeholder to be the SurfaceView. This disables the
        // SurfaceView's 'hole' clipping during animations that are notified to the window.
        mWindowAndroid.setAnimationPlaceholderView(
                mShellManager.getContentViewRenderView().getSurfaceView());

/*
        mStartupUrl = getUrlFromIntent(getIntent());
        if (!TextUtils.isEmpty(mStartupUrl)) {
            mShellManager.setStartupUrl(Shell.sanitizeUrl(mStartupUrl));
        }
*/

        copyAssetsWWW();
        mStartupUrl = "file://" + getFilesDir().getParent() + "/android_assets/www/index.html";

        if (CommandLine.getInstance().hasSwitch(RUN_WEB_TESTS_SWITCH)) {
            BrowserStartupController.getInstance().startBrowserProcessesSync(
                    LibraryProcessType.PROCESS_BROWSER, false);
        } else {
            BrowserStartupController.getInstance().startBrowserProcessesAsync(
                    LibraryProcessType.PROCESS_BROWSER, true, false,
                    new BrowserStartupController.StartupCallback() {
                        @Override
                        public void onSuccess() {
                            finishInitialization(savedInstanceState);
                        }

                        @Override
                        public void onFailure() {
                            initializationFailed();
                        }
                    });
        }
    }

    private void finishInitialization(Bundle savedInstanceState) {
        String shellUrl;
        if (!TextUtils.isEmpty(mStartupUrl)) {
            shellUrl = mStartupUrl;
        } else {
            shellUrl = ShellManager.DEFAULT_SHELL_URL;
        }

        if (savedInstanceState != null
                && savedInstanceState.containsKey(ACTIVE_SHELL_URL_KEY)) {
            shellUrl = savedInstanceState.getString(ACTIVE_SHELL_URL_KEY);
        }
        mShellManager.launchShell(shellUrl);
    }

    private void initializationFailed() {
        Log.e(TAG, "ContentView initialization failed.");
        Toast.makeText(ContentShellActivity.this,
                R.string.browser_process_initialization_failed,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        WebContents webContents = getActiveWebContents();
        if (webContents != null) {
            // TODO(yfriedman): crbug/783819 - This should use GURL serialize/deserialize.
            outState.putString(ACTIVE_SHELL_URL_KEY, webContents.getLastCommittedUrl().getSpec());
        }

        mIntentRequestTracker.saveInstanceState(outState);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            WebContents webContents = getActiveWebContents();
            if (webContents != null && webContents.getNavigationController().canGoBack()) {
                webContents.getNavigationController().goBack();
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (getCommandLineParamsFromIntent(intent) != null) {
            Log.i(TAG, "Ignoring command line params: can only be set when creating the activity.");
        }

        if (MemoryPressureListener.handleDebugIntent(this, intent.getAction())) return;

        String url = getUrlFromIntent(intent);
        if (!TextUtils.isEmpty(url)) {
            Shell activeView = getActiveShell();
            if (activeView != null) {
                activeView.loadUrl(url);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        WebContents webContents = getActiveWebContents();
        if (webContents != null) webContents.onShow();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIntentRequestTracker.onActivityResult(requestCode, resultCode, data, mWindowAndroid);
    }

    @Override
    public void startActivity(Intent i) {
        mLastSentIntent = i;
        super.startActivity(i);
    }

    @Override
    protected void onDestroy() {
        if (mShellManager != null) mShellManager.destroy();
        mWindowAndroid.destroy();
        super.onDestroy();
    }

    public Intent getLastSentIntent() {
        return mLastSentIntent;
    }

    private static String getUrlFromIntent(Intent intent) {
        return intent != null ? intent.getDataString() : null;
    }

    private static String[] getCommandLineParamsFromIntent(Intent intent) {
        return intent != null ? intent.getStringArrayExtra(COMMAND_LINE_ARGS_KEY) : null;
    }

    /**
     * @return The {@link ShellManager} configured for the activity or null if it has not been
     *         created yet.
     */
    public ShellManager getShellManager() {
        return mShellManager;
    }

    /**
     * @return The currently visible {@link Shell} or null if one is not showing.
     */
    public Shell getActiveShell() {
        return mShellManager != null ? mShellManager.getActiveShell() : null;
    }

    /**
     * @return The {@link WebContents} owned by the currently visible {@link Shell} or null if
     *         one is not showing.
     */
    public WebContents getActiveWebContents() {
        Shell shell = getActiveShell();
        return shell != null ? shell.getWebContents() : null;
    }







    private void copyAssetsWWW() {
        boolean update = true;
        String vi = null;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            vi = pi.versionName + pi.versionCode;
            if(new File(getFilesDir().getParent() + "/android_assets/" + vi).exists()){
                update = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (update) {
            deleteAllFiles(new File(getFilesDir().getParent() + "/android_assets"));
            copyAssets("www", getFilesDir().getParent() + "/android_assets/www");
            if(vi != null) {
                try {
                    new File(getFilesDir().getParent() + "/android_assets/" + vi).createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteAllFiles(File fil) {
        try {
            Process process = Runtime.getRuntime().exec("rm -rf " + fil.getAbsolutePath());
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            utils(fil, new String[]{"deleteFiles"});
            fil.delete();
        }
    }

    private void copyAssets(String oldPath, String newPath){
        utils(null, new String[]{"copyAssets", oldPath, newPath});
    }

    private void utils(File cf, String[] action) {
        String act = action[0];
        String path;
        if(!act.equals("copyAssets")) {
            path = cf.getAbsolutePath();
        }else {
            path = action[1];
        }
        ArrayList<Integer> fl = new ArrayList<>();
        ArrayList<String> sfl = new ArrayList<>();
        ArrayList<String> lfl = new ArrayList<>();
        for(int i = 0;; i++) {
            boolean flagc = false;
            int a = 0;
            String fp = path + "/" + join("/", sfl);
            if(fp.endsWith("/")){
                fp = fp.substring(0, fp.length() - 1);
            }
            String[] fi;
            if(!act.equals("copyAssets")) {
                cf = new File(fp);
                fi = cf.list();
            }else {
                try {
                    fi = getAssets().list(fp);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if(fl.size() >= i + 1) {
                a = fl.get(i) + 1;
            }
            for(;a < fi.length; a++) {
                int flag = 0;
                if(fl.size() >= i + 1) {
                    fl.set(i, a);
                }else{
                    fl.add(a);
                }
                fp = path + "/" + join("/", sfl);
                if(fp.endsWith("/")){
                    fp = fp.substring(0, fp.length() - 1);
                }
                fp += "/" + fi[a];
                if(sfl.size() >= i + 1) {
                    sfl.set(i, fi[a]);
                }else{
                    sfl.add(fi[a]);
                }
                if(!act.equals("copyAssets")) {
                    cf = new File(fp);
                    if (cf.isDirectory()) {
                        flag = 1;
                    } else if (cf.isFile()) {
                        flag = 2;
                    }
                }else{
                    try {
                        if(getAssets().list(fp).length > 0){
                            flag = 1;
                        }else{
                            flag = 2;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                if(flag == 1) {
                    lfl.add(join("/", sfl));
                    flagc = true;
                    break;
                }else if(flag == 2) {
                    lfl.add(join("/", sfl));
                    sfl.remove(i);
                }
            }
            if(flagc) {
                continue;
            }
            try {
                fl.remove(i);
            }catch(Exception ignored) {
            }
            i--;
            if(i < 0) {
                break;
            }
            sfl.remove(i);
            i--;
        }
        if(act.equals("deleteFiles")) {
            for (int b = lfl.size() - 1; b >= 0; b--) {
                try {
                    new File(path + "/" + lfl.get(b)).delete();
                    lfl.remove(b);
                } catch (Exception ignored) {
                }
            }
        }else if(act.equals("copyAssets")){
            for (int b = 0; b < lfl.size(); b++) {
                try {
                    File fil = new File(action[2] + "/" + lfl.get(b));
                    fil.getParentFile().mkdirs();
                    InputStream is = getAssets().open(path + "/" + lfl.get(b));
                    FileOutputStream fos = new FileOutputStream(fil);
                    byte[] buffer = new byte[1024];
                    int byteCount = 0;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                    lfl.remove(b);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String join(String separator, ArrayList<String> input) {
        if (input == null || input.size() <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.size(); i++) {
            sb.append(input.get(i));
            if (i != input.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

}
