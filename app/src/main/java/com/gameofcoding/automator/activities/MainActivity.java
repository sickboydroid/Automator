package com.gameofcoding.automator.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.text.TextUtils;
import android.widget.Button;

import com.gameofcoding.automator.R;
import com.gameofcoding.automator.services.AutomatorService;
import com.gameofcoding.automator.utils.AppConstants;
import com.gameofcoding.automator.utils.Utils;
import com.gameofcoding.automator.utils.XLog;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private final Context mContext = this;
    private Utils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUtils = new Utils(mContext);
        Button btnStartAutomator = findViewById(R.id.start);
        btnStartAutomator.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        if (!isAccessibilityServiceEnabled(AutomatorService.class)) {
            XLog.v(TAG, "Asking for granting accessiblity permission.");
            mUtils.showToast("Please grant accessiblilty permission to continue.");
            final Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
        switch (view.getId()) {
            case R.id.start:
                XLog.v(TAG, "Starting automator service");
                final Intent intent = new Intent();
                intent.setAction(AppConstants.AUTOMATOR_START);
                sendBroadcast(intent);
                break;
        }
    }

    /**
     * Checks whether the given accessibilty service has been enabled or not.
     * @param accessibilityService Service which you want to know is enabled or not.
     *
     * @return True if enabled otherwise false.
     */
    private boolean isAccessibilityServiceEnabled(Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(mContext, accessibilityService);
        String enabledServicesSetting = Settings.Secure.getString(mContext.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);
            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }
        return false;
    }
}
