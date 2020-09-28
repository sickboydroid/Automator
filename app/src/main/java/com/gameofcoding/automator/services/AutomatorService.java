package com.gameofcoding.automator.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.gameofcoding.automator.automator.Automator;
import com.gameofcoding.automator.automator.AutomatorUtils;
import com.gameofcoding.automator.utils.AppConstants;
import com.gameofcoding.automator.utils.XLog;

public class AutomatorService extends AccessibilityService {
    private static final String TAG = "AutomatorService";
    private Automator mAutomator;

    class AutomatorListener extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	    try {
		if(intent.getAction().equals(AppConstants.AUTOMATOR_START))
		    mAutomator.onEvent(null);
	    } catch(Throwable tr) {
                XLog.e(TAG, "Exception occurred in broadcast receiver!", tr);
	    }
	}
    }

    @Override
    protected void onServiceConnected() {
	XLog.i(TAG, "Service Connected");
	showToast("Service Connected!");
	updateServiceInfo();
        IntentFilter filter = new IntentFilter(AppConstants.AUTOMATOR_START);
	registerReceiver(new AutomatorListener(), filter);
	super.onServiceConnected();
    }

    @Override
    public void onDestroy() {
	XLog.i(TAG, "Service distroyed!");
	showToast("Service distroyed!");
	super.onDestroy();
    }

    @Override
    public void onInterrupt() {
	XLog.i(TAG, "Service interrupted!");
	showToast("Service interrupted!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
	try {
	    // AutomatorUtils.debugClick(event);
	} catch(Throwable tr) {
	    Toast.makeText(getApplicationContext(), "Access: " + tr.toString(), Toast.LENGTH_LONG).show();
	}
    }

    public void updateServiceInfo() {
	mAutomator = new Automator(this);
	AccessibilityServiceInfo info = new AccessibilityServiceInfo();
	info.packageNames = getPackagesToHandle();
	info.flags = AccessibilityServiceInfo.DEFAULT |
	    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
	    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
	info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
	info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
	setServiceInfo(info);
    }

    private String[] getPackagesToHandle() {
	final List<String> packageNames = new ArrayList<String>();
	packageNames.add("com.android.settings");
	packageNames.add(mAutomator.getPackageName());
	final String[] strArrayPackageNames = new String[packageNames.size()];
	for (int i = 0; i < packageNames.size(); i  ++)
	    strArrayPackageNames[i] = packageNames.get(i);
	return strArrayPackageNames;
    }

    public void showToast(String msg) {
	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}
