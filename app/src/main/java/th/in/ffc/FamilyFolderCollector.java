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

package th.in.ffc;

import android.app.Application;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.Map;

import io.fabric.sdk.android.Fabric;
import th.in.ffc.person.genogram.V1.Family;

/**
 * <<<<<<< HEAD Extended Application class for handle global method and hold
 * some global ======= Extended Application class for handle global method and
 * hold some global >>>>>>> a2f84ac756f5c1a54281f81b0283942d2a95f780 variable
 * such as photo file and database path
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since 1.0
 */
public class FamilyFolderCollector extends Application {

    public static final String TAG = "FFC+";
    public static final String PREF_VERSION = "version";
    public static final boolean HONEYCOMB_UP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    /**
     * Internal private directory
     */
    private static final String DIRECTORY_INTERNAL = "/data/data/th.in.ffc/";
    /**
     * External private directory on SDCard
     */
    private static final String DIRECTORY_EXTERNAL = "/sdcard/Android/data/th.in.ffc/";

    public static final String DATABASE_TEMP = "tmp_mJHCIS.sdb";
    public static final String DATABASE_USER = "uJHCIS.db";
    public static final String DATABASE_DATA = "mJHCIS.db";
    public static final String DATABASE_DATA_ENCRYPT = "mJHCIS.sdb";

    // Database path segment
    /**
     * Plain database's path which save in internal memory
     */
    public static final String PATH_PLAIN_DATABASE = DIRECTORY_INTERNAL
            + "databases/" + DATABASE_DATA;
    /**
     * Path of encrypted database
     */
    public static final String PATH_ENCRYPTED_DATABASE = DIRECTORY_EXTERNAL
            + "databases/" + DATABASE_DATA_ENCRYPT;
    /**
     * Path of user database
     */
    public static final String PATH_USER_DATABASE = DIRECTORY_EXTERNAL
            + "databases/" + DATABASE_USER;
    /**
     * Path for temp of encrypted database
     */
    public static final String PATH_ENCRYPTED_DATABASE_TEMP = DIRECTORY_EXTERNAL
            + "databases/" + DATABASE_TEMP;

    // Photo path segment
    /**
     * Root path of Photo Directory
     */
    private static final String PHOTO_DIRECTORY = DIRECTORY_EXTERNAL + "pictures/";
    /**
     * path of Photo directory for save person photos
     */
    public static final String PHOTO_DIRECTORY_PERSON = PHOTO_DIRECTORY
            + "person/";
    /**
     * path of Photo directory for save service photos
     */
    public static final String PHOTO_DIRECTORY_SERVICE = PHOTO_DIRECTORY
            + "service/";

    /**
     * path of Photo directory for save building photos
     */
    private static final String PHOTO_DIRECTORY_BUILDING = PHOTO_DIRECTORY
            + "building/";
    public static final String PHOTO_DIR_HOUSE = PHOTO_DIRECTORY
            + "HOUSE/";
    public static final String PHOTO_DIR_SCHOOL = PHOTO_DIRECTORY_BUILDING
            + "school/";
    public static final String PHOTO_DIR_POI = PHOTO_DIRECTORY_BUILDING
            + "poi/";
    public static final String PHOTO_DIR_WATER = PHOTO_DIRECTORY_BUILDING
            + "water/";
    public static final String PHOTO_DIR_BUSINESS = PHOTO_DIRECTORY_BUILDING
            + "business/";
    public static final String PHOTO_DIR_TEMPLE = PHOTO_DIRECTORY_BUILDING
            + "temple/";
    public static final String PHOTO_DIR_HOSPITAL = PHOTO_DIRECTORY_BUILDING
            + "hospital/";

    // Cache path segment
    /**
     * cache directory for save some stuff
     */
    public static final String CACHE_DIRECTORY = DIRECTORY_INTERNAL + "cache/";


    public boolean[] mFoundMember;
    public Map<Integer, Family> mFamilys;

    @Override
    public void onCreate() {
        super.onCreate();

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
            .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
            .build();
        Fabric.with(this, crashlyticsKit);
    }
}
