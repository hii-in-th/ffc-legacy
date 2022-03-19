package th.in.ffc.person.visit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import th.in.ffc.R;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.provider.PersonProvider.PersonColumns;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.provider.PersonProvider.VisitDrugDental;
import th.in.ffc.provider.PersonProvider.VisitDrugDentalDiag;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.ImageMap;
import th.in.ffc.widget.ImageMap.OnImageMapClickedHandler;

import java.util.ArrayList;

public class VisitDrugDentalActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    ArrayList<String> addedTooth = new ArrayList<String>();
    String mVISITNO;
    String mPCUCODE;
    String dentcode;
    String toothtype;
    ImageMap map;
    ScrollView scroller;

    public static final String[] PROJECTION = new String[]{
            VisitDrugDental.TOOTHAREA, VisitDrugDental.SURFACE, VisitDrugDental.COMMENT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mVISITNO = getIntent().getStringExtra(VisitDrug.NO);
        mPCUCODE = getIntent().getStringExtra(VisitDrug._PCUCODE);
        dentcode = getIntent().getStringExtra(VisitDrugDental.DENTCODE);
        toothtype = getIntent().getStringExtra(Drug.TOOTHTYPE);


        setContentView(R.layout.visit_drugdental_activity);

        scroller = (ScrollView) findViewById(R.id.Scrollteeth);

        TextView topic = (TextView) findViewById(R.id.topic);
        topic.setText(topic.getText() + (toothtype.equals("1") ? getString(R.string.primaryTooth) : getString(R.string.permanentTooth)) + getString(R.string.forTooth) + getDentString(dentcode));

        map = (ImageMap) findViewById((toothtype.equals("1") ? R.id.map : R.id.map2));
        map.setVisibility(View.VISIBLE);
        map.addOnImageMapClickedHandler(handler);
        setSupportProgressBarIndeterminateVisibility(false);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    OnImageMapClickedHandler handler = new ImageMap.OnImageMapClickedHandler() {

        public void onImageMapClicked(int id) {
            // when the area is tapped, show the name in a
            // text bubble
            map.showBubble(id);
            map.setCurrentTourches(id);
            addFragment(Action.INSERT, map.getCurrentTouches(), null, null, DateTime.getCurrentTime());
        }

        public void onBubbleClicked(int id) {
            // react to info bubble for area being tapped
        }
    };

    private String getDentString(String dentcode) {
        String name = "N/A";
        if (!TextUtils.isEmpty(dentcode)) {
            Cursor c = getContentResolver().query(Drug.CONTENT_URI, new String[]{Drug.CODE, Drug.NAME}, "drugcode = ?", new String[]{dentcode}, Drug.DEFAULT_SORTING);
            if (c.moveToFirst()) {
                return c.getString(1);
            }
        }
        return name;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_activity, menu);
        menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                //addFragment(Action.INSERT, null, null, null,DateTime.getCurrentTime());
                break;
            case R.id.save:
                setSupportProgressBarIndeterminateVisibility(true);
                doSave();
                setSupportProgressBarIndeterminateVisibility(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //DO SAVE HERE
    public void doSave() {

        boolean finishable = true;

        for (String typecode : getDeleteList()) {
            Uri uri = VisitDrugDental.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            cr.delete(uri, "visitno=? AND pcucode=? AND dentcode=? AND tootharea =?", new String[]{mVISITNO, mPCUCODE, dentcode, typecode});

//			System.out.println("deleted diag count=" + count + " +uri="
//					+ uri.toString());

            cr.delete(VisitDrugDentalDiag.CONTENT_URI, "visitno=? AND pcucode=? AND dentcode=? AND tootharea=?", new String[]{mVISITNO, mPCUCODE, dentcode, typecode});
            //System.out.println("deleted diagcode count=" + count3 + " +uri="+ VisitDrugDentalDiag.CONTENT_URI.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            dataFragment f = (dataFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(VisitDrugDental.TOOTHAREA);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_tootharea, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(VisitDrugDental.VISITNO, mVISITNO);
                cv.put(VisitDrugDental.PCUCODE, mPCUCODE);
                cv.put(VisitDrugDental.DENTCODE, dentcode);
                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(VisitDrugDental.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;


                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = VisitDrugDental.CONTENT_URI;
                    et.commit(updateUri, "visitno=? AND tootharea=?", new String[]{mVISITNO, f.key});

                }

            }
        }

        if (finishable) {
            this.finish();
        }

    }

    public boolean isAddedCode(ArrayList<String> codeList, String code) {
        for (String c : codeList) {
            if (code.equals(c))
                return true;
        }
        return false;
    }

    public void addFragment(String Action, String arg1, String arg2,
                            String arg3, String tag) {
        //Check Not Duplication add from ClickMap
        boolean isAdded = false;
        for (String code : addedTooth) {
//			System.out.println("COMPARE "+arg1+":"+code);
            if (arg1.equals(code)) {
                isAdded = true;
                break;
            }
        }

        if (!isAdded) {
            addEditFragment(dataFragment.newInstance(Action, arg1, arg2, arg3, tag), tag);
            addedTooth.add(arg1);
        } else
            Toast.makeText(this, R.string.err_duplicate_tootharea, Toast.LENGTH_SHORT).show();
        //Scroll to Buttom **Must be delay**
        scroller.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                moveScrollDown();
            }
        }, 500);
    }


    private void generateFragment(Cursor c) {

        String arg1 = c.getString(0);
        String arg2 = c.getString(1);
        String arg3 = c.getString(2);

        System.out.println("I've got tooth #" + arg1);
        addedTooth.add(arg1);
        addEditFragment(
                dataFragment.newInstance(Action.EDIT, arg1, arg2, arg3, arg1),
                arg1);

    }


    private void moveScrollDown() {


        scroller.fullScroll(View.FOCUS_DOWN);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitDrugDental.CONTENT_URI,
                PROJECTION, "visitno =? AND pcucode=? AND dentcode=?", new String[]{mVISITNO, mPCUCODE, dentcode},
                VisitDrugDental.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        // TODO Auto-generated method stub
        setSupportProgressBarIndeterminateVisibility(false);

        if (arg1.moveToFirst()) {
            final Cursor c = arg1;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    do {
                        generateFragment(c);
                    } while (c.moveToNext());
                }
            });

        }
//			else {
//			mHandler.post(new Runnable() {
//
//				@Override
//				public void run() {
//					String tag = DateTime.getCurrentDateTime();
//					dataFragment f = dataFragment.newInstance(Action.INSERT,null, null, null, tag);
//					addEditFragment(f, tag);
//
//				}
//			});
//		}
    }

    public Handler mHandler = new Handler();

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    public void StartDrugDentalDiag(Intent intent) {
        intent.putExtra(VisitDrug.NO, mVISITNO);
        intent.putExtra(VisitDrug._PCUCODE, mPCUCODE);
        intent.putExtra(VisitDrugDental.DENTCODE, dentcode);
        startActivity(intent);
    }

    public void deleteInner(String typecode) {

        int count3 = getContentResolver().delete(VisitDrugDentalDiag.CONTENT_URI, "visitno=? AND pcucode=? AND dentcode=? AND tootharea=?", new String[]{mVISITNO, mPCUCODE, dentcode, typecode});

        System.out.println("TYPECODE : " + typecode + " DDD:" + count3);

    }

    public static class dataFragment extends EditFragment {
        TextView typecode;
        ArrayFormatSpinner quality;
        EditText quality2;
        Button goDiag;
        ImageButton mClose;
        String id;
        View view;

        private String arg1;
        private String arg2;
        private String arg3;

        public static dataFragment newInstance(String action, String type,
                                               String arg2, String arg3, String tag) {

            dataFragment f = new dataFragment();

            // Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            Bundle args = new Bundle(f.getBaseArguments(action, tag, type));

            args.putString("arg1", type);
            args.putString("arg2", arg2);
            args.putString("arg3", arg3);

            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.visit_drugdental_fragment,
                    container, false);

            typecode = (TextView) v.findViewById(R.id.type);
            quality = (ArrayFormatSpinner) v.findViewById(R.id.value1);
            quality.setArray(R.array.surface);
            quality2 = (EditText) v.findViewById(R.id.value2);
            goDiag = (Button) v.findViewById(R.id.goDiag);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            arg1 = args.getString("arg1");
            arg2 = args.getString("arg2");
            arg3 = args.getString("arg3");
            doInitializeView();

        }

        @Override
        protected void setAsRemoveButton(View view) {
            if (view != null)
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "remove click");
                        if (getmRemoveListener() != null) {
                            getmRemoveListener().onRemove(action, tag, key);
                            VisitDrugDentalActivity activity = (VisitDrugDentalActivity) getActivity();
                            activity.deleteInner(arg1);
                            activity.addedTooth.remove(typecode.getText());
//								if(res)
//									System.out.println("DELETED : "+arg1 );
                        }


                    }
                });
        }

        private void doInitializeView() {
            typecode.setText(arg1);
            quality.setSelection(arg2);
            quality2.setText(arg3);
            goDiag.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    Intent intent = new Intent(getActivity(), VisitDrugDentalDiagActivity.class);
                    intent.setData(Uri.withAppendedPath(VisitDrugDentalDiag.CONTENT_URI, VisitDrug.NO));
                    intent.putExtra(PersonColumns._PID, "00000");
                    intent.putExtra(PersonColumns._PCUCODEPERSON, "00000");
                    intent.putExtra(VisitDrugDental.TOOTHAREA, typecode.getText());
                    VisitDrugDentalActivity go = (VisitDrugDentalActivity) getActivity();
                    go.StartDrugDentalDiag(intent);

                }
            });


        }

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub

            et.retrieveData(VisitDrugDental.TOOTHAREA, typecode, false, null, null);

            ContentValues cv = et.getContentValues();
            cv.put(VisitDrugDental.TOOTHAREA, typecode.getText().toString());
            cv.put(VisitDrugDental.SURFACE, quality.getSelectionId());
            cv.put(VisitDrugDental.COMMENT, quality2.getText().toString());
            return true;
        }

    }


}
