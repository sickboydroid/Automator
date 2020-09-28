package com.gameofcoding.automator.automator;

import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;

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
	StringBuilder strNodeInfo = new StringBuilder();
	strNodeInfo.append("View Debug Info: ");
	strNodeInfo.append("\n");
	strNodeInfo.append("From package: " + nodeInfo.getPackageName());
	strNodeInfo.append("\n");
	strNodeInfo.append("Classname: " + nodeInfo.getClassName());
	strNodeInfo.append("\n");
	strNodeInfo.append("ViewID: " + nodeInfo.getViewIdResourceName());
	strNodeInfo.append("\n");
	strNodeInfo.append("Text: " + nodeInfo.getText());
	strNodeInfo.append("\n");
	strNodeInfo.append("IsClickable: " + nodeInfo.isClickable());
	XLog.v(TAG, strNodeInfo.toString());
    }

    /**
     * @see logViewhierarchy(AccessibilityNodeInfo, String)
     */
    public void logViewHierarchy(AccessibilityNodeInfo rootNode) {
	logViewHierarchy(rootNode, 0);
    }

    /**
     * Logs view hierarchy in a very understandable manner
     *
     * @see logViewhierarchy(AccessibilityNodeInfo)
     */
    private void logViewHierarchy(AccessibilityNodeInfo rootNode, final int depth) {
	if (rootNode == null) return;

	String spacerString = "";
	for (int i = 0; i < depth; ++i) {
	    spacerString += '-';
	}
	// Log the view with its important info here
	XLog.v(TAG, spacerString + rootNode.getClassName().toString() + ", " + rootNode.getViewIdResourceName() + ", " + rootNode.getText());

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
