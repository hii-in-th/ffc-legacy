/*
 * Copyright (C) 2010 Family Folder Collector Project - NECTEC
 * 
 *	FamilyTree Class [version 1.0]
 *  November 09, 2010  
 *  
 *	Create by  Piruin Panichphol [Blaze]
 */

package th.in.ffc.person.genogram.V1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.PersonProvider.PersonColumns;

public class FamilyTree extends FFCFragmentActivity {

    private static final int DIALOG_UNKNOWN_PERSON = 2;
    private static final int DIALOG_CRASH = 3;

    private static final int REQUEST_PERSON_CARD = 543;

    public static final String PREFS_NAME = "FamilyTree";
    public static final String PREFS_FIRST = "first";
    public static final String PREFS_FOCUS = "focus";
    public static final String PREFS_PERSON = "person";
    public static final int ACTIVITY_RELATION_SETTING = 1;
    public static final int ACTIVITY_HEALTHT_RECORD = 2;
    public static final int ACTIVITY_PERSONAL_CONFIG = 3;
    public static final int ACTIVITY_HOUSE_SERVEY = 4;
    public static final String ACTION_CAMERA = "th.or.nectec.ff.camera";
    private static final int TIMEOUT = 180;
    private static final int SHUTDOWN_IN = 5;

    private int mHCode; // current focus House
    private String mPCUCode;
    private int mCountFamily;
    private int mFocusFamily = 1;
    private boolean[] mFoundMember;
    private Person mFocusPerson;
    public static Map<Integer, Family> mFamilys;
    private int mStartX;
    private int mCurrentX;
    private static float TOUCH_THRESHOLD;
    private ProgressDialog mProgress;
    private AbsoluteLayout mDrawingArea;
    private int mDrawingAreaWidth;
    private Button btnUnknown;
    private LinearLayout lnlFocusFamily;
    ImageView camShot;

    private Thread mThread = new Thread() {
        int count = 1;

        public void run() {

            FamilyFolderCollector ffc = (FamilyFolderCollector) getApplicationContext();
            while (count <= mCountFamily) {
                try {
                    if (mFoundMember[count] != true) {

                        long start = System.currentTimeMillis();
                        findFamilyMember(count);
                        long time = System.currentTimeMillis()
                                - start;
                        Log.d("FFC", "Find Famiily " + time);
                        if (count == mFocusFamily) {
                            mHandler.post(mRunable);
                        }
                        ffc.mFamilys = mFamilys;
                        ffc.mFoundMember = mFoundMember;
                    }
                    count++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mHandler.post(mCrashChecker);
                    // mProgress.dismiss();
                    break;
                }
            }
        }
    };

    private Handler mHandler = new Handler();
    private Runnable mRunable = new Runnable() {

        public void run() {

            long start = System.currentTimeMillis();
            showFamilyTree();
            long time = System.currentTimeMillis() - start;
            Log.d("FFC", "Show Family Tree in " + time);
            mProgress.dismiss();
        }
    };
    // private long startTime;
    private Runnable mCrashChecker = new Runnable() {
        @Override
        public void run() {

            if (mProgress.isShowing()) {
                mProgress.dismiss();

                stopThread();

                showDialog(DIALOG_CRASH);
                mHandler.postDelayed(mShutDown, SHUTDOWN_IN * 1000);
            }
        }
    };

    public synchronized void stopThread() {
        if (mThread != null) {
            Thread moribund = mThread;

            mThread = null;
            moribund.interrupt();
        }
    }

    private Runnable mShutDown = new Runnable() {
        @Override
        public void run() {
            removeDialog(DIALOG_CRASH);
            finish();
        }
    };

    private OnClickListener personViewOnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PersonView pv = (PersonView) v;
            mFocusPerson = pv.getPerson();

            Intent intent = new Intent(Action.VIEW);
            intent.setData(ContentUris.withAppendedId(
                    th.in.ffc.provider.PersonProvider.Person.CONTENT_URI,
                    mFocusPerson.getPid()));
            intent.putExtra(PersonColumns._PID, mFocusPerson.getPid());
            intent.putExtra(PersonColumns._PCUCODEPERSON,
                    mFocusPerson.getPCUCode());

            startActivity(intent);
            //
            // startActivityForResult(intent, REQUEST_PERSON_CARD);
            // FamilyTree.this.removeDialog(DIALOG_PERSONAL_CARD);
            // showDialog(DIALOG_PERSONAL_CARD);

        }
    };

//	private OnTouchListener drawingAreaOnTouch = new OnTouchListener() {
//
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			// Toast.makeText(getApplicationContext(), "Touch",
//			// Toast.LENGTH_SHORT).show();
//
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN: {
//				mCurrentX = mStartX = (int) event.getRawX();
//				// Toast.makeText(getApplicationContext(),
//				// "current X = "+mCurrentX , Toast.LENGTH_SHORT).show();
//				break;
//			}
//			case MotionEvent.ACTION_MOVE: {
//
//				int x = (int) event.getRawX();
//				mDrawingArea.scrollBy(mCurrentX - x, 0);
//				mCurrentX = x;
//				break;
//			}
//			case MotionEvent.ACTION_UP: {
//				// mDrawingArea.scrollTo(0, 0);
//				int dif;
//				int x = (int) event.getRawX();
//				// mDrawingArea.scrollBy(mCurrentX - x, 0);
//				mCurrentX = x;
//				// Toast.makeText(getApplicationContext(), "move!!!",
//				// Toast.LENGTH_SHORT).show();
//				if (x < mStartX) {
//					dif = mStartX - x;
//					if (dif > 200 * TOUCH_THRESHOLD) {
//						ImageView img = (ImageView) lnlFocusFamily
//								.getChildAt(mFocusFamily - 1);
//
//						if (nextFocusFamily()) {
//							if (mFoundMember[mFocusFamily]) {
//								ImageView img2 = (ImageView) lnlFocusFamily
//										.getChildAt(mFocusFamily - 1);
//								img.setImageResource(R.drawable.family_normal);
//								img2.setImageResource(R.drawable.family_focused);
//								mDrawingArea.scrollTo(-mDrawingAreaWidth, 0);
//								mDrawingArea.removeAllViews();
//								mDrawingArea.scrollTo(mDrawingAreaWidth, 0);
//								showFamilyTree();
//								mDrawingArea.scrollTo(0, 0);
//							} else {
//								previousFocusFamily();
//								mDrawingArea.scrollTo(0, 0);
//							}
//						}
//
//					} else
//						mDrawingArea.scrollTo(0, 0);
//				} else {
//					dif = x - mStartX;
//					if (dif > 200 * TOUCH_THRESHOLD) {
//						ImageView img = (ImageView) lnlFocusFamily
//								.getChildAt(mFocusFamily - 1);
//						if (previousFocusFamily()) {
//							if (mFoundMember[mFocusFamily]) {
//								img.setImageResource(R.drawable.family_normal);
//								ImageView img2 = (ImageView) lnlFocusFamily
//										.getChildAt(mFocusFamily - 1);
//								img2.setImageResource(R.drawable.family_focused);
//								mDrawingArea.scrollTo(mDrawingAreaWidth, 0);
//								mDrawingArea.removeAllViews();
//								mDrawingArea.scrollTo(-mDrawingAreaWidth, 0);
//								showFamilyTree();
//								mDrawingArea.scrollTo(0, 0);
//							} else {
//								nextFocusFamily();
//								mDrawingArea.scrollTo(0, 0);
//							}
//						}
//					} else
//						mDrawingArea.scrollTo(0, 0);
//				}
//
//			}
//			}
//			return true;
//		}
//	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.person_genogram_v1);

        TOUCH_THRESHOLD = getApplicationContext().getResources()
                .getDisplayMetrics().density;

        mFocusFamily = 1;

        mHCode = getIntent().getIntExtra(House.HCODE, -1); // 1155;
        if (mHCode != -1) {
            PersonView.HCode = mHCode;
            mPCUCode = getIntent().getStringExtra("pcucode");

            Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentType("Genogram")
                .putContentId(String.format("%s/%s", mPCUCode, mHCode)));

            Uri uri = Uri.withAppendedPath(
                    th.in.ffc.provider.PersonProvider.Person.CONTENT_URI,
                    "house/" + mHCode + "/family");
            Cursor c = getContentResolver().query(uri,
                    new String[]{"hcode"}, null, null, "hcode");
            if (c.moveToFirst()) {
                Log.d(TAG, "family count=" + c.getCount());
                mCountFamily = c.getCount();
            } else
                mCountFamily = 0;
            initialize();


            if (mCountFamily > 0) {
                mFamilys = new HashMap<Integer, Family>();
                mFoundMember = new boolean[mCountFamily + 1];
                for (int i = 1; i <= mCountFamily; i++) {
                    if (mPCUCode == null) {
                        mFamilys.put(i, new Family(this, mHCode, i));
                    } else {

                        mFamilys.put(i, new Family(this, mHCode, mPCUCode, i));
                    }
                    mFoundMember[i] = false;
                }
                mProgress = ProgressDialog.show(this,
                        getString(R.string.app_name),
                        getString(R.string.please_wait));
                mThread.setPriority(Thread.MAX_PRIORITY);
                mThread.start();

                mHandler.postDelayed(mCrashChecker, TIMEOUT * 1000);
            } else {
                mHandler.postDelayed(mShutDown, SHUTDOWN_IN * 300);

            }
            // } else {
            //
            // FamilyFolderCollector ffc = (FamilyFolderCollector)
            // getApplicationContext();
            //
            // this.mFoundMember = ffc.mFoundMember;
            // FamilyTree.mFamilys = ffc.mFamilys;
            // mFocusFamily = prefsFamilyTree.getInt(PREFS_FOCUS, 1);
            // Family onFocusFamily = mFamilys.get(mFocusFamily);
            // int pid = prefsFamilyTree.getInt(PREFS_PERSON, 1);
            // mFocusPerson = onFocusFamily.getPersonByPid(pid);
            //
            // showFamilyTree();
            // }
        } else {
            finish();
        }

    }

    @Override
    protected void onStart() {

        super.onStart();

        SharedPreferences pref = getSharedPreferences("relation", 0);
        boolean refresh = pref.getBoolean("refresh", false);

        if (refresh) {
            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean("refresh", false);
            edit.commit();
            refreshPage();

        }
    }

    private void refreshPage() {
        startFamilyTreeActivity(mHCode, mPCUCode);
        finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_UNKNOWN_PERSON: {
                Family onFocusFamily = mFamilys.get(mFocusFamily);
                int count = onFocusFamily.countGeneration(0);
                final List<String> name = new ArrayList<String>();
                final Person[] unknown = new Person[count];
                int i = 0;
                for (Person u : onFocusFamily.getUnknownPerson()) {
                    name.add(u.getFullName(FamilyTree.this));
                    unknown[i++] = u;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setItems(name.toArray(new String[name.size()]),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                mFocusPerson = unknown[item];

                                dialog.dismiss();
                                Intent intent = new Intent(Action.VIEW);
                                intent.setData(ContentUris
                                        .withAppendedId(
                                                th.in.ffc.provider.PersonProvider.Person.CONTENT_URI,
                                                mFocusPerson.getPid()));
                                intent.putExtra(PersonColumns._PID,
                                        mFocusPerson.getPid());
                                intent.putExtra(PersonColumns._PCUCODEPERSON,
                                        mFocusPerson.getPCUCode());

                                startActivity(intent);

                            }
                        });
                builder.setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        });
                return builder.create();
            }
            case DIALOG_CRASH: {
                ProgressDialog Progress = ProgressDialog.show(this,
                        getString(R.string.app_name), "error...");
                return Progress;

            }
            default: {
                return null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mFamilys.size() > 1) {
            MenuItem prev = menu.add(0, 1, 1, "prev");
            MenuItem next = menu.add(0, 2, 2, "next");
            prev.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            next.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ImageView img;
        switch (item.getItemId()) {
            case 1:
                img = (ImageView) lnlFocusFamily.getChildAt(mFocusFamily - 1);

                if (nextFocusFamily()) {
                    if (mFoundMember[mFocusFamily]) {
                        ImageView img2 = (ImageView) lnlFocusFamily
                                .getChildAt(mFocusFamily - 1);
                        img.setImageResource(R.drawable.family_normal);
                        img2.setImageResource(R.drawable.family_focused);
                        mDrawingArea.scrollTo(-mDrawingAreaWidth, 0);
                        mDrawingArea.removeAllViews();
                        mDrawingArea.scrollTo(mDrawingAreaWidth, 0);
                        showFamilyTree();
                        mDrawingArea.scrollTo(0, 0);
                    } else {
                        previousFocusFamily();
                        mDrawingArea.scrollTo(0, 0);
                    }
                }
                break;
            case 2:
                img = (ImageView) lnlFocusFamily.getChildAt(mFocusFamily - 1);
                if (previousFocusFamily()) {
                    if (mFoundMember[mFocusFamily]) {
                        img.setImageResource(R.drawable.family_normal);
                        ImageView img2 = (ImageView) lnlFocusFamily
                                .getChildAt(mFocusFamily - 1);
                        img2.setImageResource(R.drawable.family_focused);
                        mDrawingArea.scrollTo(mDrawingAreaWidth, 0);
                        mDrawingArea.removeAllViews();
                        mDrawingArea.scrollTo(-mDrawingAreaWidth, 0);
                        showFamilyTree();
                        mDrawingArea.scrollTo(0, 0);
                    } else {
                        nextFocusFamily();
                        mDrawingArea.scrollTo(0, 0);
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findFamilyMember(int familyNo) {
        Family family = mFamilys.get(familyNo);
        if (family != null) {
            try {
                family.findFamilyMember();
                mFoundMember[familyNo] = true;
            } catch (Exception ex) {

                Log.d("FamilyTree", "Error occur while findFamilyMeber no."
                        + familyNo);
                ex.printStackTrace();
                mFoundMember[familyNo] = false;
            }

        }
    }

    private void initialize() {
        mDrawingArea = (AbsoluteLayout) findViewById(R.id.DrawingArea);

        if (mCountFamily > 1) {
            //mDrawingArea.setOnTouchListener(drawingAreaOnTouch);
            lnlFocusFamily = (LinearLayout) findViewById(R.id.lnlFocusFamily);
            for (int i = 1; i <= mCountFamily; i++) {
                ImageView imgFocus = new ImageView(this);
                if (i == mFocusFamily)
                    imgFocus.setImageResource(R.drawable.family_focused);
                else
                    imgFocus.setImageResource(R.drawable.family_normal);
                lnlFocusFamily.addView(imgFocus, new LinearLayout.LayoutParams(
                        30, 30));
            }
            mDrawingAreaWidth = getApplication().getResources()
                    .getDisplayMetrics().widthPixels;

        }

        btnUnknown = (Button) findViewById(R.id.btnUnknown);
        btnUnknown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDialog(DIALOG_UNKNOWN_PERSON);
                showDialog(DIALOG_UNKNOWN_PERSON);
            }
        });

        // Cursor cursor = JhcisDatabaseAdapter
        // .rawQuery("select house.hno,village.villname from house, village where house.villcode = village.villcode AND hcode = "
        // + mHCode);
        // if (cursor.moveToFirst()) {
        // String hno;
        // hno = cursor.getString(0);
        // if (hno != null && hno != "null" && hno.length() > 0) {
        // txtHouse.setText("บ้านเลขที่ " + hno +
        // " หมู่บ้าน "
        // + cursor.getString(1));
        // } else {
        // txtHouse.setText("บ้านหลังหนึ่งใน หมู่บ้าน "
        // + cursor.getString(1));
        // }
        //
        // } else {
        // txtHouse.setVisibility(View.GONE);
        // }

    }

    private boolean nextFocusFamily() {
        int currentFocus = mFocusFamily;
        if (mFamilys.size() > 1) {
            mFocusFamily++;
            if (mFocusFamily > mFamilys.size())
                mFocusFamily = 1;
            if (mFocusFamily != currentFocus)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    private boolean previousFocusFamily() {
        int currentFocus = mFocusFamily;
        if (mFamilys.size() > 1) {
            mFocusFamily--;
            if (mFocusFamily == 0)
                mFocusFamily = mFamilys.size();
            if (mFocusFamily != currentFocus)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    private void showFamilyTree() {
        if (!mFoundMember[mFocusFamily]) {
            findFamilyMember(mFocusFamily);
        }
        Family onFocusFamily = mFamilys.get(mFocusFamily);
        Log.d("Family", mFamilys.get(1).toString());
        if (onFocusFamily != null) {
            mDrawingArea.removeAllViews();
            int width = getApplication().getResources().getDisplayMetrics().widthPixels;
            int height = getApplication().getResources().getDisplayMetrics().heightPixels;
            FamilyTreePainter painter = new FamilyTreePainter(this,
                    onFocusFamily, mDrawingArea, width, height);
            painter.startPainting();
            painter.setOnClickListenerToPersonView(personViewOnClick);
            if (onFocusFamily.getUnknownPerson().size() > 0) {
                btnUnknown.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        SharedPreferences prefsFamilyTree = getSharedPreferences(
                FamilyTree.PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefsFamilyTree.edit();
        editor.putInt(FamilyTree.PREFS_FOCUS, mFocusFamily);
        try {
            editor.putInt(FamilyTree.PREFS_PERSON, mFocusPerson.getPid());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        editor.commit();
        // mFamilys = null;
        // mFocusPerson = null;
    }

    protected void startFamilyTreeActivity(int hcode, String pcuCode) {
        // TODO Auto-generated method stub

        Intent familyTreeActivity = new Intent(FamilyTree.this,
                FamilyTree.class);
        familyTreeActivity.putExtra("hcode", hcode);
        familyTreeActivity.putExtra("pcucode", pcuCode);
        SharedPreferences house = getSharedPreferences("house", 0);
        SharedPreferences.Editor editor = house.edit();
        editor.putInt("hcode", hcode);
        editor.putString("pcucode", pcuCode);
        editor.commit();

        ClearData();
        startActivity(familyTreeActivity);

    }

    private void ClearData() {
        SharedPreferences prefsFamilyTree = getSharedPreferences(
                FamilyTree.PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefsFamilyTree.edit();
        edit.putBoolean(FamilyTree.PREFS_FIRST, true);
        edit.putInt(FamilyTree.PREFS_FOCUS, 1);
        edit.commit();

    }

    public Bitmap getimage(String path, ImageView iv) {
        // iv is passed to set it null to remove it from external memory
        iv = null;
        InputStream stream;
        try {
            stream = new FileInputStream("/sdcard/mydata/" + path);
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, null);
            stream.close();
            stream = null;
            return bitmap;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

}
