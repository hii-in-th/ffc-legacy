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

package th.in.ffc.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;
import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.Visit;
import th.in.ffc.security.CryptographerService;
import th.in.ffc.security.TrackingFragmentActivity;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.Log;
import th.in.ffc.util.StopWatch;

import java.io.File;
import java.util.List;

/**
 * FFCFragmentActivity
 * <p/>
 * Base FragmentActivity for use overall project. this class was implemented
 * TrackingCallback of TrackingFragmneActivity(in Security package). So, all
 * activity extends this class will automatic track user.
 * <p/>
 * Them, if user leave from application more than defined time. Step 1 when user
 * back to Application will force user to Re-Login. Step 2 Application automatic
 * Encrypt database and Re-Login and Re-Decrypt when him/her is back.
 * <p/>
 * This class also provider name and PCUcode of User you can get it by following
 * method getUser(); getPCUcode(); and also send to other activity with extras
 * <p/>
 * 1.0b fix bug on Re-Login process add clear top flag
 * 1.1 add checkApplicationState at onResum() for check SDcard-state, database,
 * date setting and User Login
 * 1.2 change re-login process to return user and PCUCODE as Result's Extra
 *
 * @author Piruin Panichphol
 * @version 1.2
 * @since Family Folder Collector Plus
 */
public class FFCFragmentActivity extends TrackingFragmentActivity implements
        TrackingFragmentActivity.TrackingCallback {

    protected static final String TAG = "FFC-Activity";
    private static final int REQUEST_RELOGIN = 1902;

    private boolean reLogin = false;
    private boolean mTimerOn = false;
    private boolean mTrack = true;
    private boolean mAutoRelogin = true;
    private boolean mCheckMediaState = true;
    private boolean mCheckDatabaseFile = true;

    protected static final String PREF_NAME = "Fragment";

    public static final String EXTRA_PCUCODE = "pcucode";
    public static final String EXTRA_USER = "user";


    private static final String STATE_RELOGIN = "relogin";

    private String extra_pcucode;
    private String extra_user;

    /**
     * Time out for reLogin step in millisecond ( 3 minute )
     */
    private static int RELOGIN_TIME_OUT = 3 * 60 * 1000;
    /**
     * Time out for Encryption step in millisecond ( 5 minute)
     */
    private static int ENCRYPT_TIME_OUT = 5 * 60 * 1000;

    private Handler mTimerHandler = new Handler();
    private StopWatch mStopWatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setTrackingCallback(this);
        this.getExtra();


        if (savedInstanceState == null)
            editPreferences(false);

        String action = getIntent().getAction();
        if (action != null && action.equals(Action.RE_LOGIN)) {
            reLogin = true;
            editPreferences(reLogin);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_PCUCODE, extra_pcucode);
        outState.putString(EXTRA_USER, extra_user);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            extra_pcucode = savedInstanceState.getString(EXTRA_PCUCODE);
            extra_user = savedInstanceState.getString(EXTRA_USER);
        }
    }

    public void setEnableCheckMediaState(boolean enable) {
        this.mCheckMediaState = enable;
    }

    public void setEnableAutoReLogin(boolean enable) {
        this.mAutoRelogin = enable;
    }

    public void setEnableCheckDatabase(boolean enable) {
        this.mCheckDatabaseFile = enable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case REQUEST_RELOGIN:
                if (resultCode == RESULT_OK) {
                    extra_pcucode = data.getStringExtra(EXTRA_PCUCODE);
                    extra_user = data.getStringExtra(EXTRA_USER);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doCheckApplicationState();
    }


    /**
     * private method to check all necessary state for make application
     * run correctly
     *
     * @return true if all check state pass, false otherwise
     * @since Family Folder Collector Plus
     */
    private boolean doCheckApplicationState() {

        //check media's state (SDcard) whether it available for read-write or not
        if (mCheckMediaState) {
            String sdcardstate = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(sdcardstate)) {
                Intent state = new Intent(Action.STATE_MEDIA_UNMOUNT);
                state.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(state);
                return false;
            }
        }

        // check whether user-database and data-database are both exist
        if (mCheckDatabaseFile) {
            File userDb = new File(FamilyFolderCollector.PATH_USER_DATABASE);
            File dataDb = new File(FamilyFolderCollector.PATH_ENCRYPTED_DATABASE);

            if (!userDb.exists() || !dataDb.exists()) {
                Intent state = new Intent(Action.STATE_NOT_FOUND_DATABASE);
                state.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(state);
                return false;
            }
        }


        //call Re-Login activity if user not already log in
        if (mAutoRelogin) {
            if (TextUtils.isEmpty(extra_user) || TextUtils.isEmpty(extra_pcucode)) {
                Log.d(TAG, "Auto Re-Login");
                Intent login = new Intent(Action.RE_LOGIN);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivityForResult(login, REQUEST_RELOGIN);
                return false;
            }
        }

        return true;
    }


    /**
     * @return true if system's date is up to date, false if not.
     * @since Family Folder Collector Plus
     */
    protected void doCheckDateSetting() {


        getSupportLoaderManager().restartLoader(0, null, new LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
                CursorLoader cl = new CursorLoader(FFCFragmentActivity.this, Visit.CONTENT_URI,
                        new String[]{Visit.UPDATE}, null, null, Visit.NO + " DESC");
                return cl;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
                boolean result = true;
                if (c.moveToFirst()) {
                    Date system = Date.newInstance(DateTime.getCurrentDate());
                    Date data = Date.newInstance(c.getString(0));

                    if (system.compareTo(data) == Date.LESS_THAN) {
                        Intent state = new Intent(Action.STATE_DATE_NOT_UPDATE);
                        state.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        if (mDateListener == null)
                            startActivity(state);
                        result = false;
                    } else {
                        result = true;
                    }
                }

                if (mDateListener != null) {
                    mDateListener.onFinish(result);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
            }

        });
    }

    protected void doCheckDateSetting(OnCheckDateUpdateListener DateListener) {
        mDateListener = DateListener;
        doCheckDateSetting();
    }

    private OnCheckDateUpdateListener mDateListener;

    public static interface OnCheckDateUpdateListener {
        public void onFinish(boolean result);
    }

    /**
     * For Log out user, this will affective when ever activity onResume() was call by
     * Automatic call re-Login Activity
     *
     * @since Family Folder Collector Plus
     */
    protected void logOut() {
        extra_pcucode = null;
        extra_user = null;
    }

    /**
     * User for login user to system
     *
     * @param pcucode
     * @param user
     * @since Family Folder Collector Plus
     */
    protected void logIn(String pcucode, String user) {
        extra_pcucode = pcucode;
        extra_user = user;
    }

    /**
     * Method to set weather you need tracking operation of FFCFragmentActivity or not.
     * if not UserLeave Timer will never start.
     *
     * @param track enable tracking operation or not default is true
     */
    public void setTrackingEnable(boolean track) {
        mTrack = track;
    }


    /**
     * private method that will try to load Extra PCUCode and User of Extra from
     * called activity
     */
    private final void getExtra() {
        Intent extra = getIntent();
        this.extra_pcucode = extra.getStringExtra(EXTRA_PCUCODE);
        this.extra_user = extra.getStringExtra(EXTRA_USER);
    }

    @Deprecated
    protected void setUser(String user) {
        this.extra_user = user;
    }

    @Deprecated
    protected void setPcuCode(String pcuCode) {
        this.extra_pcucode = pcuCode;
    }

    public String getUser() {
        return this.extra_user;
    }

    public String getPcuCode() {
        return this.extra_pcucode;
    }

    public void startHomeActivity() {
        startHomeActivity(null);
    }

    public void startHomeActivity(Intent intent) {
        if (intent == null)
            intent = new Intent();
        intent.setAction(Action.HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra(EXTRA_PCUCODE, this.extra_pcucode);
        intent.putExtra(EXTRA_USER, this.extra_user);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> IntentList = pm.queryIntentActivities(intent, 0);

        if (IntentList.size() == 0) {
            Toast.makeText(this, "Not Found Activity!", Toast.LENGTH_SHORT).show();
        } else {
            super.startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(EXTRA_PCUCODE, this.extra_pcucode);
        intent.putExtra(EXTRA_USER, this.extra_user);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> IntentList = pm.queryIntentActivities(intent, 0);

        if (IntentList.size() == 0) {
            Toast.makeText(this, "Not Found Activity!", Toast.LENGTH_SHORT).show();
        } else {
            super.startActivityForResult(intent, requestCode);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.finish_slide_in, R.anim.finish_slide_out);
    }


    @Override
    public void onScreenOn() {
        // do nothing
    }

    @Override
    public void onScreenOff() {
        startTimer();
    }

    @Override
    public void onUserLeave(boolean systemInterrupt) {
        startTimer();
    }

    @Override
    public void onSystemInterrupt() {

    }

    @Override
    public void onUserBack() {
        Log.d("FFC", "user is back!");
        stopTimer();
    }


    /**
     * to Stop Timer when user was back and lanch Must-do Operation
     */
    private synchronized void stopTimer() {
        mTimerHandler.removeCallbacks(mTimer);
        reLogin = this.getPreferences();
        boolean timeout = false;
        if (mStopWatch != null) {
            mStopWatch.stop();
            Log.d(TAG, "user leave[sec] " + mStopWatch.getElapsedTimeSecs());
            timeout = mStopWatch.getElapsedTime() > RELOGIN_TIME_OUT;
        }

        String action = getIntent().getAction();
        boolean onReLoginAct = false;
        if (!TextUtils.isEmpty(action) && action.equals(Action.RE_LOGIN))
            onReLoginAct = true;

        if ((reLogin || timeout) && !onReLoginAct) {


            Intent login = new Intent(Action.RE_LOGIN);
            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            login.putExtra("username", getUser());
            this.startActivityForResult(login, REQUEST_RELOGIN);

            logOut();
        }

        mTimerOn = false;
        reLogin = false;
        this.editPreferences(reLogin);
    }

    /**
     * Start Timer called when user leave applicaiton
     */
    private synchronized void startTimer() {
        if (mTrack) {

            if (mStopWatch == null)
                mStopWatch = new StopWatch();

            if (!mTimerOn) {
                mTimerOn = true;
                mStopWatch.start();
                mTimerHandler.removeCallbacks(mTimer);
                mTimerHandler.postDelayed(mTimer, RELOGIN_TIME_OUT);
            }
        }
    }

    /**
     * Runnable Timer for trigged each leave time step
     */
    public Runnable mTimer = new Runnable() {

        @Override
        public void run() {
            Log.d("FFC", "relogin=" + reLogin);
            reLogin = getPreferences();
            if (!reLogin) {
                Log.d("FFC", "relogin");
                reLogin = true;
                if (mTimerHandler == null)
                    mTimerHandler = new Handler();
                mTimerHandler.postDelayed(this, ENCRYPT_TIME_OUT);
                editPreferences(true);
            } else {
                Log.d("FFC", "re-encrypt");
                reLogin = true;
                Intent encrypter = new Intent(FFCFragmentActivity.this, CryptographerService.class);
                encrypter.setAction(Action.ENCRYPT);
//                startService(encrypter);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(encrypter);
                } else {
                    startService(encrypter);
                }
            }
        }

    };

    private void editPreferences(boolean reLogin) {
        SharedPreferences prefer = getSharedPreferences(FFCFragmentActivity.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean(STATE_RELOGIN, reLogin);
        editor.commit();

    }

    private boolean getPreferences() {
        SharedPreferences prefer = getSharedPreferences(FFCFragmentActivity.PREF_NAME, Context.MODE_PRIVATE);
        return prefer.getBoolean(STATE_RELOGIN, false);
    }


}
