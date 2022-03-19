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
import th.in.ffc.googlemap.directions.GPSTracker;
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

public class Map506DetailFragmentActivity extends FFCFragmentActivity implements LoaderCallbacks<Cursor> {

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
    private ArrayList<String> namePlague;
    private HashMap<String, String> tagPerson;
    private HashMap<String, LatLng> housePosition;
    // variable for update db
    private ArrayList<Integer> ERRORCODE;
    private Double radiusCircle;
    private Map506SettingDialogFragment myDialogFragment;

    private int direction;
    private GPSTracker gps;
    private LatLng beginLocation;
    private LatLng endLocation;
    private boolean hasEditHouse;
    private ProgressBar progressBar;

    LinearLayout linear_load;
    private String fillter;
    String pid;
    String pcucodeperson;

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

        fillter = "";
        getLoaderManager().initLoader(0, null, this);
        getLoaderManager().initLoader(1, null, this);
        Intent a = getIntent();
        pid = a.getExtras().getString("pid");
        pcucodeperson = a.getExtras().getString("pcucodeperson");
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
        namePlague = new ArrayList<String>();
        ERRORCODE = new ArrayList<Integer>();
        circleOptions = new HashMap<String, CircleOptions>();
        radiusCircle = 0.0;
        direction = 1;
        listNamePlague = new HashMap<String, String>();
        //	circle = new HashMap<String, Circle>();
        markerPosition = new HashMap<String, LatLng>();
        tagPerson = new HashMap<String, String>();
        hasEditHouse = false;
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
        MenuItem searchPlace = menu.add(Menu.NONE, 4, Menu.NONE, R.string.find_places);
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
                    myDialogFragment.show(getFragmentManager(), "settingDialogFragment");
                }
                return true;
            case 2:
                if (patient == null) {
                    patient = PatientDialogFragment.newInstance();
                    patient.setOnClickListenerCallBack(PatientCallback);
                    patient.show(getSupportFragmentManager(), "patientList");
                } else {
                    patient.show(getSupportFragmentManager(), "patientList");
                }
                direction = 0;
                return true;
            case 3:
                direction = 1;
                getLoaction();
                if (gpsEnable) {
                    if (patient == null) {
                        patient = PatientDialogFragment.newInstance();
                        patient.setOnClickListenerCallBack(PatientCallback);
                        patient.show(getSupportFragmentManager(), "patientList");
                    } else {
                        patient.show(getSupportFragmentManager(), "patientList");
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
                String where = "villno !=0 AND status IS NOT NULL";
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
                        String slat = c.getString(c.getColumnIndex("ygis"));
                        String slng = c.getString(c.getColumnIndex("xgis"));
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
        Intent a = getIntent();
        Double lat = a.getExtras().getDouble("lat");
        Double lng = a.getExtras().getDouble("lng");
        LatLng animate = new LatLng(lat, lng);
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(animate, 17.0f));
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
                Double lat = Double.parseDouble(c.getString(c.getColumnIndex("ygis")));
                Double lng = Double.parseDouble(c.getString(c.getColumnIndex("xgis")));
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
                    String detail = visitno
                            + ","
                            + hcode
                            + ","
                            + hno
                            + " หมู่"
                            + c.getString(c.getColumnIndex("villno"))
                            + " "
                            + c.getString(c.getColumnIndex("villname"))
                            + ","
                            + fname
                            + " "
                            + lname
                            + ","
                            + diseasenamethai
                            + ","
                            + sickdatestart;
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
                    namePlague.add(hcode + ":" + fname + " " + lname + " " + "," + c.getString(
                            c.getColumnIndex("diseasenamethai")) + "," + c.getString(
                            c.getColumnIndex("sickdatestart")));
                    housePosition.put(hcode, new LatLng(lat, lng));
                    homeStatus.put(hcode, "1");
                    ERRORCODE.add(0);
                    tagPerson.put(fname + " " + lname, detail);
                }
            } else {
                namePlague.add(hcode
                        + ":"
                        + fname
                        + " "
                        + lname
                        + " "
                        + ","
                        + c.getString(c.getColumnIndex("diseasenamethai"))
                        + ","
                        + c.getString(c.getColumnIndex("sickdatestart")));
                housePosition.put(hcode, null);
                ERRORCODE.add(1);
            }
        } else {
            namePlague.add(hcode
                    + ":"
                    + fname
                    + " "
                    + lname
                    + " "
                    + ","
                    + c.getString(c.getColumnIndex("diseasenamethai"))
                    + ","
                    + c.getString(c.getColumnIndex("sickdatestart")));
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
                }
            } else {
                askAddPositionDialog(hcode);
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
			/*String hcode[] = tagHome.get(markerTemp).split(",");
			if(circle.get(oldHcode)!=null){
				circle.get(oldHcode).remove();
			}
			if(circleOptions.get(hcode[1]) !=null){
				Circle tempCir = myMap.addCircle(circleOptions.get(hcode[1]));
				circle.put(hcode[1], tempCir);
				oldHcode = hcode[1];
			}*/
        }
    }

    private void askAddPositionDialog(String hcode) {
		/*AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title + namePlague.get(position));
		builder.setMessage("�س��ͧ�����������˹觢ͧ :"
				+ namePlague.get(position));
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String visitnoIntent = visitno.get(position);
				Intent addPosition = new Intent(Action.MAP506ADDPOSITION);
				addPosition.addCategory(Category.MAP506_ADDPOSITION);
				addPosition.putExtra("visitno", visitnoIntent);
				startActivity(addPosition);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// I do not need any action here you might
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();*/
        Toast.makeText(getApplicationContext(), "��辺���˹� ��سҵ�Ǩ�ͺ���˹�㹰ҹ������", Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Map506DetailFragmentActivity.this);
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
            String detail = tagHome.get(marker);
            hcode = detail.split(",");
            if (!hashMarkerWindowInfo.isEmpty() && hashMarkerWindowInfo.get(marker) != null && hashMarkerWindowInfo.get(
                    marker)) {
                radius = circleOptions.get(visitNoForMarkerDrag).getRadius() + "";
                visitno = visitNoForMarkerDrag;
                marker.showInfoWindow();
            } else {
                if (circleOptions.get(hcode[0]) != null) {
                    visitno = hcode[0];
                    radius = circleOptions.get(hcode[0]).getRadius() + "";
                }
            }
            hasEditHouse = true;
            hcode = tagHome.get(marker).split(",");
            String LatLng = marker.getPosition().latitude + "," + marker.getPosition().longitude;
            String status = homeStatus.get(hcode[1]);
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
                String detail = String.format(getString(R.string.patient_info), tempTag[2],
                        tempTag[3], tempTag[4], tempTag[5]);
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

    /*directionCallBack mDirectionCallBack = new directionCallBack(){
        @Override
        public void getDirection(ArrayList<LatLng> latlng) {
            if(lineDirection!=null){
                lineDirection.remove();
            }
            if(latlng!=null){
                PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);
                for(int i = 0 ; i < latlng.size() ; i++){
                    rectLine.add(latlng.get(i));
                }
                lineDirection = myMap.addPolyline(rectLine);
            }
            else{
                Toast.makeText(getApplicationContext(), "��س����������Թ����๵", Toast.LENGTH_SHORT).show();
            }
        }
    };
*/ settingCallBack callBack = new settingCallBack() {
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
        if (hasEditHouse) {

        }
    }

    private void searhPlace() {
        final CheckNetwork chk = new CheckNetwork(getApplicationContext());
        Builder searchBuilder = new Builder(Map506DetailFragmentActivity.this);
        View sv = LayoutInflater.from(this).inflate(R.layout.google_map_search_place, null);

        final EditText searchPlace = (EditText) sv.findViewById(R.id.searchP_et);
        searchBuilder.setTitle(R.string.find_places);
        searchBuilder.setView(sv);
        searchBuilder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                if (!chk.isNetworkAvailable()) {
                    Builder noNetworkDialog = new Builder(Map506DetailFragmentActivity.this);
                    noNetworkDialog.setIcon(getApplication().getResources().getDrawable(R.drawable.ic_action_add));
                    noNetworkDialog.setTitle(R.string.error);
                    noNetworkDialog.setMessage(R.string.please_connect_internet);
                    noNetworkDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    noNetworkDialog.show();
                } else if (TextUtils.isEmpty(searchPlace.getText())) {
                    Toast.makeText(getApplication(), R.string.please_define_keyword_before_search, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Geocoder geoCoder = new Geocoder(getApplication(), new Locale("th"));
                    String searchStr = searchPlace.getText().toString();
                    try {
                        final List<android.location.Address> addresses = geoCoder.getFromLocationName(searchStr, 10);
                        if (addresses.size() > 0) {
                            Builder foundDialog = new Builder(Map506DetailFragmentActivity.this);
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
                            Builder notFound = new Builder(Map506DetailFragmentActivity.this);
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
        });
        AlertDialog mDialog = searchBuilder.create();
        mDialog.show();
    }

    @Override protected void onActivityResult(int request, int result, Intent intent) {
        super.onActivityResult(request, result, intent);
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

