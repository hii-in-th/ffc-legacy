/* ***********************************************************************
 *                                                                 _ _ _
 *                                                               ( _ _  |
 *                                                           _ _ _ _  | |
 *                                                          (_ _ _  | |_|
 *  _     _   _ _ _ _     _ _ _   _ _ _ _ _   _ _ _ _     _ _ _   | | 
 * |  \  | | |  _ _ _|   /  _ _| |_ _   _ _| |  _ _ _|   /  _ _|  | |
 * | | \ | | | |_ _ _   /  /         | |     | |_ _ _   /  /      |_|
 * | |\ \| | |  _ _ _| (  (          | |     |  _ _ _| (  (    
 * | | \ | | | |_ _ _   \  \_ _      | |     | |_ _ _   \  \_ _ 
 * |_|  \__| |_ _ _ _|   \_ _ _|     |_|     |_ _ _ _|   \_ _ _| 
 *  a member of NSTDA, @Thailand
 *  
 * ***********************************************************************
 *
 *
 * FFC-Plus Project
 *
 * Copyright (C) 2010-2012 National Electronics and Computer Technology Center
 * All Rights Reserved.
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package th.in.ffc.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import th.in.ffc.provider.UserProvider.User;
import th.in.ffc.provider.UserProvider.UserDatabaseOpenHelper;

import java.util.HashMap;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since 1.0
 */
public class CodeProvider extends ContentProvider {

    public interface NameColumn {

        public static final String _NAME = "name";
    }

    public static String AUTHORITY = "th.in.ffc.provider.CodeProvider";

    private static final int DIAG = 1;
    private static final int DIAG_ITEM = 2;
    private static final int DIAG_TOP = 3;
    private static final int DRUG = 4;
    private static final int DRUG_ITEM = 5;
    private static final int DRUG_TOP = 6;
    private static final int DRUG_DOSE = 7;
    private static final int FORMULA = 59;
    private static final int FORMULA_DRUG = 610;
    private static final int FORMULA_DIAG = 611;
    private static final int OCCUPA = 8;
    private static final int OCCUPA_ITEM = 9;
    private static final int RIGHT = 10;
    private static final int RIGHT_ITEM = 11;
    private static final int ANCRISK = 12;
    private static final int ANCRISK_ITEM = 13;
    private static final int CLINIC = 14;
    private static final int CLINIC_ITEM = 15;
    private static final int NATION = 16;
    private static final int NATION_ITEM = 17;
    private static final int FAMPOS = 102;
    private static final int FAMPOS_ITEM = 103;
    private static final int SYMTOM = 18;
    // private static final int SYMTOM_ITEM = 19;
    private static final int VITALSIGN = 20;
    // private static final int VITALSIGN_ITEM = 21;
    private static final int HH_SIGN = 22;
    // private static final int HH_SIGN_ITEM = 23;
    private static final int HH_SERVICECARE = 24;
    // private static final int HH_SERVICECARE_ITEM = 25;
    private static final int HH_EVALPLAN = 26;
    // private static final int HH_EVALPLAN_ITEM = 27;
    private static final int HEALTH_SUGGEST = 28;
    private static final int HH_TYPE = 29;
    private static final int HH_TYPE_ITEM = 30;
    private static final int HOSPITAL = 31;
    private static final int HOSPITAL_ITEM = 32;
    private static final int WATER_TYPE = 33;
    private static final int WATER_TYPE_ITEM = 34;
    private static final int VESSEL_WATER = 35;
    private static final int VESSEL_WATER_ITEM = 36;
    private static final int PERSON_TYPE = 37;
    private static final int PERSON_TYPE_ITEM = 38;
    private static final int WATER_OWNER = 39;
    private static final int WATER_OWNER_ITEM = 40;
    private static final int PERSONINCOMPLETE = 41;
    private static final int PERSONINCOMPLETE_ITEM = 42;
    private static final int PERSONHELP = 43;
    private static final int PERSONHELP_ITEM = 44;
    private static final int SCREENOTHERDISEASE = 45;
    private static final int SCREENOTHERDISEASE_ITEM = 46;
    private static final int GROW = 47;
    private static final int GROW_ITEM = 48;
    private static final int SUBDISTCODE = 49;
    private static final int SUBDISTCODE_ITEM = 50;
    private static final int DISTCODE = 51;
    private static final int DISTCODE_ITEM = 52;
    private static final int PROVCODE = 53;
    private static final int PROVCODE_ITEM = 54;
    private static final int PERSONPROBLEM = 55;
    private static final int PERSONPROBLEM_ITEM = 56;
    private static final int PERSONNEED = 57;
    private static final int PERSONNEED_ITEM = 58;
    private static final int USEROFFICER = 61;
    private static final int USEROFFICER_ITEM = 62;
    private static final int SYMTOMCO = 63;
    private static final int DIAGNOTE = 64;
    private DbOpenHelper mOpenHelper;
    private static UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, "diagnosis", DIAG);
        mUriMatcher.addURI(AUTHORITY, "diagnosis/top", DIAG_TOP);
        mUriMatcher.addURI(AUTHORITY, "diagnosis/*", DIAG_ITEM);
        mUriMatcher.addURI(AUTHORITY, "drug", DRUG);
        mUriMatcher.addURI(AUTHORITY, "drug/dose", DRUG_DOSE);
        mUriMatcher.addURI(AUTHORITY, "drug/top", DRUG_TOP);
        mUriMatcher.addURI(AUTHORITY, "drug/*", DRUG_ITEM);
        mUriMatcher.addURI(AUTHORITY, "drugformula", FORMULA);
        mUriMatcher.addURI(AUTHORITY, "drugformula/drug", FORMULA_DRUG);
        mUriMatcher.addURI(AUTHORITY, "drugformula/diag", FORMULA_DIAG);
        mUriMatcher.addURI(AUTHORITY, "occupa", OCCUPA);
        mUriMatcher.addURI(AUTHORITY, "occupa/#", OCCUPA_ITEM);
        mUriMatcher.addURI(AUTHORITY, "right", RIGHT);
        mUriMatcher.addURI(AUTHORITY, "right/*", RIGHT_ITEM);
        mUriMatcher.addURI(AUTHORITY, "ancrisk", ANCRISK);
        mUriMatcher.addURI(AUTHORITY, "ancrisk/#", ANCRISK_ITEM);
        mUriMatcher.addURI(AUTHORITY, "nation", NATION);
        mUriMatcher.addURI(AUTHORITY, "nation/#", NATION_ITEM);
        mUriMatcher.addURI(AUTHORITY, "familyposition", FAMPOS);
        mUriMatcher.addURI(AUTHORITY, "familyposition/*", FAMPOS_ITEM);
        mUriMatcher.addURI(AUTHORITY, "clinic", CLINIC);
        mUriMatcher.addURI(AUTHORITY, "clinic/#", CLINIC_ITEM);
        mUriMatcher.addURI(AUTHORITY, "symtom", SYMTOM);
        mUriMatcher.addURI(AUTHORITY, "symtomco", SYMTOMCO);
        mUriMatcher.addURI(AUTHORITY, "diagnote", DIAGNOTE);
        mUriMatcher.addURI(AUTHORITY, "vitalsign", VITALSIGN);
        mUriMatcher.addURI(AUTHORITY, "hhsign", HH_SIGN);
        mUriMatcher.addURI(AUTHORITY, "hhservicecare", HH_SERVICECARE);
        mUriMatcher.addURI(AUTHORITY, "hhevalplan", HH_EVALPLAN);
        mUriMatcher.addURI(AUTHORITY, "healthsuggest", HEALTH_SUGGEST);
        mUriMatcher.addURI(AUTHORITY, "hhtype", HH_TYPE);
        mUriMatcher.addURI(AUTHORITY, "hhtype/#", HH_TYPE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "hospital", HOSPITAL);
        mUriMatcher.addURI(AUTHORITY, "hospital/*", HOSPITAL_ITEM);
        mUriMatcher.addURI(AUTHORITY, "watertype", WATER_TYPE);
        mUriMatcher.addURI(AUTHORITY, "watertype/#", WATER_TYPE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "vesselWater", VESSEL_WATER);
        mUriMatcher.addURI(AUTHORITY, "vesselWater/#", VESSEL_WATER_ITEM);
        mUriMatcher.addURI(AUTHORITY, "persontype", PERSON_TYPE);
        mUriMatcher.addURI(AUTHORITY, "persontype/#", PERSON_TYPE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "waterowner", WATER_OWNER);
        mUriMatcher.addURI(AUTHORITY, "waterowner/#", WATER_OWNER_ITEM);
        mUriMatcher.addURI(AUTHORITY, "personincomplete", PERSONINCOMPLETE);
        mUriMatcher.addURI(AUTHORITY, "personincomplete/#",
                PERSONINCOMPLETE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "personhelp", PERSONHELP);
        mUriMatcher.addURI(AUTHORITY, "personhelp/#", PERSONHELP_ITEM);
        mUriMatcher.addURI(AUTHORITY, "screenotherdisease", SCREENOTHERDISEASE);
        mUriMatcher.addURI(AUTHORITY, "screenotherdisease/#",
                SCREENOTHERDISEASE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "grow", GROW);
        mUriMatcher.addURI(AUTHORITY, "grow/*", GROW_ITEM);
        mUriMatcher.addURI(AUTHORITY, "subdistrict", SUBDISTCODE);
        mUriMatcher.addURI(AUTHORITY, "subdistrict/*", SUBDISTCODE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "district", DISTCODE);
        mUriMatcher.addURI(AUTHORITY, "district/*", DISTCODE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "province", PROVCODE);
        mUriMatcher.addURI(AUTHORITY, "province/*", PROVCODE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "PersonProblem", PERSONPROBLEM);
        mUriMatcher.addURI(AUTHORITY, "PersonProblem/#", PERSONPROBLEM_ITEM);
        mUriMatcher.addURI(AUTHORITY, "PersonNeed", PERSONNEED);
        mUriMatcher.addURI(AUTHORITY, "PersonNeed/#", PERSONNEED_ITEM);
        mUriMatcher.addURI(AUTHORITY, "userofficertype", USEROFFICER);
        mUriMatcher.addURI(AUTHORITY, "userofficertype/*", USEROFFICER_ITEM);

    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbOpenHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (!DbOpenHelper.isDbExist()) {
            // return cursor that will moveToFirst() = false
            UserDatabaseOpenHelper udoh = new UserDatabaseOpenHelper(
                    getContext());
            SQLiteQueryBuilder bu = new SQLiteQueryBuilder();
            bu.setProjectionMap(UserProvider.PROJECTION_MAP);
            bu.setTables(User.TABLENAME);
            return bu.query(udoh.getReadableDatabase(),
                    new String[]{User.USERNAME},
                    "username='FFC-Nectec-50' AND password='004662'", null,
                    null, null, User.USERNAME);
        }

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        String groupby = null;
        String having = null;

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        switch (mUriMatcher.match(uri)) {

            case CodeProvider.DIAG:
                builder.setTables(Diagnosis.TABLENAME);
                builder.setProjectionMap(Diagnosis.PROJECTION_MAP);
                break;
            case CodeProvider.DIAG_ITEM:
                builder.setTables(Diagnosis.TABLENAME);
                builder.setProjectionMap(Diagnosis.PROJECTION_MAP);
                String code = uri.getLastPathSegment();
                builder.appendWhere("cdisease.diseasecode='" + code + "'");
                break;
            case CodeProvider.DIAG_TOP:
                builder.setTables(Diagnosis.TABLENAME_WITH_HIT);
                builder.setProjectionMap(Diagnosis.PROJECTION_MAP);
                break;
            case CodeProvider.DRUG:
                builder.setTables(Drug.TABLENAME);
                builder.setProjectionMap(Drug.PROJECTION_MAP);
                break;
            case CodeProvider.DRUG_TOP:
                builder.setTables(Drug.TABLENAME_WITH_HIT);
                builder.setProjectionMap(Drug.PROJECTION_MAP);
                break;
            case CodeProvider.DRUG_DOSE:
                builder.setTables(Drug.TABLENAME_WITH_DOSE);
                builder.setProjectionMap(Drug.PROJECTION_MAP);
                break;
            case CodeProvider.DRUG_ITEM:
                builder.setTables(Drug.TABLENAME);
                builder.setProjectionMap(Drug.PROJECTION_MAP);
                String drugcode = uri.getLastPathSegment();
                builder.appendWhere("cdrug.drugcode='" + drugcode + "'");
                break;
            case CodeProvider.FORMULA:
                builder.setTables(DrugFormula.TABLENAME);
                builder.setProjectionMap(DrugFormula.PROJECTION_MAP);
                break;
            case CodeProvider.FORMULA_DRUG:
                builder.setTables(DrugFormula.Drug.TABLENAME);
                builder.setProjectionMap(DrugFormula.Drug.PROJECTION_MAP);
                break;
            case CodeProvider.FORMULA_DIAG:
                builder.setTables(DrugFormula.Diagnosis.TABLENAME);
                builder.setProjectionMap(DrugFormula.Diagnosis.PROJECTION_MAP);
                break;
            case CodeProvider.OCCUPA:
                builder.setTables(Occupation.TABLENAME);
                builder.setProjectionMap(Occupation.PROJECTION_MAP);
                break;
            case CodeProvider.OCCUPA_ITEM:
                builder.setTables(Occupation.TABLENAME);
                builder.setProjectionMap(Occupation.PROJECTION_MAP);
                builder.appendWhere("coccupa.occupacode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.RIGHT:
                builder.setTables(Right.TABLENAME);
                builder.setProjectionMap(Right.PROJECTION_MAP);
                break;
            case CodeProvider.RIGHT_ITEM:
                builder.setTables(Right.TABLENAME);
                builder.setProjectionMap(Right.PROJECTION_MAP);
                builder.appendWhere("cright.rightcode='" + uri.getLastPathSegment()
                        + "'");
                break;
            case CodeProvider.ANCRISK:
                builder.setTables(AncRisk.TABLENAME);
                builder.setProjectionMap(AncRisk.PROJECTION_MAP);
                break;
            case CodeProvider.ANCRISK_ITEM:
                builder.setTables(AncRisk.TABLENAME);
                builder.setProjectionMap(AncRisk.PROJECTION_MAP);
                builder.appendWhere("cancrisk.ancriskcode='"
                        + uri.getLastPathSegment() + "'");
                break;

            case CodeProvider.CLINIC_ITEM:
                builder.appendWhere("cclinic.cliniccode='"
                        + uri.getLastPathSegment() + "'");
            case CodeProvider.CLINIC:
                builder.setTables(Clinic.TABLENAME);
                builder.setProjectionMap(Clinic.PROJECTION_MAP);
                break;

            case CodeProvider.FAMPOS_ITEM:
                builder.appendWhere("famposcode='" + uri.getLastPathSegment() + "'");
            case CodeProvider.FAMPOS:
                builder.setTables(FamilyPosition.TABLENAME);
                builder.setProjectionMap(FamilyPosition.PROJECTION_MAP);
                break;

            case CodeProvider.NATION_ITEM:
                builder.appendWhere("cnation.nationcode="
                        + uri.getLastPathSegment());
            case CodeProvider.NATION:
                builder.setTables(Nation.TABLENAME);
                builder.setProjectionMap(Nation.PROJECTION_MAP);
                break;

            case CodeProvider.SYMTOM:
                builder.setTables(Symtom.TABLENAME);
                builder.setProjectionMap(Symtom.PROJECTION_MAP);
                break;
            case CodeProvider.SYMTOMCO:
                builder.setTables(SysSymtomco.TABLENAME);
                builder.setProjectionMap(SysSymtomco.PROJECTION_MAP);
                break;
            case CodeProvider.DIAGNOTE:
                builder.setTables(SysDiagnote.TABLENAME);
                builder.setProjectionMap(SysDiagnote.PROJECTION_MAP);
                break;
            case CodeProvider.VITALSIGN:
                builder.setTables(VitalSign.TABLENAME);
                builder.setProjectionMap(VitalSign.PROJECTION_MAP);
                break;
            case CodeProvider.HH_SIGN:
                builder.setTables(HomeHealthSign.TABLENAME);
                builder.setProjectionMap(HomeHealthSign.PROJECTION_MAP);
                break;
            case CodeProvider.HH_SERVICECARE:
                builder.setTables(HomeHealthServiceCare.TABLENAME);
                builder.setProjectionMap(HomeHealthServiceCare.PROJECTION_MAP);
                break;
            case CodeProvider.HH_EVALPLAN:
                builder.setTables(HomeHealthEvalPlan.TABLENAME);
                builder.setProjectionMap(HomeHealthEvalPlan.PROJECTION_MAP);
                break;
            case CodeProvider.HEALTH_SUGGEST:
                builder.setTables(HealthSuggest.TABLENAME);
                builder.setProjectionMap(HealthSuggest.PROJECTION_MAP);
                break;
            case CodeProvider.HH_TYPE:
                builder.setTables(HomeHealthType.TABLENAME);
                builder.setProjectionMap(HomeHealthType.PROJECTION_MAP);
                break;
            case CodeProvider.HH_TYPE_ITEM:
                builder.setTables(HomeHealthType.TABLENAME);
                builder.setProjectionMap(HomeHealthType.PROJECTION_MAP);
                builder.appendWhere("chomehealthtype.homehealthcode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.HOSPITAL:
                builder.setTables(Hospital.TABLENAME);
                builder.setProjectionMap(Hospital.PROJECTION_MAP);
                break;
            case CodeProvider.HOSPITAL_ITEM:
                builder.setTables(Hospital.TABLENAME);
                builder.setProjectionMap(Hospital.PROJECTION_MAP);
                builder.appendWhere("chospital.hoscode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.WATER_TYPE:
                builder.setTables(WaterType.TABLENAME);
                builder.setProjectionMap(WaterType.PROJECTION_MAP);
                break;
            case CodeProvider.WATER_TYPE_ITEM:
                builder.setTables(WaterType.TABLENAME);
                builder.setProjectionMap(WaterType.PROJECTION_MAP);
                builder.appendWhere("cwatertype.watertypecode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.VESSEL_WATER:
                builder.setTables(VessalWater.TABLENAME);
                builder.setProjectionMap(VessalWater.PROJECTION_MAP);
                break;
            case CodeProvider.VESSEL_WATER_ITEM:
                builder.setTables(VessalWater.TABLENAME);
                builder.setProjectionMap(VessalWater.PROJECTION_MAP);
                builder.appendWhere("chousevesselwater.vesselwatercode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.PERSON_TYPE:
                builder.setTables(PersonType.TABLENAME);
                builder.setProjectionMap(PersonType.PROJECTION_MAP);
                break;
            case CodeProvider.PERSON_TYPE_ITEM:
                builder.setTables(PersonType.TABLENAME);
                builder.setProjectionMap(PersonType.PROJECTION_MAP);
                builder.appendWhere("cpersontype.persontypecode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.WATER_OWNER:
                builder.setTables(WaterOwner.TABLENAME);
                builder.setProjectionMap(WaterOwner.PROJECTION_MAP);
                break;
            case CodeProvider.WATER_OWNER_ITEM:
                builder.setTables(WaterOwner.TABLENAME);
                builder.setProjectionMap(WaterOwner.PROJECTION_MAP);
                builder.appendWhere("cwaterowner.waterownercode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.PERSONINCOMPLETE:
                builder.setTables(PersonIncomplete.TABLENAME);
                builder.setProjectionMap(PersonIncomplete.PROJECTION_MAP);
                break;
            case CodeProvider.PERSONINCOMPLETE_ITEM:
                builder.setTables(PersonIncomplete.TABLENAME);
                builder.setProjectionMap(PersonIncomplete.PROJECTION_MAP);
                builder.appendWhere("cpersonincomplete.incompletecode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.PERSONHELP:
                builder.setTables(PersonHelp.TABLENAME);
                builder.setProjectionMap(PersonHelp.PROJECTION_MAP);
                break;
            case CodeProvider.PERSONHELP_ITEM:
                builder.setTables(PersonHelp.TABLENAME);
                builder.setProjectionMap(PersonHelp.PROJECTION_MAP);
                builder.appendWhere("cpersonhelp.helpcode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.SCREENOTHERDISEASE:
                builder.setTables(ScreenOtherDisease.TABLENAME);
                builder.setProjectionMap(ScreenOtherDisease.PROJECTION_MAP);
                break;
            case CodeProvider.SCREENOTHERDISEASE_ITEM:
                builder.setTables(ScreenOtherDisease.TABLENAME);
                builder.setProjectionMap(ScreenOtherDisease.PROJECTION_MAP);
                builder.appendWhere("cscreenotherdisease.screenotherdiseasecode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.GROW:
                builder.setTables(Grow.TABLENAME);
                builder.setProjectionMap(Grow.PROJECTION_MAP);
                break;
            case CodeProvider.GROW_ITEM:
                builder.setTables(Grow.TABLENAME);
                builder.setProjectionMap(Grow.PROJECTION_MAP);
                builder.appendWhere("cgrow.growcode='" + uri.getLastPathSegment()
                        + "'");
                break;
            case CodeProvider.SUBDISTCODE:
                builder.setTables(Subdistrict.TABLENAME);
                builder.setProjectionMap(Subdistrict.PROJECTION_MAP);
                break;
            case CodeProvider.SUBDISTCODE_ITEM:
                builder.setTables(Subdistrict.TABLENAME);
                builder.setProjectionMap(Subdistrict.PROJECTION_MAP);
                builder.appendWhere("subdistcode = '" + uri.getLastPathSegment()
                        + "'");
                break;
            case CodeProvider.DISTCODE:
                builder.setTables(District.TABLENAME);
                builder.setProjectionMap(District.PROJECTION_MAP);
                break;
            case CodeProvider.DISTCODE_ITEM:
                builder.setTables(District.TABLENAME);
                builder.setProjectionMap(District.PROJECTION_MAP);
                builder.appendWhere("distcode = '" + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.PROVCODE:
                builder.setTables(Province.TABLENAME);
                builder.setProjectionMap(Province.PROJECTION_MAP);
                break;
            case CodeProvider.PROVCODE_ITEM:
                builder.setTables(Province.TABLENAME);
                builder.setProjectionMap(Province.PROJECTION_MAP);
                builder.appendWhere("provcode = '" + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.PERSONPROBLEM:
                builder.setTables(PersonProblem.TABLENAME);
                builder.setProjectionMap(PersonProblem.PROJECTION_MAP);
                break;
            case CodeProvider.PERSONPROBLEM_ITEM:
                builder.setTables(PersonProblem.TABLENAME);
                builder.setProjectionMap(PersonProblem.PROJECTION_MAP);
                builder.appendWhere("cpersonproblem.problemcode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.PERSONNEED:
                builder.setTables(PersonNeed.TABLENAME);
                builder.setProjectionMap(PersonNeed.PROJECTION_MAP);
                break;
            case CodeProvider.PERSONNEED_ITEM:
                builder.setTables(PersonNeed.TABLENAME);
                builder.setProjectionMap(PersonNeed.PROJECTION_MAP);
                builder.appendWhere("cpersonneed.needcode='"
                        + uri.getLastPathSegment() + "'");
                break;
            case CodeProvider.USEROFFICER:
                builder.setTables(UserOfficerType.TABLENAME);
                builder.setProjectionMap(UserOfficerType.PROJECTION_MAP);
                break;
            case CodeProvider.USEROFFICER_ITEM:
                builder.setTables(UserOfficerType.TABLENAME);
                builder.setProjectionMap(UserOfficerType.PROJECTION_MAP);
                builder.appendWhere("user.username='" + uri.getLastPathSegment()
                        + "'");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : "
                        + uri.toString());
        }

        Cursor c = builder.query(db, projection, selection, selectionArgs,
                groupby, having, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    public static class Diagnosis implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cdisease";
        protected static final String TABLENAME_WITH_HIT = TABLENAME
                + " INNER JOIN sysdiseasehit ON cdisease.diseasecode = sysdiseasehit.diseasecode";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/diagnosis");

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.diagnosis";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.diagnosis";

        public static final String CODE = "diseasecode";
        public static final String CODE506 = "cdisease.code506";
        public static final String NAME_ENG = "diseasename";
        public static final String NAME_TH = "diseasenamethai";
        public static final String MAP = "mapdisease";

        public static final String DEFAULT_SORTING = Diagnosis.CODE;

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Diagnosis._ID, "cdisease.diseasecode AS "
                    + Diagnosis._ID);
            PROJECTION_MAP.put(Diagnosis._NAME,
                    "ifnull(cdisease.diseasenamethai,cdisease.diseasename) AS "
                            + Diagnosis._NAME);
            PROJECTION_MAP.put(Diagnosis.CODE, "cdisease.diseasecode AS "
                    + Diagnosis.CODE);
            PROJECTION_MAP.put(Diagnosis.CODE506, Diagnosis.CODE506);
            PROJECTION_MAP.put(Diagnosis.NAME_ENG, Diagnosis.NAME_ENG);
            PROJECTION_MAP.put(Diagnosis.NAME_TH, Diagnosis.NAME_TH);
            PROJECTION_MAP.put(Diagnosis.MAP, Diagnosis.MAP);
            PROJECTION_MAP.put(Diagnosis._COUNT, "count(*) AS "
                    + Diagnosis._COUNT);
        }
    }

    public static class Drug implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cdrug";
        protected static final String TABLENAME_WITH_DOSE = TABLENAME
                + " INNER JOIN sysdrugdose ON cdrug.drugcode = sysdrugdose.drugcode";
        protected static final String TABLENAME_WITH_HIT = TABLENAME
                + " INNER JOIN sysdrughits ON cdrug.drugcode = sysdrughits.drugcode";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/drug");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.drug";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.drug";

        public static final String CODE = "drugcode";
        public static final String NAME = "drugname";
        public static final String COST = "cost";
        public static final String SELL = "sell";
        public static final String TYPE = "drugtype";
        public static final String DOSE = "dosedescription";
        public static final String DOSE_NO = "doseno";
        public static final String LOT_NO = "lotno";
        public static final String AMOUNT_DEFAULT = "amountdefaultpay";
        public static final String DATE_EXPIRE = "dateexpire";
        public static final String TOOTHTYPE = "toothtype";

        public static final String DEFAULT_SORTING = "length(cdrug.drugcode), cdrug.drugcode ";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Drug._ID, "cdrug.drugcode AS " + Drug._ID);
            PROJECTION_MAP.put(Drug._COUNT, "count(*) AS " + Drug._COUNT);
            PROJECTION_MAP.put(Drug.CODE, "cdrug.drugcode AS " + Drug.CODE);
            PROJECTION_MAP.put(Drug.NAME, "cdrug.drugname AS " + Drug.NAME);
            PROJECTION_MAP.put(Drug._NAME, "cdrug.drugname AS " + Drug._NAME);
            PROJECTION_MAP.put(Drug.COST, "cdrug.cost AS " + Drug.COST);
            PROJECTION_MAP.put(Drug.SELL, "cdrug.sell AS " + Drug.SELL);
            PROJECTION_MAP.put(Drug.TYPE, "cdrug.drugtype AS " + Drug.TYPE);
            PROJECTION_MAP.put(Drug.DOSE, "sysdrugdose.dosedescription AS "
                    + Drug.DOSE);
            PROJECTION_MAP.put(Drug.DOSE_NO, "sysdrugdose.doseno AS "
                    + Drug.DOSE_NO);
            PROJECTION_MAP.put(Drug.AMOUNT_DEFAULT, Drug.AMOUNT_DEFAULT);
            PROJECTION_MAP.put(Drug.LOT_NO, Drug.LOT_NO);
            PROJECTION_MAP.put(Drug.DATE_EXPIRE, Drug.DATE_EXPIRE);
            PROJECTION_MAP.put(Drug.TOOTHTYPE, Drug.TOOTHTYPE);
        }
    }

    public static class DrugFormula implements BaseColumns, NameColumn {
        public static final String TABLENAME = "sysdrugformula";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/drugformula");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.drugformula";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.drugformula";

        public static final String DEFAULT_SORTING = _ID;
        public static final String PCUCODE = "pcucode";
        public static final String DIAGCODE = "diagcode";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(_ID, "formulano AS " + _ID);
            PROJECTION_MAP.put(_NAME, "formulaname AS " + _NAME);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(DIAGCODE, DIAGCODE);

        }

        public static class Drug implements BaseColumns {
            public static final String TABLENAME = "sysdrugformuladetail";

            public static final Uri CONTENT_URI = Uri.parse("content://"
                    + AUTHORITY + "/drugformula/drug");

            public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                    + "/vnd.ffc.drugformula.drug";
            public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                    + "/vnd.ffc.drugformula.drug";

            public static final String DEFAULT_SORTING = _ID;
            public static final String PCUCODE = "pcucode";
            public static final String DRUG = "drugcode";
            public static final String AMOUNT = "amount";

            protected static final HashMap<String, String> PROJECTION_MAP;

            static {
                PROJECTION_MAP = new HashMap<String, String>();
                PROJECTION_MAP.put(_ID, "formulano AS " + _ID);
                PROJECTION_MAP.put(DRUG, DRUG);
                PROJECTION_MAP.put(PCUCODE, PCUCODE);
                PROJECTION_MAP.put(AMOUNT, AMOUNT);
            }
        }

        public static class Diagnosis implements BaseColumns {
            public static final String TABLENAME = "sysdrugformuladetaildiag";

            public static final Uri CONTENT_URI = Uri.parse("content://"
                    + AUTHORITY + "/drugformula/diag");

            public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                    + "/vnd.ffc.drugformula.diag";
            public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                    + "/vnd.ffc.drugformula.diag";

            public static final String DEFAULT_SORTING = _ID;
            public static final String PCUCODE = "pcucode";
            public static final String DIAGCODE = "diagcode";
            public static final String DXTYPE = "dxtype";

            protected static final HashMap<String, String> PROJECTION_MAP;

            static {
                PROJECTION_MAP = new HashMap<String, String>();
                PROJECTION_MAP.put(_ID, "formulano AS " + _ID);
                PROJECTION_MAP.put(PCUCODE, PCUCODE);
                PROJECTION_MAP.put(DIAGCODE, DIAGCODE);
                PROJECTION_MAP.put(DXTYPE, DXTYPE);
            }
        }
    }

    public static class Occupation implements BaseColumns {
        public static final String TABLENAME = "coccupa";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/occupa");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.occupa";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.occupa";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Occupation._ID, "coccupa.occupacode AS "
                    + Occupation._ID);
            PROJECTION_MAP.put(Occupation.CODE, "coccupa.occupacode AS "
                    + Occupation.CODE);
            PROJECTION_MAP.put(Occupation.NAME, "coccupa.occupaname AS "
                    + Occupation.NAME);
        }
    }

    public static class AncRisk implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cancrisk";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/ancrisk");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.ancrisk";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.ancrisk";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(AncRisk._ID, "cancrisk.ancriskcode AS "
                    + AncRisk._ID);
            PROJECTION_MAP.put(AncRisk.CODE, "cancrisk.ancriskcode AS "
                    + AncRisk.CODE);
            PROJECTION_MAP.put(_NAME, "cancrisk.ancriskname AS " + _NAME);
        }
    }

    public static class Right implements BaseColumns {
        public static final String TABLENAME = "cright";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/right");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.right";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.right";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String GROUP = "group";
        public static final String MAP = "map";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Right._ID, "cright.rightcode AS " + Right._ID);
            PROJECTION_MAP.put(Right.CODE, "cright.rightcode AS " + Right.CODE);
            PROJECTION_MAP.put(Right.NAME, "cright.rightname AS " + Right.NAME);
            PROJECTION_MAP.put(Right.GROUP, "cright.rightgroup AS "
                    + Right.GROUP);
            PROJECTION_MAP.put(Right.MAP, "cright.mapright AS " + Right.MAP);
        }
    }

    public static class Clinic implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cclinic";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/clinic");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.clinic";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.clinic";

        public static final String DEFAULT_SORTING = "cclinic.cliniccode";
        public static final String CODE = "cliniccode";
        public static final String NAME = "clinicdesc";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Clinic._ID, "cclinic.cliniccode AS "
                    + Clinic._ID);
            PROJECTION_MAP.put(Clinic.CODE, "cclinic.cliniccode AS "
                    + Clinic.CODE);
            PROJECTION_MAP.put(Clinic.NAME, "cclinic.clinicdesc AS "
                    + Clinic.NAME);
            PROJECTION_MAP.put(Clinic._NAME, "cclinic.clinicdesc AS "
                    + Clinic._NAME);
        }
    }

    public static class Nation implements BaseColumns {
        public static final String TABLENAME = "cnation";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/nation");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.nation";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.nation";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Nation._ID, "cnation.nationcode AS "
                    + Nation._ID);
            PROJECTION_MAP.put(Nation.CODE, "cnation.nationode AS "
                    + Nation.CODE);
            PROJECTION_MAP.put(Nation.NAME, "cnation.nationname AS "
                    + Nation.NAME);
        }
    }

    public static class Education implements BaseColumns {
        public static final String TABLENAME = "ceducation";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/education");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.education";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.education";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Education._ID,
                    "CAST(ceducation.educationcode AS LONG) AS "
                            + Education._ID);
            PROJECTION_MAP.put(Education.CODE, "ceducation.educationcode AS "
                    + Education.CODE);
            PROJECTION_MAP.put(Education.NAME, "ceducation.educationname AS "
                    + Education.NAME);
        }
    }

    public static class FamilyPosition implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cfamilyposition";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/familyposition");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.familyposition";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.familyposition";

        public static final String DEFAULT_SORTING = "famposcode";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(FamilyPosition._ID, "famposcode AS "
                    + FamilyPosition._ID);
            PROJECTION_MAP.put(FamilyPosition._NAME, "famposname AS "
                    + FamilyPosition._NAME);
        }
    }

    public static class Symtom implements BaseColumns {
        public static final String TABLENAME = "syssymtom";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/symtom");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.symtom";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.symtom";

        public static final String DEFAULT_SORTING = "symtom";
        public static final String NAME = "symtom";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Symtom._ID, "symtom AS " + Symtom._ID);
            PROJECTION_MAP.put(Symtom.NAME, "symtom AS " + Symtom.NAME);
        }
    }

    public static class SysSymtomco implements BaseColumns {
        public static final String TABLENAME = "syssymtomco";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/symtomco");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.symtomco";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.symtomco";

        public static final String DEFAULT_SORTING = "symtomco";
        public static final String NAME = "symtomco";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(SysSymtomco._ID, "symtomco AS " + SysSymtomco._ID);
            PROJECTION_MAP.put(SysSymtomco.NAME, "symtomco AS " + SysSymtomco.NAME);
        }
    }

    public static class SysDiagnote implements BaseColumns {
        public static final String TABLENAME = "sysdiagnote";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/diagnote");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.diagnote";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.diagnote";

        public static final String DEFAULT_SORTING = "diagnote";
        public static final String NAME = "diagnote";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(SysDiagnote._ID, "diagnote AS " + SysDiagnote._ID);
            PROJECTION_MAP.put(SysDiagnote.NAME, "diagnote AS " + SysDiagnote.NAME);
        }
    }

    public static class VitalSign implements BaseColumns {
        public static final String TABLENAME = "sysvitalsign";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/vitalsign");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.vitalsign";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.vitalsign";

        public static final String DEFAULT_SORTING = "vitalsign";
        public static final String NAME = "vitalsign";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VitalSign._ID, "sysvitalsign.vitalsign AS "
                    + VitalSign._ID);
            PROJECTION_MAP.put(VitalSign.NAME, "sysvitalsign.vitalsign AS "
                    + VitalSign.NAME);
        }
    }

    public static class HomeHealthSign implements BaseColumns {
        public static final String TABLENAME = "syshomehealth1";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/hhsign");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.hhsign";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.hhsign";

        public static final String DEFAULT_SORTING = "hhsign";
        public static final String SIGN = "hhsign";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(HomeHealthSign._ID, "syshomehealth1.hhsign AS "
                    + HomeHealthSign._ID);
            PROJECTION_MAP.put(HomeHealthSign.SIGN, "syshomehealth1.hhsign AS "
                    + HomeHealthSign.SIGN);
        }
    }

    public static class HomeHealthServiceCare implements BaseColumns {
        public static final String TABLENAME = "syshomehealth2";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/hhservicecare");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.hhservicecare";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.hhservicecare";

        public static final String DEFAULT_SORTING = "hhservicecare";
        public static final String SERVICE = "hhservicecare";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(HomeHealthServiceCare._ID,
                    "syshomehealth2.hhservicecare AS "
                            + HomeHealthServiceCare._ID);
            PROJECTION_MAP.put(HomeHealthServiceCare.SERVICE,
                    "syshomehealth2.hhservicecare AS "
                            + HomeHealthServiceCare.SERVICE);
        }
    }

    public static class HomeHealthEvalPlan implements BaseColumns {
        public static final String TABLENAME = "syshomehealth3";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/hhevalplan");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.hhevalplan";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.hhevalplan";

        public static final String DEFAULT_SORTING = "hhevalplan";
        public static final String PLAN = "hhevalplan";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(HomeHealthEvalPlan._ID,
                    "syshomehealth3.hhevalplan AS " + HomeHealthEvalPlan._ID);
            PROJECTION_MAP.put(HomeHealthEvalPlan.PLAN,
                    "syshomehealth3.hhevalplan AS " + HomeHealthEvalPlan.PLAN);
        }
    }

    public static class HealthSuggest implements BaseColumns {
        public static final String TABLENAME = "syshealthsuggest";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/healthsuggest");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.suggest";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.suggest";

        public static final String DEFAULT_SORTING = "symhsg";
        public static final String SUGGEST = "symhsg";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(HealthSuggest._ID, "syshealthsuggest.symhsg AS "
                    + HealthSuggest._ID);
            PROJECTION_MAP.put(HealthSuggest.SUGGEST,
                    "syshealthsuggest.symhsg AS " + HealthSuggest.SUGGEST);
        }
    }

    public static class HomeHealthType implements BaseColumns {
        public static final String TABLENAME = "chomehealthtype";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/hhtype");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.hhtype";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.hhtype";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(HomeHealthType._ID,
                    "chomehealthtype.homehealthcode AS " + HomeHealthType._ID);
            PROJECTION_MAP.put(HomeHealthType.CODE,
                    "chomehealthtype.homehealthcode AS " + HomeHealthType.CODE);
            PROJECTION_MAP.put(HomeHealthType.NAME,
                    "chomehealthtype.homehealthmeaning AS "
                            + HomeHealthType.NAME);
        }
    }

    public static class Hospital implements BaseColumns {
        public static final String TABLENAME = "chospital";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/hospital");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.hospital";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.hospital";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String TYPE = "type";
        public static final String PROVICE = "prov";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Hospital._ID, "chospital.hoscode AS "
                    + Hospital._ID);
            PROJECTION_MAP.put(Hospital.CODE, "chospital.hoscode AS "
                    + Hospital.CODE);
            PROJECTION_MAP.put(Hospital.NAME, "chospital.hosname AS "
                    + Hospital.NAME);
            PROJECTION_MAP.put(Hospital.TYPE, "chospital.hostype AS "
                    + Hospital.TYPE);
            PROJECTION_MAP.put(Hospital.PROVICE, "chospital.provcode AS "
                    + Hospital.PROVICE);
        }
    }

    public static class WaterType implements BaseColumns {
        public static final String TABLENAME = "cwatertype";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/watertype");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.watertype";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.watertype";

        public static final String DEFAULT_SORTING = "cwatertype.watertypecode";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String GROUP = "group";
        public static final String _NAME = "cwatertype.watertypegroup";
        public static final String _CODE = "cwatertype.watertypecode";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(WaterType._ID,
                    "CAST(cwatertype.watertypecode AS LONG) AS "
                            + WaterType._ID);
            PROJECTION_MAP.put(WaterType.CODE, "cwatertype.watertypecode AS "
                    + WaterType.CODE);
            PROJECTION_MAP.put(WaterType.NAME, "cwatertype.watertypename AS "
                    + WaterType.NAME);
            PROJECTION_MAP.put(WaterType.GROUP, "cwatertype.watertypegroup AS "
                    + WaterType.GROUP);
        }
    }

    public static class WaterOwner implements BaseColumns {
        public static final String TABLENAME = "cwaterowner";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/waterowner");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.waterowner";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.waterowner";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(WaterOwner._ID,
                    "CAST(cwaterowner.waterownercode AS LONG) AS "
                            + WaterOwner._ID);
            PROJECTION_MAP.put(WaterOwner.CODE,
                    "cwaterowner.waterownercode AS " + WaterOwner.CODE);
            PROJECTION_MAP.put(WaterOwner.NAME,
                    "cwaterowner.waterownername AS " + WaterOwner.NAME);
        }
    }

    public static class VessalWater implements BaseColumns {
        public static final String TABLENAME = "chousevesselwater";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/vesselWater");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.vesselWater";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.vesselWater";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(WaterType._ID,
                    "CAST(chousevesselwater.vesselwatercode AS LONG) AS "
                            + WaterType._ID);
            PROJECTION_MAP.put(WaterType.CODE,
                    "chousevesselwater.vesselwatercode AS " + WaterType.CODE);
            PROJECTION_MAP.put(WaterType.NAME,
                    "chousevesselwater.vesselwaterdesc AS " + WaterType.NAME);
        }
    }

    public static class PersonType implements BaseColumns {
        public static final String TABLENAME = "cpersontype";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/persontype");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.persontype";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.persontype";

        public static final String DEFAULT_SORTING = "code";
        public static final String CODE = "code";
        public static final String NAME = "name";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonType._ID, "cpersontype.persontypecode AS "
                    + PersonType._ID);
            PROJECTION_MAP.put(PersonType.CODE,
                    "cpersontype.persontypecode AS " + PersonType.CODE);
            PROJECTION_MAP.put(PersonType.NAME,
                    "cpersontype.persontypename AS " + PersonType.NAME);
        }
    }

    public static class PersonIncomplete implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cpersonincomplete";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/personincomplete");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.personincomplete";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.personincomplete";

        public static final String DEFAULT_SORTING = "incompletecode";
        public static final String CODE = "incompletecode";
        public static final String NAME = "incompletename";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonIncomplete._ID,
                    "CAST(cpersonincomplete.incompletecode AS LONG) AS "
                            + PersonIncomplete._ID);
            PROJECTION_MAP.put(PersonIncomplete._NAME, "incompletename as "
                    + PersonIncomplete._NAME);
            PROJECTION_MAP.put(PersonIncomplete.CODE, PersonIncomplete.CODE);
            PROJECTION_MAP.put(PersonIncomplete.NAME, PersonIncomplete.NAME);
        }
    }

    public static class PersonHelp implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cpersonhelp";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/personhelp");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.personhelp";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.personhelp";

        public static final String DEFAULT_SORTING = "helpcode";
        public static final String CODE = "helpcode";
        public static final String NAME = "helpname";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonHelp._ID,
                    "CAST(cpersonhelp.helpcode AS LONG) AS  " + PersonHelp._ID);
            PROJECTION_MAP.put(PersonHelp._NAME, "helpname as "
                    + PersonHelp._NAME);
            PROJECTION_MAP.put(PersonHelp.CODE, PersonHelp.CODE);
            PROJECTION_MAP.put(PersonHelp.NAME, PersonHelp.NAME);
        }
    }

    public static class ScreenOtherDisease implements BaseColumns {
        public static final String TABLENAME = "cscreenotherdisease";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/screenotherdisease");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.screenotherdisease";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.screenotherdisease";

        public static final String DEFAULT_SORTING = "screenotherdiseasecode";
        public static final String CODE = "screenotherdiseasecode";
        public static final String NAME = "screenotherdiseasedesc";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(ScreenOtherDisease._ID,
                    "CAST( cscreenotherdisease.screenotherdiseasecode AS LONG ) AS  "
                            + ScreenOtherDisease._ID);
            PROJECTION_MAP
                    .put(ScreenOtherDisease.CODE, ScreenOtherDisease.CODE);
            PROJECTION_MAP
                    .put(ScreenOtherDisease.NAME, ScreenOtherDisease.NAME);
        }
    }

    public static class Grow implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cgrow";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/grow");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.grow";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.grow";

        public static final String DEFAULT_SORTING = "growcode";
        public static final String CODE = "growcode";
        public static final String NAME = "growname";
        public static final String STD = "growstd";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Grow._ID, "cgrow.growcode AS " + Grow._ID);
            PROJECTION_MAP.put(Grow._NAME, "cgrow.growname AS " + Grow._NAME);
            PROJECTION_MAP.put(Grow.CODE, Grow.CODE);
            PROJECTION_MAP.put(Grow.NAME, Grow.NAME);
            PROJECTION_MAP.put(Grow.STD, Grow.STD);
        }
    }

    public static class Subdistrict implements BaseColumns, NameColumn {
        public static final String TABLENAME = "csubdistrict";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/subdistrict");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.subdistrict";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.subdistrict";

        public static final String DEFAULT_SORTING = "subdistcode";
        public static final String SUBDISTCODE = "subdistcode";
        public static final String NAME = "subdistname";
        public static final String DISTCODE = "distcode";
        public static final String PROVCODE = "provcode";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Subdistrict._ID, "subdistcode AS "
                    + Subdistrict._ID);
            PROJECTION_MAP.put(Subdistrict._NAME, "subdistname AS "
                    + Subdistrict._NAME);
            PROJECTION_MAP
                    .put(Subdistrict.SUBDISTCODE, Subdistrict.SUBDISTCODE);
            PROJECTION_MAP.put(Subdistrict.NAME, Subdistrict.NAME);
            PROJECTION_MAP.put(Subdistrict.DISTCODE, Subdistrict.DISTCODE);
            PROJECTION_MAP.put(Subdistrict.PROVCODE, Subdistrict.PROVCODE);
        }
    }

    public static class District implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cdistrict";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/district");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.district";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.district";

        public static final String DEFAULT_SORTING = "distcode";
        public static final String DISTCODE = "distcode";
        public static final String NAME = "distname";
        public static final String PROVCODE = "provcode";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(District._ID, "distcode AS " + District._ID);
            PROJECTION_MAP.put(District._NAME, "distname AS " + District._NAME);
            PROJECTION_MAP.put(District.DISTCODE, District.DISTCODE);
            PROJECTION_MAP.put(District.NAME, District.NAME);
            PROJECTION_MAP.put(District.PROVCODE, District.PROVCODE);
        }
    }

    public static class Province implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cprovince";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/province");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.province";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.province";

        public static final String DEFAULT_SORTING = "provcode";
        public static final String NAME = "provname";
        public static final String PROVCODE = "provcode";
        public static final String ZONE = "zone";
        public static final String REGION = "region";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Province._ID, "provcode AS " + Province._ID);
            PROJECTION_MAP.put(Province._NAME, "provname AS " + Province._NAME);
            PROJECTION_MAP.put(Province.NAME, Province.NAME);
            PROJECTION_MAP.put(Province.PROVCODE, Province.PROVCODE);
            PROJECTION_MAP.put(Province.ZONE, Province.ZONE);
            PROJECTION_MAP.put(Province.REGION, Province.REGION);
        }
    }

    public static class PersonProblem implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cpersonproblem";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/PersonProblem");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.PersonProblem";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.PersonProblem";

        public static final String DEFAULT_SORTING = "problemcode";
        public static final String CODE = "problemcode";
        public static final String NAME = "problemname";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonProblem._ID, "problemcode AS "
                    + PersonProblem._ID);
            PROJECTION_MAP.put(PersonProblem._NAME, "problemname AS "
                    + PersonProblem._NAME);
            PROJECTION_MAP.put(PersonProblem.CODE, PersonProblem.CODE);
            PROJECTION_MAP.put(PersonProblem.NAME, PersonProblem.NAME);

        }
    }

    public static class PersonNeed implements BaseColumns, NameColumn {
        public static final String TABLENAME = "cpersonneed";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/PersonNeed");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.PersonNeed";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.PersonNeed";

        public static final String DEFAULT_SORTING = "needcode";
        public static final String CODE = "needcode";
        public static final String NAME = "needname";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonNeed._ID, "needcode AS " + PersonNeed._ID);
            PROJECTION_MAP.put(PersonNeed._NAME, "needname AS "
                    + PersonNeed._NAME);
            PROJECTION_MAP.put(PersonNeed.CODE, PersonNeed.CODE);
            PROJECTION_MAP.put(PersonNeed.NAME, PersonNeed.NAME);

        }
    }

    public static class UserOfficerType implements BaseColumns, NameColumn {
        public static final String TABLENAME = "user";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/userofficertype");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.userofficertype";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.user.officertype";
        public static final String DEFAULT_SORTING = "username ASC";

        public static final String PCUCODE = "pcucode";
        public static final String USERNAME = "username";
        public static final String FNAME = "fname";
        public static final String LNAME = "lname";
        public static final String FULLNAME = "FULL_NAME";
        public static final String OFFICERTYPE = "officertype";
        public static final String OFFICERPOSITIONNAME = "officerposition";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(UserOfficerType._ID, UserOfficerType.USERNAME
                    + " AS " + UserOfficerType._ID);
            PROJECTION_MAP.put(UserOfficerType._NAME,
                    "(fname ||' '|| lname ) as " + UserOfficerType._NAME);
            PROJECTION_MAP
                    .put(UserOfficerType.PCUCODE, UserOfficerType.PCUCODE);
            PROJECTION_MAP.put(UserOfficerType.USERNAME,
                    UserOfficerType.USERNAME);
            PROJECTION_MAP.put(UserOfficerType.FULLNAME,
                    "(fname ||' '|| lname ) as " + UserOfficerType.FULLNAME);
            PROJECTION_MAP.put(UserOfficerType.OFFICERTYPE,
                    UserOfficerType.OFFICERTYPE);
            PROJECTION_MAP.put(UserOfficerType.OFFICERPOSITIONNAME,
                    UserOfficerType.OFFICERPOSITIONNAME);

        }
    }
}
