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

package th.in.ffc.app.state;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.util.Log;

import java.io.File;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class StateNotFoundDatabase extends FFCFragmentActivity {

    private static final int REQUEST_CODE_ASK_PERMISSIONS_EXTERNAL_STORAGE=12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkExternalStoragePermission();
        setEnableAutoReLogin(false);
        setEnableCheckMediaState(true);
        setEnableCheckDatabase(false);
        setContentView(R.layout.state_database);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File userDb = new File(FamilyFolderCollector.PATH_USER_DATABASE);
        File dataDb = new File(FamilyFolderCollector.PATH_ENCRYPTED_DATABASE);
        if (userDb.exists() && dataDb.exists()) {
            this.finish();
        }
    }
//    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkExternalStoragePermission(){
        int hasStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS_EXTERNAL_STORAGE);
                return false;
            }
            return false;
        } else return true;
    }

    @Override
    public void onBackPressed() {
        File userDb = new File(FamilyFolderCollector.PATH_USER_DATABASE);
        File dataDb = new File(FamilyFolderCollector.PATH_ENCRYPTED_DATABASE);
        if (userDb.exists() && dataDb.exists()) {
            this.finish();
        }
    }
}
