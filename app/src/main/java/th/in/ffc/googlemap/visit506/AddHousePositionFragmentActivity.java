package th.in.ffc.googlemap.visit506;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.googlemap.lib.GoogleMapSearchPosition;
import th.in.ffc.googlemap.lib.GoogleMapSearchPosition.returnPosition;
import th.in.ffc.googlemap.visit506.AskRadiusDialogFragment.onDialogListener;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.PersonProvider.FFC506RADIUS;
import th.in.ffc.util.CheckNetwork;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddHousePositionFragmentActivity extends FFCFragmentActivity {
    private GoogleMap myMap;
    private String LatLng;
    private String hcode;
    private String status;
    private LatLng newHousePosition;
    private Marker marker;
    private String mode;
    private String visitno;
    private String radius;
    private String colorcode;
    private boolean commitclick;
    int levelSet;
    String radiusSet;
    private String tempColorCode;
    private String tempRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map_addposition);
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment myMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
        commitclick = false;
        myMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override public void onMapReady(GoogleMap googleMap) {
                myMap = googleMap;
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                myMap.setOnMarkerDragListener(drag);
                hcode = getIntent().getExtras().getString("hcode");
                mode = getIntent().getExtras().getString("mode");
            }
        });

        radius = null;
        colorcode = null;
        if (mode.equals("1")) {
            LatLng = getIntent().getExtras().getString("LatLng");
            String temp[] = LatLng.split(",");
            double lat = Double.parseDouble(temp[0]);
            double lng = Double.parseDouble(temp[1]);
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    lat, lng), 15.0f));
            onEditPosition();
        } else {
            myMap.setOnMapLongClickListener(longClick);
            visitno = getIntent().getExtras().getString("visitno");
            LatLng = getIntent().getExtras().getString("LatLng");
            String getRadiusDetail = getIntent().getExtras().getString("detailRatius");
            if (!TextUtils.isEmpty(LatLng)) {
                String temp[] = LatLng.split(",");
                double lat = Double.parseDouble(temp[0]);
                double lng = Double.parseDouble(temp[1]);
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        lat, lng), 15.0f));
                marker = myMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_orange));
                marker.setDraggable(true);
                newHousePosition = new LatLng(lat, lng);
                if (!TextUtils.isEmpty(getRadiusDetail)) {
                    String[] radiusDetail = getRadiusDetail.split(",");
                    createCircle(radiusDetail[1], radiusDetail[2]);
                    radiusSet = radiusDetail[1];
                    tempColorCode = radiusDetail[2];
                    tempRadius = radiusDetail[1];
                    levelSet = Integer.parseInt(radiusDetail[0]);
                }
            } else {
                GoogleMapSearchPosition searchPlace = new GoogleMapSearchPosition(getApplicationContext());
                searchPlace.setReturnPosition(getPosition);
                searchPlace.execute();
            }

        }
    }

    returnPosition getPosition = new returnPosition() {

        @Override
        public void returnPosition(LatLng position) {
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 18.0f));
        }
    };

    private void onEditPosition() {
        LatLng = getIntent().getExtras().getString("LatLng");
        String temp[] = LatLng.split(",");
        double lat = Double.parseDouble(temp[0]);
        double lng = Double.parseDouble(temp[1]);
        newHousePosition = new LatLng(lat, lng);
        marker = myMap
                .addMarker(new MarkerOptions().position(newHousePosition));
        marker.setDraggable(true);
        if (TextUtils.isEmpty(status) || status.equals("0")) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_green));
        } else {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_orange));
        }
    }

    OnMapLongClickListener longClick = new OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng point) {
            myMap.clear();
            marker = myMap.addMarker(new MarkerOptions().position(point));
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_orange));
            marker.setDraggable(true);
            newHousePosition = point;
            createCircle(tempRadius, tempColorCode);
        }
    };

    OnMarkerDragListener drag = new OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {
            newHousePosition = marker.getPosition();
            createCircle(tempRadius, tempColorCode);

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            newHousePosition = marker.getPosition();
            createCircle(tempRadius, tempColorCode);
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            newHousePosition = marker.getPosition();
            createCircle(tempRadius, tempColorCode);

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem commit = menu.add(Menu.NONE, 3, Menu.NONE, "commit");
        commit.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        commit.setIcon(R.drawable.ic_action_done);
        MenuItem searchPlace = menu.add(Menu.NONE, 4, Menu.NONE, R.string.find_places);
        searchPlace.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchPlace.setIcon(R.drawable.ic_action_search);
        MenuItem inputradius = menu.add(Menu.NONE, 2, Menu.NONE, "setradius");
        inputradius.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        inputradius.setIcon(R.drawable.ic_action_setting);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 3:
                if (mode.equals("1")) {
                    upDateDBEditMode();
                } else {
                    if (radius == null && colorcode == null) {
                        if (newHousePosition != null) {
                            askDialogRadius();
                            commitclick = true;
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.plsaddposition), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        upDateDBEditMode();
                    }
                }
                return true;
            case 2:
                commitclick = false;
                askDialogRadius();
                return true;
            case 4:
                searhPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    AskRadiusDialogFragment askRadiusDialogFragment;

    @SuppressWarnings("static-access")
    private void askDialogRadius() {
        if (askRadiusDialogFragment == null) {
            askRadiusDialogFragment = askRadiusDialogFragment.newInstance(radiusSet, levelSet);
            askRadiusDialogFragment.setOnDialogListener(listener);
        }
        askRadiusDialogFragment.show(getSupportFragmentManager(), "patientList");
    }

/*	private void askCommitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle(getString(R.string.commitdata));
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				upDateDBEditMode();
			}
		});
		builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}*/

    private void upDateDBEditMode() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("xgis", newHousePosition.longitude);
        contentValues.put("ygis", newHousePosition.latitude);
        String where = "hcode =?";
        String selectionArgs[] = {hcode};
        ContentResolver conResolver = getContentResolver();
        conResolver.update(House.CONTENT_URI, contentValues, where,
                selectionArgs);

        contentValues = new ContentValues();
        contentValues.put("visitno", visitno);
        contentValues.put("radius", radius);
        contentValues.put("colorcode", colorcode);
        contentValues.put("level", levelSet);
        conResolver = getContentResolver();
        String where1 = "visitno =?";
        String selectionArgs1[] = {visitno};
        conResolver.update(FFC506RADIUS.CONTENT_URI, contentValues, where1, selectionArgs1);
        Toast.makeText(getApplicationContext(), "HCODE UPDATE " + hcode, Toast.LENGTH_SHORT).show();
        this.finish();
    }

    Circle circle;

    private void createCircle(String radius, String colorcode) {
        if (newHousePosition != null) {
            if (!TextUtils.isEmpty(radius) && !TextUtils.isEmpty(colorcode)) {
                if (circle != null) {
                    circle.remove();
                }
                float setRadius = Float.parseFloat(radius);
                String radiusSplit[] = colorcode.split("#");
                String areaColor = "#50" + radiusSplit[1];
                circle = myMap.addCircle(new CircleOptions()
                        .center(newHousePosition).strokeWidth(3)
                        .radius(setRadius)
                        .strokeColor(Color.parseColor(colorcode))
                        .fillColor(Color.parseColor(areaColor)));
                if (commitclick) {
                    upDateDBEditMode();
                }
            } else {
                commitclick = false;
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.please_add_house_location, Toast.LENGTH_SHORT).show();
        }

    }

    onDialogListener listener = new onDialogListener() {
        @Override
        public void onDialogListener(String radius1, String colorcode1, int level) {
            radius = radius1;
            colorcode = colorcode1;
            levelSet = level;
            createCircle(radius1, colorcode1);
            tempRadius = radius1;
            tempColorCode = colorcode1;
        }
    };

    private void searhPlace() {
        final CheckNetwork chk = new CheckNetwork(getApplicationContext());
        Builder searchBuilder = new Builder(
                AddHousePositionFragmentActivity.this);
        View sv = LayoutInflater.from(this).inflate(
                R.layout.google_map_search_place, null);

        final EditText searchPlace = (EditText) sv.findViewById(R.id.searchP_et);
        searchBuilder.setTitle(R.string.find_places);
        searchBuilder.setView(sv);
        searchBuilder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!chk.isNetworkAvailable()) {
                    Builder noNetworkDialog = new Builder(AddHousePositionFragmentActivity.this);
                    noNetworkDialog.setIcon(getApplication().getResources().getDrawable(R.drawable.ic_action_add));
                    noNetworkDialog.setTitle(R.string.error);
                    noNetworkDialog.setMessage(R.string.please_connect_internet);
                    noNetworkDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    noNetworkDialog.show();

                } else if (TextUtils.isEmpty(searchPlace.getText())) {
                    Toast.makeText(getApplication(), R.string.please_define_keyword_before_search,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Geocoder geoCoder = new Geocoder(getApplication(), new Locale("th"));
                    String searchStr = searchPlace.getText().toString();
                    try {
                        final List<android.location.Address> addresses = geoCoder.getFromLocationName(searchStr, 10);
                        if (addresses.size() > 0) {
                            Builder foundDialog = new Builder(
                                    AddHousePositionFragmentActivity.this);
                            foundDialog.setIcon(R.drawable.ic_launcher);
                            foundDialog.setTitle(String.format(getString(R.string.search_place_result_of), searchStr));
                            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    getApplication(),
                                    R.layout.list_item);

                            for (int i = 0; i < addresses.size(); i++) {
                                String addressStr = null;
                                for (int j = 0; j < addresses.get(i).getMaxAddressLineIndex(); j++) {
                                    if (j == 0) {
                                        addressStr = addresses.get(i).getAddressLine(j) + "\n";
                                    } else addressStr += addresses.get(i).getAddressLine(j) + " ";
                                }
                                if (!TextUtils.isEmpty(addressStr)) {
                                    arrayAdapter.add(addressStr.replace("null", ""));
                                } else arrayAdapter.add(searchStr);

                            }

                            foundDialog.setAdapter(arrayAdapter,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addresses.get(which);
                                            double latitude = addresses.get(which).getLatitude();
                                            double longitude = addresses.get(which).getLongitude();
                                            final LatLng zone = new LatLng(latitude, longitude);
                                            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(zone, 18));
                                        }
                                    });
                            foundDialog.show();
                        } else {
                            Builder notFound = new Builder(AddHousePositionFragmentActivity.this);
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
}
