package th.in.ffc.googlemap.ballassessment;

import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.googlemap.ballassessment.NCDRiskBall.onQueryListener;
import th.in.ffc.googlemap.ballassessment.RiskListFragment.onListItemClickListener;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.person.PersonActivity;
import th.in.ffc.provider.PersonProvider.PersonHouse;
import th.in.ffc.util.CheckNetwork;
import th.in.ffc.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapBallCorlorFragmentActivity extends FFCFragmentActivity implements LoaderCallbacks<Cursor> {
    private final String[] risk = {
            "ป่วยรุนแรง(โรคแทรกซ้อน)" + "\n" + "หัวใจ,หลอดเลือดสมอง,ไตวาย",
            "ป่วย(รุนแรง)" + "\n" + "เบาหวาน ความดันโลหิตสูง HbA1c >= 8 Bp : >= 180/110 FBS : 183",
            "ป่วย(ปานกลาง)" + "\n" + "เบาหวาน ความดันโลหิตสูง HbA1c >= 7-7.9 Bp : >= 160-179/100-109 FBS : 155-182",
            "ป่วย(อ่อน)" + "\n" + "เบาหวาน ความดันโลหิตสูง HbA1c < 7 Bp : >= 140-159/90-99 FBS : 126-154",
            "กลุ่มเสี่ยงสูง" + "\n" + "Bp : >= 120-139/80-89 FBS : 100-125",
            "ดูแลตัวเองได้" + "\n" + "Bp : >= 120/80 FBS : 100", "ปกติ", "ไม่ได้ตรวจ"
    };
    private final int[] resourceBall = {
            R.drawable.ncd_ball_7, R.drawable.ncd_ball_6, R.drawable.ncd_ball_5, R.drawable.ncd_ball_4,
            R.drawable.ncd_ball_3, R.drawable.ncd_ball_2, R.drawable.ncd_ball_1, R.drawable.ncd_ball_0
    };
    private GoogleMap myMap;
    private TextView age;
    private Spinner villChoice;
    private Spinner groupChoice;
    private ArrayList<String> fname;
    private ArrayList<String> lname;
    private ArrayList<LatLng> position;
    private ArrayList<String> hno;
    private ArrayList<String> villName;
    private ArrayList<String> listVillNo;
    private ArrayList<String> listNormalVillNo;
    private ArrayList<String> hcode;
    private ArrayList<String> pid;
    private ArrayList<String> pcupersoncode;
    private ArrayList<String> ages;
    private HashMap<String, Marker> markerTag;
    private HashMap<String, String> villnameHash;
    private HashMap<Marker, String> markerDetail;
    //private HashMap<Marker,String> markerNormalDetail;
    String villno;
    //private HashMap<String,String> riskHash;
    private ArrayList<Marker> listMarker;
    private HashMap<String, ArrayList<Marker>> hashMaker;
    private String where;
    int groupRisk;
    List<String> list;
    NCDRiskBall genRisk;
    Marker marker;
    boolean firstGen;
    String fillterAge;
    LinearLayout personList;
    ProgressDialog progress;
    TextView listresult;
    ArrayList<Integer> indexSearch;
    String oldVillno;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_ball_fragmentactivity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        age = (TextView) findViewById(R.id.age);
        villChoice = (Spinner) findViewById(R.id.villchoice);
        groupChoice = (Spinner) findViewById(R.id.groupchoice);
        listresult = (TextView) findViewById(R.id.loadresult);
        personList = (LinearLayout) findViewById(R.id.personlist);
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment myMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
        myMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override public void onMapReady(GoogleMap googleMap) {
                myMap = googleMap;
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.822578, 100.514233), 6.0f));
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                myMap.setInfoWindowAdapter(markerInfo);
                myMap.setOnInfoWindowClickListener(markerInfoClick);
                myMap.setOnMarkerClickListener(markerClick);
            }
        });

        doOpenProgressLoadShow();
        hashMaker = new HashMap<String, ArrayList<Marker>>();
        fname = new ArrayList<String>();
        lname = new ArrayList<String>();
        position = new ArrayList<LatLng>();
        villName = new ArrayList<String>();
        listVillNo = new ArrayList<String>();
        listNormalVillNo = new ArrayList<String>();
        pid = new ArrayList<String>();
        pcupersoncode = new ArrayList<String>();
        ages = new ArrayList<String>();
        list = new ArrayList<String>();
        hno = new ArrayList<String>();
        indexSearch = new ArrayList<Integer>();
        villnameHash = new HashMap<String, String>();
        list.add(getString(R.string.allvillage));
        villnameHash.put(getString(R.string.allvillage), "0");
        genRisk = new NCDRiskBall(getApplicationContext(), this);
        genRisk.setOnQueryListener(onQueryListener);
        setRiskSpinner();
        getLoaderManager().initLoader(99, null, this);
        fillterAge = age.getText().toString();
        groupRisk = 0;
        oldVillno = "0";
        firstGen = true;
        markerDetail = new HashMap<Marker, String>();
        markerTag = new HashMap<String, Marker>();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setOnQueryTextListener(queryListener);
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        searchView.setQueryHint(getString(R.string.mapballsearchhint));
        menu.add("Search")
                .setIcon(R.drawable.ic_action_search)
                .setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        return true;
    }

    private void setRiskSpinner() {
        LayoutInflater inflater = getLayoutInflater();
        SpinnerRiskAdapter adp =
                new SpinnerRiskAdapter(getApplicationContext(), R.layout.map_ncd_risk_spinnercustom, risk, inflater,
                        resourceBall);
        groupChoice.setAdapter(adp);
    }

    public void onClick(View v) {
        String villname = villChoice.getSelectedItem().toString();
        if (!villnameHash.get(villname).equals("0") && !oldVillno.equals(villnameHash.get(villname))) {
            fillterAge = age.getText().toString();
            myMap.clear();
            doOpenProgressLoadShow();
            villno = villnameHash.get(villname);
            oldVillno = villno;
            where = "villno =" + villno;
            groupRisk = groupChoice.getSelectedItemPosition();
            genRisk.setFillterQueryVillno(villno);
            genRisk.setFillterAge(fillterAge);
            genRisk.onReStartQuery(groupRisk);
        } else if (villnameHash.get(villname).equals("0") && !oldVillno.equals(villnameHash.get(villname))) {
            fillterAge = age.getText().toString();
            myMap.clear();
            doOpenProgressLoadShow();
            villno = villnameHash.get(villname);
            oldVillno = villno;
            where = null;
            groupRisk = groupChoice.getSelectedItemPosition();
            genRisk.setFillterQueryVillno("");
            genRisk.setFillterAge(fillterAge);
            genRisk.onReStartQuery(groupRisk);
        } else if (!fillterAge.equals(age.getText().toString())) {
            fillterAge = age.getText().toString();
            myMap.clear();
            doOpenProgressLoadShow();
            villno = villnameHash.get(villname);
            oldVillno = villno;
            where = "villno =" + villno;
            groupRisk = groupChoice.getSelectedItemPosition();
            genRisk.setFillterAge(fillterAge);
            genRisk.onReStartQuery(groupRisk);
        } else if (groupRisk != groupChoice.getSelectedItemPosition()) {
            fillterAge = age.getText().toString();
            myMap.clear();
            doOpenProgressLoadShow();
            groupRisk = groupChoice.getSelectedItemPosition();
            genRisk.setFillterAge(fillterAge);
            genRisk.onReStartQuery(groupRisk);
        }
    }

    private void doOpenProgressLoadShow() {
        personList.setVisibility(LinearLayout.GONE);
        progress =
                ProgressDialog.show(this, getString(R.string.nowloadlisttitle), getString(R.string.nowloadlistdetail),
                        true);
    }

    @Override public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl = null;
        switch (arg0) {
            case 99:
                Uri uri = PersonHouse.CONTENT_URI;
                String[] projection = { "hcode", "fname", "lname", "xgis", "ygis", "villname", "villno", "hno" };
                String arg = "villno != 0";
                if (where != null) {
                    myMap.clear();
                    arg = arg + " AND " + where;
                }
                cl = new CursorLoader(getApplication(), uri, projection, arg, null, "village.villno");
                break;

            default:
                break;
        }
        return cl;
    }

    HashMap<String, String> hashVillno;

    @Override public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        hashVillno = new HashMap<String, String>();
        if (c.moveToFirst()) {
            do {
                String villname = c.getString(c.getColumnIndex("villname"));
                String villno = c.getString(c.getColumnIndex("villno"));
                if (TextUtils.isEmpty(hashVillno.get(villno))) {
                    hashVillno.put(villno, villno);
                    listMarker = new ArrayList<Marker>();
                    villnameHash.put(getString(R.string.mapballmho) + " " + villno + " " + villname, villno);
                    listNormalVillNo.add(villno);
                    list.add(getString(R.string.mapballmho) + " " + villno + " " + villname);
                }
            } while (c.moveToNext());
            if (where == null) {
                setSpinner(list, villChoice);
            }
            if (firstGen) {
                genRisk.onStartQuery(groupRisk);
                firstGen = false;
            }
        }
    }

    @Override public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    public void setSpinner(List<String> list, Spinner spinner) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    onQueryListener onQueryListener = new onQueryListener() {
        @Override public void onQueryFinish() {
            setMap();
        }
    };

    public void setMap() {
        fname = genRisk.getName();
        lname = genRisk.getLastName();
        position = genRisk.getPosition();
        hno = genRisk.getHno();
        villName = genRisk.getVillname();
        listVillNo = genRisk.getVillno();
        hcode = genRisk.getHcode();
        pid = genRisk.getPID();
        ages = genRisk.getAge();
        pcupersoncode = genRisk.getPCUPersonCode();
        if (fname != null) {
            setRiskPersonList();
            listresult.setVisibility(TextView.GONE);
            if (fname.isEmpty()) {
                listresult.setVisibility(TextView.VISIBLE);
                listresult.setText(getString(R.string.notdata));
            }
        }
        for (int i = 0; i < position.size(); i++) {
            if (position.get(i) != null) {
                String detail = pid.get(i)
                        + ","
                        + pcupersoncode.get(i)
                        + ","
                        + getString(R.string.fname)
                        + " "
                        + fname.get(i)
                        + " "
                        + lname.get(i)
                        + " "
                        + getString(R.string.mapballage)
                        + " "
                        + ages.get(i)
                        + " "
                        + getString(R.string.year)
                        + "\n"
                        + hno.get(i)
                        + getString(R.string.mapballmho)
                        + " "
                        + listVillNo.get(i)
                        + " "
                        + villName.get(i);
                Log.d("==> Map:",position.get(i).toString());
                Marker maker = myMap.addMarker(new MarkerOptions().position(position.get(i)));
                maker.setIcon(BitmapDescriptorFactory.fromResource(resourceBall[groupRisk]));
                markerTag.put(hcode.get(i), maker);
                markerDetail.put(maker, detail);
            }
        }
    }

    RiskListFragment RiskListFragment;

    public void setRiskPersonList() {
        progress.dismiss();
        personList.setVisibility(LinearLayout.VISIBLE);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tc = fm.beginTransaction();
        RiskListFragment = new RiskListFragment();
        RiskListFragment.setOnItemClick(onItemClickListener);
        Bundle arg = new Bundle();
        arg.putStringArrayList("name", fname);
        arg.putStringArrayList("lname", lname);
        arg.putStringArrayList("villno", listVillNo);
        arg.putStringArrayList("hno", hno);
        arg.putStringArrayList("villname", villName);
        arg.putStringArrayList("age", ages);
        RiskListFragment.setArguments(arg);
        tc.replace(R.id.personlist, RiskListFragment);
        tc.commit();
    }

    onListItemClickListener onItemClickListener = new onListItemClickListener() {

        @Override public void onItemClickListener(int positionClick) {
            if (indexSearch != null && !indexSearch.isEmpty()) {
                myMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(position.get(indexSearch.get(positionClick)), 18.0f));
                Marker markerTemp = markerTag.get(hcode.get(indexSearch.get(positionClick)));
                if (markerTemp != null) {
                    markerTemp.showInfoWindow();
                }
            } else {
                if (position.get(positionClick) != null) {
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position.get(positionClick), 18.0f));
                    Marker markerTemp = markerTag.get(hcode.get(positionClick));
                    if (markerTemp != null) {
                        markerTemp.showInfoWindow();
                    }
                } else {
                    onStartPersonActivity(pid.get(positionClick), pcupersoncode.get(positionClick));
                    //	Toast.makeText(getApplicationContext(), "��辺���˹觢ͧ��ҹ��ѧ��� ��سҵ�Ǩ�ͺ�ҹ������", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void onStartPersonActivity(String pid, String pcupersoncode) {
        Intent intent = new Intent(Action.VIEW);
        intent.addCategory(Category.PERSON);
        PersonActivity.startPersonActivity(this, intent, "" + pid, pcupersoncode);
    }

    InfoWindowAdapter markerInfo = new InfoWindowAdapter() {
        @Override public View getInfoContents(Marker marker) {
            return null;
        }

        @Override public View getInfoWindow(Marker marker) {
            View v = getLayoutInflater().inflate(R.layout.map_risk_infowindow, null);
            TextView txthno = (TextView) v.findViewById(R.id.hno);
            String tempTag[] = markerDetail.get(marker).split(",");
            if (!TextUtils.isEmpty(tempTag[1])) txthno.setText(tempTag[2]);
            return v;
        }
    };

    OnInfoWindowClickListener markerInfoClick = new OnInfoWindowClickListener() {

        @Override public void onInfoWindowClick(Marker marker) {
            String temp[] = markerDetail.get(marker).split(",");
            onStartPersonActivity(temp[0], temp[1]);
        }
    };

    OnMarkerClickListener markerClick = new OnMarkerClickListener() {
        @Override public boolean onMarkerClick(Marker marker) {
            String temp[] = markerDetail.get(marker).split(",");
            onStartPersonActivity(temp[0], temp[1]);
            return true;
        }
    };

    SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {
        @Override public boolean onQueryTextChange(String newText) {
            if (TextUtils.isEmpty(newText)) {
                indexSearch = null;
                setRiskPersonList();
            } else {
                String temp[] = new String[fname.size()];
                ArrayList<String> tempString = new ArrayList<String>();
                indexSearch = new ArrayList<Integer>();
                for (int i = 0; i < fname.size(); i++) {
                    temp[i] = fname.get(i) + " " + lname.get(i) + " " + hno.get(i);
                    if (temp[i].contains(newText)) {
                        tempString.add(temp[i]);
                        indexSearch.add(i);
                    }
                }
                setNewList();
            }
            return false;
        }

        @Override public boolean onQueryTextSubmit(String query) {
            Toast.makeText(getApplicationContext(), "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    public void setNewList() {
        ArrayList<String> tempfname = new ArrayList<String>();
        ArrayList<String> templname = new ArrayList<String>();
        ArrayList<String> templistVillNo = new ArrayList<String>();
        ArrayList<String> temphno = new ArrayList<String>();
        ArrayList<String> tempvillName = new ArrayList<String>();
        ArrayList<String> tempages = new ArrayList<String>();
        for (int i = 0; i < indexSearch.size(); i++) {
            int index = indexSearch.get(i);
            tempfname.add(fname.get(index));
            templname.add(lname.get(index));
            templistVillNo.add(listVillNo.get(index));
            temphno.add(hno.get(index));
            tempvillName.add(villName.get(index));
            tempages.add(ages.get(index));
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tc = fm.beginTransaction();
        RiskListFragment = new RiskListFragment();
        RiskListFragment.setOnItemClick(onItemClickListener);
        Bundle arg = new Bundle();
        arg.putStringArrayList("name", tempfname);
        arg.putStringArrayList("lname", templname);
        arg.putStringArrayList("villno", templistVillNo);
        arg.putStringArrayList("hno", temphno);
        arg.putStringArrayList("villname", tempvillName);
        arg.putStringArrayList("age", tempages);
        RiskListFragment.setArguments(arg);
        tc.replace(R.id.personlist, RiskListFragment);
        tc.commit();
    }

    public void clickSearch(View v) {
        EditText textsearch = (EditText) findViewById(R.id.textsearch);
        CheckNetwork chk = new CheckNetwork(getApplicationContext());
        if (TextUtils.isEmpty(textsearch.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.please_define_keyword_before_search, Toast.LENGTH_SHORT)
                    .show();
        } else {
            if (!chk.isNetworkAvailable()) {
                Builder noNetworkDialog = new Builder(MapBallCorlorFragmentActivity.this);
                noNetworkDialog.setIcon(getApplication().getResources().getDrawable(R.drawable.ic_action_add));
                noNetworkDialog.setTitle(R.string.error);
                noNetworkDialog.setMessage(R.string.please_connect_internet);
                noNetworkDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                    }
                });
                noNetworkDialog.show();
            } else {
                Geocoder geoCoder = new Geocoder(getApplication(), new Locale("th"));
                String searchStr = textsearch.getText().toString();
                try {
                    final List<android.location.Address> addresses = geoCoder.getFromLocationName(searchStr, 10);
                    if (addresses.size() > 0) {
                        AlertDialog.Builder foundDialog = new AlertDialog.Builder(MapBallCorlorFragmentActivity.this,android.R.style.Theme_Material_Light_Dialog_Alert);
                        foundDialog.setIcon(R.drawable.ic_launcher);
                        foundDialog.setTitle(String.format(getString(R.string.search_place_result_of), searchStr));
                        final ArrayAdapter<String> arrayAdapter =
                                new ArrayAdapter<String>(getApplication(), R.layout.list_item);

                        for (int i = 0; i < addresses.size(); i++) {
                            String addressStr = null;
                            for (int j = 0; j < addresses.get(i).getMaxAddressLineIndex(); j++) {
                                if (j == 0) {
                                    addressStr = addresses.get(i).getAddressLine(j) + "\n";
                                } else {
                                    addressStr += addresses.get(i).getAddressLine(j) + " ";
                                }
                            }
                            if (!TextUtils.isEmpty(addressStr)) {
                                arrayAdapter.add(addressStr.replace("null", ""));
                            } else {
                                arrayAdapter.add(searchStr);
                            }
                        }
                        foundDialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                            @Override public void onClick(DialogInterface dialog, int which) {
                                addresses.get(which);
                                double latitude = addresses.get(which).getLatitude();
                                double longitude = addresses.get(which).getLongitude();
                                final LatLng zone = new LatLng(latitude, longitude);
                                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(zone, 18));
                            }
                        });
                        foundDialog.show();
                    } else {
                        Builder notFound = new Builder(MapBallCorlorFragmentActivity.this);
                        notFound.setIcon(getApplication().getResources().getDrawable(R.drawable.ic_action_add));
                        notFound.setTitle(R.string.error);
                        notFound.setMessage(R.string.place_not_found);
                        notFound.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        notFound.show();
                    }
                } catch (IOException e) { // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //
            }
        }
    }
}
