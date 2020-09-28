package com.gameofcoding.automator.automator;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.net.Uri;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gameofcoding.automator.utils.XLog;

import java.util.List;

public abstract class BaseAutomator {
    private static final String TAG = "BaseAutomator";
    private AccessibilityService mService;
    private Context mContext;

    public BaseAutomator(Context context) {
        mContext = context;
    }

    public BaseAutomator(AccessibilityService service) {
        mService = service;
    }

    protected Context getContext() {
        if(mContext == null)
            return getService();
        return mContext;
    }

    protected AccessibilityService getService() {
        return mService;
    }

    protected AccessibilityNodeInfo getRootInActiveWindow() {
        return getService().getRootInActiveWindow();
    }

    /**
     * Handy method for waiting.
     * @param from Instance of thread from where you want to wait.
     * @param millis Waiting time
     */
    protected void sleep(long millis) {
        try {
            synchronized (this) {
                wait(millis);
            }
        } catch (InterruptedException e) {
            XLog.e(TAG, "sleep(Object, long): Failed to wait", e);
        }
    }

    protected void clickView(String viewIdResourceName) {
        AccessibilityNodeInfo rootNodeInfo = mService.getRootInActiveWindow();
        if (rootNodeInfo == null) {
            XLog.w(TAG, "click(String): Could not retrive widnow content!");
            return;
        }
        List<AccessibilityNodeInfo> nodesToClick = rootNodeInfo
            .findAccessibilityNodeInfosByViewId(viewIdResourceName);
        clickAllNodes(nodesToClick);
    }

    protected void clickAt(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder
            .addStroke(new GestureDescription.StrokeDescription(path, 10, 10))
            .build();
        getService().dispatchGesture(gestureDescription, null, null);
    }

    protected boolean clickAllNodes(List<AccessibilityNodeInfo> nodesToClick) {
        boolean clickedAnyNode = false;
        for(AccessibilityNodeInfo node : nodesToClick) {
            if(node.isClickable() && node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                clickedAnyNode = true;
            }
        }
        return clickedAnyNode;
    }

    protected void launchApp(String packageName) {
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
        getContext().startActivity(intent);
    }

    protected void closeApp(String packageName) {
        // Start settings
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        mService.startActivity(intent);
        sleep(3500);

        // Try to find out what it says on the Force Stop button (different in different languages)
        String forceStopButtonName = null;
        try {
            String resourcesPackageName = "com.android.settings";
            Resources resources = getContext().getPackageManager().getResourcesForApplication(resourcesPackageName);
            int resourceId = resources.getIdentifier("force_stop", "string", resourcesPackageName);
            if (resourceId > 0)
                forceStopButtonName = resources.getString(resourceId);
            else
                XLog.e(TAG, "Label for the force stop button in settings could not be found");
        } catch (PackageManager.NameNotFoundException e) {
            XLog.e(TAG, "Settings activity's resources not found");
        }

        // Click on force stop button
        List<AccessibilityNodeInfo> nodesForceStop = getRootInActiveWindow().findAccessibilityNodeInfosByText("FORCE STOP");
        if (nodesForceStop.isEmpty())
            nodesForceStop = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button");

        if (nodesForceStop.isEmpty())
            nodesForceStop = getRootInActiveWindow().findAccessibilityNodeInfosByText(forceStopButtonName);

        if (nodesForceStop.isEmpty())
            nodesForceStop = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.android.settings:id/right_button");

        if(!clickAllNodes(nodesForceStop))
            return;

        sleep(1500); // Wait for rendering of diakog

        // Click on okay button from dialog
        List<AccessibilityNodeInfo> nodesOkayButton = getRootInActiveWindow().findAccessibilityNodeInfosByText(getContext().getString(android.R.string.ok));
        if (nodesOkayButton.isEmpty())
            // In some phones 'Ok' button is sometimes labled as 'Force Stop'.
            nodesOkayButton = getRootInActiveWindow().findAccessibilityNodeInfosByText(forceStopButtonName);
        if(nodesOkayButton.isEmpty())
            nodesOkayButton = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("android:id/button1");
        clickAllNodes(nodesOkayButton);   
    }

    protected void restartApp(String packageName) {
        closeApp(packageName);
        sleep(1500);

        // Launch again the stopped packge
        launchApp(packageName);
    }

    public abstract String getPackageName();
    public abstract void onEvent(AccessibilityEvent event);
}
