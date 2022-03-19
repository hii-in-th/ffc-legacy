package th.in.ffc.googlemap.visit506;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.googlemap.lib.GoogleMapSearchPosition;
import th.in.ffc.googlemap.lib.GoogleMapSearchPosition.returnPosition;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.util.CheckNetwork;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditPostionHomeFragmentActivity extends FFCFragmentActivity {

    private GoogleMap myMap;
    private String LatLng;
    private String hcode;
    private String status;
    private LatLng newHousePosition;
    private Marker marker;
    private String mode;
    private ProgressBar progressBar;
    LinearLayout linear_load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map506_fragment_activity);
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment myMapFragment = (SupportMapFragment) myFragmentManager
                .findFragmentById(R.id.map);
        myMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override public void onMapReady(GoogleMap googleMap) {
                googleMap.setOnMarkerDragListener(drag);
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                myMap = googleMap;
            }
        });
        hcode = getIntent().getExtras().getString("hcode");
        mode = getIntent().getExtras().getString("mode");
        status = getIntent().getExtras().getString("status");
        progressBar = (ProgressBar) findViewById(R.id.nowLoading);
        linear_load = (LinearLayout) findViewById(R.id.linear_load);
        progressBar.setVisibility(ProgressBar.GONE);
        linear_load.setVisibility(LinearLayout.GONE);
        if (mode.equals("1")) {
            LatLng = getIntent().getExtras().getString("LatLng");
            String temp[] = LatLng.split(",");
            double lat = Double.parseDouble(temp[0]);
            double lng = Double.parseDouble(temp[1]);
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    lat, lng), 18.0f));
            onEditPosition();
        } else {
            GoogleMapSearchPosition searchPlace = new GoogleMapSearchPosition(getApplicationContext());
            searchPlace.setReturnPosition(getPosition);
            myMap.setOnMapLongClickListener(longClick);

        }
    }

    returnPosition getPosition = new returnPosition() {
        @Override
        public void returnPosition(LatLng position) {
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 8.0f));
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
            marker.setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.house_green));
        } else {
            marker.setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.house_orange));
        }

    }

    OnMapLongClickListener longClick = new OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng point) {
            myMap.clear();
            marker = myMap.addMarker(new MarkerOptions().position(point));
            marker.setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.house_orange));
            marker.setDraggable(true);
            newHousePosition = point;
        }
    };

    OnMarkerDragListener drag = new OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            newHousePosition = marker.getPosition();
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            // TODO Auto-generated method stub

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 3:
                if (mode.equals("1")) {
                    upDateDBEditMode();
                } else {
                    upDateDBEditMode();
                }
                return true;
            case 4:
                searhPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//	AskRadiusDialogFragment askRadiusDialogFragment;
    /*private void askDialogRadius(){
		askRadiusDialogFragment = askRadiusDialogFragment.newInstance();
		askRadiusDialogFragment.setOnDialogListener(listener);
		askRadiusDialogFragment.show(getSupportFragmentManager(), "patientList");
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
        onBackPressed();
    }
/*	onDialogListener listener = new onDialogListener(){

		@Override
		public void onDialogListener(String radius, String colorcode) {


		}
	};*/

    private void searhPlace() {
        final CheckNetwork chk = new CheckNetwork(getApplicationContext());
        Builder searchBuilder = new Builder(
                EditPostionHomeFragmentActivity.this);
        View sv = LayoutInflater.from(this).inflate(
                R.layout.google_map_search_place, null);

        final EditText searchPlace = (EditText) sv.findViewById(R.id.searchP_et);
        searchBuilder.setTitle(R.string.find_places);
        searchBuilder.setView(sv);
        searchBuilder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!chk.isNetworkAvailable()) {
                    Builder noNetworkDialog = new Builder(EditPostionHomeFragmentActivity.this);
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
                                    EditPostionHomeFragmentActivity.this);
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
                            Builder notFound = new Builder(EditPostionHomeFragmentActivity.this);
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
