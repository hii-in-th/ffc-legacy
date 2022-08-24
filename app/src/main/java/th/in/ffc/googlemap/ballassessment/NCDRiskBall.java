package th.in.ffc.googlemap.ballassessment;


import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import th.in.ffc.provider.PersonProvider.PersonDeath;
import th.in.ffc.provider.PersonProvider.PersonNCDBall;
import th.in.ffc.provider.PersonProvider.PersonRiskBlackBall;
import th.in.ffc.provider.PersonProvider.PersonRiskRedGrayBall;
import th.in.ffc.util.AgeCalculator;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;

import java.util.ArrayList;
import java.util.HashMap;

public class NCDRiskBall implements LoaderCallbacks<Cursor> {
    public static final Uri queryBlackBall = PersonRiskBlackBall.CONTENT_URI;
    public static final Uri queryRedBall = PersonRiskRedGrayBall.CONTENT_URI;
    public static final Uri queryNCDBall = PersonNCDBall.CONTENT_URI;
    public static final Uri queryPersonDeath = PersonDeath.CONTENT_URI;

    Context context;
    MapBallCorlorFragmentActivity activityForStartQuery;
    onQueryListener onQueryListener;
    private ArrayList<String> fname;
    private ArrayList<String> lname;
    private ArrayList<LatLng> position;
    private ArrayList<String> villno;
    private ArrayList<String> hno;
    private ArrayList<String> villname;
    private ArrayList<String> hcode;
    private HashMap<String, String> HashVillno;
    private ArrayList<String> pid;
    private ArrayList<String> pcucode;
    private ArrayList<String> age;
    private HashMap<String, String> personDeath;
    int groupRisk;
    String fillterVillNo;
    String fillterAge;

    public NCDRiskBall(Context context, MapBallCorlorFragmentActivity activityForStartQuery) {
        this.context = context;
        this.activityForStartQuery = activityForStartQuery;
        activityForStartQuery.getLoaderManager().initLoader(999, null, this);
        fillterVillNo = "";
        fillterAge = "30";
        HashVillno = new HashMap<String, String>();
        personDeath = new HashMap<String, String>();
    }

    public void setOnQueryListener(onQueryListener onQueryListener) {
        this.onQueryListener = onQueryListener;
    }

    public void init() {
        pcucode = new ArrayList<String>();
        pid = new ArrayList<String>();
        age = new ArrayList<String>();
        fname = new ArrayList<String>();
        lname = new ArrayList<String>();
        hcode = new ArrayList<String>();
        position = new ArrayList<LatLng>();
        villno = new ArrayList<String>();
        hno = new ArrayList<String>();
        villname = new ArrayList<String>();
    }

    public void onStartQuery(int groupRisk) {
        init();
        this.groupRisk = groupRisk;
        activityForStartQuery.getLoaderManager().initLoader(groupRisk, null, this);

    }

    public void setFillterQueryVillno(String villno) {
        if (!villno.equals("")) {
            fillterVillNo = villno;
        } else {
            fillterVillNo = "";
        }
    }

    public void setFillterAge(String fillterAge) {
        if (!TextUtils.isEmpty(fillterAge)) {
            this.fillterAge = fillterAge;
        } else {
            fillterAge = "";
        }
    }


    public void onReStartQuery(int groupRisk) {
        init();
        this.groupRisk = groupRisk;
        activityForStartQuery.getLoaderManager().restartLoader(groupRisk, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where;
        CursorLoader cl = null;
        switch (id) {
            case 0:
                String[] projection = {"pid", "hcode", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villcode", "villname", "hno"};
                where = "villno !=0 AND dischargetype = 9 AND (typelive = 1 OR typelive = 3) AND typedischart != \"01\" AND " +
                        "typedischart != \"02\" AND typedischart != \"07\" AND (groupcode = \"03\" OR " +
                        "groupcode = \"07\" OR groupcode = \"09\" OR groupcode = \"13\")";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryBlackBall, projection, where, null, "birth");
                break;
            case 1:
                String[] projection1 = {"pid", "hcode", "hno", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villname", "villcode", "labresultdigit", "bsl", "hbp_s1", "hbp_d1", "hbp_s2", "hbp_d2", "labcode"};
                where = "villno != 0  AND (groupcode IN('01','10') AND " +
                        "((((ncd_person_ncd_screen.hbp_s2 IS NOT NULL AND ncd_person_ncd_screen.hbp_s2  >=180) "
                        + "OR (ncd_person_ncd_screen.hbp_s2  IS NULL AND ncd_person_ncd_screen.hbp_s1  >=180))"
                        + "OR((ncd_person_ncd_screen.hbp_d2 IS NOT NULL AND ncd_person_ncd_screen.hbp_d2 >=100) "
                        + "OR (ncd_person_ncd_screen.hbp_d2  IS NULL AND ncd_person_ncd_screen.hbp_d1 >=100))) "
                        + "OR ncd_person_ncd_screen.bsl > 183 "
                        + "OR (visitlabchcyhembmsse.labcode ='CH99' AND visitlabchcyhembmsse.labresultdigit >=8)))";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryRedBall, projection1, where, null, "birth");
                break;
            case 2:
                String[] projection2 = {"pid", "hcode", "hno", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villname", "villcode", "labresultdigit", "bsl", "hbp_s1", "hbp_d1", "hbp_s2", "hbp_d2", "labcode"};
                where = "villno != 0  AND (groupcode IN('01','10') AND " +
                        "((((ncd_person_ncd_screen.hbp_s2 IS NOT NULL AND ncd_person_ncd_screen.hbp_s2  BETWEEN 160 and 179) "
                        + "OR (ncd_person_ncd_screen.hbp_s2  IS NULL AND ncd_person_ncd_screen.hbp_s1  BETWEEN 160 and 179))"
                        + "OR((ncd_person_ncd_screen.hbp_d2 IS NOT NULL AND ncd_person_ncd_screen.hbp_d2 BETWEEN 100 and 109) "
                        + "OR (ncd_person_ncd_screen.hbp_d2  IS NULL AND ncd_person_ncd_screen.hbp_d1 BETWEEN 100 and 109))) "
                        + "OR ncd_person_ncd_screen.bsl BETWEEN 155 and 182  "
                        + "OR (visitlabchcyhembmsse.labcode ='CH99' AND visitlabchcyhembmsse.labresultdigit BETWEEN 7 and 7.9)))";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryRedBall, projection2, where, null, "birth");
                break;
            case 3:
                String[] projection3 = {"distinct pid", "hcode", "hno", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villname", "villcode", "labresultdigit", "bsl", "hbp_s1", "hbp_d1", "hbp_s2", "hbp_d2", "labcode"};
                where = "villno != 0  AND (groupcode IN('01','10') AND " +
                        "((((ncd_person_ncd_screen.hbp_s2 IS NOT NULL AND ncd_person_ncd_screen.hbp_s2  BETWEEN 140 and 159) "
                        + "OR (ncd_person_ncd_screen.hbp_s2  IS NULL AND ncd_person_ncd_screen.hbp_s1  BETWEEN 140 and 159))"
                        + "OR((ncd_person_ncd_screen.hbp_d2 IS NOT NULL AND ncd_person_ncd_screen.hbp_d2 BETWEEN 90 and 99) "
                        + "OR (ncd_person_ncd_screen.hbp_d2  IS NULL AND ncd_person_ncd_screen.hbp_d1 BETWEEN 90 and 99))) "
                        + "OR ncd_person_ncd_screen.bsl BETWEEN 126 and 154  "
                        + "OR (visitlabchcyhembmsse.labcode ='CH99' AND visitlabchcyhembmsse.labresultdigit < 7)))";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryRedBall, projection3, where, null, "birth");
                break;
            case 4:
                String[] projection4 = {"pid", "hcode", "hno", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villcode"
                        , "villname", "bsl", "hbp_s1", "hbp_d1", "hbp_s2", "hbp_d2", "groupcode", "labcode", "labresultdigit"};
                where = "villno != 0";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryNCDBall, projection4, where, null, "birth");
                break;
            case 5:
                String[] projection5 = {"pid", "hcode", "hno", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villcode"
                        , "villname", "bsl", "hbp_s1", "hbp_d1", "hbp_s2", "hbp_d2", "groupcode", "labcode", "labresultdigit"};
                where = "villno != 0 AND  (codechronic IS NULL OR codechronic NOT IN(\'10\',\'01\',\'03\',\'07\',\'09\',\'13\')) "
                        + " AND ncd_person_ncd_screen.bsl < 100 AND "
                        + "((ncd_person_ncd_screen.hbp_s2 IS NOT NULL AND ncd_person_ncd_screen.hbp_s2 BETWEEN 120 and 139)  "
                        + " OR (ncd_person_ncd_screen.hbp_s2  IS NULL AND ncd_person_ncd_screen.hbp_s1 BETWEEN 120 and 139)) "
                        + " AND((ncd_person_ncd_screen.hbp_d2 IS NOT NULL AND ncd_person_ncd_screen.hbp_d2 BETWEEN 80 and 89) "
                        + " OR (ncd_person_ncd_screen.hbp_d2  IS NULL AND ncd_person_ncd_screen.hbp_d1 BETWEEN 80 and 89)) ";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryNCDBall, projection5, where, null, "birth");
                break;
            case 6:
                String[] projection6 = {"pid", "hcode", "hno", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villcode"
                        , "villname", "bsl", "hbp_s1", "hbp_d1", "hbp_s2", "hbp_d2", "groupcode", "labcode", "labresultdigit"};
                where = "villno != 0 AND  (codechronic IS NULL OR codechronic NOT IN(\'10\',\'01\',\'03\',\'07\',\'09\',\'13\')) "
                        + " AND ncd_person_ncd_screen.bsl < 100 AND ((ncd_person_ncd_screen.hbp_s2 IS NOT NULL AND ncd_person_ncd_screen.hbp_s2 < 120)  "
                        + " OR (ncd_person_ncd_screen.hbp_s2  IS NULL AND ncd_person_ncd_screen.hbp_s1 < 120)) "
                        + " AND((ncd_person_ncd_screen.hbp_d2 IS NOT NULL AND ncd_person_ncd_screen.hbp_d2 < 80) "
                        + " OR (ncd_person_ncd_screen.hbp_d2  IS NULL AND ncd_person_ncd_screen.hbp_d1 < 80)) ";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryNCDBall, projection6, where, null, "birth");
                break;
            case 7:
                String[] projection7 = {"pid", "hcode", "hno", "xgis", "ygis", "pid", "birth", "pcucodeperson", "fname", "lname", "villno", "villcode"
                        , "villname", "bsl", "hbp_s1", "hbp_d1", "hbp_s2", "hbp_d2", "groupcode", "labcode", "labresultdigit"};
                where = "villno != 0"
                        + " AND ncd_person_ncd_screen.bsl IS NULL AND "
                        + "ncd_person_ncd_screen.hbp_s2 IS NULL AND ncd_person_ncd_screen.hbp_s2 IS NULL AND  "
                        + "ncd_person_ncd_screen.hbp_d2 IS NULL AND ncd_person_ncd_screen.hbp_d2 IS NULL";
                if (!fillterVillNo.equals("")) {
                    where += " AND villno=" + fillterVillNo;
                }
                where +=" AND (person.pid|| person.pcucodeperson) NOT IN ( SELECT   persondeath.pid||persondeath.pcucodeperson  FROM persondeath )";

                cl = new CursorLoader(context, queryNCDBall, projection7, where, null, "birth");
                break;
            case 999:
                String[] projection999 = {"pid"};
                cl = new CursorLoader(context, queryPersonDeath, projection999, null, null, "pid");
                break;
            default:
                break;
        }

        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        int id = arg0.getId();
        if (id != 99 && groupRisk != 4 && id != 999) {
            Log.d("TEST", "EIEI11");
            if (c.moveToFirst()) {
                do {
                    String pid = c.getString(c.getColumnIndex("pid"));
                    if (personDeath.isEmpty() || TextUtils.isEmpty(personDeath.get(pid))) {
                        if (calAge(c.getString(c.getColumnIndex("birth")))) {
                            setValue(c);
                        }
                    }
                } while (c.moveToNext());
            }
        } else if (id == 999) {
            Log.d("TEST", "EIEI");
            if (c.moveToFirst()) {
                do {
                    String pid = c.getString(c.getColumnIndex("pid"));
                    personDeath.put(pid, pid);
                } while (c.moveToNext());
            }
        } else {
            if (c.moveToFirst()) {
                do {
                    if (!TextUtils.isEmpty(personDeath.get(c.getString(c.getColumnIndex("pid"))))) {
                        if (calAge(c.getString(c.getColumnIndex("birth")))) {
                            if (chkBallGray(c)) {
                                setValue(c);
                            }
                        }
                    }
                } while (c.moveToNext());
            }
        }
        if (id != 999 && id != 99) {
            if (onQueryListener != null) {
                onQueryListener.onQueryFinish();
            }
        }

    }

    private boolean chkRedBall(double HbA1c, int Hbp_d, int Hpb_s, int FBS) {
        boolean redBall = false;
        if (HbA1c > 8 || Hpb_s > 180 || Hbp_d > 110 || FBS > 183) {
            redBall = true;
        }
        return redBall;
    }

    private boolean chkOrangeBall(double HbA1c, int Hbp_d, int Hpb_s, int FBS) {
        boolean orangeBall = false;
        if (HbA1c != -1 || Hbp_d != -1 || Hpb_s != -1 || FBS != -1) {
            if ((HbA1c > 6.9 && HbA1c < 8) || (Hpb_s > 159 && Hpb_s < 180)
                    || (Hbp_d > 99 && Hbp_d < 110) || (FBS > 154 && FBS < 183)) {
                orangeBall = true;
            }
        }
        return orangeBall;
    }

    private boolean chkYellowBall(double HbA1c, int Hbp_d, int Hpb_s, int FBS) {

        boolean yellowBall = false;
        if (HbA1c < 7 && (Hpb_s > 139 && Hpb_s < 160)
                || (Hbp_d > 89 && Hbp_d < 100) || (FBS > 125 && FBS < 155)) {
            yellowBall = true;
        }
        return yellowBall;
    }

    private boolean chkBallGray(Cursor c) {
        boolean gray = false;
        int Hbp_d = getHbp_d(c);
        int Hpb_s = getHbp_s(c);
        int FBS = getFBS(c);
        String labcode = c.getString(c.getColumnIndex("labcode"));
        if (!TextUtils.isEmpty(labcode) && labcode.equals("CH99")) {
            double HbA1c = getResultNumDigit(c);
            if (!chkRedBall(HbA1c, Hbp_d, Hpb_s, FBS) && !chkOrangeBall(HbA1c, Hbp_d, Hpb_s, FBS)
                    && !chkYellowBall(HbA1c, Hbp_d, Hpb_s, FBS)) {
                gray = true;
            }
        } else {
            if ((Hpb_s > 120 || Hbp_d > 80) || FBS >= 100) {
                gray = true;
            }
        }
        return gray;
    }

    private Double getResultNumDigit(Cursor c) {
        double labresultnumdigit = -1;
        if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("labresultdigit")))) {
            labresultnumdigit = Double.parseDouble(c.getString(c.getColumnIndex("labresultdigit")));
        }
        return labresultnumdigit;
    }

    private int getHbp_d(Cursor c) {
        int Hbp_d = -1;
        if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("hbp_d2")))) {
            Hbp_d = Integer.parseInt(c.getString(c.getColumnIndex("hbp_d2")));
        } else if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("hbp_d1")))) {
            Hbp_d = Integer.parseInt(c.getString(c.getColumnIndex("hbp_d1")));
        }
        return Hbp_d;
    }

    private int getHbp_s(Cursor c) {
        int Hbp_s = -1;
        if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("hbp_s2")))) {
            Hbp_s = Integer.parseInt(c.getString(c.getColumnIndex("hbp_s2")));
        } else if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("hbp_s1")))) {
            Hbp_s = Integer.parseInt(c.getString(c.getColumnIndex("hbp_s1")));
        }
        return Hbp_s;
    }

    private int getFBS(Cursor c) {
        int FBS = -1;
        if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("bsl")))) {
            FBS = Integer.parseInt(c.getString(c.getColumnIndex("bsl")));
        }
        return FBS;
    }

    public void setValue(Cursor c) {
        fname.add(checkNull(c.getString(c.getColumnIndex("fname"))));
        lname.add(checkNull(c.getString(c.getColumnIndex("lname"))));
        villno.add(checkNull(c.getString(c.getColumnIndex("villno"))));
        villname.add(checkNull(c.getString(c.getColumnIndex("villname"))));
        hno.add(checkNull(c.getString(c.getColumnIndex("hno"))));
        hcode.add(checkNull(c.getString(c.getColumnIndex("hcode"))));
        pid.add(checkNull(c.getString(c.getColumnIndex("pid"))));
        pcucode.add(checkNull(c.getString(c.getColumnIndex("pcucodeperson"))));
        age.add(getAge(c.getString(c.getColumnIndex("birth"))));
//        String lat = c.getString(c.getColumnIndex("ygis"));
//        String lng = c.getString(c.getColumnIndex("xgis"));
        String lat = c.getString(c.getColumnIndex("xgis"));
        String lng = c.getString(c.getColumnIndex("ygis"));
        HashVillno.put(c.getString(c.getColumnIndex("villname")), c.getString(c.getColumnIndex("villno")));
        if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
            if (checkDoubletype(lat) && checkDoubletype(lng)) {
                double lattitude = Double.parseDouble(lat);
                double longitude = Double.parseDouble(lng);
                LatLng positionLatLng = new LatLng(lattitude, longitude);
                position.add(positionLatLng);
            }
        } else {
            position.add(null);
        }
    }


    private String checkNull(String chk) {
        String sreturn = "";
        if (!TextUtils.isEmpty(chk)) {
            sreturn = chk;
        }
        return sreturn;
    }

    private boolean checkDoubletype(String type) {
        boolean hasType = false;
        try {
            Double.parseDouble(type);
            hasType = true;

        } catch (NumberFormatException nfe) {
            hasType = false;
        }
        return hasType;
    }

    public ArrayList<String> getName() {
        return fname;
    }

    public ArrayList<String> getLastName() {
        return lname;
    }

    public ArrayList<LatLng> getPosition() {
        return position;
    }

    public ArrayList<String> getHno() {
        return hno;
    }

    public ArrayList<String> getVillname() {
        return villname;
    }

    public ArrayList<String> getVillno() {
        return villno;
    }

    public ArrayList<String> getHcode() {
        return hcode;
    }

    public ArrayList<String> getPID() {
        return pid;
    }

    public ArrayList<String> getPCUPersonCode() {
        return pcucode;
    }

    public ArrayList<String> getAge() {
        return age;
    }


    public String getAge(String born) {
        Date mBorn = Date.newInstance(born);
        Date current = Date.newInstance(DateTime.getCurrentDate());
        AgeCalculator cal = new AgeCalculator(current, mBorn);
        Date age = cal.calulate();
        String ageReturn[] = age.toString().split("-");
        return ageReturn[0];
    }

    public boolean calAge(String born) {
        boolean pass = false;
        int tempAge = 0;
        if (!TextUtils.isEmpty(fillterAge)) {
            tempAge = Integer.parseInt(fillterAge);
        }
        Date mBorn = Date.newInstance(born);
        Date current = Date.newInstance(DateTime.getCurrentDate());
        AgeCalculator cal = new AgeCalculator(current, mBorn);
        Date age = cal.calulate();
        String ageReturn[] = age.toString().split("-");
        int tempAge1 = Integer.parseInt(ageReturn[0]);
        if (tempAge1 >= tempAge) {
            pass = true;
        }
        return pass;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    public static interface onQueryListener {
        public void onQueryFinish();
    }
}
