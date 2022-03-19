package th.in.ffc.person.visit;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCActionBarTabsPagerActivity;
import th.in.ffc.app.form.ViewFormFragment;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.person.visit.VisitSugarBloodFragment.onShowListener;
import th.in.ffc.provider.CodeProvider.Clinic;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.PersonColumns;
import th.in.ffc.provider.PersonProvider.Visit;
import th.in.ffc.provider.PersonProvider.VisitColumns;
import th.in.ffc.provider.PersonProvider.VisitDiag;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.util.DateTime;

public class VisitViewActivity extends FFCActionBarTabsPagerActivity {

    String mPid;
    String mPcuCodePerson;
    String mFullName;
    String mCitizenID;
    String visitNo;
    ArrayList<String> datePager;
    int currentpage;
    boolean haveGraph;
    boolean haveGraphSugar;
    boolean graphShow;
    LinearLayout graphWeight;
    LinearLayout graphSugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextGraph = getApplicationContext();
        datePager = new ArrayList<String>();
        currentpage = 0;
        haveGraph = false;
        haveGraphSugar = false;
        graphShow = true;
        comeFirst = true;
        graphWeight = (LinearLayout) findViewById(R.id.weight_pulse);
        graphSugar = (LinearLayout) findViewById(R.id.sugar_graph1);
        Intent intent = getIntent();
        mPid = intent.getData().getLastPathSegment();
        mPcuCodePerson = intent.getStringExtra(
                PersonColumns._PCUCODEPERSON);
        mFullName = intent.getStringExtra(Person.FULL_NAME);
        mCitizenID = intent.getStringExtra(Person.CITIZEN_ID);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(mFullName);
        ab.setSubtitle(mCitizenID);
        ab.setHomeButtonEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
        Log.d(TAG, "pcucodeperson=" + mPcuCodePerson);

        ActionBarTabPagersAdapter adapter = new ActionBarTabPagersAdapter(
                this, getSupportFragmentManager(), getSupportActionBar(),
                getViewPager());

        Uri visitUri = Visit.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(visitUri, new String[]{Visit.NO, Visit.PCUCODE, Visit.DATE},
                "pid=? AND pcucodeperson=?", new String[]{mPid,
                        mPcuCodePerson}, "visitno ASC");

        if (c.moveToFirst()) {
            int counts = 1;
            do {
                visitNo = c.getString(0);
                String pcucode = c.getString(1);
                String visitDate = c.getString(2);

                Bundle args = new Bundle();
                args.putString(VisitColumns._PCUCODE, pcucode);
                args.putString(VisitColumns._VISITNO, visitNo);
                args.putString(Visit.DATE, visitDate);

                String date = c.getString(1);
                String title = "";
                title = counts++ + "";
                if (!TextUtils.isEmpty(date)) {
                    title = title + " - " + DateTime.getFullFormatThai(this, c.getString(c.getColumnIndex(Visit.DATE)));
                }
                adapter.addTab(title, VisitViewFragment.class, args);
            } while (c.moveToNext());
            setTabPagerAdapter(adapter, adapter.getCount());
            setShowPagerTitleStrip(true);
            adapter.setTabVisible(false);
        } else {
            adapter.addTab("Visit", EmptyVisitFragment.class, null);
            setTabPagerAdapter(adapter);
            adapter.setTabVisible(false);
        }
        createSugarGraph();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem switchGraph = menu.add(Menu.NONE, 4, Menu.NONE, "switch_graph");
        switchGraph.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        switchGraph.setIcon(R.drawable.ic_action_switch_graph);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (haveGraph) {
            if (graphShow) {
                if (haveGraphSugar) {
                    graphSugar.setVisibility(LinearLayout.GONE);
                }
                graphWeight.setVisibility(LinearLayout.GONE);
                graphShow = false;
            } else {
                if (haveGraphSugar) {
                    graphSugar.setVisibility(LinearLayout.VISIBLE);
                }
                graphWeight.setVisibility(LinearLayout.VISIBLE);
                graphShow = true;
            }
        } else {
            Toast.makeText(this, "��辺�����š���������֧�������ö�ʴ���ҿ��", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    boolean comeFirst;
    int lastPage;

    @Override
    public void setCurrentPage(int currentpage) {
        this.currentpage = currentpage;
        if (comeFirst) {
            lastPage = currentpage;
            comeFirst = false;
            //createGraph();
        } else {
            if (currentpage == lastPage || currentpage == 0) {
                createGraph();
            }
        }

    }

    Fragment fragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Context contextGraph;

    public void createDummyGraph() {
        LinearLayout linearGraph = (LinearLayout) findViewById(R.id.sugar_graph1);
        linearGraph.setVisibility(LinearLayout.GONE);
        LinearLayout linearGraph1 = (LinearLayout) findViewById(R.id.weight_pulse);
        linearGraph1.setVisibility(LinearLayout.GONE);
    /*	fragmentManager = getSupportFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();
		fragment = new VisitGraphFragment(contextGraph);
		fragmentTransaction.replace(R.id.weight_pulse, fragment);
		fragmentTransaction.commit();*/
        haveGraph = false;
    }

    public void createGraph() {
        haveGraph = true;
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new VisitGraphFragment();
        Bundle bundle = new Bundle();
        bundle.putString("pid", mPid);
        bundle.putString("visitdate", datePager.get(lastPage - currentpage));
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.weight_pulse, fragment);
        fragmentTransaction.commit();

    }

    public void createSugarGraph() {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        VisitSugarBloodFragment visitSugarBloodFragment = new VisitSugarBloodFragment();
        visitSugarBloodFragment.setOnShowListener(showLayoutListener);
        this.fragment = visitSugarBloodFragment;
        Bundle bundle = new Bundle();
        bundle.putString("pid", mPid);
        this.fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.sugar_graph1, this.fragment);
        fragmentTransaction.commit();
    }

    onShowListener showLayoutListener = new onShowListener() {

        @Override
        public void onShowLayoutListener(boolean show) {
            if (!show) {
                LinearLayout linearGraph = (LinearLayout) findViewById(R.id.sugar_graph1);
                linearGraph.setVisibility(LinearLayout.GONE);
            } else {
                LinearLayout linearGraph = (LinearLayout) findViewById(R.id.sugar_graph1);
                linearGraph.setVisibility(LinearLayout.VISIBLE);
                haveGraphSugar = true;
            }

        }
    };

    @Override
    public void onTabChangeGetItem(Fragment getItem, int position) {
        int index = lastPage - position;
        Bundle a = getItem.getArguments();
        if (a != null) {
            if (datePager.isEmpty()) {
                datePager.add(a.getString("visitdate"));
            } else if (datePager.size() < lastPage + 1) {
                if (index == datePager.size()) {
                    datePager.add(a.getString("visitdate"));
                }
            }
        }
        if (!datePager.isEmpty()) {
            createGraph();
        } else {
            createDummyGraph();
        }
    }


    public static class VisitViewFragment extends ViewFormFragment {

        public static final String[] DEFAULT_PROJ = new String[]{Visit._ID,
                Visit.DATE, Visit.HEIGHT, Visit.WEIGHT, Visit.BMI, Visit.WAIST,
                Visit.ASS, Visit.PULSE, Visit.PRESSURE, Visit.PRESSURE_2, Visit.TEMPERATURE,
                Visit.SYMPTOMS, Visit.VITAL, Visit.HEALTH_SUGGEST_1,
                Visit.HEALTH_SUGGEST_2,};

        public static final String[] DIAG_PROJ = new String[]{
                VisitDiag.CLINIC, VisitDiag.TYPE, VisitDiag.CODE,
                VisitDiag.CONTINUE, VisitDiag.APPOINT_DATE,
                VisitDiag.APPOINT_TYPE,};

        public static final String[] DRUG_PROJ = new String[]{VisitDrug.CODE,
                VisitDrug.UNIT,};

        public static final String SELECTION = "visitno=? AND pcucode=? ";
        public static final String SORT = "visitno";
        public String[] mSelectionArgs;

        String mVisitNo;
        String mPcucode;
        String mVisitDate;

        public static final int LOAD_DEFAULT = 1;
        public static final int LOAD_DIAG = 2;
        public static final int LOAD_DRUG = 3;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();
            if (args == null)
                throw new IllegalArgumentException(
                        "Visit view must have arguments");

            mVisitNo = args.getString(VisitColumns._VISITNO);
            mPcucode = args.getString(VisitColumns._PCUCODE);
            mVisitDate = args.getString(Visit.DATE);

            mSelectionArgs = new String[]{mVisitNo, mPcucode,};

            String current = DateTime.getCurrentDate();
            if (current.equals(mVisitDate)) {
                setHasOptionsMenu(true);
            }
            String name = mVisitNo.concat(".jpg");
            File pic = new File(FamilyFolderCollector.PHOTO_DIRECTORY_SERVICE, name);
            if (pic.exists()) {
                ImageView img = new ImageView(getActivity());
                img.setImageDrawable(Drawable.createFromPath(pic.getAbsolutePath()));
                LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                param.gravity = Gravity.CENTER_HORIZONTAL;
                img.setLayoutParams(param);
                mForm.addView(img);
            }
            mLoader.run();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.editable_fragment, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.edit:
                    VisitViewActivity v = (VisitViewActivity) getActivity();
                    Intent visit = new Intent(v, VisitMainActivity.class);
                    visit.setAction(Action.MAIN);
                    visit.addCategory(Category.VISIT);
                    visit.putExtra(Visit.NO, mVisitNo);
                    visit.putExtra(Person.FULL_NAME, v.mFullName);
                    visit.putExtra(Person.CITIZEN_ID, v.mCitizenID);
                    visit.setData(Uri.withAppendedPath(Person.CONTENT_URI, v.mPid));
                    startActivity(visit);
                    v.finish();
                    return true;

            }
            return super.onOptionsItemSelected(item);
        }

        ;

        Runnable mLoader = new Runnable() {

            @Override
            public void run() {
                VisitViewFragment v = VisitViewFragment.this;
                ContentResolver cr = getActivity().getContentResolver();
                Cursor c = cr.query(Visit.CONTENT_URI, DEFAULT_PROJ, SELECTION, mSelectionArgs, Visit.DEFAULT_SORTING);
                mHandler.post(new Displayer(v, LOAD_DEFAULT, c));
                c = cr.query(VisitDiag.CONTENT_URI, DIAG_PROJ, SELECTION,
                        mSelectionArgs, VisitDiag.DEFAULT_SORTING);
                mHandler.post(new Displayer(v, LOAD_DIAG, c));

                c = cr.query(VisitDrug.CONTENT_URI, DRUG_PROJ, SELECTION,
                        mSelectionArgs, VisitDrug.DEFAULT_SORTING);
                mHandler.post(new Displayer(v, LOAD_DRUG, c));
            }
        };

        Handler mHandler = new Handler();

        public static class Displayer implements Runnable {

            int id;
            Cursor c;
            VisitViewFragment v;

            public Displayer(VisitViewFragment v, int id, Cursor c) {
                this.id = id;
                this.c = c;
                this.v = v;
            }

            @Override
            public void run() {
                if (v.isDetached()) {
                    return;
                }
                switch (id) {
                    case LOAD_DEFAULT:
                        v.doShowDefaultVisit(c);
                        break;
                    case LOAD_DIAG:
                        v.doShowVisitDiag(c);
                        break;
                    case LOAD_DRUG:
                        v.doShowVisitDrug(c);
                        break;
                }

                c.close();
                c = null;

                v = null;
            }

        }

        private void doShowDefaultVisit(Cursor c) {
            addTitle(R.string.general);
            if (c.moveToFirst()) {

                addContentWithUnit(R.string.height,
                        c.getString(c.getColumnIndex(Visit.HEIGHT)), getString(R.string.centimeter));
                addContentWithUnit(R.string.weight,
                        c.getString(c.getColumnIndex(Visit.WEIGHT)), getString(R.string.kilogram));
                addContentWithUnit(R.string.temperature,
                        c.getString(c.getColumnIndex(Visit.TEMPERATURE)), getString(R.string.celcius));

                String p1 = c.getString(c.getColumnIndex(Visit.PRESSURE));
                String p2 = c.getString(c.getColumnIndex(Visit.PRESSURE_2));
                if (!TextUtils.isEmpty(p2)) {
                    addContentWithUnit(R.string.pressure, p2, getString(R.string.mmhg));
                } else {
                    addContentWithUnit(R.string.pressure, p1, getString(R.string.mmhg));
                }

                addContentWithUnit(R.string.pulse, c.getString(c.getColumnIndex(Visit.PULSE)), getString(R.string.time_per_minute));
                addContent(R.string.symtom,
                        c.getString(c.getColumnIndex(Visit.SYMPTOMS)));
                addContent(R.string.vitalcheck,
                        c.getString(c.getColumnIndex(Visit.VITAL)));
                addContent(R.string.heatlhsuggest,
                        c.getString(c.getColumnIndex(Visit.HEALTH_SUGGEST_1)));
                addContent(R.string.heatlhsuggest,
                        c.getString(c.getColumnIndex(Visit.HEALTH_SUGGEST_2)));

            }
        }

        public void doShowVisitDiag(Cursor c) {
            addTitle(R.string.diagnosis);
            if (c.moveToFirst()) {
                String clinic = c.getString(c.getColumnIndex(VisitDiag.CLINIC));
                if (!TextUtils.isEmpty(clinic))
                    addContentQuery(R.string.clinic, Clinic.NAME, Uri.withAppendedPath(Clinic.CONTENT_URI, clinic), clinic);
                String[] array = getResources().getStringArray(R.array.diag_DX);
                do {
                    String dxType = array[c.getInt(c.getColumnIndex(VisitDiag.TYPE))];
                    String dxCode = c.getString(c.getColumnIndex(VisitDiag.CODE));
                    addContentQuery(dxType, Diagnosis.NAME_TH,
                            Uri.withAppendedPath(Diagnosis.CONTENT_URI, dxCode), dxCode);
                } while (c.moveToNext());
            }
        }

        private void doShowVisitDrug(Cursor c) {

            addTitle(R.string.drug);
            if (c.moveToFirst()) {
                do {
                    String drugCode = c.getString(c.getColumnIndex(VisitDrug.CODE));
                    addContentQuery(drugCode, Drug.NAME,
                            Uri.withAppendedPath(Drug.CONTENT_URI, drugCode),
                            null);
                    String unit = c.getString(c.getColumnIndex(VisitDrug.UNIT));
                    if (!TextUtils.isEmpty(unit))
                        addSubContent("x " + unit);
                } while (c.moveToNext());
            }
        }

    }

    public static class EmptyVisitFragment extends ViewFormFragment {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            addTitle(R.string.not_available);
        }
    }
}
