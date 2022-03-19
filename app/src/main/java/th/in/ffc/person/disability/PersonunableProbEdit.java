package th.in.ffc.person.disability;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.intent.Action;
import th.in.ffc.person.visit.VisitActivity;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.PersonProvider.PersonunableProblem;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

public class PersonunableProbEdit extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    String mPID;
    String mPCUCODEPERSON;

    public static final String[] PROJECTION = new String[]{
            PersonunableProblem.PROBLEMCODE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mPID = getIntent().getStringExtra(PersonunableProblem.PID);
        mPCUCODEPERSON = getIntent().getStringExtra(House.PCUCODE);

        System.out.println("HELLO I'M FROM " + mPID + ":" + mPCUCODEPERSON);
        setContentView(R.layout.personunable_edit_activity);
        TextView topic = (TextView) findViewById(R.id.topic);
        topic.setText(getString(R.string.pu_problem));
        setSupportProgressBarIndeterminateVisibility(false);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addFragment(Action.INSERT, null, null, null, DateTime.getCurrentTime());
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
            Uri uri = PersonunableProblem.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, "pid=? AND pcucodeperson=? AND unableproblemcode =?", new String[]{mPID, mPCUCODEPERSON, typecode});

            System.out.println("deleted diag count=" + count + " +uri="
                    + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            unableFragment f = (unableFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(PersonunableProblem.PROBLEMCODE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_problem, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(PersonunableProblem.PID, mPID);
                cv.put(PersonunableProblem.PCUCODEPERSON, mPCUCODEPERSON);
                cv.put(PersonunableProblem.DATEUPDATE, DateTime.getCurrentDateTime());

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(PersonunableProblem.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;

                    System.out.println("I'M HERE");
                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = PersonunableProblem.CONTENT_URI;
                    et.commit(updateUri, "pid=? AND unableproblemcode=?", new String[]{mPID, f.key});

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

        addEditFragment(unableFragment.newInstance(Action, arg1, arg2, arg3, tag), tag);
    }

    private void generateanimaltype(Cursor c) {

        String arg1 = c.getString(0);


        System.out.println("I've got this grow arg1 = " + arg1);
        addEditFragment(
                unableFragment.newInstance(Action.EDIT, arg1, null, null, arg1),
                arg1);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, PersonunableProblem.CONTENT_URI,
                PROJECTION, "pid =? AND pcucodeperson=?", new String[]{mPID, mPCUCODEPERSON},
                PersonunableProblem.DEFAULT_SORTING);
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
                        System.out.println("I'm gening");
                        generateanimaltype(c);
                    } while (c.moveToNext());
                }
            });

        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    unableFragment f = unableFragment.newInstance(Action.INSERT, null, null, null, tag);
                    addEditFragment(f, tag);

                }
            });
        }
    }

    public Handler mHandler = new Handler();

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    public static class unableFragment extends EditFragment {
        SearchableButton typecode;
        ImageButton mClose;

        private String arg1;
//		private String arg2;
//		private String arg3;

        public static unableFragment newInstance(String action, String type,
                                                 String arg2, String arg3, String tag) {

            unableFragment f = new unableFragment();

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
            View v = inflater.inflate(R.layout.pesonunable_problem_fragment,
                    container, false);

            typecode = (SearchableButton) v.findViewById(R.id.type);
            typecode.setDialog(getFragmentManager(), ProblemDialogList.class, "Problem?");
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            arg1 = args.getString("arg1");
//			arg2 = args.getString("arg2");
//			arg3 = args.getString("arg3");
            doInitializeView();

        }

        private void doInitializeView() {
            if (arg1 != null)
                typecode.setSelectionById(arg1);


        }

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(PersonunableProblem.PROBLEMCODE, typecode, false, null, null);

            ContentValues cv = et.getContentValues();
            cv.put(PersonunableProblem.PROBLEMCODE, typecode.getSelectId());
            return true;
        }

    }
}
