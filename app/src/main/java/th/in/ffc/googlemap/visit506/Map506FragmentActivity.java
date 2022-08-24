package th.in.ffc.googlemap.visit506;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.util.TypedValue;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.googlemap.directions.DirectionGen;
import th.in.ffc.googlemap.directions.DirectionGen.directionCallBack;
import th.in.ffc.googlemap.directions.GPSTracker;
import th.in.ffc.googlemap.lib.GoogleMapSearchPosition;
import th.in.ffc.googlemap.lib.GoogleMapSearchPosition.returnPosition;
import th.in.ffc.googlemap.visit506.Map506SettingDialogFragment.settingCallBack;
import th.in.ffc.googlemap.visit506.PatientDialogFragment.PatientListCallback;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.PersonProvider.Visit506_Person;
import th.in.ffc.util.CheckNetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Map506FragmentActivity extends FFCFragmentActivity implements LoaderCallbacks<Cursor> {

    private GoogleMap myMap;
    private Marker marker;
    private HashMap<String, Marker> tagHomeMarker;
    private HashMap<Marker, String> tagHome;
    private HashMap<String, String> tagHCODE;
    private HashMap<String, String> listNamePlague;
    private HashMap<String, String> homeStatus;
    private HashMap<String, CircleOptions> circleOptions;
    private HashMap<String, LatLng> markerPosition;
    private HashMap<String, HashMap<String, String>> tagHomeMemberPlague;
    private ArrayList<String> HCODE;
    private HashMap<String, String> listPersonDetail;
    private HashMap<String, String> tagPerson;
    private HashMap<String, LatLng> housePosition;
    private boolean searchPlace;
    // variable for update db
    private ArrayList<Integer> ERRORCODE;
    private Double radiusCircle;
    private Map506SettingDialogFragment myDialogFragment;
    private ArrayList<String> directionBox;
    private int direction;
    private GPSTracker gps;
    private LatLng beginLocation;
    private LatLng endLocation;
    private boolean clickPerson;
    private ProgressBar progressBar;

    LinearLayout linear_load;
    private String fillter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map506_fragment_activity);
        linear_load = (LinearLayout) findViewById(R.id.linear_load);
        init();
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment myMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
        myMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override public void onMapReady(GoogleMap googleMap) {
                myMap = googleMap;
                myMap.setOnMarkerClickListener(markerClick);
                myMap.setInfoWindowAdapter(markerInfo);
                myMap.setOnInfoWindowClickListener(markerInfoClick);
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                myMap.setOnMarkerDragListener(makerLongClick);
            }
        });

        searchPlace = false;
        fillter = "";
        getLoaderManager().initLoader(0, null, this);
        getLoaderManager().initLoader(1, null, this);
    }

    private void setDiaplay(int displayMode) {
        if (displayMode == 0) {
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    private void init() {
        tagHomeMemberPlague = new HashMap<String, HashMap<String, String>>();
        tagHome = new HashMap<Marker, String>();
        tagHomeMarker = new HashMap<String, Marker>();
        homeStatus = new HashMap<String, String>();
        HCODE = new ArrayList<String>();
        housePosition = new HashMap<String, LatLng>();
        tagHCODE = new HashMap<String, String>();
        listPersonDetail = new HashMap<String, String>();
        ERRORCODE = new ArrayList<Integer>();
        circleOptions = new HashMap<String, CircleOptions>();
        radiusCircle = 0.0;
        direction = 1;
        listNamePlague = new HashMap<String, String>();
        //	circle = new HashMap<String, Circle>();
        markerPosition = new HashMap<String, LatLng>();
        tagPerson = new HashMap<String, String>();
        clickPerson = false;
        hashMarkerWindowInfo = new HashMap<Marker, Boolean>();
        progressBar = (ProgressBar) findViewById(R.id.nowLoading);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem listPatient = menu.add(Menu.NONE, 2, Menu.NONE, "listPatient");
        listPatient.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        listPatient.setIcon(R.drawable.ic_action_patient_list);
        MenuItem direction = menu.add(Menu.NONE, 3, Menu.NONE, "direction");
        direction.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        direction.setIcon(R.drawable.ic_action_direction);
        MenuItem setting = menu.add(Menu.NONE, 1, Menu.NONE, "setting");
        setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setting.setIcon(R.drawable.ic_action_setting);
        MenuItem searchPlace = menu.add(Menu.NONE, 4, Menu.NONE, "����ʶҹ����ҧ�");
        searchPlace.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchPlace.setIcon(R.drawable.ic_action_search);
        return super.onCreateOptionsMenu(menu);
    }

    private PatientDialogFragment patient;

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                if (myDialogFragment == null) {
                    myDialogFragment = Map506SettingDialogFragment.newInstance(listNamePlague);
                    myDialogFragment.setOnClickListenerCallBack(callBack);
                    myDialogFragment.show(getFragmentManager(), "settingDialogFragment");
                } else {
                    if (myDialogFragment.isAdded()) {
                        myDialogFragment.dismiss();
                    }
                    myDialogFragment.show(getFragmentManager(), "settingDialogFragment");
                }
                return true;
            case 2:
                if (patient == null) {
                    patient = PatientDialogFragment.newInstance();
                    patient.setOnClickListenerCallBack(PatientCallback);
                    patient.show(getSupportFragmentManager(), "patientList");
                } else {
                    if (patient.isAdded()) {
                        patient.dismiss();
                    }
                    patient.show(getSupportFragmentManager(), "patientList");
                }
                direction = 0;
                return true;
            case 3:
                if (directionBox != null) {
                    directionDialog();
                } else {
                    direction = 1;
                    getLoaction();
                    if (gpsEnable) {
                        if (checkNetwork()) {
                            if (patient == null) {
                                patient = PatientDialogFragment.newInstance();
                                patient.setOnClickListenerCallBack(PatientCallback);
                                patient.show(getSupportFragmentManager(), "patientList");
                            } else {
                                patient.show(getSupportFragmentManager(), "patientList");
                            }
                        }
                    }
                }
                return true;
            case 4:
                searhPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void directionDialog() {
        Builder directionDialog = new Builder(Map506FragmentActivity.this);
        View sv = LayoutInflater.from(this).inflate(R.layout.direction_dialog, null);
        final RadioButton getdirection = (RadioButton) sv.findViewById(R.id.getdirection);
        getdirection.setChecked(true);
        final RadioButton seedirection = (RadioButton) sv.findViewById(R.id.seedirection);
        directionDialog.setTitle(R.string.need_find_route_or_read_route_guide);
        directionDialog.setView(sv);
        directionDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int which) {
                if (getdirection.isChecked()) {
                    if (patient == null) {
                        patient = PatientDialogFragment.newInstance();
                        patient.setOnClickListenerCallBack(PatientCallback);
                        patient.show(getSupportFragmentManager(), "patientList");
                    } else {
                        patient.show(getSupportFragmentManager(), "patientList");
                    }
                } else if (seedirection.isChecked()) {
                    showDirectionBox();
                }
            }
        });
        AlertDialog mDialog = directionDialog.create();
        mDialog.show();
    }

    public void showDirectionBox() {
        Builder builder = new Builder(Map506FragmentActivity.this);
        ListView modeList = new ListView(getApplicationContext());
        String distance = directionBox.remove(0);
        String time = directionBox.remove(0);
        ArrayAdapter<String> modeAdapter =
                new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.listItem, directionBox);
        modeList.setAdapter(modeAdapter);

        LinearLayout title = new LinearLayout(getApplicationContext());
        title.setOrientation(LinearLayout.VERTICAL);

        TextView l1 = new TextView(getApplicationContext());
        l1.setText(R.string.direction_guide);
        l1.setTextAppearance(getApplicationContext(), R.style.FFC_Theme_Light);
        l1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        l1.setTextColor(getResources().getColor(R.color.holo_blue_dark));
        TextView l2 = new TextView(getApplicationContext());
        l2.setText(distance + " " + time);
        l2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        l2.setTextColor(getResources().getColor(R.color.holo_orange_dark));

        title.addView(l1);
        title.addView(l2);

        title.setPadding(10, 10, 0, 10);

        builder.setView(modeList);
        builder.setCustomTitle(title);
        builder.create().show();
    }

    @Override public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl = null;
        switch (arg0) {
            case 0:
                Uri uri = Visit506_Person.CONTENT_URI;
                String PROJECTION[] = {
                        "visitno", "pid", "fname", "lname", "hcode", "hno", "xgis", "ygis", "villcode", "villno",
                        "latitude", "longitude", "status", "sickdatestart", "diseasenamethai", "villname", "radius",
                        "colorcode"
                };
                String where = "villno !=0 AND status IS NOT NULL AND code506 IS NOT NULL";
                if (!fillter.equals("")) {
                    where += fillter;
                }
                cl = new CursorLoader(getApplication(), uri, PROJECTION, where, null, "visit.visitno");
                break;
            case 1:
                Uri URIHOUSE = House.CONTENT_URI;
                String[] projection = { "hcode", "xgis", "ygis", "hno" };
                cl = new CursorLoader(getApplication(), URIHOUSE, projection, null, null, "hcode");
                break;
            default:
                break;
        }
        return cl;
    }

    @Override public void onLoadFinished(Loader<Cursor> l, Cursor c) {
        int id = l.getId();
        switch (id) {
            case 0:
                if (c.moveToFirst()) {
                    do {
                        setPlagueHome(c);
                    } while (c.moveToNext());
                }
                break;
            case 1:
                if (c.moveToFirst()) {
                    do {
                        String hcode = c.getString(c.getColumnIndex("hcode"));
                        String slat = c.getString(c.getColumnIndex("xgis"));
                        String slng = c.getString(c.getColumnIndex("ygis"));
//                        String slat = c.getString(c.getColumnIndex("ygis"));
//                        String slng = c.getString(c.getColumnIndex("xgis"));
                        String hno = c.getString(c.getColumnIndex("hno"));
                        if (!TextUtils.isEmpty(slat) && !TextUtils.isEmpty(slng)) {
                            Double lat = Double.parseDouble(slat);
                            Double lng = Double.parseDouble(slng);
                            tagHCODE.put(hcode, hcode);
                            HCODE.add(hcode);
                            markerPosition.put(hcode, new LatLng(lat, lng));
                            marker = myMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_black));
                            marker.setDraggable(true);
                            tagHome.put(marker, "dummy" + "," + hcode + "," + hno);
                            tagHomeMarker.put(hcode, marker);
                        }
                    } while (c.moveToNext());
                }
                break;
            default:
                break;
        }
        if (!searchPlace) {
            GoogleMapSearchPosition search = new GoogleMapSearchPosition(getApplicationContext());
            search.setReturnPosition(getPosition);
            search.execute();
            searchPlace = true;
        }
        linear_load.setVisibility(LinearLayout.GONE);
        progressBar.setVisibility(ProgressBar.GONE);
    }

    returnPosition getPosition = new returnPosition() {
        @Override public void returnPosition(LatLng position) {
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13.0f));
        }
    };

    @Override public void onLoaderReset(Loader<Cursor> arg0) {
    }

    private void setPlagueHome(Cursor c) {
        String fname = c.getString(c.getColumnIndex("fname"));
        String lname = c.getString(c.getColumnIndex("lname"));
        String hcode = c.getString(c.getColumnIndex("hcode"));
        if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("xgis"))) && !TextUtils.isEmpty(
                c.getString(c.getColumnIndex("ygis")))) {
            if (isNumeric(c.getString(c.getColumnIndex("xgis"))) && isNumeric(c.getString(c.getColumnIndex("ygis")))) {
//                Double lat = Double.parseDouble(c.getString(c.getColumnIndex("ygis")));
//                Double lng = Double.parseDouble(c.getString(c.getColumnIndex("xgis")));
                Double lat = Double.parseDouble(c.getString(c.getColumnIndex("xgis")));
                Double lng = Double.parseDouble(c.getString(c.getColumnIndex("ygis")));
                String hno = c.getString(c.getColumnIndex("hno"));
                String diseasenamethai = c.getString(c.getColumnIndex("diseasenamethai"));
                String sickdatestart = c.getString(c.getColumnIndex("sickdatestart"));
                String visitno = c.getString(c.getColumnIndex("visitno"));
                String[] temp = sickdatestart.split("-");
                int year = Integer.parseInt(temp[0]) + 543;
                int month = Integer.parseInt(temp[1]);
                int day = Integer.parseInt(temp[2]);
                sickdatestart = day + "/" + month + "/" + year;
                if (tagHomeMemberPlague.get(hcode) == null) {
                    HashMap<String, String> member = new HashMap<String, String>();
                    tagHomeMemberPlague.put(hcode, member);
                }
                if (TextUtils.isEmpty(listNamePlague.get(c.getString(c.getColumnIndex("diseasenamethai"))))) {
                    listNamePlague.put(c.getString(c.getColumnIndex("diseasenamethai")),
                            c.getString(c.getColumnIndex("diseasenamethai")));
                }
                if (!fname.equals(tagHomeMemberPlague.get(hcode).get(fname))) {
                    showCircle(c, visitno, lat, lng);
                    String detail = String.format(getString(R.string.address_506_string_format), visitno, hcode, hno,
                            c.getString(c.getColumnIndex("villno")), c.getString(c.getColumnIndex("villname")), fname,
                            lname, diseasenamethai, sickdatestart);
                    markerPosition.put(hcode, new LatLng(lat, lng));
                    marker = tagHomeMarker.get(hcode);

                    // set marker separate by color
                    /*	String level = c.getString(c.getColumnIndex("level"));
                        if(level.equals("1")){
							// YELLOW
							marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_orange));
						}
						else if(level.equals("2")){
							// ORANGE
							marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_orange));
						}
						else{
							// RED 
							marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_orange));
						}*/
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_orange));

                    marker.setDraggable(true);
                    tagHomeMarker.put(hcode, marker);
                    tagHome.put(marker, detail);
                    tagHomeMemberPlague.get(hcode).put(fname, fname);
                    //		listPersonDetail.add(hcode+","+c.getString(c.getColumnIndex("hno"))+","+c.getString(c.getColumnIndex("visitno"))+","+c.getString(c.getColumnIndex("villno"))+","+c.getString(c.getColumnIndex("villname")));
                    housePosition.put(hcode, new LatLng(lat, lng));
                    homeStatus.put(hcode, "1");
                    ERRORCODE.add(0);
                    tagPerson.put(fname + " " + lname, detail);
                }
            } else {
                listPersonDetail.put(fname + " " + lname,
                        hcode
                                + ","
                                + c.getString(c.getColumnIndex("hno"))
                                + ","
                                + c.getString(c.getColumnIndex("visitno"))
                                + ","
                                + c.getString(c.getColumnIndex("villno"))
                                + ","
                                + c.getString(c.getColumnIndex("villname")));
                housePosition.put(hcode, null);
                ERRORCODE.add(1);
            }
        } else {
            listPersonDetail.put(fname + " " + lname, hcode
                    + ","
                    + c.getString(c.getColumnIndex("hno"))
                    + ","
                    + c.getString(c.getColumnIndex("visitno"))
                    + ","
                    + c.getString(c.getColumnIndex("villno"))
                    + ","
                    + c.getString(c.getColumnIndex("villname")));
            housePosition.put(hcode, null);
            ERRORCODE.add(2);
        }
    }

    private void showCircle(Cursor c, String visitno, double lat, double lng) {
        if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("radius")))) {
            CircleOptions cirOptions = createCircle(new LatLng(lat, lng), c.getString(c.getColumnIndex("radius")),
                    c.getString(c.getColumnIndex("colorcode")));
            circleOptions.put(visitno, cirOptions);
            myMap.addCircle(cirOptions);
        }
    }

    private CircleOptions createCircle(LatLng circleCenter, String radius, String colorCode) {
        radiusCircle = Double.parseDouble(radius);
        String radiusSplit[] = colorCode.split("#");
        String areaColor = "#50" + radiusSplit[1];
        CircleOptions temps = new CircleOptions().center(circleCenter)
                .strokeWidth(3)
                .radius(radiusCircle)
                .strokeColor(Color.parseColor(colorCode))
                .fillColor(Color.parseColor(areaColor));
        return temps;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    PatientListCallback PatientCallback = new PatientListCallback() {
        @Override public void listCallBack(String hcode, String name) {
            if (housePosition.get(hcode) != null) {
                if (direction == 0) {
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(housePosition.get(hcode), 18.0f));
                    showBalloon(hcode, name);
                } else {
                    if (beginLocation != null) {
                        DirectionGen direction = new DirectionGen();
                        direction.setDirectionClick(mDirectionCallBack);
                        if (gps.checknetwork()) {
                            endLocation = housePosition.get(hcode);
                            direction.execute(beginLocation, endLocation);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.please_connect_internet, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                askAddPositionDialog(name);
            }
        }
    };

    String fullNameForWindowInfo;
    String visitNoForMarkerDrag;
    String tempVisitNoForMarkerDrag;
    HashMap<Marker, Boolean> hashMarkerWindowInfo;

    private void showBalloon(String hcode, String fullname) {
        fullNameForWindowInfo = fullname;
        Marker markerTemp = tagHomeMarker.get(hcode);
        hashMarkerWindowInfo.put(markerTemp, true);
        if (markerTemp != null) {
            markerTemp.showInfoWindow();
        }
    }

    private void askAddPositionDialog(final String name) {
        Builder builder = new Builder(this);
        builder.setTitle("ไม่พบตำแหน่งบ้าน");
        builder.setMessage("คุณต้องการเพิ่มต่ำแหน่งของบ้านหรือไม่");
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String detail[] = listPersonDetail.get(name).split(",");
                Intent a = new Intent(Action.INSERT);
                a.addCategory(Category.ADD_EDITPOSITION);
                a.putExtra("hno", detail[1]);
                a.putExtra("villno", detail[3]);
                a.putExtra("villname", detail[4]);
                a.putExtra("hcode", detail[0]);
                a.putExtra("visitno", detail[2]);
                startActivityForResult(a, 2);
                //	finish();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                // I do not need any action here you might
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        Toast.makeText(getApplicationContext(), "ไม่พบตำแหน่ง กรุณาตรวจสอบตำแหน่งในฐานข้อมูล", Toast.LENGTH_SHORT).show();
    }

    private void changeStateMaker(boolean state) {
        if (state) {
            for (int i = 0; i < HCODE.size(); i++) {
                String hcode = HCODE.get(i);
                if (TextUtils.isEmpty(homeStatus.get(hcode)) || !homeStatus.get(hcode).equals("1")) {
                    marker = tagHomeMarker.put(hcode, marker);
                    marker.setVisible(true);
                    marker = tagHomeMarker.put(hcode, marker);
                }
            }
        } else {
            for (int i = 0; i < HCODE.size(); i++) {
                String hcode = HCODE.get(i);
                if (TextUtils.isEmpty(homeStatus.get(hcode)) || !homeStatus.get(hcode).equals("1")) {
                    marker = tagHomeMarker.put(hcode, marker);
                    marker.setVisible(false);
                    marker = tagHomeMarker.put(hcode, marker);
                }
            }
        }
    }

    boolean gpsEnable;

    private void getLoaction() {
        gps = new GPSTracker(getApplicationContext());
        gpsEnable = gps.canGetLocation();
        if (gpsEnable) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude,
                    Toast.LENGTH_LONG).show();
            beginLocation = new LatLng(latitude, longitude);
        } else {
            settingGPS();
        }
    }

    private void settingGPS() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Map506FragmentActivity.this,android.R.style.Theme_Material_Light_Dialog_Alert);
        alertDialog.setTitle("ตั้งค่า GPS");
        alertDialog.setMessage("คุณไม่ได้เปิดใช้ GPS ต้องการเปิดใช้หรือไม่ ?");
        alertDialog.setPositiveButton("ตั้งค่า", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    String oldHcode;
    OnMarkerClickListener markerClick = new OnMarkerClickListener() {

        @Override public boolean onMarkerClick(Marker marker) {
            clickPerson = true;
            String temp[] = tagHome.get(marker).split(",");
            Intent house = new Intent(Action.VIEW);
            house.addCategory(Category.HOUSE);
            house.setType(House.CONTENT_ITEM_TYPE);
            house.setData(Uri.withAppendedPath(House.CONTENT_URI, "" + temp[1]));
            startActivity(house);
            return true;
        }
    };

    OnMarkerDragListener makerLongClick = new OnMarkerDragListener() {
        String hcode[];
        Marker tempMarker;
        String radius = "";
        String visitno;

        @Override public void onMarkerDragStart(Marker marker) {
            hcode = tagHome.get(marker).split(",");
            String LatLng = marker.getPosition().latitude + "," + marker.getPosition().longitude;
            String status = homeStatus.get(hcode[1]);
            String detail = tagHome.get(marker);
            hcode = detail.split(",");
            if (!hashMarkerWindowInfo.isEmpty() && hashMarkerWindowInfo.get(marker) != null && hashMarkerWindowInfo.get(
                    marker)) {
                radius = circleOptions.get(visitNoForMarkerDrag) != null ? circleOptions.get(visitNoForMarkerDrag)
                        .getRadius() + "" : "0";
                visitno = visitNoForMarkerDrag;
                marker.showInfoWindow();
            } else {
                if (circleOptions.get(hcode[0]) != null) {
                    visitno = hcode[0];
                    radius = circleOptions.get(hcode[0]) != null ? circleOptions.get(hcode[0]).getRadius() + "" : "0";
                } else if (!TextUtils.isEmpty(status) && status.equals("1")) {
                    visitno = hcode[0];
                    radius = "0";
                }
            }
            clickPerson = true;
            Intent a = new Intent(Action.EDIT);
            a.putExtra("status", status);
            a.putExtra("detail", detail);
            a.putExtra("visitno", visitno);
            a.putExtra("LatLng", LatLng);
            a.putExtra("radius", radius);
            a.putExtra("result", 1);
            a.addCategory(Category.ADD_EDITPOSITION);
            startActivityForResult(a, 1);
        }

        @Override public void onMarkerDragEnd(Marker marker) {
            hcode = tagHome.get(marker).split(",");
            tempMarker = tagHomeMarker.get(hcode[1]);
            LatLng position = markerPosition.get(hcode[1]);
            tempMarker.setPosition(position);
            tagHomeMarker.put(hcode[1], tempMarker);
        }

        @Override public void onMarkerDrag(Marker marker) {

        }
    };

    InfoWindowAdapter markerInfo = new InfoWindowAdapter() {
        @Override public View getInfoContents(Marker marker) {
            return null;
        }

        @Override public View getInfoWindow(Marker marker) {
            View v = getLayoutInflater().inflate(R.layout.map506_info_window, null);
            TextView txthno = (TextView) v.findViewById(R.id.hno);
            String tempTag[] = null;
            if (fullNameForWindowInfo != null) {
                tempTag = tagPerson.get(fullNameForWindowInfo).split(",");
            }
            if (!TextUtils.isEmpty(homeStatus.get(tempTag[1])) && homeStatus.get(tempTag[1]).equals("1")) {
                String detail = "เลขที่ :" + tempTag[2]+"\n"+"ชื่อ :" + tempTag[3]+"\n"+"ป่วยเป็นโรค :" + tempTag[4]+"\n"+"เริ่มป่วยวันที่ :" + tempTag[5];
                txthno.setText(detail);
                visitNoForMarkerDrag = tempTag[0];
                return v;
            } else {
                return v;
            }
        }
    };

    OnInfoWindowClickListener markerInfoClick = new OnInfoWindowClickListener() {

        @Override public void onInfoWindowClick(Marker marker) {
            String temp[] = tagHome.get(marker).split(",");
            Intent house = new Intent(Action.VIEW);
            house.addCategory(Category.HOUSE);
            house.setType(House.CONTENT_ITEM_TYPE);
            house.setData(Uri.withAppendedPath(House.CONTENT_URI, "" + temp[1]));
            startActivity(house);
        }
    };
    private Polyline lineDirection;

    directionCallBack mDirectionCallBack = new directionCallBack() {
        @Override public void getDirection(ArrayList<LatLng> latlng, ArrayList<String> direction) {
            directionBox = direction;
            if (lineDirection != null) {
                lineDirection.remove();
            }
            if (latlng != null) {
                PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);
                for (int i = 0; i < latlng.size(); i++) {
                    rectLine.add(latlng.get(i));
                }
                lineDirection = myMap.addPolyline(rectLine);
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_connect_internet, Toast.LENGTH_SHORT).show();
            }
        }
    };

    settingCallBack callBack = new settingCallBack() {
        @Override
        public void settingCallBack(boolean chkSwitch, String queryNamePlauge, String dateStart, String dateEnd,
                int displaymode) {
            if (TextUtils.isEmpty(queryNamePlauge) && TextUtils.isEmpty(dateStart) && TextUtils.isEmpty(dateEnd)) {
                if (TextUtils.isEmpty(fillter)) {
                    fillter = "";
                    PatientDialogFragment.settingPlaugeQuery(fillter);
                    PatientDialogFragment.settingTimeQuery(fillter);
                } else {
                    fillter = "";
                    PatientDialogFragment.settingPlaugeQuery(fillter);
                    PatientDialogFragment.settingTimeQuery(fillter);
                    newQuery();
                }
            } else {
                if (!TextUtils.isEmpty(queryNamePlauge)) {
                    fillter = " AND (diseasenamethai =" + "\"" + queryNamePlauge + "\")";
                    PatientDialogFragment.settingPlaugeQuery(fillter);
                } else {
                    fillter = "";
                    PatientDialogFragment.settingPlaugeQuery(fillter);
                }
                if (!TextUtils.isEmpty(dateStart) && !TextUtils.isEmpty(dateEnd)) {
                    if (!fillter.equals("")) {
                        fillter += " AND (date(sickdatestart) >= "
                                + "\""
                                + dateStart
                                + "\")"
                                + " AND (date(sickdatestart) <= "
                                + "\""
                                + dateEnd
                                + "\")";
                    } else {
                        fillter = " AND (date(sickdatestart) >= "
                                + "\""
                                + dateStart
                                + "\")"
                                + " AND (date(sickdatestart) <= "
                                + "\""
                                + dateEnd
                                + "\")";
                    }
                    PatientDialogFragment.settingTimeQuery(fillter);
                }
                newQuery();
            }
            changeStateMaker(chkSwitch);
            setDiaplay(displaymode);
        }
    };

    public void newQuery() {
        myMap.clear();
        init();
        linear_load.setVisibility(LinearLayout.VISIBLE);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        getLoaderManager().restartLoader(0, null, this);
        getLoaderManager().restartLoader(1, null, this);
    }

    @Override protected void onResume() {
        super.onResume();
        if (clickPerson) {
            myMap.clear();
            init();
            linear_load.setVisibility(LinearLayout.VISIBLE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
            getLoaderManager().restartLoader(0, null, this);
            getLoaderManager().restartLoader(1, null, this);
            clickPerson = false;
        }
    }

    private void searhPlace(){
        AlertDialog.Builder searchBuilder = new AlertDialog.Builder(
                Map506FragmentActivity.this,android.R.style.Theme_Material_Light_Dialog_Alert);
        View sv = LayoutInflater.from(this).inflate(
                R.layout.google_map_search_place, null);

        final EditText searchPlace = (EditText)sv.findViewById(R.id.searchP_et);
        searchBuilder.setTitle(R.string.find_places);
        searchBuilder.setView(sv);
        searchBuilder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!checkNetwork()){

                }
                else if (TextUtils.isEmpty(searchPlace.getText())) {
                    Toast.makeText(getApplication(), R.string.please_define_keyword_before_search,
                            Toast.LENGTH_SHORT).show();
                }else{
                    Geocoder geoCoder = new Geocoder(getApplication(),new Locale("th"));
                    String searchStr = searchPlace.getText().toString();
                    try {
                        final List<android.location.Address> addresses = geoCoder.getFromLocationName(searchStr, 10);
                        if (addresses.size() >  0) {
                            AlertDialog.Builder foundDialog = new AlertDialog.Builder(
                                    Map506FragmentActivity.this,android.R.style.Theme_Material_Light_Dialog_Alert);
                            foundDialog.setIcon(R.drawable.ic_launcher);
                            foundDialog.setTitle(String.format(getString(R.string.search_place_result_of), searchStr));
                            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    getApplication(),
                                    R.layout.list_item);

                            for(int i=0;i<addresses.size();i++){
                                String addressStr = null;
                                for(int j=0;j<addresses.get(i).getMaxAddressLineIndex();j++){
                                    if(j==0){
                                        addressStr = addresses.get(i).getAddressLine(j)+"\n";
                                    }else addressStr += addresses.get(i).getAddressLine(j)+" ";
                                }
                                if(!TextUtils.isEmpty(addressStr)){
                                    arrayAdapter.add(addressStr.replace("null", ""));
                                }else arrayAdapter.add(searchStr);

                            }

                            foundDialog.setAdapter(arrayAdapter,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addresses.get(which);
                                            double latitude = addresses.get(which).getLatitude();
                                            double longitude = addresses.get(which).getLongitude();
                                            final LatLng zone = new LatLng(latitude, longitude);
                                            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(zone,18));
                                        }
                                    });
                            foundDialog.show();
                        }else {
                            Builder notFound = new Builder(Map506FragmentActivity.this);
                            notFound.setIcon(getApplication().getResources().getDrawable(R.drawable.ic_action_add));
                            notFound.setTitle(R.string.error);
                            notFound.setMessage(R.string.place_not_found);
                            notFound.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
        });
        AlertDialog mDialog = searchBuilder.create();
        mDialog.show();
    }

    public boolean checkNetwork() {
        final CheckNetwork chk = new CheckNetwork(getApplicationContext());
        boolean check = chk.isNetworkAvailable();
        if (!chk.isNetworkAvailable()) {
            Builder noNetworkDialog = new Builder(Map506FragmentActivity.this);
            noNetworkDialog.setIcon(getApplication().getResources().getDrawable(R.drawable.ic_action_add));
            noNetworkDialog.setTitle(R.string.error);
            noNetworkDialog.setMessage(R.string.please_connect_internet);
            noNetworkDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                }
            });
            noNetworkDialog.show();
        }
        return check;
    }

    @Override protected void onActivityResult(int request, int result, Intent intent) {
        super.onActivityResult(request, result, intent);
        if (request == 2) {
            if (result == RESULT_OK) {
                myMap.clear();
                init();
                linear_load.setVisibility(LinearLayout.VISIBLE);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                getLoaderManager().restartLoader(0, null, this);
                getLoaderManager().restartLoader(1, null, this);
            }
        } else {
            if (result == RESULT_OK) {
                myMap.clear();
                init();
                linear_load.setVisibility(LinearLayout.VISIBLE);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                getLoaderManager().restartLoader(0, null, this);
                getLoaderManager().restartLoader(1, null, this);
            }
        }
    }
}

