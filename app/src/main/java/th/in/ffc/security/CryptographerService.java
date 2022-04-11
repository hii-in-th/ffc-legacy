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

import android.app.IntentService;
import android.content.Intent;
import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.intent.Action;
import th.in.ffc.util.Log;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * Encrypter / Decrypter service operation by Intent Action. and send broadcast
 * when it finish
 * <p/>
 * see th.in.ffc.intent.Action
 *
 * @author Piruin Panichphol
 * @version 2.0
 * @since Family Folder Collector 2.0
 */
public class CryptographerService extends IntentService {

    public static final String NAME = "th.in.ffc.security.CryptographerService";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_MESSAGE = "msg";

    private static Cryptographer mCrypto;

    private static final float FILE_SIZE_MARGIN = 0.95f;

    public CryptographerService() {
        super(CryptographerService.NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("FFC", "crypto was called action=" + intent.getAction());

        String action = intent.getAction();
        boolean success = false;

        if (action.equals(Action.DECRYPT))
            success = doDecryptDatabase();
        else if (action.equals(Action.ENCRYPT))
            success = doEncryptDatabase();
        else
            throw new InvalidParameterException(
                    "Invailid argrument action to service");

        Log.d("FFC", "success=" + success);

        Intent boardcast = new Intent(action);
        boardcast.putExtra(EXTRA_SUCCESS, success);
        sendBroadcast(boardcast);


    }

    /**
     * @return true if Decrypted
     */
    private synchronized boolean doDecryptDatabase() {


        Log.d("FFC", "Decrypting");
        File encryptDb = new File(FamilyFolderCollector.PATH_ENCRYPTED_DATABASE);
        if (!encryptDb.exists()) {
            Log.d("FFC", "encrypted db not exist");
            return false;
        }
        File plainDb = new File(FamilyFolderCollector.PATH_PLAIN_DATABASE);
        if (plainDb.exists()) {
            if ((plainDb.length() - encryptDb.length() * FILE_SIZE_MARGIN) < 0) {
                // Database exist but it may broken
                plainDb.delete();
            } else {
                // Database is already exist
                return true;
            }
        }

        plainDb = DatabaseCreator.CreateSecureDatabasGroup(
                getApplicationContext(), FamilyFolderCollector.DATABASE_DATA);

        mCrypto = new Cryptographer(Cryptographer.getKey(),
                Cryptographer.ALGORITHM_FFC);

        boolean success = mCrypto.decrypt(encryptDb, plainDb);
        if (!success)
            plainDb.delete();

        return success;

    }

    /**
     * @return true if Encrypted success
     */
    private synchronized boolean doEncryptDatabase() {

        Log.d("FFC", "Encrypting");
        File plainDb = new File(FamilyFolderCollector.PATH_PLAIN_DATABASE);
        if (!plainDb.exists())
            return false;

        File encryptDb = new File(FamilyFolderCollector.PATH_ENCRYPTED_DATABASE);

        try {
            if (!encryptDb.exists())
                Log.d("FFC", "encrypted db not exist");
            File temp_encryptDb = new File(FamilyFolderCollector.PATH_ENCRYPTED_DATABASE_TEMP);
            if (temp_encryptDb.exists())
                temp_encryptDb.delete();
            temp_encryptDb.createNewFile();

            mCrypto = new Cryptographer(Cryptographer.getKey(),
                    Cryptographer.ALGORITHM_FFC);boolean success = mCrypto.encrypt(plainDb, temp_encryptDb);
            mCrypto.close();
            mCrypto = null;
            Log.d("EN", "encrypt=" + success);
            if (success) {
                if ((temp_encryptDb.length() - encryptDb.length() * FILE_SIZE_MARGIN) < 0) {
                    Log.d("EN", "Invalid encrypt file size");
                    //new encrypted db's size less than older one
                    temp_encryptDb.delete();
                    return false;
                }

                if (encryptDb.exists())
                    encryptDb.delete();
                success &= temp_encryptDb.renameTo(encryptDb);
                Log.d("EN", "rename=" + success);

                if (success)
                    success &= plainDb.delete();
                Log.d("EN", "delete plain=" + success);
                return success;

            } else {
                temp_encryptDb.delete();
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FFC", e.toString());
            return false;
        }

    }

}
