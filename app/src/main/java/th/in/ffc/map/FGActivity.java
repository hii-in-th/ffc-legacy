package th.in.ffc.map;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import org.osmdroid.google.wrapper.MyLocationOverlay;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.HashMap;

import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.map.map.FGMapManager;
import th.in.ffc.map.preference.PreferenceFilter;
import th.in.ffc.map.service.GeneralAsyncTask;
import th.in.ffc.map.service.ProcessingFilterAsyncTask;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.value.FinalValue;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.person.genogram.V1.Family;
import th.in.ffc.person.genogram.V1.FamilyTree;
import th.in.ffc.provider.HouseProvider.House;

public class FGActivity extends FFCFragmentActivity {

    public static String EXTRA_HCODE = "hcode";

    public static FGSystemManager fgsys = null;
    private static String PICTURE_PATH = null;
    private static String TEMP_PATH = null;
    private static String DATA_PATH = null;
    private ConnectivityReceiver conRec;

    public static final int INITIALIZE = 0;
    public static final int LOCATION_INITIALIZE = 1;
    public static final int UPDATE_WIFI_ICON = 2;
    public static final int UPDATE_GPS_ICON = 3;
    public static final int FAILED = -1;

    private MenuItem wifi_item;
    private MenuItem gps_item;

    private Handler handler;

    public static boolean filter_enabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setSupportProgressBarIndeterminateVisibility(false);

        Answers.getInstance().logContentView(new ContentViewEvent()
            .putContentId(getPcuCode())
            .putContentType("Map"));

        DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Android/data/" + this.getPackageName() + "/";
        PICTURE_PATH = DATA_PATH + "pictures/";
        TEMP_PATH = DATA_PATH + "temps/";

        this.setContentView(R.layout.main_maps_activity);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (fgsys == null)
                    return;

                switch (msg.what) {
                    case INITIALIZE:
                        if (conRec == null) {
                            Log.d("TAG!", "conRec is null!");
                            conRec = new ConnectivityReceiver();
                            registerReceiver(conRec, new IntentFilter(
                                "android.net.conn.CONNECTIVITY_CHANGE"));

                            //TODO Change to Google Map
//                            final MyLocationOverlay lo = fgsys.getFGMapManager()
//                                    .getMyLocationOverlay();
//                            lo.runOnFirstFix(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.d("TAG!", "RUN!");
//                                    fgsys.getFGGPSManager().regenerateProvider();
//                                    handler.sendEmptyMessage(LOCATION_INITIALIZE);
//                                }
//                            });
//                            handler.sendEmptyMessage(LOCATION_INITIALIZE);
//
//                            fgsys.getFGMapManager().getMyLocationOverlay()
//                                .enableMyLocation();
                        }

                        fgsys.getFGMapManager().checkGPS();
                        break;
                    case LOCATION_INITIALIZE:
                        GeoPoint point = fgsys.getFGGPSManager()
                            .getGeoPointCurrent();
                        if (point == null)
                            point = FinalValue.GEOPOINT_VICTORY;
                        fgsys.getFGMapManager().getMapController().setCenter(point);
                        break;
                    case UPDATE_WIFI_ICON:
                        wifi_item.setIcon(msg.arg1);
                        wifi_item.setTitle(msg.obj.toString());
                        break;
                    case UPDATE_GPS_ICON:
                        gps_item.setIcon(msg.arg1);
                        gps_item.setTitle(msg.obj.toString());
                        break;
                }
            }
        };

    }

    public static String getPictureDir() {
        return PICTURE_PATH;
    }

    public static String getTempDir() {
        return TEMP_PATH;
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefsFamilyTree = getSharedPreferences(
            FamilyTree.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefsFamilyTree.edit();
        editor.putBoolean(FamilyTree.PREFS_FIRST, true);
        editor.putInt(FamilyTree.PREFS_FOCUS, 1);
        editor.commit();

        FamilyFolderCollector ffc = (FamilyFolderCollector) getApplicationContext();

        ffc.mFamilys = new HashMap<Integer, Family>();


        if (fgsys == null) {
            Log.i("TAG!", "First time NULL!!");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    fgsys = new FGSystemManager(FGActivity.this);
                    Log.d("TAG!", "Completed This Thread!");
                }
            };
            new GeneralAsyncTask(this, null, handler, INITIALIZE, FAILED)
                .execute(r, null);
        } else if (!fgsys.getFGActivity().equals(this)) {
            Log.i("TAG!", "Not equal!");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    fgsys.setFGActivity(FGActivity.this);
                }
            };
            new GeneralAsyncTask(this, null, handler, INITIALIZE, FAILED)
                .execute(r, null);
        } else {
            Log.i("TAG!", "Normally functional");
            fgsys.getFGMapManager().checkGPS();
        }


    }

    @Override
    public boolean onSearchRequested() {
        Dialog dia = fgsys.getFGDialogManager().getDialogSearchHouseMarker();
        dia.show();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (conRec != null) {
            registerReceiver(conRec, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
            //fgsys.getFGMapManager().getMyLocationOverlay().enableMyLocation();
        }
    }

    // @Override
    // public void onBackPressed() {
    // //super.onBackPressed();
    // Intent intent = new Intent(FGActivity.this, MainPage.class);
    // intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    // startActivityForResult(intent, 1);
    // }

    public static Bitmap getPicture(MARKER_TYPE type, String filename) {
        if (type == null || filename == null)
            return null;

        String path = FGActivity.PICTURE_PATH + type + "/" + filename + ".jpg";

        return BitmapFactory.decodeFile(path);
    }

    public static Bitmap getPictureThumb(MARKER_TYPE type, String filename) {
        if (type == null || filename == null)
            return null;

        return BitmapFactory.decodeFile(FGActivity.PICTURE_PATH + type + "/"
            + filename + "_thumb.jpg");
    }

    public static String getPicturePath(MARKER_TYPE type, String filename) {
        if (type == null || filename == null)
            return null;

        return (FGActivity.PICTURE_PATH + type + "/" + filename + ".jpg");
    }

    public static boolean removePicture(MARKER_TYPE type, String filename) {
        File file1 = new File(FGActivity.PICTURE_PATH + type + "/" + filename
            + ".jpg");
        File file2 = new File(FGActivity.PICTURE_PATH + type + "/" + filename
            + "_thumb.jpg");
        return file1.delete() && file2.delete();
    }

    public Handler getHandler() {
        return handler;
    }

    public void startFamilyTree(int hcode) {

        Intent intent = new Intent(Action.MAIN);
        intent.addCategory(Category.HOUSE);
        intent.setData(ContentUris.withAppendedId(House.CONTENT_URI, hcode));
        intent.putExtra("hcode", hcode);


        // SharedPreferences house = getSharedPreferences("house", 0);
        // SharedPreferences.Editor editor = house.edit();
        // editor.putInt("hcode", hcode);
        // editor.commit();

        startActivity(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TAG!", "Map paused");
        if (fgsys != null) {
            Log.d("TAG!", "Map paused Passed!");
            fgsys.getFGMapManager().clearCahce();
            //fgsys.getFGMapManager().getMyLocationOverlay().disableMyLocation();
        }
        if (conRec != null)
            unregisterReceiver(conRec);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FinalValue.FILTER_REQUEST) {
            SharedPreferences sh = this.getSharedPreferences(
                PreferenceFilter.FILE_XML, MODE_PRIVATE);

            // Check if whether this function is open by user
            new ProcessingFilterAsyncTask(this, sh).execute("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        wifi_item = menu.findItem(R.id.menu_wifi_status);
        gps_item = menu.findItem(R.id.menu_gps_status);

        int mapStyle = FGMapManager.getCurrentMapStyle();
        int menuID = -1;
        switch (mapStyle) {
            case FinalValue.INT_SATELLITE:
                menuID = R.id.menu_sat;
                break;
            case FinalValue.INT_GOOGLE_MAPS:
                menuID = R.id.menu_normal;
                break;
            default:
                menuID = R.id.menu_sat_path;
        }
        menu.findItem(menuID).setChecked(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("TAG!", item.getTitle().toString());

        switch (item.getItemId()) {
            case R.id.menu_center:
                Location location = fgsys.getFGGPSManager().getLastKnownLocation();
                GeoPoint geoPointCurrent;
                MyLocationOverlay mLocation = fgsys.getFGMapManager()
                    .getMyLocationOverlay();
                //TODO Change to Google Map
/*                if (mLocation.getLastFix() != null){
                    fgsys.getFGMapManager().getMapController()
                            .setCenter(mLocation.getMyLocation());
                } else if (location != null) {
                    geoPointCurrent = new GeoPoint(location.getLatitude(),
                            location.getLongitude());
                    fgsys.getFGMapManager().getMapController()
                            .setCenter(geoPointCurrent);
                }*/

                // For testing-purpose only

                // Spot[] set =
                // fgsys.getFGDatabaseManager().getMarked().values().toArray(new
                // Spot[fgsys.getFGDatabaseManager().getMarked().size()]);
                // for (Spot spot : set) {
                // fgsys.removeMarkerOnMap(spot);
                // }
                // fgsys.getFGMapManager().getMapView().invalidate();

                // -------------------------
                return true;
            case R.id.menu_filter:
                this.startActivityForResult(
                    new Intent(this, PreferenceFilter.class),
                    FinalValue.FILTER_REQUEST);
                return true;
            case R.id.menu_search:
                Dialog dialogSearchHouseMarker = fgsys.getFGDialogManager()
                    .getDialogSearchHouseMarker();
                dialogSearchHouseMarker.show();
                return true;
            case R.id.menu_wifi_status:
                this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                return true;
            case R.id.menu_gps_status:
                this.startActivity(new Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return true;
            case R.id.menu_sat:
                fgsys.getFGMapManager().setMapStyle(FinalValue.INT_SATELLITE);
                item.setChecked(true);
                return true;
            case R.id.menu_sat_path:
                fgsys.getFGMapManager().setMapStyle(
                    FinalValue.INT_SATELLITE_OVERLAY);
                item.setChecked(true);
                return true;
            case R.id.menu_normal:
                fgsys.getFGMapManager().setMapStyle(FinalValue.INT_GOOGLE_MAPS);
                item.setChecked(true);
                return true;
            case android.R.id.home:
                startHomeActivity();
        }
        return super.onOptionsItemSelected(item);
    }


    public void goToHouse(String hcode) {
        String[] projection = new String[]{House._ID, House.X_GIS, House.Y_GIS};
        String selection = "hcode=?";
        Cursor c = getContentResolver().query(House.CONTENT_URI, projection, selection,
            new String[]{hcode}, House._ID);
        if (c.moveToFirst()) {
            double x = c.getDouble(1);
            double y = c.getDouble(2);
            if (x > 0 || y > 0) {
                Toast.makeText(this, "x=" + x + " y=" + y, Toast.LENGTH_SHORT).show();
                GeoPoint geo = new GeoPoint(y, x);
                fgsys.getFGMapManager().getMapController()
                    .setCenter(geo);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG!", "Map Destroyed");
        if (fgsys != null) {
            fgsys.close();
            fgsys = null;
        }


        handler = null;
        wifi_item = null;
        gps_item = null;
        conRec = null;

    }
}
