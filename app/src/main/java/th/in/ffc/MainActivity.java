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

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.GridView;
import android.widget.TextView;
import th.in.ffc.app.FFCGridActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.security.CryptographerService;
import th.in.ffc.util.AssetReader;
import th.in.ffc.widget.IntentBaseAdapter;

/**
 * add description here! please
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class MainActivity extends FFCGridActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle(R.string.app_version);

        boolean quit = getIntent().getBooleanExtra("quit", false);
        if (quit) {
            this.finish();
        }

        Intent intent = new Intent(Action.MAIN);
        intent.addCategory(Category.TAB);

        IntentBaseAdapter adapter = new IntentBaseAdapter(this, intent,
                R.layout.grid_item, R.id.image, R.id.text);

        super.setGridAdapter(adapter);
        GridView grid = super.getGridView();

        grid.setOnItemClickListener(adapter.getOnItemClickListener());

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("receiver")) {
                Log.d(TAG, "regis");
                registerReceiver(mEncrypterReceiver, mEncryptFilter);
                mRegis = true;

                p = new ProgressDialog(MainActivity.this);
                p.setMessage(getString(R.string.please_wait));
                p.setCancelable(false);
                p.show();
            }
        } else {
            super.doCheckDateSetting();
        }
    }


    private boolean mRegis = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("receiver", mRegis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem whatnew = menu.add(Menu.NONE, R.layout.whatnew_dialog,
                Menu.NONE, "what new");
        whatnew.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        SharedPreferences sp = getSharedPreferences(FamilyFolderCollector.TAG, MODE_PRIVATE);
        int prev = sp.getInt(FamilyFolderCollector.PREF_VERSION, 0);
        int now = getResources().getInteger(R.integer.version_code);
        whatnew.setIcon((now > prev) ? R.drawable.ic_action_star : R.drawable.ic_action_start_dark);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.layout.whatnew_dialog:
                String tag = "whatnew";
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment prev = fm.findFragmentByTag(tag);
                WhatnewDialogFragment f;
                if (prev != null) {
                    f = (WhatnewDialogFragment) prev;
                    ft.remove(f);
                } else {
                    f = (WhatnewDialogFragment) Fragment.instantiate(this,
                            WhatnewDialogFragment.class.getName(), null);
                }
                ft.addToBackStack(null);
                f.show(fm, tag);

                SharedPreferences.Editor se = getSharedPreferences(FamilyFolderCollector.TAG, MODE_PRIVATE).edit();
                se.putInt(FamilyFolderCollector.PREF_VERSION, getResources().getInteger(R.integer.version_code));
                se.commit();
                item.setIcon(R.drawable.ic_action_start_dark);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private EncrypterServiceRevicer mEncrypterReceiver = new EncrypterServiceRevicer();
    private IntentFilter mEncryptFilter = new IntentFilter(Action.ENCRYPT);
    ProgressDialog p;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegis) {
            p.dismiss();
            p = null;
            unregisterReceiver(mEncrypterReceiver);
        }
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("exit?");
        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        p = new ProgressDialog(MainActivity.this);
                        p.setMessage(getString(R.string.please_wait));
                        p.setCancelable(false);
                        p.show();

                        registerReceiver(mEncrypterReceiver, mEncryptFilter);
                        mRegis = true;

                        Intent service = new Intent(MainActivity.this, CryptographerService.class);
                        service.setAction(Action.ENCRYPT);
                        startService(service);
                    }
                });

        builder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private class EncrypterServiceRevicer extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (p != null)
                p.dismiss();

            MainActivity.super.onBackPressed();

            Process.killProcess(Process.myPid());
        }
    }

    public static class WhatnewDialogFragment extends DialogFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setStyle(DialogFragment.STYLE_NORMAL,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        }

        TextView text;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            getDialog().setTitle("What's New!");
            View view = inflater.inflate(R.layout.whatnew_dialog, container,
                    false);
            text = (TextView) view.findViewById(R.id.content);

            return view;
        }

        @Override
        public void onActivityCreated(Bundle arg0) {
            super.onActivityCreated(arg0);

            String txt = AssetReader.read(getActivity(), "whatnew.txt");
            text.setMovementMethod(new ScrollingMovementMethod());
            text.setText(txt);
        }

    }

}
