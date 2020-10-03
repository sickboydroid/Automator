package com.gameofcoding.automator.automator;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import com.gameofcoding.automator.R;
import com.gameofcoding.automator.utils.XLog;
import com.gameofcoding.automator.utils.Utils;
import com.gameofcoding.automator.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Random;

public class Automator extends BaseAutomator {
    private static final String TAG = "Automator";

    // TODO: Enter package name
    private static final String PACKAGE_NAME = "com.byjus.thelearningapp";

    /////////////////////////////////////////////
    //             Thread Runnable             //
    /////////////////////////////////////////////
    private final Runnable mAutomatorThreadRunnable = new Runnable() {
        final File ANSWER_FILE = new File("/sdcard/Android/data/com.byjus.thelearningapp/cache/answer.txt");
        final String SPLASH_ACTIVITY = "com.byjus.thelearningapp/com.byjus.app.onboarding.activity.SplashActivity";
        final String HOME_ACTIVITY = "com.byjus.thelearningapp/com.byjus.app.home.activity.HomeActivity";
        final String QUIZOO_HOME_ACTIVITY = "com.byjus.thelearningapp/com.byjus.quizzo.QuizzoHomeActivity";
        final String SELECT_OPPONENT_ACTIVITY = "com.byjus.thelearningapp/com.byjus.quizzo.SelectOpponentActivity";
        final String SELECT_TOPIC_ACTIVITY = "com.byjus.thelearningapp/com.byjus.quizzo.SelectTopicActivity";
        final String START_MATCH_ACTIVITY = "com.byjus.thelearningapp/com.byjus.quizzo.StartMatchActivity";
        final String MATCH_ACTIVITY = "com.byjus.thelearningapp/com.byjus.quizzo.MatchActivity";
        final String MATCH_RESULT_ACTIVITY = "com.byjus.thelearningapp/com.byjus.quizzo.MatchResultActivity";
        final String[] TOPICS = {
            "MATH",
            "PHYSICS",
            "CHEMISTRY",
            "BIOLOGY",
            "HISTORY",
            "GEOGRAPHY",
            "SCIENCE",
            "GK"
        };

        long mStartTime;

        private void onStart() {
            XLog.i(TAG, "Thread: Started");
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
            mStartTime = System.currentTimeMillis();
            while(continueThread()) {
                if(isHomeActivity()) {
                    handleHomeActivity();
                } else if(isQuizooHomeActivity()) {
                    handleQuizooHomeActivity();
                } else if(isSelectOpponentActivity()) {
                    handleSelectOpponentActivity();
                } else if(isSelectTopicActivity()) {
                    handleSelectTopicActivity();
                } else if(isStartMatchActivity()) {
                    handleStartMatchActivity();
                } else if(isMatchActivity()) {
                    handleMatchActivity();
                } else if(isMatchResultActivity()) {
                    handleMatchResultActivity();
                } else if(isSplashActivity()) {
                    sleep(500);
                }
                restartAppIfNecessary();
            }
            onStop();
        }

        public boolean continueThread() {
            return mContinueThread;
        }

        public void restartAppIfNecessary() {
            long time = System.currentTimeMillis() - mStartTime;
            time = ((time / 1000) / 60);
            if(time >= 15) {
                getService().performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                restartApp(getPackageName());
                mStartTime = System.currentTimeMillis();
                partialSleep(5);
            }
        }

        private void handleHomeActivity() {
            clickView("com.byjus.thelearningapp:id/backNav");
            partialSleep(1);
            List <AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText("Quizzo");
            for(AccessibilityNodeInfo node : nodes) {
                node = node.getParent();
                if(node != null && node.isClickable() && node.isEnabled())
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            partialSleep(1);
        }

        private void handleQuizooHomeActivity() {
            clickView("com.byjus.thelearningapp:id/btnContinue");
            sleep(500);
        }

        private void handleSelectOpponentActivity() {
            clickView("com.byjus.thelearningapp:id/btnContinue");
            partialSleep(2);
        }

        private void handleSelectTopicActivity() {
            String topic = TOPICS[new Random().nextInt(TOPICS.length)];
            List<AccessibilityNodeInfo> nodes =  getRootInActiveWindow().findAccessibilityNodeInfosByText(topic);
            for(AccessibilityNodeInfo node : nodes) {
                node = node.getParent();
                if(node != null && node.isClickable() && node.isEnabled())
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            sleep(500);
            clickView("com.byjus.thelearningapp:id/btnContinue");
            partialSleep(1);
        }

        private void handleStartMatchActivity() {
            clickAt(340, 1440);
            sleep(600);
        }

        private void handleMatchActivity() {
            List<AccessibilityNodeInfo> options = getOptions();
            if(options == null)
                return;
            if(options.size() > 1) {
                String correctOption = getCorrectOption();
                if(correctOption == null)
                    return;
                for(AccessibilityNodeInfo option : options) {
                    if(option.getText().equals(correctOption)) {
                        option = option.getParent();
                        if(option != null && option.isClickable() && option.isEnabled()) {
                            option.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            sleep(250);
                        }
                        break;
                    }
                }
            } 
        }

        private List<AccessibilityNodeInfo> getOptions() {
            if(getRootInActiveWindow() != null)
                return getRootInActiveWindow()
                    .findAccessibilityNodeInfosByViewId("com.byjus.thelearningapp:id/tvAnswer");
            return null;
        }

        private String getCorrectOption() {
            try {
                if(ANSWER_FILE.exists() && ANSWER_FILE.isFile())
                    return FileUtils.read(ANSWER_FILE);
            } catch(Exception e) {
                XLog.e(TAG, "Exception occurred while reading answer file", e);
            }
            return null;
        }

        public void handleMatchResultActivity() {
            clickView("com.byjus.thelearningapp:id/btnClose");
            sleep(500);
        }

        public boolean isSplashActivity() {
            return mCurrentActivity.equals(SPLASH_ACTIVITY);
        }

        private boolean isHomeActivity() {
            return mCurrentActivity.equals(HOME_ACTIVITY);
        }

        private boolean isQuizooHomeActivity() {
            return mCurrentActivity.equals(QUIZOO_HOME_ACTIVITY);
        }

        public boolean isSelectOpponentActivity() {
            return mCurrentActivity.equals(SELECT_OPPONENT_ACTIVITY);
        }

        public boolean isSelectTopicActivity() {
            return mCurrentActivity.equals(SELECT_TOPIC_ACTIVITY);
        }

        public boolean isStartMatchActivity() {
            return mCurrentActivity.equals(START_MATCH_ACTIVITY);
        }

        public boolean isMatchActivity() {
            return mCurrentActivity.equals(MATCH_ACTIVITY);
        }

        public boolean isMatchResultActivity() {
            return mCurrentActivity.equals(MATCH_RESULT_ACTIVITY);
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
        AutomatorUtils.debugClick(mLastEvent);
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
            //FIXME;------>
            makeOverlayWrapContent();
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
