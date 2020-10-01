package com.gameofcoding.automator.automator;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.gameofcoding.automator.R;
import com.gameofcoding.automator.automator.AutomatorPrefs;
import com.gameofcoding.automator.utils.XLog;
import com.gameofcoding.automator.utils.Utils;

public class Automator extends BaseAutomator {
    private static final String TAG = "Automator";

    // TODO: Enter package name
    private static final String PACKAGE_NAME = "package_name";

    /////////////////////////////////////////////
    //             Thread Runnable             //
    /////////////////////////////////////////////
    private final Runnable mAutomatorThreadRunnable = new Runnable() {
        private void onStart() {
            XLog.i(TAG, "Thread: Started");
            // TODO: Load your coordinates here
            // AutomatorPrefs prefs = new AutomatorPrefs(getContext());
            mIsThreadRunning = true;
        }

        private void onStop() {
            mIsThreadRunning = false;
            removeOverlay();
            onAutomatorThreadStopped();
            XLog.i(TAG, "Thread: Finished!");
        }

        @Override
        public void run() {
            onStart();
            while(mContinueThread) {
                if(mCurrentActivity != null)
                    XLog.v(TAG, mCurrentActivity);
                partialSleep(5000);
            }
            onStop();
        }

        /**
         * This method is very useful if you want to wait for a few seconds but also want
         * to check whether we are still in game and user had not asked us to stop the automation.
         *
         * @param secs Number of seconds you want to wait
         */
        private void partialSleep(int secs) {
            try {
                synchronized (this) {
                    while(mContinueThread && secs > 0) {
                        wait(1000);
                        secs--;
                    }
                }
            } catch (InterruptedException e) {
                XLog.e(TAG, "partialSleep(int): Failed to wait, secs=" + secs, e);
            }	
        }


    };
    /////////////////////////////////////////////

    private AccessibilityEvent mLastEvent;
    private AutomatorUtils mAutomatorUtils;
    private String mCurrentActivity;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private View mLayout;
    private Button mBtnStartAutomator;
    private Button mBtnStopAutomator;
    private Handler mHandler;
    private boolean mIsThreadRunning = false;
    private boolean mContinueThread = false;
    private Thread mAutomatorThread = null;

    public Automator(AccessibilityService service) {
        super(service);
        mHandler = new Handler(Looper.getMainLooper());
        mAutomatorUtils = new AutomatorUtils(this);
    }

    public Automator(Context context) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onStart() {
        if(!hasOverlay()) {
            launchApp(getPackageName());
            createOverlay();
            makeOverlayWrapContent();
        }
    }

    @Override
    public void onEvent(AccessibilityEvent event) {
        mLastEvent = event;
        String currentActivity = mAutomatorUtils.getCurrentActivity(event);
         if(currentActivity != null)
             mCurrentActivity = currentActivity;
    }

    @Override
    public void clickAt(int x, int y) {
        makeOverlayNotTouchable();
        sleep(100);
        super.clickAt(x, y);
        sleep(50);
        makeOverlayTouchable();
    }

    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    private void startAutomatorThread() {
        if (!mIsThreadRunning) {
            mContinueThread = true;
            makeOverlayMatchParent();
            mAutomatorThread = new Thread(mAutomatorThreadRunnable);
            mAutomatorThread.setPriority(Thread.MAX_PRIORITY);
            mAutomatorThread.start();
            showStopAutomatorBtn();
        } else {
            showToast("Automator has already been started!");
        }
    }

    private void stopAutomatorThread() {
        if (mIsThreadRunning) {
            if (mContinueThread) {
                mContinueThread = false;
                disableStopAutomatorBtn();
            }
        }
    }

    /**
     * Called when thread is fianlly stopped.
     */
    private void onAutomatorThreadStopped() {
        removeOverlay();
        showToast("Stopped!");
    }

    /**
     * Shows 'stop' button and hides 'start' button.
     *
     */
    public void showStopAutomatorBtn() {
        if(mBtnStartAutomator != null && mBtnStopAutomator != null) {
            mBtnStartAutomator.setVisibility(View.GONE);
            mBtnStopAutomator.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Makes 'stop' button (that is on overlay) disabled.
     */
    public void disableStopAutomatorBtn() {
        if(mBtnStopAutomator != null)
            mBtnStopAutomator.setEnabled(false);
    }

    /**
     * Checks whether we have added overlay or not.
     */
    private boolean hasOverlay() {
        return (mLayout != null && mLayout.getParent() != null);
    }


    /**
     * Makes overlay to take all available space of screen.
     */
    private void makeOverlayMatchParent() {
        if(!hasOverlay())
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                mWindowManager.updateViewLayout(mLayout, mLayoutParams);
            }
        });
    }

    /**
     * Makes overlay to take only that much space that its content requests.
     */
    private void makeOverlayWrapContent() {
        if(!hasOverlay())
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mWindowManager.updateViewLayout(mLayout, mLayoutParams);
            }
        });
    }

    /**
     * By calling this user will not be able to click on the actual content
     * on the screen but will be able to click on overlay.
     */
    private void makeOverlayTouchable() {
        if(!hasOverlay())
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                mWindowManager.updateViewLayout(mLayout, mLayoutParams);
            }
        });
    }

    /**
     * By calling this method user will be able to click that phone normally.
     */
    private void makeOverlayNotTouchable() {
        if(!hasOverlay())
            return;
        mHandler.post(new Runnable() {

            @Override
            public void run() {   
                mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                mWindowManager.updateViewLayout(mLayout, mLayoutParams);
            }
        });
    }

    /**
     * Creates overlay over screen.
     */
    private void createOverlay() {
        if(mLayout == null || !hasOverlay()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {			
                    mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                    mLayoutParams = new WindowManager.LayoutParams();
                    mLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                    mLayoutParams.format = PixelFormat.TRANSLUCENT;
                    mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mLayoutParams.gravity = Gravity.TOP | Gravity.CENTER;
                    final LayoutInflater inflater = LayoutInflater.from(getContext());
                    mLayout  = inflater.inflate(R.layout.layout_overlay, null);
                    mWindowManager.addView(mLayout, mLayoutParams);
                    mBtnStartAutomator = mLayout.findViewById(R.id.start_automator);
                    mBtnStopAutomator = mLayout.findViewById(R.id.stop_automator);
                    mBtnStartAutomator.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startAutomatorThread();
                        }
                    });
                    mBtnStopAutomator.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            stopAutomatorThread();
                        }
                    });
                }
            });
        }
    }

    /**
     * Removes overlay from screen.
     */
    private void removeOverlay() {
        if(mLayout != null && hasOverlay()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWindowManager.removeView(mLayout);
                }
            });
        }
    }

    /**
     * Handy method for showing toasts, even from another thread.
     */
    private void showToast(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(getService(), msg);
            }
        });
    }
}
