package com.gameofcoding.automator.automator;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gameofcoding.automator.utils.XLog;

public class AutomatorUtils {
    private static final String TAG = "AutomatorUtils";
    private BaseAutomator mAutomator;

    public AutomatorUtils(BaseAutomator automator) {
        mAutomator = automator;
    }

    public static final void debugClick(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED)
            return;
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo == null) {
            return;
        }
        final String SPACE = "\n\t\t\t";
        StringBuilder strNodeInfo = new StringBuilder();
        strNodeInfo.append(SPACE);
        strNodeInfo.append("-----------------");
        strNodeInfo.append(SPACE);
        strNodeInfo.append("View Debug Info: ");
        strNodeInfo.append(SPACE);
        strNodeInfo.append("From package: " + nodeInfo.getPackageName());
        strNodeInfo.append(SPACE);
        strNodeInfo.append("Classname: " + nodeInfo.getClassName());
        strNodeInfo.append(SPACE);
        strNodeInfo.append("ViewID: " + nodeInfo.getViewIdResourceName());
        strNodeInfo.append(SPACE);
        strNodeInfo.append("Text: " + nodeInfo.getText());
        strNodeInfo.append(SPACE);
        strNodeInfo.append("IsClickable: " + nodeInfo.isClickable());
        strNodeInfo.append(SPACE);
        strNodeInfo.append("-----------------");
        XLog.v(TAG, strNodeInfo.toString());
    }

    public String getCurrentActivity(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString());
                ActivityInfo activityInfo = null;
                try {
                    activityInfo =  mAutomator.getService().getPackageManager()
                        .getActivityInfo(componentName, 0);
                } catch (PackageManager.NameNotFoundException e) {}
                if (activityInfo != null)
                    return componentName.flattenToShortString();
            }
        }
        return null;
    }

    /**
     * @see logViewhierarchy(AccessibilityNodeInfo, String)
     */
    public static void logViewHierarchy(AccessibilityNodeInfo rootNode) {
        logViewHierarchy(rootNode, 0);
    }

    /**
     * Logs view hierarchy in a very understandable manner
     *
     * @see logViewhierarchy(AccessibilityNodeInfo)
     */
    private static void logViewHierarchy(AccessibilityNodeInfo rootNode, final int depth) {
        if (rootNode == null) return;

        String spacerString = "";
        for (int i = 0; i < depth; ++i) {
            spacerString += '-';
        }
        // Log the view with its important info here
        XLog.v(TAG, spacerString + rootNode.getClassName().toString() + ", id=" + rootNode.getViewIdResourceName() + ", text=" + rootNode.getText() + ", " + "Clickable=" + rootNode.isClickable());

        for (int i = 0; i < rootNode.getChildCount(); ++i) {
            logViewHierarchy(rootNode.getChild(i), depth + 1);
        }
    }

    /**
     * @see getViewhierachy(AccessibilityNodeInfo)
     * @see getViewhierachy(AccessibilityNodeInfo, String)
     */
    public String getViewHierachy() {
        return getViewHierachy(mAutomator.getRootInActiveWindow());
    }

    /**
     * @see getViewhierachy()
     * @see getViewhierachy(AccessibilityNodeInfo, String)
     */
    public String getViewHierachy(AccessibilityNodeInfo rootNode) {
        return getViewHierachy(rootNode, null);
    }

    /**
     * 
     * Returns view hierarchy of the given node.
     * <b>NOTE: It gets class name of each view and returns them as single string.</b>
     * @param rootNode Node from which you want to get view hierarchy.
     *
     * @return view hierarchy as a string
     *
     * @see getViewhierachy()
     * @see getViewHierachy(AccessibilityNodeInfo)
     */
    private String getViewHierachy(AccessibilityNodeInfo rootNode, String viewHierachy) {
        if (rootNode == null) return viewHierachy;
        if (viewHierachy == null) viewHierachy = new String();

        // Add view's class name to string
        viewHierachy += " " + rootNode.getClassName().toString();

        for (int i = 0; i < rootNode.getChildCount(); ++i) {
            viewHierachy = getViewHierachy(rootNode.getChild(i), viewHierachy);
        }
        return viewHierachy;
    }
}
