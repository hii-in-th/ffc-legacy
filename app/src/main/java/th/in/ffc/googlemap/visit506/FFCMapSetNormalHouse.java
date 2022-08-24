package th.in.ffc.googlemap.visit506;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import th.in.ffc.provider.HouseProvider.House;

import java.util.ArrayList;
import java.util.HashMap;

public class FFCMapSetNormalHouse {
    private Context context;
    //	private HashMap<String, HashMap<String, String>> houseDetail;
    private ArrayList<HashMap<String, String>> houseDetail;
    private HashMap<String, String> hashHcode;

    public FFCMapSetNormalHouse(Context context, HashMap<String, String> hashHcode) {
        this.hashHcode = hashHcode;
        this.context = context;
        houseDetail = new ArrayList<HashMap<String, String>>();
        //	queryNormalNome();
    }

    public ArrayList<HashMap<String, String>> queryNormalNome() {
        Uri uri = House.CONTENT_URI;
        String[] projection = {"hcode", "xgis", "ygis", "hno"};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = contentResolver.query(uri, projection, null, null, "hcode");
        if (c.moveToFirst()) {
            do {
                String hcode = c.getString(c.getColumnIndex("hcode"));
//                String lat = c.getString(c.getColumnIndex("ygis"));
//                String lng = c.getString(c.getColumnIndex("xgis"));
                String lat = c.getString(c.getColumnIndex("xgis"));
                String lng = c.getString(c.getColumnIndex("ygis"));
                String hno = c.getString(c.getColumnIndex("hno"));
                if (TextUtils.isEmpty(hashHcode.get(hcode))) {
                    if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
                        HashMap<String, String> detail = new HashMap<String, String>();
                        detail.put("hno", hno);
                        detail.put("hcode", hcode);
                        detail.put("lat", lat);
                        detail.put("lng", lng);
                        houseDetail.add(detail);
                    }
                }
            } while (c.moveToNext());
        }
        return houseDetail;
    }
}
