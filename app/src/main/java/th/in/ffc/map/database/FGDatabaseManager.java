package th.in.ffc.map.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.value.FILTER_GROUP;
import th.in.ffc.map.value.FinalValue;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.map.village.spot.Spot;
import th.in.ffc.util.DateTime;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class FGDatabaseManager {

    // private FGSystemManager fgSystemManager;

    private DatabaseManager databaseManager;

    private TreeMap<String, String> village_name;

    private TreeMap<String, Spot> marked;

    private TreeMap<String, Spot> available;

    String pcucode;

    // private ArrayList<House> rawHouseMarked;
    // private ArrayList<House> rawHouseNoMark;

    public FGDatabaseManager(FGSystemManager fgSystemManager) {
        // this.fgSystemManager = fgSystemManager;

        this.databaseManager = new DatabaseManager();

        SharedPreferences sp = fgSystemManager.getFGActivity().getSharedPreferences("filter_preference", Context.MODE_PRIVATE);
        sp.edit().clear().commit();

        sp = fgSystemManager.getFGActivity().getSharedPreferences("house_preference", Context.MODE_PRIVATE);
        sp.edit().clear().commit();

        for (FILTER_GROUP fg : FILTER_GROUP.values()) {
            sp = fgSystemManager.getFGActivity().getSharedPreferences(fg.getPreferenceName(), Context.MODE_PRIVATE);
            sp.edit().clear().commit();
        }

        this.pcucode = fgSystemManager.getFGActivity().getPcuCode();

        this.initializeSpot();
    }

    private void initializeSpot() {
        this.marked = new TreeMap<String, Spot>();
        this.available = new TreeMap<String, Spot>();
        this.village_name = new TreeMap<String, String>();

        // House Spot

        this.databaseManager.openDatabase();

        Log.d("TAG!", "House Query processing");

        String stringSQL = "SELECT DISTINCT village.villcode, village.villname, house.hcode, house.hno, house.xgis, house.ygis, atable.afield, btable.bfield, house.telephonehouse " +

                "FROM village, house, (SELECT person.hcode, COUNT(personchronic.pid) AS afield " +

                "FROM person " + "LEFT JOIN personchronic ON person.pid = personchronic.pid " + "GROUP BY person.hcode) AS atable, " +

                "(SELECT person.hcode, COUNT(persontype.pid) AS bfield " + " FROM person " + " LEFT JOIN persontype ON person.pid = persontype.pid " + " GROUP BY person.hcode) AS btable " +

                "WHERE village.villcode = house.villcode " + "AND house.hcode = atable.hcode " + "AND house.hcode = btable.hcode " +
//                "ORDER BY CAST(house.hno AS INTEGER), length(hno), hno ";
                "order by  villname,house.hno";

        Cursor cursor = this.databaseManager.getCursor(stringSQL);
        if (cursor.moveToFirst()) {
            MARKER_TYPE type = MARKER_TYPE.HOUSE;
            do {
                String stringVillCode = cursor.getString(0);
                String stringVillName = cursor.getString(1);

                this.addVillageName(stringVillCode, stringVillName);

                int intHCode = cursor.getInt(2);
                String stringHNo = cursor.getString(3);
                if (stringHNo == null)
                    continue;

                int intCountPIDPersonchronic = cursor.getInt(6);
                String stringColor = null;
                if (intCountPIDPersonchronic > 0) {
                    stringColor = FinalValue.STRING_RED;
                } else {
                    stringColor = FinalValue.STRING_GREEN;
                }

                int intCountPIDPersontype = cursor.getInt(7);
                boolean booleanSpecial = false;
                if (intCountPIDPersontype > 0) {
                    booleanSpecial = true;
                }

                String stringXgis = cursor.getString(4);
                String stringYgis = cursor.getString(5);

                String tel_num = cursor.getString(8);

                Bundle addition = setHouseBundle(stringHNo, stringColor, booleanSpecial, tel_num);

                double doubleXgis = 0;
                double doubleYgis = 0;

                if (!this.isPointExist(stringXgis, stringYgis)) {

                    Spot house = new Spot(pcucode, type, stringVillCode, intHCode, doubleXgis, doubleYgis, addition);
                    this.available.put(type + "_" + house.getID(), house);
                } else {
                    if(!stringXgis.isEmpty() && !stringYgis.isEmpty()) {
                        doubleXgis = Double.parseDouble(stringXgis);
                        doubleYgis = Double.parseDouble(stringYgis);

                        Spot house = new Spot(pcucode, type, stringVillCode, intHCode, doubleXgis, doubleYgis, addition);
                        this.marked.put(type + "_" + house.getID(), house);
                    }
                    else {
                        Log.d("lat",stringXgis);
                        Log.d("log",stringYgis);
                    }

                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;

        Log.d("TAG!", "House Query finished");

        // ----

        // Water Spot

        stringSQL = "SELECT distinct w.villcode, v.villname, w.waterno, w.quality, w.xgis, w.ygis,c.waterownername,t.watertypename FROM villagewater w INNER JOIN village v ON w.villcode = v.villcode LEFT JOIN cwaterowner c ON c.waterownercode = w.owner LEFT JOIN cwatertype t ON t.watertypecode = w.watertype";

        cursor = this.databaseManager.getCursor(stringSQL);
        if (cursor.moveToFirst()) {
            MARKER_TYPE type = MARKER_TYPE.WATER;
            do {
                String stringVillCode = cursor.getString(0);
                String stringVillName = cursor.getString(1);

                this.addVillageName(stringVillCode, stringVillName);

                int intWaterNo = cursor.getInt(2);
                // int intWaterQuality = cursor.getInt(3);

                String stringXgis = cursor.getString(4);
                String stringYgis = cursor.getString(5);

                String stringOwner = cursor.getString(6);

                String stringWaterType = cursor.getString(7);

                Bundle addition = setWaterBundle(stringWaterType, stringOwner);

                double doubleXgis = 0;
                double doubleYgis = 0;

                if (!this.isPointExist(stringXgis, stringYgis)) {
                    Spot water = new Spot(pcucode, type, stringVillCode, intWaterNo, doubleXgis, doubleYgis, addition);
                    this.available.put(type + "_" + water.getID(), water);
                } else {
                    if(!stringXgis.isEmpty() && !stringYgis.isEmpty()) {
                        doubleXgis = Double.parseDouble(stringXgis);
                        doubleYgis = Double.parseDouble(stringYgis);

                        Spot water = new Spot(pcucode, type, stringVillCode, intWaterNo, doubleXgis, doubleYgis, addition);
                        this.marked.put(type + "_" + water.getID(), water);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;

        // ----

        // Temple Spot
        stringSQL = "select distinct t.villcode,v.villname,t.templeno,t.templename,t.address,t.xgis,t.ygis,c.religionname FROM villagetemple t INNER JOIN village v ON t.villcode=v.villcode LEFT JOIN creligion c ON cast(c.religioncode as integer) = cast(t.religion as integer) WHERE t.templename IS NOT NULL AND t.templename <> '' AND t.templename <> ' '";
        cursor = this.databaseManager.getCursor(stringSQL);
//		Log.d("TAG!", "row is " + cursor.getCount());
        if (cursor.moveToFirst()) {
            MARKER_TYPE type = MARKER_TYPE.TEMPLE;
            do {
                String stringVillCode = cursor.getString(0);
                String stringVillName = cursor.getString(1);

                this.addVillageName(stringVillCode, stringVillName);

                int intTempleNo = cursor.getInt(2);

                String stringTempleName = cursor.getString(3);
                String stringAddress = cursor.getString(4);

                String stringXgis = cursor.getString(5);
                String stringYgis = cursor.getString(6);

                String religion_name = cursor.getString(7);

                Bundle addition = setTempleBundle(stringTempleName, stringAddress, religion_name);

                double doubleXgis = 0;
                double doubleYgis = 0;

                if (!this.isPointExist(stringXgis, stringYgis)) {
                    Spot temple = new Spot(pcucode, type, stringVillCode, intTempleNo, doubleXgis, doubleYgis, addition);

                    this.available.put(type + "_" + temple.getID(), temple);
                } else {
                    if(!stringXgis.isEmpty() && !stringYgis.isEmpty()) {
                        doubleXgis = Double.parseDouble(stringXgis);
                        doubleYgis = Double.parseDouble(stringYgis);

                        Spot temple = new Spot(pcucode, type, stringVillCode, intTempleNo, doubleXgis, doubleYgis, addition);

                        this.marked.put(type + "_" + temple.getID(), temple);
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;

        // ----

        // School Marker

        stringSQL = "SELECT distinct s.villcode,v.villname,s.schoolno,s.schoolname,s.xgis,s.ygis,s.telephone FROM villageschool s INNER JOIN village v ON s.villcode = v.villcode WHERE s.schoolname IS NOT NULL AND s.schoolname <> '' AND s.schoolname <> ' '";
        cursor = this.databaseManager.getCursor(stringSQL);
        if (cursor.moveToFirst()) {
            MARKER_TYPE type = MARKER_TYPE.SCHOOL;
            do {
                String stringVillCode = cursor.getString(0);
                String stringVillName = cursor.getString(1);

                this.addVillageName(stringVillCode, stringVillName);

                int intSchoolNo = cursor.getInt(2);

                String stringSchoolName = cursor.getString(3);
                // String stringAddress = cursor.getString(4);

                String stringXgis = cursor.getString(4);
                String stringYgis = cursor.getString(5);

                String telephone = cursor.getString(6);

                Bundle addition = setSchoolBundle(stringSchoolName, telephone);

                double doubleXgis = 0;
                double doubleYgis = 0;

                if (!this.isPointExist(stringXgis, stringYgis)) {
                    Spot school = new Spot(pcucode, type, stringVillCode, intSchoolNo,  doubleXgis, doubleYgis,addition);

                    this.available.put(type + "_" + school.getID(), school);
                } else {
                    if(!stringXgis.isEmpty() && !stringYgis.isEmpty()) {
                        doubleXgis = Double.parseDouble(stringXgis);
                        doubleYgis = Double.parseDouble(stringYgis);

                        Spot school = new Spot(pcucode, type, stringVillCode, intSchoolNo, doubleXgis, doubleYgis, addition);

                        this.marked.put(type + "_" + school.getID(), school);
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;

        // ----

        // Business Marker

        stringSQL = "SELECT distinct b.villcode, v.villname, b.businessno, b.businessname, b.xgis, b.ygis,c.businessdesc FROM villagebusiness b INNER JOIN village v ON b.villcode = v.villcode LEFT JOIN cbusiness c ON c.businesstypecode = b.businesstype WHERE b.businessname IS NOT NULL AND b.businessname <> '' AND b.businessname <> ' '";

        cursor = this.databaseManager.getCursor(stringSQL);
        if (cursor.moveToFirst()) {
            MARKER_TYPE type = MARKER_TYPE.BUSINESS;
            do {
                String stringVillCode = cursor.getString(0);
                String stringVillName = cursor.getString(1);

                this.addVillageName(stringVillCode, stringVillName);

                int intShopNo = cursor.getInt(2);

                String stringShopName = cursor.getString(3);
                // String stringAddress = cursor.getString(4);
                // String stringOwner = cursor.getString(5);

                String stringXgis = cursor.getString(4);
                String stringYgis = cursor.getString(5);

                String stringType = cursor.getString(6);

                Bundle addition = setBusinessBundle(stringShopName, stringType);

                double doubleXgis = 0;
                double doubleYgis = 0;

                if (!this.isPointExist(stringXgis, stringYgis)) {
                    Spot shop = new Spot(pcucode, type, stringVillCode, intShopNo, doubleXgis, doubleYgis,  addition);

                    this.available.put(type + "_" + shop.getID(), shop);
                } else {
                    if(!stringXgis.isEmpty() && !stringYgis.isEmpty()) {
                        doubleXgis = Double.parseDouble(stringXgis);
                        doubleYgis = Double.parseDouble(stringYgis);

                        Spot shop = new Spot(pcucode, type, stringVillCode, intShopNo, doubleXgis, doubleYgis, addition);

                        this.marked.put(type + "_" + shop.getID(), shop);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;
        // ----

        // POI Marker

        stringSQL = "SELECT distinct p.villcode, v.villname, p.poino, p.poiname, p.tel, p.xgis, p.ygis,p1.poitype FROM ffc_poi p INNER JOIN village v ON p.villcode = v.villcode LEFT JOIN ffc_cpoitype p1 ON p.poitype = p1.poitype WHERE p.poiname IS NOT NULL AND p.poiname <> '' AND p.poiname <> ' '";
        cursor = this.databaseManager.getCursor(stringSQL);
        Log.d("TAG!", cursor.getCount() + " rows fetched");
        if (cursor.moveToFirst()) {
            MARKER_TYPE type = MARKER_TYPE.POI;
            do {
                String stringVillCode = cursor.getString(0);
                String stringVillName = cursor.getString(1);

                this.addVillageName(stringVillCode, stringVillName);

                int intPOINo = cursor.getInt(2);

                String stringPOIName = cursor.getString(3);
                String stringtTelephone = cursor.getString(4);

                String stringXgis = cursor.getString(5);
                String stringYgis = cursor.getString(6);

                int intType = cursor.getInt(7);

                Bundle addition = setPOIBundle(stringPOIName, stringtTelephone, intType);

                double doubleXgis = 0;
                double doubleYgis = 0;

                if (!this.isPointExist(stringXgis, stringYgis)) {
                    Log.d("TAG!", intPOINo + " not exist");
                    Spot poi = new Spot(pcucode, type, stringVillCode, intPOINo, doubleXgis, doubleYgis, addition);

                    this.available.put(type + "_" + poi.getID(), poi);
                } else {
                    Log.d("TAG!", intPOINo + " exist " + stringXgis + "," + stringYgis);
                    if(!stringXgis.isEmpty() && !stringYgis.isEmpty()) {
                        doubleXgis = Double.parseDouble(stringXgis);
                        doubleYgis = Double.parseDouble(stringYgis);

                        Spot poi = new Spot(pcucode, type, stringVillCode, intPOINo, doubleXgis, doubleYgis, addition);

                        this.marked.put(type + "_" + poi.getID(), poi);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;
        // ----

        // Hospital Marker

        stringSQL = "SELECT distinct h.villcode, v.villname, h.hospitalno, h.hospitalname, h.bedtotal, h.tel, h.xgis, h.ygis FROM ffc_hospital h INNER JOIN village v ON h.villcode = v.villcode WHERE h.hospitalname IS NOT NULL AND h.hospitalname <> '' AND h.hospitalname <> ' '";
        cursor = this.databaseManager.getCursor(stringSQL);
        if (cursor.moveToFirst()) {
            MARKER_TYPE type = MARKER_TYPE.HOSPITAL;
            do {
                String stringVillCode = cursor.getString(0);
                String stringVillName = cursor.getString(1);

                this.addVillageName(stringVillCode, stringVillName);

                int intHospitalNo = cursor.getInt(2);

                String stringHospitalName = cursor.getString(3);
                int intHospitalBed = cursor.getInt(4);
                String stringtTelephone = cursor.getString(5);

                String stringXgis = cursor.getString(6);
                String stringYgis = cursor.getString(7);

                Bundle addition = setHospitalBundle(stringHospitalName, intHospitalBed, stringtTelephone);

                double doubleXgis = 0;
                double doubleYgis = 0;

                if (!this.isPointExist(stringXgis, stringYgis)) {
                    Spot hospital = new Spot(pcucode, type, stringVillCode, intHospitalNo, doubleXgis, doubleYgis, addition);

                    this.available.put(type + "_" + hospital.getID(), hospital);
                } else {
                    if(!stringXgis.isEmpty() && !stringYgis.isEmpty()) {
                        doubleXgis = Double.parseDouble(stringXgis);
                        doubleYgis = Double.parseDouble(stringYgis);

                        Spot hospital = new Spot(pcucode, type, stringVillCode, intHospitalNo, doubleXgis, doubleYgis, addition);

                        this.marked.put(type + "_" + hospital.getID(), hospital);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;
        // ----

        this.databaseManager.closeDatabase();
    }

    private boolean isPointExist(String stringXgis, String stringYgis) {
        if ((stringXgis == null || stringXgis.equals("") || stringXgis.equals(" ") || stringXgis.equals("  ") || stringXgis.equals("0") || stringXgis.equals("0.0")) && (stringYgis == null || stringYgis.equals("") || stringYgis.equals(" ") || stringYgis.equals("  ") || stringYgis.equals("0") || stringYgis.equals("0.0"))) {
            return false;
        }
        return true;
    }

    public boolean updateGeoPointToDatabase(Spot spot) {

        boolean booleanCheck = false;
        this.databaseManager.openDatabase();

        MARKER_TYPE type = Enum.valueOf(MARKER_TYPE.class, spot.getUid());

        ContentValues args = new ContentValues();
//        args.put("xgis", spot.getDoubleLong() + "");
//        args.put("ygis", spot.getDoubleLat() + "");
        args.put("xgis", spot.getDoubleLat() + "");
        args.put("ygis", spot.getDoubleLong() + "");
        args.put("dateupdate", DateTime.getCurrentDateTime());

//		switch (type) {
//		case TEMPLE:
//			break;
//		default:
//			args.put("dateupdate", DateTime.getCurrentDateTime());
//		}

        String column_name = type.getColumnName();

        booleanCheck = this.databaseManager.update(type.getDBTableName(), args, "villcode='" + spot.getStringVillCode() + "' and " + column_name + "=" + spot.getPartialID());

        this.databaseManager.closeDatabase();

        return booleanCheck;
    }

    public static Bundle setTempleBundle(String name, String address, String religion) {
        Bundle b = new Bundle();
        b.putString("Name", name);
        b.putString("Address", address);
        b.putString("Religion", religion);
        return b;
    }

    public static Bundle setHouseBundle(String stringHNo, String stringColor, boolean booleanSpecial, String stringTelephone) {
        Bundle b = new Bundle();
        b.putString("HNo", stringHNo);
        b.putString("Color", stringColor);
        b.putBoolean("Special", booleanSpecial);
        b.putString("Telephone", stringTelephone);
        return b;
    }

    public static Bundle setWaterBundle(String stringWaterType, String stringSector) {
        Bundle b = new Bundle();
        b.putString("Type", stringWaterType);
        b.putString("Sector", stringSector);
        return b;
    }

    public static Bundle setSchoolBundle(String stringSchoolName, String stringTelephone) {
        Bundle b = new Bundle();
        b.putString("Name", stringSchoolName);
        b.putString("Telephone", stringTelephone);
        return b;
    }

    public static Bundle setBusinessBundle(String stringShopName, String stringType) {
        Bundle b = new Bundle();
        b.putString("Name", stringShopName);
        b.putString("Type", stringType);
        return b;
    }

    public static Bundle setPOIBundle(String stringPOIName, String stringtTelephone, int intType) {
        Bundle b = new Bundle();
        b.putString("Name", stringPOIName);
        b.putString("Telephone", stringtTelephone);
        b.putInt("Type", intType);
        return b;
    }

    public static Bundle setHospitalBundle(String stringHospitalName, int intHospitalBed, String stringtTelephone) {
        Bundle b = new Bundle();
        b.putString("Name", stringHospitalName);
        b.putInt("TotalBed", intHospitalBed);
        b.putString("Telephone", stringtTelephone);
        return b;
    }

    public TreeMap<String, Spot> getMarked() {
        return marked;
    }

    public TreeMap<String, Spot> getAvailable() {
        return available;
    }

    public Set<Entry<String, Spot>> getMarkedIterator() {
        return marked.entrySet();
    }

    public Set<Entry<String, Spot>> getAvailableIterator() {
        return available.entrySet();
    }

    public Spot getSpotInMarked(String key) {
        return marked.get(key);
    }

    public Spot getSpotInAvailable(String key) {
        return available.get(key);
    }

    public void addSpotToMarked(Spot spot) {
        String key = spot.getUid().toUpperCase() + "_" + spot.getID();
        marked.put(key, spot);
        available.remove(key);
    }

    public void addSpotToAvailable(Spot spot) {
        String key = spot.getUid().toUpperCase() + "_" + spot.getID();
        available.put(key, spot);
        marked.remove(key);
    }

    public String getVillageName(String stringVillCode) {
        return this.village_name.get(stringVillCode);
    }

    public void addVillageName(String stringVillCode, String stringVillageName) {
        if (!village_name.containsKey(stringVillCode))
            village_name.put(stringVillCode, stringVillageName);
    }

    // public ArrayList<House> getRawHouseMark() {
    // return rawHouseMarked;
    // }
    //
    // public ArrayList<House> getRawHouseNoMark() {
    // return rawHouseNoMark;
    // }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

}
