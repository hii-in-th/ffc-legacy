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

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import java.io.File;
import java.util.ArrayList;

import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.DbOpenHelper;

/**
 * This Login Activity use LoginFragment for handler with Login process, if
 * Login success class will call Decrypter Service for handler. when decrypt
 * success will bring user to main page or just disappear for Re-Login Process.
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class LoginActivity extends FFCFragmentActivity implements
        LoginFragment.OnLoginListener {

    private LoginFragment mLoginFrag;
    private DecrypterServiceReceiver mDecrypterReceiver = new DecrypterServiceReceiver();
    private EncrypterServiceRevicer mEncrypterReceiver = new EncrypterServiceRevicer();


    private boolean mRegistedEncrypter = false;
    private boolean mRegistedDecrypter = false;

    private IntentFilter mDecryptFilter = new IntentFilter(Action.DECRYPT);
    private IntentFilter mEncryptFilter = new IntentFilter(Action.ENCRYPT);
    private int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.login_activity);
        this.setSupportProgressBarIndeterminateVisibility(false);
        this.setTrackingEnable(false);
        this.setEnableAutoReLogin(false);

        mLoginFrag = (LoginFragment) getSupportFragmentManager()
                .findFragmentById(R.id.content);
        mLoginFrag.setOnLoginListener(this);


        if (savedInstanceState == null) {

            mLoginFrag.clearPassword();

            if (getIntent().getAction().equals(Action.RE_LOGIN)) {

                mLoginFrag.lockUsername(true);
                mLoginFrag.setUsername(getIntent().getStringExtra("username"));

                if (this.isEncryptServiceRunning()) {

                    this.registerReceiver(mEncrypterReceiver, mEncryptFilter);
                    this.mRegistedEncrypter = true;

                    this.setSupportProgressBarIndeterminateVisibility(true);
                    this.mLoginFrag.setEnable(false);

                }
            }
        }

        if (savedInstanceState != null) {
            quiting = savedInstanceState.getBoolean("quit");
            mRegistedDecrypter = savedInstanceState.getBoolean("regDecrypt");
            mRegistedEncrypter = savedInstanceState.getBoolean("regEncrypt");

            if (mRegistedDecrypter) {
                this.registerReceiver(mDecrypterReceiver, mDecryptFilter);
                this.setSupportProgressBarIndeterminateVisibility(true);
                mLoginFrag.setEnable(false);
            }
            if (mRegistedEncrypter) {
                this.registerReceiver(mEncrypterReceiver, mEncryptFilter);
                this.setSupportProgressBarIndeterminateVisibility(true);
                mLoginFrag.setEnable(false);
            }
        }
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET
        });
    }
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean quiting = false;

    @Override
    public void onBackPressed() {
        if (getIntent().getAction().equals(Action.RE_LOGIN)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.sure_to_exit);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    quiting = true;

                    if (!mRegistedEncrypter) {
                        registerReceiver(mEncrypterReceiver, mEncryptFilter);
                        mRegistedEncrypter = true;
                    }

                    Intent service = new Intent(LoginActivity.this, CryptographerService.class);
                    service.setAction(Action.ENCRYPT);
                    startService(service);

                    ProgressDialog pro = new ProgressDialog(LoginActivity.this);
                    pro.setMessage(getString(R.string.please_wait));
                    pro.show();

                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("regDecrypt", mRegistedDecrypter);
        outState.putBoolean("regEncrypt", mRegistedEncrypter);
        outState.putBoolean("quit", quiting);

        Log.d(TAG, this.toString());


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegistedDecrypter)
            this.unregisterReceiver(mDecrypterReceiver);
        mDecrypterReceiver = null;
        mDecryptFilter = null;

        if (mRegistedEncrypter)
            this.unregisterReceiver(mEncrypterReceiver);
        mEncrypterReceiver = null;
        mEncryptFilter = null;

        mLoginFrag = null;
    }


    @Override
    public String toString() {
        return "LoginActivity [mRegistedEncrypter=" + mRegistedEncrypter
                + ", mRegistedDecrypter=" + mRegistedDecrypter + "]";
    }

    @Override
    public void onLoginSuccess(String pcuCode, String user) {

        super.logIn(pcuCode, user);
        Answers.getInstance().logLogin(new LoginEvent()
            .putMethod("basic")
            .putSuccess(true)
            .putCustomAttribute("pcu", pcuCode));
        Crashlytics.setUserName(user);
        Crashlytics.setUserIdentifier(String.format("%s:%s", pcuCode, user));
        doDecryptDatabase();
    }

    @Override
    public void onLoginFailre(String message) {
        Answers.getInstance().logLogin(new LoginEvent()
            .putMethod("basic")
            .putSuccess(false));
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

    }

    private void doDecryptDatabase() {

        mLoginFrag.setEnable(false);
        this.setSupportProgressBarIndeterminateVisibility(true);

        registerReceiver(mDecrypterReceiver, mDecryptFilter);
        mRegistedDecrypter = true;

        Intent service = new Intent(this, CryptographerService.class);
        service.setAction(Action.DECRYPT);
        startService(service);
    }

    private boolean isEncryptServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (CryptographerService.NAME
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class DecrypterServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            boolean successed;

            successed = intent.getBooleanExtra(CryptographerService.EXTRA_SUCCESS, false);
            if (successed) {
                String sql = "select * from user where pcucode=?" +
                        " AND username=?";
                SQLiteDatabase db = new DbOpenHelper(LoginActivity.this).getReadableDatabase();
                Cursor c = db.rawQuery(sql, new String[]{getPcuCode(), getUser()});
                if (c.moveToFirst()) {
                    if (!getIntent().getAction().equals(Action.RE_LOGIN)) {
                        Intent main = new Intent(Action.MAIN);
                        startActivity(main);
                    } else {
                        //Return Username and Pcucode as result for caller FFCActivity
                        Intent data = new Intent();
                        data.putExtra(FFCFragmentActivity.EXTRA_PCUCODE, getPcuCode());
                        data.putExtra(FFCFragmentActivity.EXTRA_USER, getUser());
                        LoginActivity.this.setResult(RESULT_OK, data);
                    }
                    LoginActivity.this.finish();

                } else {

                    File dbFile = new File(FamilyFolderCollector.PATH_PLAIN_DATABASE);
                    if (dbFile.exists()) {
                        dbFile.delete();
                    }
                    Toast.makeText(LoginActivity.this, "Invalid PCUCODE", Toast.LENGTH_LONG).show();
                }
                c.close();
                db.close();
            } else {
                Toast.makeText(LoginActivity.this,
                        "Try Again,\nFailre to decrypt database",
                        Toast.LENGTH_SHORT).show();
            }

            setSupportProgressBarIndeterminateVisibility(false);
            mLoginFrag.setEnable(true);
        }
    }

    private class EncrypterServiceRevicer extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            mLoginFrag.setEnable(true);
            setSupportProgressBarIndeterminateVisibility(false);

            if (quiting) {

                //call main activity with clear-top flag and extra quit,
                //then main will finish. So, result equal with kill process.
                Intent main = new Intent(Action.MAIN);
                main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                main.putExtra("quit", true);
                startActivity(main);

                LoginActivity.this.finish();
            }
        }
    }


}
