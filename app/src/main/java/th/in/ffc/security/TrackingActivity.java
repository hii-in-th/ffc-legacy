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

/**
 * Tracking Activity (User Tracking Activity)
 * <p/>
 * Extended Activity class can check whether user leave application or screen
 * off/on event. Use it with TrackingCallback Interface Class to implement
 * operation for each event -UserLeave (by Home button press or Switch
 * Application) -UserBack -SystemInterrupt (Suck as Phone in) -Screen ON/OFF
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class TrackingActivity extends AppCompatActivity {

    protected static String TAG = "UserTrackingActivity";

    protected static final String KEY_LEAVE = "Leave";

    protected boolean mLeave = false;
    protected boolean mStartActivity = false;
    protected boolean mUserLeaveHint = false;
    protected boolean mCreateDescription = false;
    protected boolean mScreenReceiverRegisted = false;
    protected BroadcastReceiver mScreenReceiver;
    protected IntentFilter mScreenActionfilter;
    protected TrackingCallback mCallback = null;
    protected Bundle mSaveState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "OnCreate()");

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLeave = savedInstanceState.getBoolean(KEY_LEAVE);
        }

    }

    @Override
    protected void onStart() {
        //Log.d(TAG, "Onstart()");

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
        //Log.d(TAG, "OnResume()");

        mStartActivity = false;
        mUserLeaveHint = false;
        mCreateDescription = false;

        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //Log.d(TAG, "onWindowFocusChanged()");

        if (hasFocus && mLeave && (mCallback != null)) {
            mCallback.onUserBack();
            mLeave = false;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onUserLeaveHint() {
        //Log.d(TAG, "OnUserLeaveHint()");

        mUserLeaveHint = true;
        super.onUserLeaveHint();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Log.d(TAG, "onSaveInstanceState()");

        // TODO: Copy outState's Pointer for Save Instance State in other method
        super.onSaveInstanceState(outState);
        mSaveState = outState;
    }

    @Override
    public CharSequence onCreateDescription() {
        //Log.d(TAG, "onCreateDescription()");

        mCreateDescription = true;
        return super.onCreateDescription();
    }

    @Override
    protected void onStop() {
        //Log.d(TAG, "OnStop()");

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
				 * If user leave this activity by Call in or Something that was
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
            //Log.w(TAG, "UTA : Unregistered ScreenReceiver!");
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Log.d(TAG, "OnDestroy()");
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
        mScreenActionfilter = null;
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

    public class NonConfigurationInstanceRetainer {
        boolean leave = false;
        boolean startActivity = false;
        boolean userLeaveHint = false;
        boolean createDescription = false;
        boolean screenReceiverRegisted = false;
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

    protected static interface TrackingCallback {

        public void onScreenOn();

        public void onScreenOff();

        public void onUserLeave(boolean systemInterrupt);

        public void onSystemInterrupt();

        public void onUserBack();

    }
}
