/* ***********************************************************************
 *                                                                 _ _ _
 *                                                               ( _ _  |
 *                                                           _ _ _ _  | |
 *                                                          (_ _ _  | |_|
 *  _     _   _ _ _ _     _ _ _   _ _ _ _ _   _ _ _ _     _ _ _   | | 
 * |  \  | | |  _ _ _|   /  _ _| |_ _   _ _| |  _ _ _|   /  _ _|  | |
 * | | \ | | | |_ _ _   /  /         | |     | |_ _ _   /  /      |_|
 * | |\ \| | |  _ _ _| (  (          | |     |  _ _ _| (  (    
 * | | \ | | | |_ _ _   \  \_ _      | |     | |_ _ _   \  \_ _ 
 * |_|  \__| |_ _ _ _|   \_ _ _|     |_|     |_ _ _ _|   \_ _ _| 
 *  a member of NSTDA, @Thailand
 *  
 * ***********************************************************************
 *
 *
 * FFC-Plus Project
 *
 * Copyright (C) 2010-2012 National Electronics and Computer Technology Center
 * All Rights Reserved.
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package th.in.ffc.security;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import th.in.ffc.util.Log;

/**
 * Tracking FragmentActivity (User Tracking Activity)
 * <p/>
 * Extended FragmentActivityActivity class can check whether user leave
 * application or screen off/on event. Use it with TrackingCallback Interface
 * Class to implement operation for each event -UserLeave (by Home button press
 * or Switch Application) -UserBack -SystemInterrupt (Suck as Phone in) -Screen
 * ON/OFF
 *
 * @author piruin panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class TrackingFragmentActivity extends AppCompatActivity {

    private static String TAG = "UserTrackingActivity";
    private static final String KEY_LEAVE = "Leave";

    private boolean mLeave = false;
    private boolean mStartActivity = false;
    private boolean mUserLeaveHint = false;
    private boolean mCreateDescription = false;
    private boolean mScreenReceiverRegisted = false;
    private BroadcastReceiver mScreenReceiver;
    private TrackingCallback mCallback = null;
    private Bundle mSaveState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate()");

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLeave = savedInstanceState.getBoolean(KEY_LEAVE);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Onstart()");

        if (!mScreenReceiverRegisted) {

            mScreenReceiverRegisted = true;
            IntentFilter mScreenActionfilter = new IntentFilter();
            mScreenActionfilter.addAction(Intent.ACTION_SCREEN_ON);
            mScreenActionfilter.addAction(Intent.ACTION_SCREEN_OFF);
            mScreenReceiver = new ScreenActionReceiver();
            registerReceiver(mScreenReceiver, mScreenActionfilter);
        }
        super.onStart();
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume()");

        mStartActivity = false;
        mUserLeaveHint = false;
        mCreateDescription = false;

        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged()");

        if (hasFocus && mLeave && (mCallback != null)) {
            mCallback.onUserBack();
            mLeave = false;
        }
        super.onWindowFocusChanged(hasFocus);
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "OnPause()");
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        Log.d(TAG, "OnUserLeaveHint()");

        mUserLeaveHint = true;
        super.onUserLeaveHint();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState()");

        // TODO: Copy outState's Pointer for Save Instance State in other method
        super.onSaveInstanceState(outState);
        mSaveState = outState;
        mSaveState.putBoolean(KEY_LEAVE, mLeave);
    }

    @Override
    public CharSequence onCreateDescription() {
        Log.d(TAG, "onCreateDescription()");

        mCreateDescription = true;
        return super.onCreateDescription();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "OnStop()");

        if (!mStartActivity && mCreateDescription) {
            /*
			 * User Leave this activity by somehow suck as Press Home Button,
			 * Press Notification in Notification bar Or Phone incoming
			 */
            mLeave = true;
            if (mSaveState != null)
                mSaveState.putBoolean(KEY_LEAVE, mLeave);

            if (mCallback != null) {
                mCallback.onUserLeave(!mUserLeaveHint);
				/*
				 * If user leave this activity by Phone or Something that was
				 * not choice of user then call onSystemInterrupt() for more
				 * flexible
				 */
                if (!mUserLeaveHint)
                    mCallback.onSystemInterrupt();
            }
        } else {

			/*
			 * If start other Activity we must unregister ScreenReceiver before;
			 */
            unregisterReceiver(mScreenReceiver);
            mScreenReceiverRegisted = false;
            Log.w(TAG, "UTA : Unregistered ScreenReceiver!");
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "OnDestroy()");
		/*
		 * if ScreenReceiver not unregister yet, do it here.
		 */
        if (mScreenReceiverRegisted)
            unregisterReceiver(mScreenReceiver);
		/*
		 * Avoid cause of memory leak
		 */
        mSaveState = null;
        mScreenReceiver = null;
        mCallback = null;
        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        mStartActivity = true;
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        mStartActivity = true;
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityFromChild(Activity child, Intent intent,
                                       int requestCode) {
        mStartActivity = true;
        super.startActivityFromChild(child, intent, requestCode);
    }

    protected void setTrackingCallback(TrackingCallback callback) {
        mCallback = callback;
    }

    private class ScreenActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mLeave = true;
                if (mCallback != null)
                    mCallback.onScreenOff();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (mCallback != null)
                    mCallback.onScreenOn();
            }
        }
    }

    public static interface TrackingCallback {

        public void onScreenOn();

        public void onScreenOff();

        public void onUserLeave(boolean systemInterrupt);

        public void onSystemInterrupt();

        public void onUserBack();

    }
}
