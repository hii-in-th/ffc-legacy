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

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import th.in.ffc.R;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.HouseProvider.Village;
import th.in.ffc.provider.UserProvider.User;
import th.in.ffc.provider.UserProvider.UserDatabaseOpenHelper;

import java.util.HashMap;
import java.util.List;

/**
 * Person's Content Provider with handler all about person such as PersonInfo,
 * RelationShip Health Information, Service & Visit Record
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since 1.0
 */
public class PersonProvider extends ContentProvider {

    public interface PersonColumns {
        public static final String _PCUCODEPERSON = "pcucodeperson";
        public static final String _PID = "pid";
    }

    public interface DateUpdateColumns {
        public static final String _DATEUPDATE = "dateupdate";
    }

    public static interface PregnancyColumns {
        public static final String _PREGNO = "pregno";
    }

    public static interface VisitColumns {
        public static final String _PCUCODE = "pcucode";
        public static final String _VISITNO = "visitno";
    }

    public static String AUTHORITY = "th.in.ffc.provider.PersonProvider";

    private static final int PERSON = 1;
    private static final int PERSON_ID = 2;
    private static final int PERSON_BY_HOUSE = 3;
    private static final int PERSON_WITH_HOUSE = 331;
    private static final int FAMILY_NO = 123;
    private static final int BEHAVIOR = 4;
    private static final int BEHAVIOR_ID = 5;
    private static final int DEATH = 6;
    private static final int DEATH_ID = 7;
    private static final int CHRONIC = 8;
    private static final int CHRONIC_ID = 9;
    private static final int TEMPLE = 10;
    private static final int TEMPLE_ID = 11;
    private static final int PROTAGONIST = 12;
    private static final int PROTAGONIST_ID = 13;
    private static final int VOLA = 14;
    private static final int VOLA_ID = 15;
    private static final int VISIT = 16;
    private static final int VISIT_ID = 17;
    private static final int VISIT_ANC = 50;
    private static final int VISIT_ANC_ID = 51;
    private static final int VISIT_PREGNANC = 60;
    private static final int VISIT_PREGNANC_ID = 62;
    private static final int VISIT_EPI = 63;
    private static final int VISIT_EPI_VN = 65;
    private static final int VISIT_EPI_ID = 64;
    private static final int VISIT_ANC_RISK = 52;
    private static final int VISIT_ANC_RISK_ID = 53;
    private static final int VISIT_ANC_RISK_PID = 54;
    private static final int VISIT_ANC_RISK_PREGNO = 57;
    private static final int VISIT_BLOOD_LAB = 55;
    private static final int VISIT_BLOOD_LAB_ID = 56;
    private static final int VISIT_DIAG = 18;
    private static final int VISIT_DIAG_ID = 20;
    private static final int VISIT_DRUG = 21;
    private static final int VISIT_DRUG_ID = 22;
    private static final int CHRONIC_FAMILY = 401;
    private static final int CHRONIC_FAMILY_ID = 402;

    private static final int WOMEN = 23;
    private static final int WOMEN_ID = 24;
    private static final int VISITHOMEHEALTHINDIVIDUAL = 25;
    private static final int VISITHOMEHEALTHINDIVIDUAL_VISITNO = 26;
    private static final int VISITFP = 27;
    private static final int VISITFP_VISITNO = 28;
    private static final int VISITLABCANCER = 29;
    private static final int VISITLABCANCER_VISITNO = 30;
    private static final int VISITBABYCARE = 31;
    private static final int VISITBABYCARE_VISITNO = 32;
    private static final int VISITDIAG506 = 33;
    private static final int VISITDIAG506_ITEM = 34;
    private static final int VISITSPECIALPERSON = 35;
    private static final int VISITSPECIALPERSON_ITEM = 36;
    private static final int VISITSCREENSPECIALDISEASE = 37;
    private static final int VISITSCREENSPECIALDISEASE_ITEM = 38;
    private static final int VISITANCDELIVER = 39;
    private static final int VISITANCDELIVER_ITEM = 40;
    private static final int VISITANCMOTHERCARE = 41;
    private static final int VISITANCMOTHERCARE_ITEM = 42;
    private static final int VISITOLDTER = 43;
    private static final int VISITOLDTER_ITEM = 44;
    private static final int VISITNUTRITION = 45;
    private static final int VISITNUTRITION_ITEM = 46;
    private static final int VISITDIAGAPPOINT = 47;
    private static final int VISITDIAGAPPOINT_ITEM = 48;
    private static final int VISITDENTALCHECK = 310;
    private static final int VISITDENTALCHECK_ITEM = 311;
    private static final int VISITPERSONGROW = 312;
    private static final int VISITPERSONGROW_ITEM = 313;
    private static final int PERSONDEATH = 314;
    private static final int PERSONDEATH_ITEM = 315;
    private static final int VISITEPIAPPOINT = 316;
    private static final int VISITEPIAPPOINT_ITEM = 317;
    private static final int PERSONUNABLETYPE = 901;
    private static final int PERSONUNABLETYPE_ITEM = 902;
    private static final int PERSONUNABLEPROB = 903;
    private static final int PERSONUNABLEPROB_ITEM = 904;
    private static final int PERSONUNABLENEED = 905;
    private static final int PERSONUNABLENEED_ITEM = 906;
    private static final int PERSONUNABLEHELP = 907;
    private static final int PERSONUNABLEHELP_ITEM = 908;
    private static final int VISITDRUGDENTAL = 909;
    private static final int VISITDRUGDENTAL_ITEM = 910;
    private static final int VISITDRUGDENTALDIAG = 911;
    private static final int VISITDRUGDENTALDIAG_ITEM = 912;
    private static final int PERSONUNABLE = 913;

    private static final int NCDPERSON = 662;
    private static final int NCDPERSONTYPE = 663;
    private static final int NCDPERSONPATIENT = 664;
    private static final int NCDPERSONSYMPTOM = 665;
    private static final int NCDPERSONSCREEN = 666;
    private static final int NCDJOIN = 777;
    private static final int VISIT506_PERSON = 778;
    private static final int VISIT506_PERSON_ITEM = 779;
    private static final int GETADDRESS = 782;
    private static final int GETADDRESS_ITEM = 783;
    private static final int PERSON_BALL_COLOR = 784;
    private static final int PERSON_BALL_COLOR_ITEM = 785;
    private static final int FFC_506RADIUS = 786;
    private static final int FFC_506RADIUS_ITEM = 787;
    private static final int VISIT_LABSUGARBLOOD = 788;
    private static final int VISIT_LABSUGARBLOOD_ITEM = 789;
    private static final int VISIT_VISIT_LABSUGARBLOOD = 790;
    private static final int VISIT_VISIT_LABSUGARBLOOD_ITEM = 791;
    private static final int PERSON_HOUSE = 792;
    private static final int PERSON_HOUSE_ITEM = 793;
    private static final int PERSON_BALL_RED_COLOR = 794;
    private static final int PERSON_BALL_RED_COLOR_ITEM = 795;
    private static final int PERSON_NCD_BALL = 796;
    private static final int PERSON_NCD_BALL_ITEM = 797;

    private DbOpenHelper mOpenHelper;
    private static UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, "person", PERSON);
        mUriMatcher.addURI(AUTHORITY, "person/#", PERSON_ID);
        mUriMatcher.addURI(AUTHORITY, "person/house/#", PERSON_BY_HOUSE);
        mUriMatcher.addURI(AUTHORITY, "person/house/#/family", FAMILY_NO);
        mUriMatcher.addURI(AUTHORITY, "person/house", PERSON_WITH_HOUSE);
        mUriMatcher.addURI(AUTHORITY, "person/behavior", BEHAVIOR);
        mUriMatcher.addURI(AUTHORITY, "person/behavior/#", BEHAVIOR_ID);
        mUriMatcher.addURI(AUTHORITY, "person/death", DEATH);
        mUriMatcher.addURI(AUTHORITY, "person/death/#", DEATH_ID);
        mUriMatcher.addURI(AUTHORITY, "person/chronic", CHRONIC);
        mUriMatcher.addURI(AUTHORITY, "person/chronic/#", CHRONIC_ID);
        mUriMatcher.addURI(AUTHORITY, "person/temple", TEMPLE);
        mUriMatcher.addURI(AUTHORITY, "person/temple/#", TEMPLE_ID);
        mUriMatcher.addURI(AUTHORITY, "person/protagonist", PROTAGONIST);
        mUriMatcher.addURI(AUTHORITY, "person/protagonist/#", PROTAGONIST_ID);
        mUriMatcher.addURI(AUTHORITY, "person/protagonist/vola", VOLA);
        mUriMatcher.addURI(AUTHORITY, "person/protagonist/vola/#", VOLA_ID);
        mUriMatcher.addURI(AUTHORITY, "person/chronicfamily", CHRONIC_FAMILY);
        mUriMatcher.addURI(AUTHORITY, "person/chronicfamily/#", CHRONIC_FAMILY_ID);
        mUriMatcher.addURI(AUTHORITY, "person/visit", VISIT);
        mUriMatcher.addURI(AUTHORITY, "person/visit/#", VISIT_ID);
        mUriMatcher.addURI(AUTHORITY, "person/visit/anc", VISIT_ANC);
        mUriMatcher.addURI(AUTHORITY, "person/visit/anc/#", VISIT_ANC_ID);
        mUriMatcher.addURI(AUTHORITY, "person/anc", VISIT_PREGNANC);
        mUriMatcher.addURI(AUTHORITY, "person/#/anc/#", VISIT_PREGNANC_ID);
        mUriMatcher.addURI(AUTHORITY, "person/anc/risk", VISIT_ANC_RISK);
        mUriMatcher.addURI(AUTHORITY, "person/#/anc/risk", VISIT_ANC_RISK_PID);
        mUriMatcher.addURI(AUTHORITY, "person/#/anc/#/risk", VISIT_ANC_RISK_PREGNO);
        mUriMatcher.addURI(AUTHORITY, "person/#/anc/#/risk/*", VISIT_ANC_RISK_ID);
        mUriMatcher.addURI(AUTHORITY, "person/visit/epi", VISIT_EPI);
        mUriMatcher.addURI(AUTHORITY, "person/visit/#/epi/*", VISIT_EPI_ID);
        mUriMatcher.addURI(AUTHORITY, "person/visit/#/epi", VISIT_EPI_VN);
        mUriMatcher.addURI(AUTHORITY, "person/visit/labblood", VISIT_BLOOD_LAB);
        mUriMatcher.addURI(AUTHORITY, "person/visit/labblood/#", VISIT_BLOOD_LAB_ID);
        mUriMatcher.addURI(AUTHORITY, "person/visit/diag", VISIT_DIAG);
        mUriMatcher.addURI(AUTHORITY, "person/visit/#/diag/*", VISIT_DIAG_ID);
        mUriMatcher.addURI(AUTHORITY, "person/visit/drug", VISIT_DRUG);
        mUriMatcher.addURI(AUTHORITY, "person/visit/#/drug/*", VISIT_DRUG_ID);
        mUriMatcher.addURI(AUTHORITY, "person/women", WOMEN);
        mUriMatcher.addURI(AUTHORITY, "person/women/#", WOMEN_ID);
        mUriMatcher.addURI(AUTHORITY, "person/visit/homehealthindividual", VISITHOMEHEALTHINDIVIDUAL);
        mUriMatcher.addURI(AUTHORITY, "person/visit/homehealthindividual/#", VISITHOMEHEALTHINDIVIDUAL_VISITNO);
        mUriMatcher.addURI(AUTHORITY, "person/visit/familyplan", VISITFP);
        mUriMatcher.addURI(AUTHORITY, "person/visit/familyplan/#", VISITFP_VISITNO);
        mUriMatcher.addURI(AUTHORITY, "person/visit/labcancer", VISITLABCANCER);
        mUriMatcher.addURI(AUTHORITY, "person/visit/labcancer/#", VISITLABCANCER_VISITNO);
        mUriMatcher.addURI(AUTHORITY, "person/visit/babycare", VISITBABYCARE);
        mUriMatcher.addURI(AUTHORITY, "person/visit/babycare/#", VISITBABYCARE_VISITNO);
        mUriMatcher.addURI(AUTHORITY, "person/visit/diag506", VISITDIAG506);
        mUriMatcher.addURI(AUTHORITY, "person/visit/diag506/#", VISITDIAG506_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/specialperson", VISITSPECIALPERSON);
        mUriMatcher.addURI(AUTHORITY, "person/visit/specialperson/#", VISITSPECIALPERSON_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/screenspecialdisease", VISITSCREENSPECIALDISEASE);
        mUriMatcher.addURI(AUTHORITY, "person/visit/screenspecialdisease/#", VISITSCREENSPECIALDISEASE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/ancdeliver", VISITANCDELIVER);
        mUriMatcher.addURI(AUTHORITY, "person/visit/ancdeliver/#", VISITANCDELIVER_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/ancmothercare", VISITANCMOTHERCARE);
        mUriMatcher.addURI(AUTHORITY, "person/visit/ancmothercare/#", VISITANCMOTHERCARE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/oldter", VISITOLDTER);
        mUriMatcher.addURI(AUTHORITY, "person/visit/oldter/#", VISITOLDTER_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/nutrition", VISITNUTRITION);
        mUriMatcher.addURI(AUTHORITY, "person/visit/nutrition/#", VISITNUTRITION_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/diagappoint", VISITDIAGAPPOINT);
        mUriMatcher.addURI(AUTHORITY, "person/visit/diagappoint/#", VISITDIAGAPPOINT_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/dentalcheck", VISITDENTALCHECK);
        mUriMatcher.addURI(AUTHORITY, "person/visit/dentalcheck/#", VISITDENTALCHECK_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/persongrow", VISITPERSONGROW);
        mUriMatcher.addURI(AUTHORITY, "person/visit/persongrow/#", VISITPERSONGROW_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/persondeath", PERSONDEATH);
        mUriMatcher.addURI(AUTHORITY, "person/visit/persondeath/#", PERSONDEATH_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/epiappoint", VISITEPIAPPOINT);
        mUriMatcher.addURI(AUTHORITY, "person/visit/epiappoint/#", VISITEPIAPPOINT_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/unabletype", PERSONUNABLETYPE);
        mUriMatcher.addURI(AUTHORITY, "person/unabletype/#", PERSONUNABLETYPE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/unableproblem", PERSONUNABLEPROB);
        mUriMatcher.addURI(AUTHORITY, "person/unableproblem/#", PERSONUNABLEPROB_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/unableneed", PERSONUNABLENEED);
        mUriMatcher.addURI(AUTHORITY, "person/unableneed/#", PERSONUNABLENEED_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/unablehelp", PERSONUNABLEHELP);
        mUriMatcher.addURI(AUTHORITY, "person/unablehelp/#", PERSONUNABLEHELP_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/drugdental", VISITDRUGDENTAL);
        mUriMatcher.addURI(AUTHORITY, "person/visit/drugdental/#", VISITDRUGDENTAL_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/drugdentaldiag", VISITDRUGDENTALDIAG);
        mUriMatcher.addURI(AUTHORITY, "person/visit/drugdentaldiag/#", VISITDRUGDENTALDIAG_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/unable", PERSONUNABLE);
        mUriMatcher.addURI(AUTHORITY, "person/ncdperson", NCDPERSON);
        mUriMatcher.addURI(AUTHORITY, "person/ncdperson_type", NCDPERSONTYPE);
        mUriMatcher.addURI(AUTHORITY, "person/ncdperson_patienttype", NCDPERSONPATIENT);
        mUriMatcher.addURI(AUTHORITY, "person/ncdperson_symptom", NCDPERSONSYMPTOM);
        mUriMatcher.addURI(AUTHORITY, "person/ncdperson_screen", NCDPERSONSCREEN);
        mUriMatcher.addURI(AUTHORITY, "person/ncdperson_screen_join", NCDJOIN);
        mUriMatcher.addURI(AUTHORITY, "person/visit506_person", VISIT506_PERSON);
        mUriMatcher.addURI(AUTHORITY, "person/visit506_person/#", VISIT506_PERSON_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/get_address", GETADDRESS);
        mUriMatcher.addURI(AUTHORITY, "person/get_address/#", GETADDRESS_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/personriskblackball", PERSON_BALL_COLOR);
        mUriMatcher.addURI(AUTHORITY, "person/personriskblackball/#", PERSON_BALL_COLOR_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/personriskredgrayball", PERSON_BALL_RED_COLOR);
        mUriMatcher.addURI(AUTHORITY, "person/personriskredgrayball/#", PERSON_BALL_RED_COLOR_ITEM);

        mUriMatcher.addURI(AUTHORITY, "person/ffc_506radius", FFC_506RADIUS);
        mUriMatcher.addURI(AUTHORITY, "person/ffc_506radius/#", FFC_506RADIUS_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visitlabsugarblood", VISIT_LABSUGARBLOOD);
        mUriMatcher.addURI(AUTHORITY, "person/visitlabsugarblood/#", VISIT_LABSUGARBLOOD_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/visit/visit_visitlabsugarblood", VISIT_VISIT_LABSUGARBLOOD);
        mUriMatcher.addURI(AUTHORITY, "person/visit/visit_visitlabsugarblood/#", VISIT_VISIT_LABSUGARBLOOD_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/personhouse", PERSON_HOUSE);
        mUriMatcher.addURI(AUTHORITY, "person/personhouse/#", PERSON_HOUSE_ITEM);
        mUriMatcher.addURI(AUTHORITY, "person/personncdball", PERSON_NCD_BALL);
        mUriMatcher.addURI(AUTHORITY, "person/personncdball/#", PERSON_NCD_BALL_ITEM);


    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int delete = 0;
        String[] whereArgs;
        switch (mUriMatcher.match(uri)) {
            case VISIT:
                delete = db.delete(Visit.TABLENAME, selection, selectionArgs);
                break;
            case VISIT_ID:
                delete = db.delete(Visit.TABLENAME, "visitno=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case VISIT_DIAG:
                delete = db.delete(VisitDiag.TABLENAME, selection, selectionArgs);
                break;
            case VISIT_DIAG_ID:
                String visitno = uri.getPathSegments().get(2);
                String diagcode = uri.getPathSegments().get(4);
                whereArgs = new String[]{visitno, diagcode};
                delete = db.delete(VisitDiag.TABLENAME, "visitno=? AND diagcode=?",
                        whereArgs);
                break;
            case VISIT_DRUG:
                delete = db.delete(VisitDrug.TABLENAME, selection, selectionArgs);
                break;
            case VISIT_DRUG_ID:
                String visitDrugNo = uri.getPathSegments().get(2);
                String drugcode = uri.getPathSegments().get(4);
                whereArgs = new String[]{visitDrugNo, drugcode};
                delete = db.delete(VisitDrug.TABLENAME, "visitno=? AND drugcode=?",
                        whereArgs);
                break;
            case VISIT_EPI:
                delete = db.delete(VisitEpi.TABLENAME, selection, selectionArgs);
                break;
            case VISIT_EPI_ID:
                whereArgs = new String[]{uri.getPathSegments().get(2),
                        uri.getPathSegments().get(4),};
                delete = db.delete(VisitEpi.TABLENAME,
                        "visitno=? AND vaccinecode=?", whereArgs);
                break;
            case VISIT_ANC:
                delete = db.delete(VisitAnc.TABLENAME, selection, selectionArgs);
                break;
            case VISIT_ANC_RISK:
                delete = db
                        .delete(VisitAncRisk.TABLENAME, selection, selectionArgs);
                break;
            case VISIT_ANC_RISK_ID:
                String pid = uri.getPathSegments().get(1);
                String pregno = uri.getPathSegments().get(3);
                String riskNo = uri.getLastPathSegment();
                whereArgs = new String[]{pid, pregno, riskNo};
                delete = db.delete(VisitAncRisk.TABLENAME,
                        "pid=? AND pregno=? AND ancriskcode=?", whereArgs);
                break;
            case VISIT_BLOOD_LAB:
                delete = db.delete(VisitLabBlood.TABLENAME, selection,
                        selectionArgs);
                break;
            case WOMEN:
                delete = db.delete(Women.TABLENAME, selection, selectionArgs);
                break;
            case VISITHOMEHEALTHINDIVIDUAL:
                delete = db.delete(VisitIndividual.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITFP:
                delete = db.delete(VisitFamilyplan.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITLABCANCER:
                delete = db.delete(VisitLabcancer.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITBABYCARE:
                delete = db.delete(VisitBabycare.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITDIAG506:
                delete = db.delete(VisitDiag506address.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITSPECIALPERSON:
                delete = db.delete(VisitSpecialperson.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITSCREENSPECIALDISEASE:
                delete = db.delete(VisitScreenspecialdisease.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITANCDELIVER:
                delete = db.delete(VisitAncDeliver.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITANCMOTHERCARE:
                delete = db.delete(VisitAncMotherCare.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITOLDTER:
                delete = db.delete(VisitOldter.TABLENAME, selection, selectionArgs);
                break;
            case VISITNUTRITION:
                delete = db.delete(VisitNutrition.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITDIAGAPPOINT:
                delete = db.delete(VisitDiagAppoint.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITDENTALCHECK:
                delete = db.delete(VisitDentalCheck.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITPERSONGROW:
                delete = db.delete(VisitPersongrow.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITEPIAPPOINT:
                delete = db.delete(VisitEpiAppoint.TABLENAME, selection,
                        selectionArgs);
                break;

            case PERSONUNABLETYPE:
                delete = db.delete(PersonunableType.TABLENAME, selection,
                        selectionArgs);
                break;

            case PERSONUNABLEPROB:
                delete = db.delete(PersonunableProblem.TABLENAME, selection,
                        selectionArgs);
                break;

            case PERSONUNABLENEED:
                delete = db.delete(PersonunableNeed.TABLENAME, selection,
                        selectionArgs);
                break;

            case PERSONUNABLEHELP:
                delete = db.delete(PersonunableHelp.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITDRUGDENTAL:
                delete = db.delete(VisitDrugDental.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISITDRUGDENTALDIAG:
                delete = db.delete(VisitDrugDentalDiag.TABLENAME, selection,
                        selectionArgs);
                break;
            case PERSONUNABLE:
                delete = db
                        .delete(Personunable.TABLENAME, selection, selectionArgs);
                break;
            case NCDPERSON:
                delete = db.delete(NCDPerson.TABLENAME, selection, selectionArgs);
                break;
            case NCDPERSONTYPE:
                delete = db
                        .delete(NCDPersonNCD.TABLENAME, selection, selectionArgs);
                break;
            case NCDPERSONPATIENT:
                delete = db.delete(NCDPersonNCDHist.TABLENAME, selection,
                        selectionArgs);
                break;
            case NCDPERSONSYMPTOM:
                delete = db.delete(NCDPersonNCDHistDetail.TABLENAME, selection,
                        selectionArgs);
                break;
            case NCDPERSONSCREEN:
                delete = db.delete(NCDPersonNCDScreen.TABLENAME, selection,
                        selectionArgs);
                break;
            case FFC_506RADIUS:
                delete = db.delete(FFC506RADIUS.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISIT_LABSUGARBLOOD:
                delete = db.delete(VISITLABSUGARBLOOD.TABLENAME, selection,
                        selectionArgs);
                break;
            case VISIT_VISIT_LABSUGARBLOOD:
                delete = db.delete(VISIT_VISITLABSUGARBLOOD.TABLENAME, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : "
                        + uri.toString());

        }
        return delete;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case PERSON:
            case PERSON_BY_HOUSE:
            case DEATH:
            case CHRONIC:
            case VOLA:
            case PROTAGONIST:
            case TEMPLE:
                return Person.CONTENT_DIR_TYPE;

            case PERSON_ID:
            case DEATH_ID:
            case CHRONIC_ID:
            case VOLA_ID:
            case PROTAGONIST_ID:
            case TEMPLE_ID:
                return Person.CONTENT_ITEM_TYPE;

            case WOMEN:
                return Women.CONTENT_DIR_TYPE;
            case WOMEN_ID:
                return Women.CONTENT_ITEM_TYPE;

            case BEHAVIOR:
                return Behavior.CONTENT_DIR_TYPE;
            case BEHAVIOR_ID:
                return Behavior.CONTENT_ITEM_TYPE;

            case VISIT:
                return Visit.CONTENT_DIR_TYPE;
            case VISIT_ID:
                return Visit.CONTENT_ITEM_TYPE;

            case VISIT_DIAG:
                return VisitDiag.CONTENT_DIR_TYPE;
            case VISIT_DIAG_ID:
                return VisitDiag.CONTENT_ITEM_TYPE;

            case VISIT_DRUG:
                return VisitDrug.CONTENT_DIR_TYPE;
            case VISIT_DRUG_ID:
                return VisitDrug.CONTENT_ITEM_TYPE;

            default:
                return null;
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long id = 0;
        Uri uriReturn = null;

        switch (mUriMatcher.match(uri)) {
            case PERSON:
                id = db.insert(Person.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = ContentUris.withAppendedId(Person.CONTENT_URI,
                            values.getAsLong(Person.PID));
                break;
            case BEHAVIOR:
                id = db.insert(Behavior.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = ContentUris.withAppendedId(Behavior.CONTENT_URI,
                            values.getAsLong(Behavior.PID));
                break;
            case VISIT:
                id = db.insert(Visit.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = ContentUris.withAppendedId(Visit.CONTENT_URI,
                            values.getAsLong(Visit.NO));
                break;
            case VISIT_ANC:
                id = db.insert(VisitAnc.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = ContentUris.withAppendedId(VisitAnc.CONTENT_URI,
                            values.getAsLong(VisitAnc._VISITNO));
                break;
            case VISIT_PREGNANC:
                id = db.insert(VisitAncPregnancy.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = VisitAncPregnancy.getContentUri(
                            values.getAsString(PersonColumns._PID),
                            values.getAsString(PregnancyColumns._PREGNO));
                break;
            case VISIT_ANC_RISK:
                id = db.insert(VisitAncRisk.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = VisitAncRisk.getContentUri(
                            values.getAsString(PersonColumns._PID),
                            values.getAsString(PregnancyColumns._PREGNO),
                            values.getAsString(VisitAncRisk.CODE));
                break;
            case VISIT_BLOOD_LAB:
                id = db.insert(VisitLabBlood.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = ContentUris.withAppendedId(
                            VisitLabBlood.CONTENT_URI,
                            values.getAsLong(VisitLabBlood._VISITNO));
                break;
            case VISIT_EPI:
                id = db.insert(VisitEpi.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = Uri.withAppendedPath(VisitEpi.CONTENT_URI,
                            values.getAsString(VisitLabBlood._VISITNO));
                break;
            case VISIT_DIAG:
                id = db.insert(VisitDiag.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = VisitDiag.getContentUriId(
                            values.getAsLong(VisitDiag.NO),
                            values.getAsString(VisitDiag.CODE));
                break;
            case VISIT_DRUG:
                id = db.insert(VisitDrug.TABLENAME, null, values);
                if (id > 0)
                    uriReturn = VisitDrug.getContentUriId(
                            values.getAsLong(VisitDrug.NO),
                            values.getAsString(VisitDrug.CODE));
                break;
            case VISITHOMEHEALTHINDIVIDUAL:
                id = db.insert(VisitIndividual.TABLENAME, null, values);
                return null;
            case VISITFP:
                id = db.insert(VisitFamilyplan.TABLENAME, null, values);
                return null;
            case VISITBABYCARE:
                id = db.insert(VisitBabycare.TABLENAME, null, values);
                return null;
            case VISITLABCANCER:
                id = db.insert(VisitLabcancer.TABLENAME, null, values);
                return null;
            case WOMEN:
                id = db.insert(Women.TABLENAME, null, values);
                return null;
            case VISITDIAG506:
                id = db.insert(VisitDiag506address.TABLENAME, null, values);
                return null;
            case VISITSPECIALPERSON:
                id = db.insert(VisitSpecialperson.TABLENAME, null, values);
                return null;
            case VISITSCREENSPECIALDISEASE:
                id = db.insert(VisitScreenspecialdisease.TABLENAME, null, values);
                return null;
            case VISITANCDELIVER:
                id = db.insert(VisitAncDeliver.TABLENAME, null, values);
                return null;
            case VISITANCMOTHERCARE:
                id = db.insert(VisitAncMotherCare.TABLENAME, null, values);
                return null;
            case VISITOLDTER:
                id = db.insert(VisitOldter.TABLENAME, null, values);
                return null;
            case VISITNUTRITION:
                id = db.insert(VisitNutrition.TABLENAME, null, values);
                return null;
            case VISITDIAGAPPOINT:
                id = db.insert(VisitDiagAppoint.TABLENAME, null, values);
                return null;
            case VISITDENTALCHECK:
                id = db.insert(VisitDentalCheck.TABLENAME, null, values);
                return null;
            case VISITPERSONGROW:
                id = db.insert(VisitPersongrow.TABLENAME, null, values);
                return null;
            case PERSONDEATH:
                id = db.insert(PersonDeath.TABLENAME, null, values);
                return null;
            case VISITEPIAPPOINT:
                id = db.insert(VisitEpiAppoint.TABLENAME, null, values);
                return null;
            case PERSONUNABLETYPE:
                id = db.insert(PersonunableType.TABLENAME, null, values);
                return null;
            case PERSONUNABLEPROB:
                id = db.insert(PersonunableProblem.TABLENAME, null, values);
                return null;
            case PERSONUNABLENEED:
                id = db.insert(PersonunableNeed.TABLENAME, null, values);
                return null;
            case PERSONUNABLEHELP:
                id = db.insert(PersonunableHelp.TABLENAME, null, values);
                return null;
            case VISITDRUGDENTAL:
                id = db.insert(VisitDrugDental.TABLENAME, null, values);
                return null;
            case VISITDRUGDENTALDIAG:
                id = db.insert(VisitDrugDentalDiag.TABLENAME, null, values);
                return null;
            case PERSONUNABLE:
                id = db.insert(Personunable.TABLENAME, null, values);
                return null;
            case NCDPERSON:
                id = db.insert(NCDPerson.TABLENAME, null, values);
                Log.d("INSERT_PERSON ID", id + "");
                return null;
            case NCDPERSONTYPE:
                id = db.insert(NCDPersonNCD.TABLENAME, null, values);
                return null;
            case NCDPERSONPATIENT:
                id = db.insert(NCDPersonNCDHist.TABLENAME, null, values);
                return null;
            case NCDPERSONSYMPTOM:
                id = db.insert(NCDPersonNCDHistDetail.TABLENAME, null, values);
                return null;
            case NCDPERSONSCREEN:
                id = db.insert(NCDPersonNCDScreen.TABLENAME, null, values);
                return null;
            case FFC_506RADIUS:
                id = db.insert(FFC506RADIUS.TABLENAME, null, values);
                return null;
            case VISIT_LABSUGARBLOOD:
                id = db.insert(VISITLABSUGARBLOOD.TABLENAME, null, values);
                return null;
            case VISIT_VISIT_LABSUGARBLOOD:
                id = db.insert(VISIT_VISITLABSUGARBLOOD.TABLENAME, null, values);
                return null;
            default:
                throw new IllegalArgumentException("Unknown URI : "
                        + uri.toString());
        }

        if (id > 0) {
            getContext().getContentResolver().notifyChange(uriReturn, null);
        }
        return uriReturn;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbOpenHelper(this.getContext());
        return true;
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

        if (TextUtils.isEmpty(sortOrder))
            sortOrder = Person.DEFAULT_SORTING;

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        switch (mUriMatcher.match(uri)) {
            case PERSON:
                builder.setTables(Person.TABLENAME);
                builder.setProjectionMap(Person.PROJECTION_MAP);
                break;
            case PERSON_ID:
                builder.setTables(Person.TABLENAME);
                builder.setProjectionMap(Person.PROJECTION_MAP);
                String id = uri.getLastPathSegment();
                if (id.matches("\\d{13}"))
                    builder.appendWhere("person.idcard=" + id);
                else
                    builder.appendWhere("person.pid=" + id);
                break;
            case PERSON_BY_HOUSE:
                builder.setTables(Person.TABLENAME);
                builder.setProjectionMap(Person.PROJECTION_MAP);
                builder.appendWhere("person.hcode=" + uri.getLastPathSegment());
                break;
            case PERSON_WITH_HOUSE:
                builder.setTables(Person.TABLENAME + " LEFT JOIN "
                        + House.TABLENAME + " ON person.hcode = house.hcode");
                builder.setProjectionMap(Person.PROJECTION_MAP);
                break;
            case FAMILY_NO:
                builder.setTables(Person.TABLENAME);
                builder.setProjectionMap(Person.PROJECTION_MAP);
                builder.appendWhere("hcode=" + uri.getPathSegments().get(2));
                groupby = "familyno";
                break;

            case BEHAVIOR_ID:
                builder.appendWhere(Behavior.PID + "=" + uri.getLastPathSegment());
            case BEHAVIOR:
                builder.setTables(Behavior.TABLENAME);
                builder.setProjectionMap(Behavior.PROJECTION_MAP);
                break;

            case PROTAGONIST_ID:
                builder.appendWhere(Protagonist.PID + "="
                        + uri.getLastPathSegment());
            case PROTAGONIST:
                builder.setTables(Protagonist.TABLENAME_WITH_PERSON);
                builder.setProjectionMap(Protagonist.PROJECTION_MAP);
                break;

            case VOLA_ID:
                builder.appendWhere("persontype.typecode='09' AND "
                        + Protagonist.PID + "=" + uri.getLastPathSegment());
            case VOLA:
                builder.setTables(Protagonist.TABLENAME_WITH_PERSON);
                builder.setProjectionMap(Protagonist.PROJECTION_MAP);
                builder.appendWhere("persontype.typecode='09'");
                break;
            case DEATH_ID:
                builder.appendWhere(Death.PID + "=" + uri.getLastPathSegment());
            case DEATH:
                builder.setTables(Death.TABLE_WITH_DISEASE);
                HashMap<String, String> dMap = new HashMap<String, String>(
                        Diagnosis.PROJECTION_MAP);
                dMap.putAll(Death.PROJECTION_MAP);
                builder.setProjectionMap(dMap);
                break;
            case CHRONIC_ID:
                builder.appendWhere("pid=" + uri.getLastPathSegment());
            case CHRONIC:
                builder.setTables(Chronic.TABLENAME_WITH_DIAG);
                HashMap<String, String> map = new HashMap<String, String>(
                        Diagnosis.PROJECTION_MAP);
                map.putAll(Chronic.PROJECTION_MAP);
                builder.setProjectionMap(map);
                break;
            case CHRONIC_FAMILY_ID:
                builder.appendWhere("pid=" + uri.getLastPathSegment());
            case CHRONIC_FAMILY:
                builder.setTables(FamilyChronic.TABLENAME);
                builder.setProjectionMap(FamilyChronic.PROJECTION_MAP);
                break;
            case TEMPLE_ID:
                builder.appendWhere(TempleMember.PID + "="
                        + uri.getLastPathSegment());
            case TEMPLE:
                builder.setTables(TempleMember.TABLENAME_WITH_PERSON);
                builder.setProjectionMap(TempleMember.PROJECTION_MAP);
                break;

            case VISIT_ID:
                builder.appendWhere(Visit._ID + "=" + uri.getLastPathSegment());
            case VISIT:
                builder.setTables(Visit.TABLENAME);
                builder.setProjectionMap(Visit.PROJECTION_MAP);
                break;
            case VISIT_DIAG_ID:
                String visitno = uri.getPathSegments().get(2);
                String diagcode = uri.getPathSegments().get(4);
                builder.appendWhere("visitno=" + visitno + " AND diagcode='"
                        + diagcode + "'");
            case VISIT_DIAG:
                builder.setTables(VisitDiag.TABLENAME);
                builder.setProjectionMap(VisitDiag.PROJECTION_MAP);
                break;
            case VISIT_ANC_ID:
                builder.appendWhere("visitno=" + uri.getLastPathSegment());
            case VISIT_ANC:
                builder.setTables(VisitAnc.TABLENAME);
                builder.setProjectionMap(VisitAnc.PROJECTION_MAP);
                break;

            case VISIT_ANC_RISK_PREGNO:
                String pid = uri.getPathSegments().get(1);
                String pregno = uri.getPathSegments().get(3);
                builder.appendWhere("pid=" + pid + " AND pregno=" + pregno);
            case VISIT_ANC_RISK:
                builder.setTables(VisitAncRisk.TABLENAME);
                builder.setProjectionMap(VisitAncRisk.PROJECTION_MAP);
                break;

            case VISIT_PREGNANC_ID:
                String preg_pid = uri.getPathSegments().get(1);
                String preg_no = uri.getPathSegments().get(3);
                builder.appendWhere("pid=" + preg_pid + " AND pregno=" + preg_no);
            case VISIT_PREGNANC:
                builder.setTables(VisitAncPregnancy.TABLENAME);
                builder.setProjectionMap(VisitAncPregnancy.PROJECTION_MAP);
                break;

            case VISIT_BLOOD_LAB_ID:
                builder.appendWhere(VisitLabBlood._VISITNO + "="
                        + uri.getLastPathSegment());
            case VISIT_BLOOD_LAB:
                builder.setTables(VisitLabBlood.TABLENAME);
                builder.setProjectionMap(VisitLabBlood.PROJECTION_MAP);
                break;

            case VISIT_EPI_VN:
                builder.appendWhere(VisitEpi._VISITNO + "="
                        + uri.getPathSegments().get(2));
            case VISIT_EPI:
                builder.setTables(VisitEpi.TABLENAME);
                builder.setProjectionMap(VisitEpi.PROJECTION_MAP);
                break;

            case VISIT_DRUG_ID:
                String visitdrug = uri.getPathSegments().get(2);
                String drugcode = uri.getPathSegments().get(4);
                builder.appendWhere("visitno=" + visitdrug + " AND drugcode='"
                        + drugcode + "'");
            case VISIT_DRUG:
                builder.setTables(VisitDrug.TABLENAME);
                builder.setProjectionMap(VisitDrug.PROJECTION_MAP);
                break;

            case VISITHOMEHEALTHINDIVIDUAL:

                builder.setTables(VisitIndividual.TABLENAME);
                builder.setProjectionMap(VisitIndividual.PROJECTION_MAP);
                break;
            case VISITHOMEHEALTHINDIVIDUAL_VISITNO:
                builder.setTables(VisitIndividual.TABLENAME);
                builder.setProjectionMap(VisitIndividual.PROJECTION_MAP);
                builder.appendWhere(VisitIndividual.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITFP:
                builder.setTables(VisitFamilyplan.TABLENAME);
                builder.setProjectionMap(VisitFamilyplan.PROJECTION_MAP);
                break;
            case VISITFP_VISITNO:
                builder.setTables(VisitFamilyplan.TABLENAME);
                builder.setProjectionMap(VisitFamilyplan.PROJECTION_MAP);
                builder.appendWhere(VisitFamilyplan.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITLABCANCER:
                builder.setTables(VisitLabcancer.TABLENAME);
                builder.setProjectionMap(VisitLabcancer.PROJECTION_MAP);
                break;
            case VISITLABCANCER_VISITNO:
                builder.setTables(VisitLabcancer.TABLENAME);
                builder.setProjectionMap(VisitLabcancer.PROJECTION_MAP);
                builder.appendWhere(VisitLabcancer.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITBABYCARE:
                builder.setTables(VisitBabycare.TABLENAME);
                builder.setProjectionMap(VisitBabycare.PROJECTION_MAP);
                break;
            case VISITBABYCARE_VISITNO:
                builder.setTables(VisitBabycare.TABLENAME);
                builder.setProjectionMap(VisitBabycare.PROJECTION_MAP);
                builder.appendWhere(VisitBabycare.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITDIAG506:
                builder.setTables(VisitDiag506address.TABLENAME);
                builder.setProjectionMap(VisitDiag506address.PROJECTION_MAP);
                break;
            case VISITDIAG506_ITEM:
                builder.setTables(VisitDiag506address.TABLENAME);
                builder.setProjectionMap(VisitDiag506address.PROJECTION_MAP);
                builder.appendWhere(VisitDiag506address.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITSPECIALPERSON:
                builder.setTables(VisitSpecialperson.TABLENAME);
                builder.setProjectionMap(VisitSpecialperson.PROJECTION_MAP);
                break;
            case VISITSPECIALPERSON_ITEM:
                builder.setTables(VisitSpecialperson.TABLENAME);
                builder.setProjectionMap(VisitSpecialperson.PROJECTION_MAP);
                builder.appendWhere(VisitSpecialperson.PID + "="
                        + uri.getLastPathSegment());
                break;
            case VISITSCREENSPECIALDISEASE:
                builder.setTables(VisitScreenspecialdisease.TABLENAME);
                builder.setProjectionMap(VisitScreenspecialdisease.PROJECTION_MAP);
                break;
            case VISITSCREENSPECIALDISEASE_ITEM:
                builder.setTables(VisitScreenspecialdisease.TABLENAME);
                builder.setProjectionMap(VisitScreenspecialdisease.PROJECTION_MAP);
                builder.appendWhere(VisitScreenspecialdisease.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case WOMEN:
                builder.setTables(Women.TABLENAME);
                builder.setProjectionMap(Women.PROJECTION_MAP);
                break;
            case WOMEN_ID:
                builder.setTables(Women.TABLENAME);
                builder.setProjectionMap(Women.PROJECTION_MAP);
                builder.appendWhere(Women.PID + "=" + uri.getLastPathSegment());
                break;
            case VISITANCDELIVER:
                builder.setTables(VisitAncDeliver.TABLENAME);
                builder.setProjectionMap(VisitAncDeliver.PROJECTION_MAP);
                break;
            case VISITANCDELIVER_ITEM:
                builder.setTables(VisitAncDeliver.TABLENAME);
                builder.setProjectionMap(VisitAncDeliver.PROJECTION_MAP);
                builder.appendWhere(VisitAncDeliver.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITANCMOTHERCARE:
                builder.setTables(VisitAncMotherCare.TABLENAME);
                builder.setProjectionMap(VisitAncMotherCare.PROJECTION_MAP);
                break;
            case VISITANCMOTHERCARE_ITEM:
                builder.setTables(VisitAncMotherCare.TABLENAME);
                builder.setProjectionMap(VisitAncMotherCare.PROJECTION_MAP);
                builder.appendWhere(VisitAncMotherCare.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITOLDTER:
                builder.setTables(VisitOldter.TABLENAME);
                builder.setProjectionMap(VisitOldter.PROJECTION_MAP);
                break;
            case VISITOLDTER_ITEM:
                builder.setTables(VisitOldter.TABLENAME);
                builder.setProjectionMap(VisitOldter.PROJECTION_MAP);
                builder.appendWhere(VisitOldter.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITNUTRITION:
                builder.setTables(VisitNutrition.TABLENAME);
                builder.setProjectionMap(VisitNutrition.PROJECTION_MAP);
                break;
            case VISITNUTRITION_ITEM:
                builder.setTables(VisitNutrition.TABLENAME);
                builder.setProjectionMap(VisitNutrition.PROJECTION_MAP);
                builder.appendWhere(VisitNutrition.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITDIAGAPPOINT:
                builder.setTables(VisitDiagAppoint.TABLENAME);
                builder.setProjectionMap(VisitDiagAppoint.PROJECTION_MAP);
                break;
            case VISITDIAGAPPOINT_ITEM:
                builder.setTables(VisitDiagAppoint.TABLENAME);
                builder.setProjectionMap(VisitDiagAppoint.PROJECTION_MAP);
                builder.appendWhere(VisitDiagAppoint.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITDENTALCHECK:
                builder.setTables(VisitDentalCheck.TABLENAME);
                builder.setProjectionMap(VisitDentalCheck.PROJECTION_MAP);
                break;
            case VISITDENTALCHECK_ITEM:
                builder.setTables(VisitDentalCheck.TABLENAME);
                builder.setProjectionMap(VisitDentalCheck.PROJECTION_MAP);
                builder.appendWhere(VisitDentalCheck.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITPERSONGROW:
                builder.setTables(VisitPersongrow.TABLENAME);
                builder.setProjectionMap(VisitPersongrow.PROJECTION_MAP);
                break;
            case VISITPERSONGROW_ITEM:
                builder.setTables(VisitPersongrow.TABLENAME);
                builder.setProjectionMap(VisitPersongrow.PROJECTION_MAP);
                builder.appendWhere(VisitPersongrow.PID + "="
                        + uri.getLastPathSegment());
                break;
            case PERSONDEATH:
                builder.setTables(PersonDeath.TABLENAME);
                builder.setProjectionMap(PersonDeath.PROJECTION_MAP);
                break;
            case PERSONDEATH_ITEM:
                builder.setTables(PersonDeath.TABLENAME);
                builder.setProjectionMap(PersonDeath.PROJECTION_MAP);
                builder.appendWhere(PersonDeath.PID + "="
                        + uri.getLastPathSegment());
                break;

            case VISITEPIAPPOINT:
                builder.setTables(VisitEpiAppoint.TABLENAME);
                builder.setProjectionMap(VisitEpiAppoint.PROJECTION_MAP);

                break;
            case VISITEPIAPPOINT_ITEM:
                builder.setTables(VisitEpiAppoint.TABLENAME);
                builder.setProjectionMap(VisitEpiAppoint.PROJECTION_MAP);
                builder.appendWhere(VisitEpiAppoint.PID + "="
                        + uri.getLastPathSegment());
                break;
            case PERSONUNABLETYPE:
                builder.setTables(PersonunableType.TABLENAME);
                builder.setProjectionMap(PersonunableType.PROJECTION_MAP);
                break;
            case PERSONUNABLETYPE_ITEM:
                builder.setTables(PersonunableType.TABLENAME);
                builder.setProjectionMap(PersonunableType.PROJECTION_MAP);
                builder.appendWhere(PersonunableType.PID + "="
                        + uri.getLastPathSegment());
                break;
            case PERSONUNABLEPROB:
                builder.setTables(PersonunableProblem.TABLENAME);
                builder.setProjectionMap(PersonunableProblem.PROJECTION_MAP);
                break;
            case PERSONUNABLEPROB_ITEM:
                builder.setTables(PersonunableProblem.TABLENAME);
                builder.setProjectionMap(PersonunableProblem.PROJECTION_MAP);
                builder.appendWhere(PersonunableProblem.PID + "="
                        + uri.getLastPathSegment());
                break;
            case PERSONUNABLENEED:
                builder.setTables(PersonunableNeed.TABLENAME);
                builder.setProjectionMap(PersonunableNeed.PROJECTION_MAP);
                break;
            case PERSONUNABLENEED_ITEM:
                builder.setTables(PersonunableNeed.TABLENAME);
                builder.setProjectionMap(PersonunableNeed.PROJECTION_MAP);
                builder.appendWhere(PersonunableNeed.PID + "="
                        + uri.getLastPathSegment());
                break;
            case PERSONUNABLEHELP:
                builder.setTables(PersonunableHelp.TABLENAME);
                builder.setProjectionMap(PersonunableHelp.PROJECTION_MAP);
                break;
            case PERSONUNABLEHELP_ITEM:
                builder.setTables(PersonunableHelp.TABLENAME);
                builder.setProjectionMap(PersonunableHelp.PROJECTION_MAP);
                builder.appendWhere(PersonunableHelp.PID + "="
                        + uri.getLastPathSegment());
                break;
            case VISITDRUGDENTAL:
                builder.setTables(VisitDrugDental.TABLENAME);
                builder.setProjectionMap(VisitDrugDental.PROJECTION_MAP);
                break;
            case VISITDRUGDENTAL_ITEM:
                builder.setTables(VisitDrugDental.TABLENAME);
                builder.setProjectionMap(VisitDrugDental.PROJECTION_MAP);
                builder.appendWhere(VisitDrugDental.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISITDRUGDENTALDIAG:
                builder.setTables(VisitDrugDentalDiag.TABLENAME);
                builder.setProjectionMap(VisitDrugDentalDiag.PROJECTION_MAP);
                break;
            case VISITDRUGDENTALDIAG_ITEM:
                builder.setTables(VisitDrugDentalDiag.TABLENAME);
                builder.setProjectionMap(VisitDrugDentalDiag.PROJECTION_MAP);
                builder.appendWhere(VisitDrugDentalDiag.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case PERSONUNABLE:
                builder.setTables(Personunable.TABLENAME);
                builder.setProjectionMap(Personunable.PROJECTION_MAP);
                break;
            case NCDPERSON:
                builder.setTables(NCDPerson.TABLENAME);
                builder.setProjectionMap(NCDPerson.PROJECTION_MAP);
                break;
            case NCDPERSONTYPE:
                builder.setTables(NCDPersonNCD.TABLENAME);
                builder.setProjectionMap(NCDPersonNCD.PROJECTION_MAP);
                break;
            case NCDPERSONPATIENT:
                builder.setTables(NCDPersonNCDHist.TABLENAME);
                builder.setProjectionMap(NCDPersonNCDHist.PROJECTION_MAP);
                break;
            case NCDPERSONSYMPTOM:
                builder.setTables(NCDPersonNCDHistDetail.TABLENAME);
                builder.setProjectionMap(NCDPersonNCDHistDetail.PROJECTION_MAP);
                break;
            case NCDPERSONSCREEN:
                builder.setTables(NCDPersonNCDScreen.TABLENAME);
                builder.setProjectionMap(NCDPersonNCDScreen.PROJECTION_MAP);
                break;
            case NCDJOIN:
                builder.setTables(NCDScreenJOIN.TABLENAME);
                builder.setProjectionMap(NCDScreenJOIN.PROJECTION_MAP);
                break;
            case VISIT506_PERSON:
                builder.setTables(Visit506_Person.TABLENAME);
                builder.setProjectionMap(Visit506_Person.PROJECTION_MAP);
                break;
            case VISIT506_PERSON_ITEM:
                builder.setTables(Visit506_Person.TABLENAME);
                builder.setProjectionMap(Visit506_Person.PROJECTION_MAP);
                builder.appendWhere(VisitDiag506address.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case GETADDRESS:
                builder.setTables(GET_ADDRESS.TABLENAME);
                builder.setProjectionMap(GET_ADDRESS.PROJECTION_MAP);
                break;
            case PERSON_BALL_COLOR:
                builder.setTables(PersonRiskBlackBall.TABLENAME);
                builder.setProjectionMap(PersonRiskBlackBall.PROJECTION_MAP);
                groupby = "cdiseasechronic.groupcode,person.pid";
                break;
            case PERSON_BALL_COLOR_ITEM:
                builder.setTables(PersonRiskBlackBall.TABLENAME);
                builder.setProjectionMap(PersonRiskBlackBall.PROJECTION_MAP);
                builder.appendWhere("person.pid ="
                        + uri.getLastPathSegment());
                break;
            case PERSON_BALL_RED_COLOR:
                builder.setTables(PersonRiskRedGrayBall.TABLENAME);
                builder.setProjectionMap(PersonRiskRedGrayBall.PROJECTION_MAP);
                groupby = "person.pid";
                break;
            case PERSON_BALL_RED_COLOR_ITEM:
                builder.setTables(PersonRiskRedGrayBall.TABLENAME);
                builder.setProjectionMap(PersonRiskRedGrayBall.PROJECTION_MAP);
                builder.appendWhere("person.pid ="
                        + uri.getLastPathSegment());
                break;
            case FFC_506RADIUS:
                builder.setTables(FFC506RADIUS.TABLENAME);
                builder.setProjectionMap(FFC506RADIUS.PROJECTION_MAP);
                break;
            case FFC_506RADIUS_ITEM:
                builder.setTables(FFC506RADIUS.TABLENAME);
                builder.setProjectionMap(FFC506RADIUS.PROJECTION_MAP);
                builder.appendWhere(FFC506RADIUS.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISIT_LABSUGARBLOOD:
                builder.setTables(VISITLABSUGARBLOOD.TABLENAME);
                builder.setProjectionMap(VISITLABSUGARBLOOD.PROJECTION_MAP);
                break;
            case VISIT_LABSUGARBLOOD_ITEM:
                builder.setTables(VISITLABSUGARBLOOD.TABLENAME);
                builder.setProjectionMap(VISITLABSUGARBLOOD.PROJECTION_MAP);
                builder.appendWhere(VISITLABSUGARBLOOD.VISITNO + "="
                        + uri.getLastPathSegment());
                break;
            case VISIT_VISIT_LABSUGARBLOOD:
                builder.setTables(VISIT_VISITLABSUGARBLOOD.TABLENAME);
                builder.setProjectionMap(VISIT_VISITLABSUGARBLOOD.PROJECTION_MAP);
                break;
            case VISIT_VISIT_LABSUGARBLOOD_ITEM:
                builder.setTables(VISIT_VISITLABSUGARBLOOD.TABLENAME);
                builder.setProjectionMap(VISIT_VISITLABSUGARBLOOD.PROJECTION_MAP);
                builder.appendWhere(VISIT_VISITLABSUGARBLOOD.VISITNO + "="
                        + uri.getLastPathSegment());
                break;

            case PERSON_HOUSE:
                builder.setTables(PersonHouse.TABLENAME);
                builder.setProjectionMap(PersonHouse.PROJECTION_MAP);
                break;
            case PERSON_HOUSE_ITEM:
                builder.setTables(PersonHouse.TABLENAME);
                builder.setProjectionMap(PersonHouse.PROJECTION_MAP);
                builder.appendWhere("hcode ="
                        + uri.getLastPathSegment());
                break;
            case PERSON_NCD_BALL:
                builder.setTables(PersonNCDBall.TABLENAME);
                builder.setProjectionMap(PersonNCDBall.PROJECTION_MAP);
                groupby = "person.pid";
                break;
            case PERSON_NCD_BALL_ITEM:
                builder.setTables(PersonNCDBall.TABLENAME);
                builder.setProjectionMap(PersonNCDBall.PROJECTION_MAP);
                builder.appendWhere("hcode ="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : "
                        + uri.toString());
        }

        // System.out.println(builder.buildQuery(projection, selection, groupby,
        // having, sortOrder, null));
        System.out.println(">> query table <<");
        System.out.println(builder.getTables());
        System.out.println(">> database <<");
        System.out.println("db:"+db.toString());
        System.out.println("projection:"+projection);
        System.out.println("selection:"+selection );
        System.out.println("selectionArgs:"+selectionArgs);
        builder.setDistinct(true);
        Cursor c = builder.query(db, projection, selection, selectionArgs,
                groupby, having, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        String where;
        String[] whereArgs;
        List<String> segments;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        System.out.println(uri.toString());
        switch (mUriMatcher.match(uri)) {
            case PERSON:
                count = db.update(Person.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case PERSON_ID:
                where = "person.pid=?";
                whereArgs = new String[]{uri.getLastPathSegment()};
                count = db.update(Person.TABLENAME, values, where, whereArgs);
                break;
            case BEHAVIOR:
                Log.d("Provider", "call selection=" + selection);
                count = db.update(Behavior.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case BEHAVIOR_ID:
                Log.d("Provider", "call selection=" + selection);
                where = "pid=?";
                whereArgs = new String[]{uri.getLastPathSegment()};
                count = db.update(Behavior.TABLENAME, values, where, whereArgs);
                break;
            case VISIT_ID:
                count = db.update(Visit.TABLENAME, values,
                        "visit.visitno=" + uri.getLastPathSegment(), null);
                break;
            case VISIT_ANC:
                count = db.update(VisitAnc.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISIT_ANC_ID:
                count = db.update(VisitAnc.TABLENAME, values,
                        "visitno=" + uri.getLastPathSegment(), null);
                break;
            case VISIT_ANC_RISK_ID:
                segments = uri.getPathSegments();
                whereArgs = new String[]{segments.get(1), segments.get(3),
                        segments.get(5),};
                count = db.update(VisitAncRisk.TABLENAME, values,
                        "pid=? AND pregno=? AND ancriskcode=?", whereArgs);
                break;
            case VISIT_PREGNANC_ID:
                whereArgs = new String[]{uri.getPathSegments().get(1),
                        uri.getLastPathSegment(),};
                count = db.update(VisitAncPregnancy.TABLENAME, values,
                        "pid=? AND pregno=?", whereArgs);
                break;
            case VISIT_BLOOD_LAB_ID:
                count = db.update(VisitLabBlood.TABLENAME, values,
                        "visitno=" + uri.getLastPathSegment(), null);
                break;
            case VISIT_EPI_ID:
                whereArgs = new String[]{uri.getPathSegments().get(2),
                        uri.getLastPathSegment(),};
                count = db.update(VisitEpi.TABLENAME, values,
                        "visitno=? AND vaccinecode=?", whereArgs);
                break;
            case VISIT_DIAG_ID:
                String visitno = uri.getPathSegments().get(2);
                String diagcode = uri.getPathSegments().get(4);
                where = "visitno=" + visitno + " AND diagcode='" + diagcode + "'";
                count = db.update(VisitDiag.TABLENAME, values, where, null);
                break;
            case VISIT_DRUG_ID:
                String visitdrug = uri.getPathSegments().get(2);
                String drugcode = uri.getPathSegments().get(4);
                where = "visitno=" + visitdrug + " AND drugcode='" + drugcode + "'";
                count = db.update(VisitDrug.TABLENAME, values, where, null);
                break;

            case VISITHOMEHEALTHINDIVIDUAL:
                count = db.update(VisitIndividual.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITFP:
                count = db.update(VisitFamilyplan.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITBABYCARE:
                count = db.update(VisitBabycare.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITLABCANCER:
                count = db.update(VisitLabcancer.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITDIAG506:
                count = db.update(VisitDiag506address.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITSPECIALPERSON:
                count = db.update(VisitSpecialperson.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITSCREENSPECIALDISEASE:
                count = db.update(VisitScreenspecialdisease.TABLENAME, values,
                        selection, selectionArgs);
                break;
            case VISIT_DIAG:
                System.out.println("I'M HERE");
                count = db.update(VisitDiag.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case WOMEN_ID:
                // where = "house.hcode="+uri.getLastPathSegment();
                count = db
                        .update(Women.TABLENAME, values, selection, selectionArgs);
                break;
            case VISITANCDELIVER:
                count = db.update(VisitAncDeliver.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITANCMOTHERCARE:
                count = db.update(VisitAncMotherCare.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITOLDTER:
                db.update(VisitOldter.TABLENAME, values, selection, selectionArgs);
                break;
            case VISITNUTRITION:
                db.update(VisitNutrition.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITDIAGAPPOINT:
                db.update(VisitDiagAppoint.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITDENTALCHECK:
                db.update(VisitDentalCheck.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITPERSONGROW:
                db.update(VisitPersongrow.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case PERSONDEATH:
                db.update(PersonDeath.TABLENAME, values, selection, selectionArgs);
                break;
            case VISITEPIAPPOINT:
                db.update(VisitEpiAppoint.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case PERSONUNABLETYPE:
                db.update(PersonunableType.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case PERSONUNABLEPROB:
                db.update(PersonunableProblem.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case PERSONUNABLENEED:
                db.update(PersonunableNeed.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case PERSONUNABLEHELP:
                db.update(PersonunableHelp.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITDRUGDENTAL:
                db.update(VisitDrugDental.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISITDRUGDENTALDIAG:
                db.update(VisitDrugDentalDiag.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISIT_DRUG:
                db.update(VisitDrug.TABLENAME, values, selection, selectionArgs);
                break;
            case PERSONUNABLE:
                db.update(Personunable.TABLENAME, values, selection, selectionArgs);
                break;
            case NCDPERSON:
                db.update(NCDPerson.TABLENAME, values, selection, selectionArgs);
                break;
            case NCDPERSONTYPE:
                db.update(NCDPersonNCD.TABLENAME, values, selection, selectionArgs);
                break;
            case NCDPERSONPATIENT:
                db.update(NCDPersonNCDHist.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case NCDPERSONSYMPTOM:
                db.update(NCDPersonNCDHistDetail.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case NCDPERSONSCREEN:
                db.update(NCDPersonNCDScreen.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case GETADDRESS:
                count = db.update(GET_ADDRESS.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case FFC_506RADIUS:
                count = db.update(FFC506RADIUS.TABLENAME, values, selection,
                        selectionArgs);
                break;
    /*	case GETADDRESS_ITEM:
			where = "person.pid=?";
			whereArgs = new String[] { uri.getLastPathSegment() };
			count = db.update(GET_ADDRESS.TABLENAME, values, where, whereArgs);
			break;*/

            case VISIT_LABSUGARBLOOD:
                count = db.update(VISITLABSUGARBLOOD.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISIT_VISIT_LABSUGARBLOOD:
                count = db.update(VISIT_VISITLABSUGARBLOOD.TABLENAME, values, selection,
                        selectionArgs);
                break;
            case VISIT:
                count = db.update(Visit.TABLENAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : "
                        + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public static class Person implements BaseColumns, DateUpdateColumns {

        public static final String TABLENAME = "person";
        public static final String TABLENAME_CHRONIC = "person INNER JOIN personchronic ON person.pid = personchronic.pid";
        public static final String TABLENAME_WITH_CHRONIC = "person LEFT JOIN personchronic ON person.pid = personchronic.pid";
        public static final String TABLENAME_DEATH = "person INNER JOIN persondeath ON person.pid = persondeath.pid";
        public static final String TABLENAME_WITH_DEATH = "person LEFT JOIN persondeath ON person.pid = persondeath.pid";
        public static final String TABLENAME_TEMPLE = "person INNER JOIN persontemplemem ON person.pid = persontemplemem.pid";
        public static final String TABLENAME_WITH_TEMPLE = "person LEFT JOIN persontemplemem ON person.pid = persontemplemem.pid";
        public static final String TABLENAME_PERSONTYPE = "person INNER JOIN persontype ON person.pid = persontype.pid";
        public static final String TABLENAME_WITH_PERSONTYPE = "person LEFT JOIN persontype ON person.pid = persontype.pid";
        public static final String TABLENAME_UNABLE = "person INNER JOIN personunable ON person.pid = personunable.pid";
        public static final String TABLENAME_WITH_UNABLE = "person LEFT JOIN personunable ON person.pid = personunable.pid AND"
                + " (personunable.dateexpire IS NULL OR date(personunable.dateexpire) < date('now'))";
        public static final String TABLENAME_PREGNANT = "person LEFT JOIN visitancpregnancy ON person.pid = visitancpregnancy.pid";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person";

        public static final String DEFAULT_SORTING = "person.pcucodeperson ASC, person.pid ASC";

        // KEY
        public static final String PCUPERSONCODE = "pcucodeperson";
        public static final String PID = "pid";
        public static final String MAX_PID = "maxpid";
        public static final String HCODE = "hcode";
        // PROFILE
        public static final String PRENAME = "prename";
        public static final String FIRST_NAME = "fname";
        public static final String LAST_NAME = "lname";
        public static final String FULL_NAME = "fullname";
        public static final String NICKNAME = "nick";
        public static final String BIRTH = "birth";
        public static final String SEX = "sex";
        public static final String CITIZEN_ID = "idcard";
        public static final String BLOOD_GROUP = "bloodgroup";
        public static final String BLOOD_RH = "bloodrh";
        public static final String ALLERGIC = "allergic";
        public static final String OCCUPA = "occupa";
        public static final String EDUCATION = "educate";
        public static final String NATION = "nation";
        public static final String ORIGIN = "origin";
        public static final String RELIGION = "religion";
        public static final String INCOME = "income";
        public static final String NAME_EN = "fnameeng";
        public static final String LAST_NAME_EN = "lnameeng";
        public static final String TEL = "telephoneperson";
        // FAMILY
        public static final String MARRY_STATUS = "marystatus";
        public static final String FATHER = "father";
        public static final String MOTHER = "mother";
        public static final String FATHER_ID = "fatherid";
        public static final String MOTHER_ID = "motherid";
        public static final String MATE = "mate";
        public static final String MATE_ID = "mateid";
        public static final String FAMILY_NO = "familyno";
        public static final String FAMILY_POSITION = "familyposition";
        // RIGHT
        public static final String RIGHT_HMAIN = "hosmain";
        public static final String RIGHT_HSUB = "hossub";
        public static final String RIGHT_CODE = "rightcode";
        public static final String RIGHT_NO = "rightno";
        public static final String RIGHT_REGIS = "dateregis";
        public static final String RIGHT_START = "datestart";
        public static final String RIGHT_EXPIRE = "dateexpire";
        // ADDRESS
        public static final String ADDR_NO = "hnomoi";
        public static final String ADDR_ROAD = "roadmoi";
        public static final String ADDR_MU = "mumoi";
        public static final String ADDR_SUBDIST = "subdistcodemoi";
        public static final String ADDR_DIST = "distcodemoi";
        public static final String ADDR_PROVICE = "provcodemoi";
        public static final String POSTCODE = "postcodemoi";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Person._ID, "person.pid AS " + Person._ID);
            PROJECTION_MAP.put(Person.PID, "person.pid AS " + Person.PID);
            PROJECTION_MAP.put(Person._COUNT, "count(*) AS " + Person._COUNT);
            PROJECTION_MAP.put(Person.MAX_PID, "max(person.pid) AS "
                    + Person.MAX_PID);
            PROJECTION_MAP.put(Person.PCUPERSONCODE, "person.pcucodeperson AS "
                    + Person.PCUPERSONCODE);

            PROJECTION_MAP.put(Person.HCODE, "person.hcode AS " + Person.HCODE);
            PROJECTION_MAP.put(Person._DATEUPDATE, "person.dateupdate AS "
                    + Person._DATEUPDATE);
            PROJECTION_MAP.put(Person.PRENAME, "person.prename AS "
                    + Person.PRENAME);
            PROJECTION_MAP.put(Person.FIRST_NAME, "person.fname AS "
                    + Person.FIRST_NAME);
            PROJECTION_MAP.put(Person.LAST_NAME, "person.lname AS "
                    + Person.LAST_NAME);
            PROJECTION_MAP.put(Person.FULL_NAME,
                    "person.fname || ' ' || person.lname AS "
                            + Person.FULL_NAME);
            PROJECTION_MAP.put(Person.NICKNAME, "person.nickname AS "
                    + Person.NICKNAME);
            PROJECTION_MAP.put(Person.BIRTH, "person.birth AS " + Person.BIRTH);
            PROJECTION_MAP.put(Person.CITIZEN_ID, "person.idcard AS "
                    + Person.CITIZEN_ID);
            PROJECTION_MAP.put(Person.SEX, "person.sex AS " + Person.SEX);
            PROJECTION_MAP.put(Person.BLOOD_GROUP, "person.bloodgroup AS "
                    + Person.BLOOD_GROUP);
            PROJECTION_MAP.put(Person.BLOOD_RH, "person.bloodrh AS "
                    + Person.BLOOD_RH);
            PROJECTION_MAP.put(Person.ALLERGIC, "person.allergic AS "
                    + Person.ALLERGIC);
            PROJECTION_MAP.put(Person.OCCUPA, "person.occupa AS "
                    + Person.OCCUPA);
            PROJECTION_MAP.put(Person.INCOME, "person.income AS "
                    + Person.INCOME);
            PROJECTION_MAP.put(Person.NATION, "person.nation AS "
                    + Person.NATION);
            PROJECTION_MAP.put(Person.ORIGIN, "person.origin AS "
                    + Person.ORIGIN);
            PROJECTION_MAP.put(Person.EDUCATION, "person.educate AS "
                    + Person.EDUCATION);
            PROJECTION_MAP.put(Person.RELIGION, "person.religion AS "
                    + Person.RELIGION);

            PROJECTION_MAP.put(Person.MARRY_STATUS, Person.MARRY_STATUS);
            PROJECTION_MAP.put(Person.FAMILY_NO, "person.familyno AS "
                    + Person.FAMILY_NO);
            PROJECTION_MAP.put(Person.FAMILY_POSITION,
                    "person.familyposition AS " + Person.FAMILY_POSITION);
            PROJECTION_MAP.put(Person.FATHER, "person.father AS "
                    + Person.FATHER);
            PROJECTION_MAP.put(Person.FATHER_ID, "person.fatherid AS "
                    + Person.FATHER_ID);
            PROJECTION_MAP.put(Person.MOTHER, "person.mother AS "
                    + Person.MOTHER);
            PROJECTION_MAP.put(Person.MOTHER_ID, "person.motherid AS "
                    + Person.MOTHER_ID);
            PROJECTION_MAP.put(Person.MATE, "person.mate AS " + Person.MATE);
            PROJECTION_MAP.put(Person.MATE_ID, "person.mateid AS "
                    + Person.MATE_ID);

            PROJECTION_MAP.put(Person.RIGHT_CODE, "person.rightcode AS "
                    + Person.RIGHT_CODE);
            PROJECTION_MAP.put(Person.RIGHT_NO, "person.rightno AS "
                    + Person.RIGHT_NO);
            PROJECTION_MAP.put(Person.RIGHT_HMAIN, "person.hosmain AS "
                    + Person.RIGHT_HMAIN);
            PROJECTION_MAP.put(Person.RIGHT_HSUB, "person.hossub AS "
                    + Person.RIGHT_HSUB);
            PROJECTION_MAP.put(Person.RIGHT_REGIS, "person.dateregis AS "
                    + Person.RIGHT_REGIS);
            PROJECTION_MAP.put(Person.RIGHT_START, "person.datestart AS "
                    + Person.RIGHT_START);
            PROJECTION_MAP.put(Person.RIGHT_EXPIRE, "person.dateexpire AS "
                    + Person.RIGHT_EXPIRE);

            PROJECTION_MAP.put(Person.ADDR_NO, "person.hnomoi AS "
                    + Person.ADDR_NO);
            PROJECTION_MAP.put(Person.ADDR_ROAD, "person.roadmoi AS "
                    + Person.ADDR_ROAD);
            PROJECTION_MAP.put(Person.ADDR_MU, "person.mumoi AS "
                    + Person.ADDR_MU);
            PROJECTION_MAP.put(Person.ADDR_SUBDIST, "person.subdistcodemoi AS "
                    + Person.ADDR_SUBDIST);
            PROJECTION_MAP.put(Person.ADDR_DIST, "person.distcodemoi AS "
                    + Person.ADDR_DIST);
            PROJECTION_MAP.put(Person.ADDR_PROVICE, "person.provcodemoi AS "
                    + Person.ADDR_PROVICE);
            PROJECTION_MAP.put(Person.POSTCODE, "person.postcodemoi AS "
                    + Person.POSTCODE);
            PROJECTION_MAP.put(Person.TEL, "person.telephoneperson AS "
                    + Person.TEL);

        }
    }

    public static class Chronic implements BaseColumns {
        public static final String TABLENAME = "personchronic";
        public static final String TABLENAME_WITH_PERSON = "personchronic INNER JOIN person ON person.pid = personchronic.pid";
        public static final String TABLENAME_WITH_DIAG = "personchronic LEFT JOIN cdisease ON chroniccode = diseasecode";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/chronic");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.chronic";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.chronic";

        public static final String DEFAULT_SORTING = "personchronic.pid, personchronic.chroniccode";

        public static final String PCUCODE = "personchronic.pcucodeperson";
        public static final String PID = "personchronic.pid";
        public static final String CODE = "personchronic.chroniccode";
        public static final String FIRST_DIAG = "personchronic.datefirstdiag";
        public static final String FIRST_DX = "personchronic.datedxfirst";
        public static final String BEHAVIOR_RISK = "personchronic.behaviorrisk";
        public static final String DATE_DISCHART = "personchronic.datedischart";
        public static final String TYPE_DISCHART = "personchronic.typedischart";
        public static final String CUP = "personchronic.cup";
        public static final String UPDATE = "personchronic.dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(_ID,
                    "personchronic.pid || personchronic.chroniccode AS " + _ID);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(CODE, CODE);
            PROJECTION_MAP.put(BEHAVIOR_RISK, BEHAVIOR_RISK);
            PROJECTION_MAP.put(DATE_DISCHART, DATE_DISCHART);
            PROJECTION_MAP.put(TYPE_DISCHART, TYPE_DISCHART);
            PROJECTION_MAP.put(FIRST_DIAG, FIRST_DIAG);
            PROJECTION_MAP.put(FIRST_DX, FIRST_DX);
            PROJECTION_MAP.put(CUP, CUP);
            PROJECTION_MAP.put(UPDATE, UPDATE);
            PROJECTION_MAP.putAll(Person.PROJECTION_MAP);
        }
    }

    public static class FamilyChronic implements BaseColumns, PersonColumns {
        public static final String TABLENAME = "personchronicfamily";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/chronicfamily");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.chronicfamily";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.chronicfamily";
        public static final String DEFAULT_SORTING = "pid ASC, relationcode ASC";

        public static final String RELATION_CODE = "relationcode";
        public static final String CHRONIC_CODE = "chroniccode";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(_PCUCODEPERSON, _PCUCODEPERSON);
            PROJECTION_MAP.put(_PID, _PID);
            PROJECTION_MAP.put(_ID, "pid AS " + _ID);
            PROJECTION_MAP.put(RELATION_CODE, RELATION_CODE);
            PROJECTION_MAP.put(CHRONIC_CODE, CHRONIC_CODE);
        }
    }

    public static class Death implements BaseColumns {
        public static final String TABLENAME = "persondeath";
        public static final String TABLENAME_WITH_PERSON = "persondeath INNER JOIN person ON persondeath.pid = person.pid";
        public static final String TABLE_WITH_DISEASE = "persondeath LEFT JOIN cdisease ON deadcause = diseasecode";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/death");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.death";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.death";

        public static final String DEFAULT_SORTING = "persondeath.pcucodeperson, persondeath.pid";
        public static final String PCUCODE = "persondeath.pcucodeperson";
        public static final String PID = "persondeath.pid";
        public static final String A = "persondeath.cdeatha";
        public static final String B = "persondeath.cdeathb";
        public static final String C = "persondeath.cdeathc";
        public static final String D = "persondeath.cdeathd";
        public static final String CAUSE = "persondeath.deadcause";
        public static final String PLACE = "persondeath.deadplace";
        public static final String DATE = "persondeath.deaddate";
        public static final String UPDATE = "persondeath.dateupdate";
        public static final String SOURCE = "persondeath.source";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(_ID, "persondeath.pid AS " + _ID);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(A, A);
            PROJECTION_MAP.put(B, B);
            PROJECTION_MAP.put(C, C);
            PROJECTION_MAP.put(D, D);
            PROJECTION_MAP.put(CAUSE, CAUSE);
            PROJECTION_MAP.put(PLACE, PLACE);
            PROJECTION_MAP.put(DATE, DATE);
            PROJECTION_MAP.put(UPDATE, UPDATE);
            PROJECTION_MAP.put(SOURCE, SOURCE);
            PROJECTION_MAP.putAll(Person.PROJECTION_MAP);
        }
    }

    public static class Protagonist implements BaseColumns {
        public static final String TABLENAME = "persontype";
        public static final String TABLENAME_WITH_PERSON = "persontype INNER JOIN person ON persontype.pid = person.pid";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/protagonist");
        public static final Uri CONTENT_URI_VOLA = Uri.withAppendedPath(
                Protagonist.CONTENT_URI, "vola");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.protagonist";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.protagonist";

        public static final String DEFAULT_SORTING = "persontype.pcucodeperson, persontype.pid";
        public static final String PCUCODEPERSON = "persontype.pcucodeperson";
        public static final String PID = "persontype.pid";
        public static final String TYPE = "persontype.typecode";
        public static final String DATE_ELECT = "persontype.dateelect";
        public static final String DATE_RETIRE = "persontype.dateretire";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(_ID, "persontype.pid AS " + _ID);
            PROJECTION_MAP.put(PCUCODEPERSON, PCUCODEPERSON);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(TYPE, TYPE);
            PROJECTION_MAP.put(DATE_ELECT, DATE_ELECT);
            PROJECTION_MAP.put(DATE_RETIRE, DATE_RETIRE);
            PROJECTION_MAP.putAll(Person.PROJECTION_MAP);
        }
    }

    public static class TempleMember implements BaseColumns {
        public static final String TABLENAME = "persontemplemem";
        public static final String TABLENAME_WITH_PERSON = "persontemplemem INNER JOIN person ON persontemplemem.pid = person.pid";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/temple");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.temple";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.temple";

        public static final String DEFAULT_SORTING = "persontemplemem.pcucodeperson, persontemplemem.villcode, persontemplemem.templeno, persontemplemem.pid";
        public static final String PCUPERSONCODE = "persontemplemem.pcucodeperson";
        public static final String PID = "persontemplemem.pid";
        public static final String PCUCODE = "persontemplemem.pcucode";
        public static final String VILLCODE = "persontemplemem.villcode";
        public static final String TEMPLE_NO = "persontemplemem.templeno";
        public static final String TYPE = "persontemplemem.membertype";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(_ID, "persontemplemem.pid AS " + _ID);
            PROJECTION_MAP.put(PCUPERSONCODE, PCUPERSONCODE);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(VILLCODE, VILLCODE);
            PROJECTION_MAP.put(TEMPLE_NO, TEMPLE_NO);
            PROJECTION_MAP.put(TYPE, TYPE);
            PROJECTION_MAP.putAll(Person.PROJECTION_MAP);
        }
    }

    public static class Behavior implements BaseColumns {
        public static final String TABLENAME = "personbehavior";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/behavior");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.behavior";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.behavior";

        public static final String DEFAULT_SORTING = "pcucodeperson ASC, "
                + Behavior.PID;

        public static final String PCUCODE = "pcucodeperson";
        public static final String PID = Person.PID;
        public static final String CIGA = "ciga";
        public static final String WISKY = "wisky";
        public static final String EXERCISE = "exercise";
        public static final String ACCIDENT = "bigaccidentever";
        public static final String TONIC = "tonic";
        public static final String HABITFOMING = "habitfoming";
        public static final String DRUGBYSELF = "drugbyyourseft";
        public static final String SUGAR = "sugar";
        public static final String SALT = "salt";
        public static final String DATEUPDATE = "dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Behavior._ID, "personbehavior.pid AS "
                    + Behavior._ID);
            PROJECTION_MAP.put(Behavior.PID, Behavior._ID);
            PROJECTION_MAP.put(Behavior.CIGA, Behavior.CIGA);
            PROJECTION_MAP.put(Behavior.WISKY, Behavior.WISKY);
            PROJECTION_MAP.put(Behavior.EXERCISE, Behavior.EXERCISE);
            PROJECTION_MAP.put(Behavior.ACCIDENT, Behavior.ACCIDENT);
            PROJECTION_MAP.put(Behavior.TONIC, Behavior.TONIC);
            PROJECTION_MAP.put(Behavior.HABITFOMING, Behavior.HABITFOMING);
            PROJECTION_MAP.put(Behavior.DRUGBYSELF, Behavior.DRUGBYSELF);
            PROJECTION_MAP.put(Behavior.SUGAR, Behavior.SUGAR);
            PROJECTION_MAP.put(Behavior.SALT, Behavior.SALT);
            PROJECTION_MAP.put(Behavior.DATEUPDATE, Behavior.DATEUPDATE);
        }
    }

    public static class Visit implements BaseColumns {
        public static final String TABLENAME = "visit";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit";

        public static final String DEFAULT_SORTING = "pcucodeperson, pid, visitno DESC";
        public static final String PCUCODE = "pcucode";
        public static final String PCUCODE_PERSON = "pcucodeperson";
        public static final String NO = "visitno";
        public static final String DATE = "visitdate";
        public static final String PID = "pid";
        public static final String UPDATE = "dateupdate";
        public static final String MAX = "max";
        public static final String TIME_SERIVICE = "timeservice";
        public static final String TIME_START = "timestart";
        public static final String TIME_END = "timeend";
        public static final String RIGHT_HMAIN = "hosmain";
        public static final String RIGHT_HSUB = "hossub";
        public static final String RIGHT_CODE = "rightcode";
        public static final String RIGHT_NO = "rightno";
        public static final String INCUP = "incup";

        public static final String BMI = "bmilevel";
        public static final String SYMPTOMS = "symptoms";
        public static final String SYMPTOMSCO = "symptomsco";
        public static final String DIAGNOTE = "diagnote";
        public static final String VITALCHECK = "vitalcheck";
        public static final String HEALTHSUGGEST1 = "healthsuggest1";

        public static final String VITAL = "vitalcheck";
        public static final String WEIGHT = "weight";
        public static final String HEIGHT = "height";
        public static final String PRESSURE = "pressure";
        public static final String PRESSURE_LEVEL = "pressurelevel";
        public static final String PRESSURE_2 = "pressure2";
        public static final String PRESSURE_LEVEL_2 = "pressurelevel2";
        public static final String TEMPERATURE = "temperature";
        public static final String PULSE = "pulse";
        public static final String RESPI = "respi";
        public static final String WAIST = "waist";
        public static final String ASS = "ass";
        public static final String SCREEN_OTHER_DISEASE = "screenotherdisease";
        public static final String ALIVE_STATUS = "alivestatus";
        public static final String HEALTH_SUGGEST_1 = "healthsuggest1";
        public static final String HEALTH_SUGGEST_2 = "healthsuggest2";
        public static final String HEALTH_SUGGEST_TYPE = "healthsuggesttype";

        public static final String RECEIVE_PATIENT = "receivepatient";
        public static final String RECEIVE_FROM = "receivefromhos";
        public static final String REFER_PATIENT = "refer";
        public static final String REFER_TO_HOS = "refertohos";
        public static final String REFER_BACK = "referback";

        public static final String MONEY1 = "money1";
        public static final String MONEY2 = "money2";
        public static final String MONEY3 = "money3";

        public static final String USERNAME = "username";
        public static final String FLAG_SERVICE = "flagservice";
        public static final String SERVICE_TYPE = "servicetype";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(Visit._ID, "visit.visitno AS " + Visit._ID);
            PROJECTION_MAP.put(Visit.PCUCODE, Visit.PCUCODE);
            PROJECTION_MAP.put(Visit.NO, Visit.NO);
            PROJECTION_MAP.put(Visit.PCUCODE_PERSON, Visit.PCUCODE_PERSON);
            PROJECTION_MAP.put(Visit.DATE, Visit.DATE);
            PROJECTION_MAP.put(Visit.PID, Visit.PID);
            PROJECTION_MAP.put(Visit.MAX, "MAX(visit.visitno) AS " + Visit.MAX);

            PROJECTION_MAP.put(Visit.TIME_SERIVICE, Visit.TIME_SERIVICE);
            PROJECTION_MAP.put(Visit.TIME_START, Visit.TIME_START);
            PROJECTION_MAP.put(Visit.TIME_END, Visit.TIME_END);
            PROJECTION_MAP.put(Visit.RIGHT_CODE, Visit.RIGHT_CODE);
            PROJECTION_MAP.put(Visit.RIGHT_HMAIN, Visit.RIGHT_HMAIN);
            PROJECTION_MAP.put(Visit.RIGHT_HSUB, Visit.RIGHT_HSUB);
            PROJECTION_MAP.put(Visit.RIGHT_NO, Visit.RIGHT_NO);
            PROJECTION_MAP.put(Visit.INCUP, Visit.INCUP);

            PROJECTION_MAP.put(Visit.BMI, Visit.BMI);
            PROJECTION_MAP.put(Visit.SYMPTOMS, Visit.SYMPTOMS);
            PROJECTION_MAP.put(Visit.SYMPTOMSCO, Visit.SYMPTOMSCO);
            PROJECTION_MAP.put(Visit.VITALCHECK, Visit.VITALCHECK);
            PROJECTION_MAP.put(Visit.DIAGNOTE, Visit.DIAGNOTE);
            PROJECTION_MAP.put(Visit.HEALTHSUGGEST1, Visit.HEALTHSUGGEST1);

            PROJECTION_MAP.put(Visit.VITAL, Visit.VITAL);
            PROJECTION_MAP.put(Visit.WEIGHT, Visit.WEIGHT);
            PROJECTION_MAP.put(Visit.HEIGHT, Visit.HEIGHT);
            PROJECTION_MAP.put(Visit.PRESSURE, Visit.PRESSURE);
            PROJECTION_MAP.put(Visit.PRESSURE_LEVEL, Visit.PRESSURE_LEVEL);
            PROJECTION_MAP.put(Visit.PRESSURE_2, Visit.PRESSURE_2);
            PROJECTION_MAP.put(Visit.PRESSURE_LEVEL_2, Visit.PRESSURE_LEVEL_2);
            PROJECTION_MAP.put(Visit.TEMPERATURE, Visit.TEMPERATURE);
            PROJECTION_MAP.put(Visit.PULSE, Visit.PULSE);
            PROJECTION_MAP.put(Visit.RESPI, Visit.RESPI);
            PROJECTION_MAP.put(Visit.WAIST, Visit.WAIST);
            PROJECTION_MAP.put(Visit.ASS, Visit.ASS);
            PROJECTION_MAP.put(Visit.SCREEN_OTHER_DISEASE,
                    Visit.SCREEN_OTHER_DISEASE);

            PROJECTION_MAP.put(Visit.HEALTH_SUGGEST_1, Visit.HEALTH_SUGGEST_1);
            PROJECTION_MAP.put(Visit.HEALTH_SUGGEST_2, Visit.HEALTH_SUGGEST_2);
            PROJECTION_MAP.put(Visit.HEALTH_SUGGEST_TYPE,
                    Visit.HEALTH_SUGGEST_TYPE);

            PROJECTION_MAP.put(Visit.RECEIVE_PATIENT, Visit.RECEIVE_PATIENT);
            PROJECTION_MAP.put(Visit.RECEIVE_FROM, Visit.RECEIVE_FROM);
            PROJECTION_MAP.put(Visit.REFER_PATIENT, Visit.REFER_PATIENT);
            PROJECTION_MAP.put(Visit.REFER_TO_HOS, Visit.REFER_TO_HOS);
            PROJECTION_MAP.put(Visit.REFER_BACK, Visit.REFER_BACK);

            PROJECTION_MAP.put(Visit.ALIVE_STATUS, Visit.ALIVE_STATUS);

            PROJECTION_MAP.put(Visit.MONEY1, Visit.MONEY1);
            PROJECTION_MAP.put(Visit.MONEY2, Visit.MONEY2);
            PROJECTION_MAP.put(Visit.MONEY3, Visit.MONEY3);

            PROJECTION_MAP.put(Visit.USERNAME, Visit.USERNAME);
            PROJECTION_MAP.put(Visit.UPDATE, Visit.UPDATE);
            PROJECTION_MAP.put(Visit.FLAG_SERVICE, Visit.FLAG_SERVICE);
            PROJECTION_MAP.put(Visit.SERVICE_TYPE, Visit.SERVICE_TYPE);

            PROJECTION_MAP.put(Visit.UPDATE, Visit.UPDATE);
        }
    }

    public static class VisitDiag implements BaseColumns {
        public static final String TABLENAME = "visitdiag";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/diag");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.diag";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.diag";

        public static final String DEFAULT_SORTING = "visitno DESC, dxtype, diagcode";
        public static final String PCUCODE = "pcucode";
        public static final String CODE = "diagcode";
        public static final String TYPE = "dxtype";
        public static final String NO = "visitno";
        public static final String CONTINUE = "conti";
        public static final String CLINIC = "clinic";
        public static final String APPOINT_DATE = "appointdate";
        public static final String APPOINT_TYPE = "appointtype";
        public static final String DATEUPDATE = "dateupdate";
        public static final String MAX = "max";
        public static final String DOCTOR = "doctordiag";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(VisitDiag._ID, "visitdiag.visitno AS "
                    + VisitDiag._ID);
            PROJECTION_MAP.put(VisitDiag.PCUCODE, VisitDiag.PCUCODE);
            PROJECTION_MAP.put(VisitDiag.NO, VisitDiag.NO);
            PROJECTION_MAP.put(VisitDiag.CODE, VisitDiag.CODE);
            PROJECTION_MAP.put(VisitDiag.TYPE, VisitDiag.TYPE);
            PROJECTION_MAP.put(VisitDiag.CLINIC, VisitDiag.CLINIC);
            PROJECTION_MAP.put(VisitDiag.CONTINUE, VisitDiag.CONTINUE);
            PROJECTION_MAP.put(VisitDiag.APPOINT_DATE, VisitDiag.APPOINT_DATE);
            PROJECTION_MAP.put(VisitDiag.APPOINT_TYPE, VisitDiag.APPOINT_TYPE);
            PROJECTION_MAP.put(VisitDiag.DATEUPDATE, VisitDiag.DATEUPDATE);
            PROJECTION_MAP.put(VisitDiag.MAX, "max(visitdiag.visitno) AS "
                    + VisitDiag.MAX);
            PROJECTION_MAP.put(VisitDiag.DOCTOR, VisitDiag.DOCTOR);
        }

        public static Uri getContentUriId(long visitno, String diagcode) {
            return Uri.withAppendedPath(Visit.CONTENT_URI, visitno + "/diag/"
                    + diagcode);
        }
    }

    public static class VisitLabBlood implements BaseColumns, PersonColumns,
            VisitColumns {
        public static final String TABLENAME = "visitlabblood";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/labblood");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.labblood";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.labblood";

        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String DATECHECK = "datecheck";

        public static final String VDRL = "vdrl";
        public static final String HIV = "hiv";
        public static final String HBsAg = "hbag";
        public static final String TALAS_OF = "talas1";
        public static final String TALAS_DCIP = "talas2";
        public static final String CLINIC = "clinic";
        public static final String HOS_SERVICE = "hosservice";
        public static final String HOS_LAB = "hoslab";
        public static final String HCT = "hct";
        public static final String DATEUPDATE = "dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(_ID, "visitlabblood.visitno AS " + _ID);
            PROJECTION_MAP.put(_PCUCODE, _PCUCODE);
            PROJECTION_MAP.put(_VISITNO, _VISITNO);
            PROJECTION_MAP.put(DATECHECK, DATECHECK);
            PROJECTION_MAP.put(_PCUCODEPERSON, _PCUCODEPERSON);
            PROJECTION_MAP.put(_PID, _PID);

            PROJECTION_MAP.put(VDRL, VDRL);
            PROJECTION_MAP.put(HIV, HIV);
            PROJECTION_MAP.put(HBsAg, HBsAg);
            PROJECTION_MAP.put(TALAS_DCIP, TALAS_DCIP);
            PROJECTION_MAP.put(TALAS_OF, TALAS_OF);
            PROJECTION_MAP.put(CLINIC, CLINIC);
            PROJECTION_MAP.put(HOS_LAB, HOS_LAB);
            PROJECTION_MAP.put(HOS_SERVICE, HOS_SERVICE);
            PROJECTION_MAP.put(HCT, HCT);

            PROJECTION_MAP.put(DATEUPDATE, DATEUPDATE);
        }
    }

    public static class VisitAncRisk implements BaseColumns {
        public static final String TABLENAME = "visitancrisk";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/anc/risk");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.anc.risk";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.anc.risk";

        public static final String DEFAULT_SORTING = "pid, pregno DESC, ancriskcode";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String PREGNO = "pregno";
        public static final String CODE = "ancriskcode";
        public static final String REMARK = "remark";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(_ID, "pid || pregno AS " + _ID);
            PROJECTION_MAP.put(PCUCODEPERSON, PCUCODEPERSON);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(PREGNO, PREGNO);
            PROJECTION_MAP.put(CODE, CODE);
            PROJECTION_MAP.put(REMARK, REMARK);
        }

        public static Uri getContentUri(String pid, String pregno) {
            return Uri.withAppendedPath(Person.CONTENT_URI, pid + "/anc/"
                    + pregno + "/risk/");
        }

        public static Uri getContentUri(String pid, String pregno,
                                        String riskcode) {
            return Uri.withAppendedPath(Person.CONTENT_URI, pid + "/anc/"
                    + pregno + "/risk/" + riskcode);
        }

    }

    public static class VisitAnc implements PersonColumns, PregnancyColumns,
            VisitColumns, DateUpdateColumns {
        public static final String TABLENAME = "visitanc";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/anc");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.anc";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.anc";

        public static final String DEFAULT_SORTING = _PID + ", " + _VISITNO
                + " DESC";
        public static final String DATECHECK = "datecheck";
        public static final String DATE_APPOINT = "dateappointcheck";
        public static final String PREGNANC_AGE = "pregage";
        public static final String ANC_NO = "ancno";
        public static final String CARIES = "caries";
        public static final String GUM_FAIL = "gumfail";
        public static final String TARTAR = "tartar";
        public static final String TOOTH_CHECK = "toothcheck";
        public static final String THALAS_BABY_CHECK = "thalasbabycheck";
        public static final String DTANC = "ttno35";
        public static final String WEIGHT = "weight";
        public static final String HEIGHT = "height";
        public static final String BMI = "bmi";
        public static final String SUGAR = "sugar";
        public static final String ALBUMIN = "albumin";
        public static final String BREAST_CHECK = "breastcheck";
        public static final String HEADAHCE = "headache";
        public static final String SICKENING = "sickening";
        public static final String TYROID = "tyroid";
        public static final String DANCE_BABY = "dancebaby";
        public static final String UTMUGO = "utmugo";
        public static final String EDEMA = "edima";
        public static final String UTBLOOD = "utblood";
        public static final String CRAMP = "cramp";
        public static final String URINARY = "urinary";
        public static final String HEART_ATTACK = "heartattach";
        public static final String FUNDUS = "fundus";
        public static final String POSITION_BABY = "positionbaby";
        public static final String PILOT = "pilot";
        public static final String HEART_BABY = "heartbaby";
        public static final String HEART_BABY_COUNT = "heartbabycount";
        public static final String ANCRES = "ancres";
        public static final String HOSPITAL_SERVICE = "hosservice";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(_PCUCODEPERSON, _PCUCODEPERSON);
            PROJECTION_MAP.put(_PID, _PID);
            PROJECTION_MAP.put(_PREGNO, _PREGNO);
            PROJECTION_MAP.put(_DATEUPDATE, _DATEUPDATE);
            PROJECTION_MAP.put(_VISITNO, _VISITNO);

            PROJECTION_MAP.put(DATECHECK, DATECHECK);
            PROJECTION_MAP.put(DATE_APPOINT, DATE_APPOINT);
            PROJECTION_MAP.put(PREGNANC_AGE, PREGNANC_AGE);
            PROJECTION_MAP.put(ANC_NO, ANC_NO);
            PROJECTION_MAP.put(CARIES, CARIES);
            PROJECTION_MAP.put(GUM_FAIL, GUM_FAIL);
            PROJECTION_MAP.put(TARTAR, TARTAR);
            PROJECTION_MAP.put(TOOTH_CHECK, TOOTH_CHECK);
            PROJECTION_MAP.put(THALAS_BABY_CHECK, THALAS_BABY_CHECK);
            PROJECTION_MAP.put(DTANC, DTANC);
            PROJECTION_MAP.put(WEIGHT, WEIGHT);
            PROJECTION_MAP.put(HEIGHT, HEIGHT);
            PROJECTION_MAP.put(BMI, BMI);
            PROJECTION_MAP.put(SUGAR, SUGAR);
            PROJECTION_MAP.put(ALBUMIN, ALBUMIN);
            PROJECTION_MAP.put(BREAST_CHECK, BREAST_CHECK);
            PROJECTION_MAP.put(HEADAHCE, HEADAHCE);
            PROJECTION_MAP.put(SICKENING, SICKENING);
            PROJECTION_MAP.put(TYROID, TYROID);
            PROJECTION_MAP.put(DANCE_BABY, DANCE_BABY);
            PROJECTION_MAP.put(UTMUGO, UTMUGO);
            PROJECTION_MAP.put(EDEMA, EDEMA);
            PROJECTION_MAP.put(UTBLOOD, UTBLOOD);
            PROJECTION_MAP.put(CRAMP, CRAMP);
            PROJECTION_MAP.put(URINARY, URINARY);
            PROJECTION_MAP.put(HEART_ATTACK, HEART_ATTACK);
            PROJECTION_MAP.put(FUNDUS, FUNDUS);
            PROJECTION_MAP.put(POSITION_BABY, POSITION_BABY);
            PROJECTION_MAP.put(PILOT, PILOT);
            PROJECTION_MAP.put(HEART_BABY, HEART_BABY);
            PROJECTION_MAP.put(HEART_BABY_COUNT, HEART_BABY_COUNT);
            PROJECTION_MAP.put(ANCRES, ANCRES);
            PROJECTION_MAP.put(HOSPITAL_SERVICE, HOSPITAL_SERVICE);
        }

    }


    public static class VisitAncPregnancy implements PersonColumns,
            PregnancyColumns, DateUpdateColumns {
        public static final String TABLENAME = "visitancpregnancy";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/anc/");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.anc";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.anc";

        public static final String FIRST_DATE_CHECK = "firstdatecheck";
        public static final String LMP = "lmp";
        public static final String EDC = "edc";
        public static final String FP_BEFORE = "fpbefore";
        public static final String FIRST_ABNORMAL = "firstabnormal";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(_PCUCODEPERSON, _PCUCODEPERSON);
            PROJECTION_MAP.put(_PID, _PID);
            PROJECTION_MAP.put(_PREGNO, _PREGNO);
            PROJECTION_MAP.put(_DATEUPDATE, _DATEUPDATE);
            PROJECTION_MAP.put(FIRST_DATE_CHECK, FIRST_DATE_CHECK);
            PROJECTION_MAP.put(LMP, LMP);
            PROJECTION_MAP.put(EDC, EDC);
            PROJECTION_MAP.put(FIRST_ABNORMAL, FIRST_ABNORMAL);
            PROJECTION_MAP.put(FP_BEFORE, FP_BEFORE);
        }

        public static Uri getContentUri(String pid, String pregno) {
            return Uri.withAppendedPath(Person.CONTENT_URI, pid + "/anc/"
                    + pregno);
        }
    }

    public static class VisitEpi implements BaseColumns, VisitColumns,
            PersonColumns, DateUpdateColumns {
        public static final String TABLENAME = "visitepi";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/epi");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.epi";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.epi";

        public static final String DEFAULT_SORTING = "pid, visitno DESC";
        public static final String VACCINE_CODE = "vaccinecode";
        public static final String LOT_NO = "lotno";
        public static final String DATE_VACCINE_EXPIRE = "datevacineexpire";
        public static final String DATE_EPI = "dateepi";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(_ID, "visitepi.visitno AS " + VisitDiag._ID);
            PROJECTION_MAP.put(_PCUCODE, _PCUCODE);
            PROJECTION_MAP.put(_PCUCODEPERSON, _PCUCODEPERSON);
            PROJECTION_MAP.put(_PID, _PID);
            PROJECTION_MAP.put(_VISITNO, _VISITNO);
            PROJECTION_MAP.put(_DATEUPDATE, _DATEUPDATE);
            PROJECTION_MAP.put(DATE_EPI, DATE_EPI);
            PROJECTION_MAP.put(VACCINE_CODE, VACCINE_CODE);
            PROJECTION_MAP.put(LOT_NO, LOT_NO);
            PROJECTION_MAP.put(DATE_VACCINE_EXPIRE, DATE_VACCINE_EXPIRE);
        }

        public static final Uri getContentUri(String vn) {
            return Uri.withAppendedPath(Visit.CONTENT_URI, vn + "/epi");
        }

        public static final Uri getContentUri(String vn, String vaccineCode) {
            return Uri.withAppendedPath(Visit.CONTENT_URI, vn + "/epi/"
                    + vaccineCode);
        }
    }

    public static class VisitDrug implements BaseColumns, VisitColumns,
            DateUpdateColumns {
        public static final String TABLENAME = "visitdrug";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/drug");

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.drug";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.drug";

        public static final String DEFAULT_SORTING = "visitno DESC, dateupdate";
        public static final String NO = "visitno";
        public static final String CODE = "drugcode";
        public static final String COST = "costprice";
        public static final String REAL = "realprice";
        public static final String DOSE = "dose";
        public static final String UNIT = "unit";
        public static final String CLINIC = "clinic";
        public static final String DOCTOR1 = "doctor1";
        public static final String DOCTOR2 = "doctor2";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(_ID, "visitdiag.visitno AS " + VisitDiag._ID);
            PROJECTION_MAP.put(_PCUCODE, _PCUCODE);
            PROJECTION_MAP.put(NO, NO);
            PROJECTION_MAP.put(CODE, CODE);
            PROJECTION_MAP.put(COST, COST);
            PROJECTION_MAP.put(REAL, REAL);
            PROJECTION_MAP.put(DOSE, DOSE);
            PROJECTION_MAP.put(UNIT, UNIT);
            PROJECTION_MAP.put(CLINIC, CLINIC);
            PROJECTION_MAP.put(DOCTOR1, DOCTOR1);
            PROJECTION_MAP.put(DOCTOR2, DOCTOR2);
            PROJECTION_MAP.put(_DATEUPDATE, _DATEUPDATE);
        }

        public static Uri getContentUriId(long visitno, String drugcode) {
            return Uri.withAppendedPath(Visit.CONTENT_URI, visitno + "/drug/"
                    + drugcode);
        }
    }

    public static class VisitIndividual implements BaseColumns {
        public static final String TABLENAME = "visithomehealthindividual";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY
                + "/person/visit/homehealthindividual");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.homehealthindividual";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.homehealthindividual";

        public static final String DEFAULT_SORTING = "visitno ASC ";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String TYPE = "homehealthtype";
        public static final String PATIENTSIGN = "patientsign";
        public static final String DETAIL = "homehealthdetail";
        public static final String RESULT = "homehealthresult";
        public static final String PLAN = "homehealthplan";
        public static final String DATEAPPOINT = "dateappoint";
        public static final String USER = "user";


        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitIndividual._ID, "pcucode || visitno  AS "
                    + VisitIndividual._ID);
            PROJECTION_MAP
                    .put(VisitIndividual.PCUCODE, VisitIndividual.PCUCODE);
            PROJECTION_MAP
                    .put(VisitIndividual.VISITNO, VisitIndividual.VISITNO);
            PROJECTION_MAP.put(VisitIndividual.TYPE, VisitIndividual.TYPE);
            PROJECTION_MAP.put(VisitIndividual.PATIENTSIGN,
                    VisitIndividual.PATIENTSIGN);
            PROJECTION_MAP.put(VisitIndividual.DETAIL, VisitIndividual.DETAIL);
            PROJECTION_MAP.put(VisitIndividual.RESULT, VisitIndividual.RESULT);
            PROJECTION_MAP.put(VisitIndividual.PLAN, VisitIndividual.PLAN);
            PROJECTION_MAP.put(VisitIndividual.DATEAPPOINT,
                    VisitIndividual.DATEAPPOINT);
            PROJECTION_MAP.put(VisitIndividual.USER, VisitIndividual.USER);
        }

        public static Uri getContentUriId(long visitno, String drugcode) {
            return Uri.withAppendedPath(Visit.CONTENT_URI, visitno + "/drug/"
                    + drugcode);
        }
    }

    public static class Women implements BaseColumns {
        public static final String TABLENAME = "women";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/women");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.women";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.women";

        public static final String DEFAULT_SORTING = "pid ASC ";

        public static final String PCUCODE = "pcucodeperson";
        public static final String PID = "pid";
        public static final String FPTYPE = "fptype";
        public static final String REASONNOFP = "reasonnofp";
        public static final String CHILDALIVE = "childalive";
        public static final String DATESURVEY = "datesurvey";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Women._ID, "pcucode || pid  AS " + Women._ID);
            PROJECTION_MAP.put(Women.PCUCODE, Women.PCUCODE);
            PROJECTION_MAP.put(Women.PID, Women.PID);
            PROJECTION_MAP.put(Women.FPTYPE, Women.FPTYPE);
            PROJECTION_MAP.put(Women.REASONNOFP, Women.REASONNOFP);
            PROJECTION_MAP.put(Women.CHILDALIVE, Women.CHILDALIVE);
            PROJECTION_MAP.put(Women.DATESURVEY, Women.DATESURVEY);

        }
    }

    public static class VisitFamilyplan implements BaseColumns {
        public static final String TABLENAME = "visitfp";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/familyplan");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.familyplan";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.familyplan";
        public static final String DEFAULT_SORTING = "pid,datefp ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String VISITNO = "visitno";
        public static final String PID = "pid";
        public static final String DATEFP = "datefp";
        public static final String PREGTEST = "pregtest";
        public static final String PREGTESTUNIT = "pregtestunit";
        public static final String PREGTESTRESULT = "pregtestresult";
        public static final String TYPEFP = "typefp";
        public static final String FPCODE = "fpcode";
        public static final String UNIT = "unit";
        public static final String DATEDUE = "datedue";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitFamilyplan._ID, "pcucodeperson || pid AS "
                    + VisitFamilyplan._ID);
            PROJECTION_MAP.put(VisitFamilyplan.PCUCODEPERSON,
                    VisitFamilyplan.PCUCODEPERSON);
            PROJECTION_MAP
                    .put(VisitFamilyplan.VISITNO, VisitFamilyplan.VISITNO);
            PROJECTION_MAP.put(VisitFamilyplan.PID, VisitFamilyplan.PID);
            PROJECTION_MAP.put(VisitFamilyplan.DATEFP, VisitFamilyplan.DATEFP);
            PROJECTION_MAP.put(VisitFamilyplan.PREGTEST,
                    VisitFamilyplan.PREGTEST);
            PROJECTION_MAP.put(VisitFamilyplan.PREGTESTUNIT,
                    VisitFamilyplan.PREGTESTUNIT);
            PROJECTION_MAP.put(VisitFamilyplan.PREGTESTRESULT,
                    VisitFamilyplan.PREGTESTRESULT);
            PROJECTION_MAP.put(VisitFamilyplan.TYPEFP, VisitFamilyplan.TYPEFP);
            PROJECTION_MAP.put(VisitFamilyplan.FPCODE, VisitFamilyplan.FPCODE);
            PROJECTION_MAP.put(VisitFamilyplan.UNIT, VisitFamilyplan.UNIT);
            PROJECTION_MAP
                    .put(VisitFamilyplan.DATEDUE, VisitFamilyplan.DATEDUE);
        }

        public static Uri getContentUriId(long visitno, String pid) {
            return Uri.withAppendedPath(Visit.CONTENT_URI, visitno + "/drug/"
                    + pid);
        }
    }

    public static class VisitLabcancer implements BaseColumns {
        public static final String TABLENAME = "visitlabcancer";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/labcancer");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.labcancer";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.labcancer";
        public static final String DEFAULT_SORTING = "pid,datecheck ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String PID = "pid";
        public static final String DATECHECK = "datecheck";
        public static final String TYPECANCER = "typecancer";
        public static final String RESULT = "result";
        public static final String HOSSERVICE = "hosservice";
        public static final String HOSLAB = "hoslab";
        public static final String DATEUPDATE = "dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitLabcancer._ID, "pcucode || pid AS "
                    + VisitLabcancer._ID);
            PROJECTION_MAP.put(VisitLabcancer.PCUCODEPERSON,
                    VisitLabcancer.PCUCODEPERSON);
            PROJECTION_MAP.put(VisitLabcancer.PCUCODE, VisitLabcancer.PCUCODE);
            PROJECTION_MAP.put(VisitLabcancer.VISITNO, VisitLabcancer.VISITNO);
            PROJECTION_MAP.put(VisitLabcancer.PID, VisitLabcancer.PID);
            PROJECTION_MAP.put(VisitLabcancer.DATECHECK,
                    VisitLabcancer.DATECHECK);
            PROJECTION_MAP.put(VisitLabcancer.TYPECANCER,
                    VisitLabcancer.TYPECANCER);
            PROJECTION_MAP.put(VisitLabcancer.RESULT, VisitLabcancer.RESULT);
            PROJECTION_MAP.put(VisitLabcancer.HOSSERVICE,
                    VisitLabcancer.HOSSERVICE);
            PROJECTION_MAP.put(VisitLabcancer.HOSLAB, VisitLabcancer.HOSLAB);
            PROJECTION_MAP.put(VisitLabcancer.DATEUPDATE,
                    VisitLabcancer.DATEUPDATE);

        }

    }

    public static class VisitBabycare implements BaseColumns {
        public static final String TABLENAME = "visitbabycare";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/babycare");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.babycare";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.babycare";
        public static final String DEFAULT_SORTING = "pid,datecare ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String PID = "pid";
        public static final String DATECARE = "datecare";
        public static final String LOCATECARE = "locatecare";
        public static final String NAVEL = "navel";
        public static final String SKIN = "skin";
        public static final String FECI = "feci";
        public static final String URINE = "urine";
        public static final String BABYHEALTH = "babyhealth";
        public static final String DATEAPPOINTCARE = "dateappointcare";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitBabycare._ID, "pcucode || pid  AS "
                    + VisitBabycare._ID);
            PROJECTION_MAP.put(VisitBabycare.PCUCODEPERSON,
                    VisitBabycare.PCUCODEPERSON);
            PROJECTION_MAP.put(VisitBabycare.PCUCODE, VisitBabycare.PCUCODE);
            PROJECTION_MAP.put(VisitBabycare.VISITNO, VisitBabycare.VISITNO);
            PROJECTION_MAP.put(VisitBabycare.PID, VisitBabycare.PID);
            PROJECTION_MAP.put(VisitBabycare.DATECARE, VisitBabycare.DATECARE);
            PROJECTION_MAP.put(VisitBabycare.LOCATECARE,
                    VisitBabycare.LOCATECARE);
            PROJECTION_MAP.put(VisitBabycare.NAVEL, VisitBabycare.NAVEL);
            PROJECTION_MAP.put(VisitBabycare.SKIN, VisitBabycare.SKIN);
            PROJECTION_MAP.put(VisitBabycare.FECI, VisitBabycare.FECI);
            PROJECTION_MAP.put(VisitBabycare.URINE, VisitBabycare.URINE);
            PROJECTION_MAP.put(VisitBabycare.BABYHEALTH,
                    VisitBabycare.BABYHEALTH);
            PROJECTION_MAP.put(VisitBabycare.DATEAPPOINTCARE,
                    VisitBabycare.DATEAPPOINTCARE);
        }

    }

    public static class VisitSpecialperson implements BaseColumns {
        public static final String TABLENAME = "ffc_visitspecialperson";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/specialperson");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.specialperson";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.specialperson";
        public static final String DEFAULT_SORTING = "pid,disabtype ASC";

        public static final String PCUCODE = "pcucode";
        public static final String PID = "pid";
        public static final String DISABLE_ID = "disabid";
        public static final String DISABLE_TYPE = "disabtype";
        public static final String DISABLE_CAUSE = "disabcause";
        public static final String DIAGCODE = "diagcode";
        public static final String DATEDETECT = "datedetect";
        public static final String DATEEXPIRE = "dateexpire";
        public static final String DATEUPDATE = "dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitSpecialperson._ID,
                    "pcucode || disabtype  AS " + VisitSpecialperson._ID);
            PROJECTION_MAP.put(VisitSpecialperson.PCUCODE,
                    VisitSpecialperson.PCUCODE);
            PROJECTION_MAP.put(VisitSpecialperson.PID, VisitSpecialperson.PID);
            PROJECTION_MAP.put(VisitSpecialperson.DISABLE_ID,
                    VisitSpecialperson.DISABLE_ID);
            PROJECTION_MAP.put(VisitSpecialperson.DISABLE_TYPE,
                    VisitSpecialperson.DISABLE_TYPE);
            PROJECTION_MAP.put(VisitSpecialperson.DISABLE_CAUSE,
                    VisitSpecialperson.DISABLE_CAUSE);
            PROJECTION_MAP.put(VisitSpecialperson.DIAGCODE,
                    VisitSpecialperson.DIAGCODE);
            PROJECTION_MAP.put(VisitSpecialperson.DATEDETECT,
                    VisitSpecialperson.DATEDETECT);
            PROJECTION_MAP.put(VisitSpecialperson.DATEEXPIRE,
                    VisitSpecialperson.DATEEXPIRE);
            PROJECTION_MAP.put(VisitSpecialperson.DATEUPDATE,
                    VisitSpecialperson.DATEUPDATE);
        }

    }

    public static class VisitDiag506address implements BaseColumns {
        public static final String TABLENAME = "visitdiag506address";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/diag506");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.diag506";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.diag506";
        public static final String DEFAULT_SORTING = "visitno,diagcode ASC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String DIAGCODE = "diagcode";
        public static final String HNO = "hno";
        public static final String MU = "mu";
        public static final String ROAD = "road";
        public static final String SUBDISTCODE = "subdistcode";
        public static final String DISTCODE = "distcode";
        public static final String PROVCODE = "provcode";
        public static final String AREA = "area";
        public static final String SICKDATESTART = "sickdatestart";
        public static final String SICKDATEFIND = "sickdatefind";
        public static final String STATUS = "status";
        public static final String DEADDATE = "deaddate";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String MAX = "max";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP
                    .put(VisitDiag506address._ID,
                            "pcucode || visitno || type  AS "
                                    + VisitDiag506address._ID);
            PROJECTION_MAP.put(VisitDiag506address.PCUCODE,
                    VisitDiag506address.PCUCODE);
            PROJECTION_MAP.put(VisitDiag506address.VISITNO,
                    VisitDiag506address.VISITNO);
            PROJECTION_MAP.put(VisitDiag506address.DIAGCODE,
                    VisitDiag506address.DIAGCODE);
            PROJECTION_MAP
                    .put(VisitDiag506address.HNO, VisitDiag506address.HNO);
            PROJECTION_MAP.put(VisitDiag506address.MU, VisitDiag506address.MU);
            PROJECTION_MAP.put(VisitDiag506address.ROAD,
                    VisitDiag506address.ROAD);
            PROJECTION_MAP.put(VisitDiag506address.SUBDISTCODE,
                    VisitDiag506address.SUBDISTCODE);
            PROJECTION_MAP.put(VisitDiag506address.DISTCODE,
                    VisitDiag506address.DISTCODE);
            PROJECTION_MAP.put(VisitDiag506address.PROVCODE,
                    VisitDiag506address.PROVCODE);
            PROJECTION_MAP.put(VisitDiag506address.AREA,
                    VisitDiag506address.AREA);
            PROJECTION_MAP.put(VisitDiag506address.SICKDATESTART,
                    VisitDiag506address.SICKDATESTART);
            PROJECTION_MAP.put(VisitDiag506address.SICKDATEFIND,
                    VisitDiag506address.SICKDATEFIND);
            PROJECTION_MAP.put(VisitDiag506address.STATUS,
                    VisitDiag506address.STATUS);
            PROJECTION_MAP.put(VisitDiag506address.DEADDATE,
                    VisitDiag506address.DEADDATE);
            PROJECTION_MAP.put(VisitDiag506address.MAX,
                    "MAX(visitdiag506address.visitno) AS "
                            + VisitDiag506address.MAX);

        }
    }

    public static class VisitScreenspecialdisease implements BaseColumns {
        public static final String TABLENAME = "visitscreenspecialdisease";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY
                + "/person/visit/screenspecialdisease");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.screenspecialdisease";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.screenspecialdisease";
        public static final String DEFAULT_SORTING = "visitno,codescreen ASC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String CODESCREEN = "codescreen";
        public static final String CODERESULT = "coderesult";
        public static final String REMART = "remart";
        public static final String DEPRESSED = "depressed";
        public static final String FEDUP = "fedup";
        public static final String Q91 = "q91";
        public static final String Q92 = "q92";
        public static final String Q93 = "q93";
        public static final String Q94 = "q94";
        public static final String Q95 = "q95";
        public static final String Q96 = "q96";
        public static final String Q97 = "q97";
        public static final String Q98 = "q98";
        public static final String Q99 = "q99";
        public static final String RISKGROUP = "riskgroup";
        public static final String HEALTHF = "healthf";
        public static final String CONSULTF = "consultf";
        public static final String CBT = "cbt";
        public static final String SATIR = "satir";
        public static final String HEALTHBYDRUG = "healthbydrug";
        public static final String HEALTHOTHER = "healthother";
        public static final String SCREEN = "screen";
        public static final String DIAG = "diag";
        public static final String TREATMIND = "treatmind";
        public static final String TREATFOLLOW = "treatfollow";
        public static final String WATCHCAREFUL = "watchcareful";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitScreenspecialdisease._ID,
                    "pcucode || visitno || codescreen  AS "
                            + VisitScreenspecialdisease._ID);
            PROJECTION_MAP.put(VisitScreenspecialdisease.PCUCODE,
                    VisitScreenspecialdisease.PCUCODE);
            PROJECTION_MAP.put(VisitScreenspecialdisease.VISITNO,
                    VisitScreenspecialdisease.VISITNO);
            PROJECTION_MAP.put(VisitScreenspecialdisease.CODESCREEN,
                    VisitScreenspecialdisease.CODESCREEN);
            PROJECTION_MAP.put(VisitScreenspecialdisease.CODERESULT,
                    VisitScreenspecialdisease.CODERESULT);
            PROJECTION_MAP.put(VisitScreenspecialdisease.REMART,
                    VisitScreenspecialdisease.REMART);
            PROJECTION_MAP.put(VisitScreenspecialdisease.DEPRESSED,
                    VisitScreenspecialdisease.DEPRESSED);
            PROJECTION_MAP.put(VisitScreenspecialdisease.FEDUP,
                    VisitScreenspecialdisease.FEDUP);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q91,
                    VisitScreenspecialdisease.Q91);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q92,
                    VisitScreenspecialdisease.Q92);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q93,
                    VisitScreenspecialdisease.Q93);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q94,
                    VisitScreenspecialdisease.Q94);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q95,
                    VisitScreenspecialdisease.Q95);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q96,
                    VisitScreenspecialdisease.Q96);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q97,
                    VisitScreenspecialdisease.Q97);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q98,
                    VisitScreenspecialdisease.Q98);
            PROJECTION_MAP.put(VisitScreenspecialdisease.Q99,
                    VisitScreenspecialdisease.Q99);
            PROJECTION_MAP.put(VisitScreenspecialdisease.RISKGROUP,
                    VisitScreenspecialdisease.RISKGROUP);
            PROJECTION_MAP.put(VisitScreenspecialdisease.HEALTHF,
                    VisitScreenspecialdisease.HEALTHF);
            PROJECTION_MAP.put(VisitScreenspecialdisease.CONSULTF,
                    VisitScreenspecialdisease.CONSULTF);
            PROJECTION_MAP.put(VisitScreenspecialdisease.CBT,
                    VisitScreenspecialdisease.CBT);
            PROJECTION_MAP.put(VisitScreenspecialdisease.SATIR,
                    VisitScreenspecialdisease.SATIR);
            PROJECTION_MAP.put(VisitScreenspecialdisease.HEALTHBYDRUG,
                    VisitScreenspecialdisease.HEALTHBYDRUG);
            PROJECTION_MAP.put(VisitScreenspecialdisease.HEALTHOTHER,
                    VisitScreenspecialdisease.HEALTHOTHER);
            PROJECTION_MAP.put(VisitScreenspecialdisease.SCREEN,
                    VisitScreenspecialdisease.SCREEN);
            PROJECTION_MAP.put(VisitScreenspecialdisease.DIAG,
                    VisitScreenspecialdisease.DIAG);
            PROJECTION_MAP.put(VisitScreenspecialdisease.TREATMIND,
                    VisitScreenspecialdisease.TREATMIND);
            PROJECTION_MAP.put(VisitScreenspecialdisease.TREATFOLLOW,
                    VisitScreenspecialdisease.TREATFOLLOW);
            PROJECTION_MAP.put(VisitScreenspecialdisease.WATCHCAREFUL,
                    VisitScreenspecialdisease.WATCHCAREFUL);
        }
    }

    public static class VisitAncDeliver implements BaseColumns {
        public static final String TABLENAME = "visitancdeliver";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/ancdeliver");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ancdeliver";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ancdeliver";
        public static final String DEFAULT_SORTING = "pid,visitno DESC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String PREGNO = "pregno";
        public static final String HOSSERVICE = "hosservice";
        public static final String DATEDELIVER = "datedeliver";
        public static final String DELIVERTIME = "delivertime";
        public static final String DELIVERRESULT = "deliverresult";
        public static final String OPERATER = "operater";
        public static final String DELIVERTYPE = "delivertype";
        public static final String NUMDATEINPREG = "numdeadinpreg";
        public static final String DELIVERPLACE = "deliverplace";
        public static final String DATEAPPOINTCARE = "dateappointcare";
        public static final String DELIVEREND = "deliverend";
        public static final String SIGNABNORMALANC = "signabnormalanc";
        public static final String SIGNABNORMALDELIVER = "signabnormaldeliver";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitAncDeliver._ID,
                    "visitno|| visitno || pid  AS " + VisitAncDeliver._ID);
            PROJECTION_MAP.put(VisitAncDeliver.PCUCODEPERSON,
                    VisitAncDeliver.PCUCODEPERSON);
            PROJECTION_MAP
                    .put(VisitAncDeliver.PCUCODE, VisitAncDeliver.PCUCODE);
            PROJECTION_MAP
                    .put(VisitAncDeliver.VISITNO, VisitAncDeliver.VISITNO);
            PROJECTION_MAP.put(VisitAncDeliver.PID, VisitAncDeliver.PID);
            PROJECTION_MAP.put(VisitAncDeliver.PREGNO, VisitAncDeliver.PREGNO);
            PROJECTION_MAP.put(VisitAncDeliver.HOSSERVICE,
                    VisitAncDeliver.HOSSERVICE);
            PROJECTION_MAP.put(VisitAncDeliver.DATEDELIVER,
                    VisitAncDeliver.DATEDELIVER);
            PROJECTION_MAP.put(VisitAncDeliver.DELIVERTIME,
                    VisitAncDeliver.DELIVERTIME);
            PROJECTION_MAP.put(VisitAncDeliver.DELIVERRESULT,
                    VisitAncDeliver.DELIVERRESULT);
            PROJECTION_MAP.put(VisitAncDeliver.OPERATER,
                    VisitAncDeliver.OPERATER);
            PROJECTION_MAP.put(VisitAncDeliver.DELIVERTYPE,
                    VisitAncDeliver.DELIVERTYPE);
            PROJECTION_MAP.put(VisitAncDeliver.NUMDATEINPREG,
                    VisitAncDeliver.NUMDATEINPREG);
            PROJECTION_MAP.put(VisitAncDeliver.DELIVERPLACE,
                    VisitAncDeliver.DELIVERPLACE);
            PROJECTION_MAP.put(VisitAncDeliver.DATEAPPOINTCARE,
                    VisitAncDeliver.DATEAPPOINTCARE);
            PROJECTION_MAP.put(VisitAncDeliver.DELIVEREND,
                    VisitAncDeliver.DELIVEREND);
            PROJECTION_MAP.put(VisitAncDeliver.SIGNABNORMALANC,
                    VisitAncDeliver.SIGNABNORMALANC);
            PROJECTION_MAP.put(VisitAncDeliver.SIGNABNORMALDELIVER,
                    VisitAncDeliver.SIGNABNORMALDELIVER);
        }
    }

    public static class VisitAncMotherCare implements BaseColumns {
        public static final String TABLENAME = "visitancmothercare";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/ancmothercare");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ancmothercare";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ancmothercare";
        public static final String DEFAULT_SORTING = "pid,visitno DESC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String PREGNO = "pregno";
        public static final String LOCATECARE = "locatecare";
        public static final String DATEAPPOINTCARE = "dateappointcare";
        public static final String FUNDUSLEVEL = "funduslevel";
        public static final String WABAD = "wabad";
        public static final String BREAST = "breast";
        public static final String MILK = "milk";
        public static final String MEN = "men";
        public static final String ALB = "alb";
        public static final String SUGAR = "sugar";
        public static final String TEAR = "tear";
        public static final String TTNO35 = "ttno35";
        public static final String HOSSERVICE = "hosservice";
        public static final String RESULTCHECK = "resultcheck";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitAncMotherCare._ID,
                    "visitno|| visitno || pid  AS " + VisitAncMotherCare._ID);
            PROJECTION_MAP.put(VisitAncMotherCare.PCUCODEPERSON,
                    VisitAncMotherCare.PCUCODEPERSON);
            PROJECTION_MAP.put(VisitAncMotherCare.PCUCODE,
                    VisitAncMotherCare.PCUCODE);
            PROJECTION_MAP.put(VisitAncMotherCare.VISITNO,
                    VisitAncMotherCare.VISITNO);
            PROJECTION_MAP.put(VisitAncMotherCare.PID, VisitAncMotherCare.PID);
            PROJECTION_MAP.put(VisitAncMotherCare.PREGNO,
                    VisitAncMotherCare.PREGNO);
            PROJECTION_MAP.put(VisitAncMotherCare.LOCATECARE,
                    VisitAncMotherCare.LOCATECARE);
            PROJECTION_MAP.put(VisitAncMotherCare.DATEAPPOINTCARE,
                    VisitAncMotherCare.DATEAPPOINTCARE);
            PROJECTION_MAP.put(VisitAncMotherCare.FUNDUSLEVEL,
                    VisitAncMotherCare.FUNDUSLEVEL);
            PROJECTION_MAP.put(VisitAncMotherCare.WABAD,
                    VisitAncMotherCare.WABAD);
            PROJECTION_MAP.put(VisitAncMotherCare.BREAST,
                    VisitAncMotherCare.BREAST);
            PROJECTION_MAP
                    .put(VisitAncMotherCare.MILK, VisitAncMotherCare.MILK);
            PROJECTION_MAP.put(VisitAncMotherCare.MEN, VisitAncMotherCare.MEN);
            PROJECTION_MAP.put(VisitAncMotherCare.ALB, VisitAncMotherCare.ALB);
            PROJECTION_MAP.put(VisitAncMotherCare.SUGAR,
                    VisitAncMotherCare.SUGAR);
            PROJECTION_MAP
                    .put(VisitAncMotherCare.TEAR, VisitAncMotherCare.TEAR);
            PROJECTION_MAP.put(VisitAncMotherCare.TTNO35,
                    VisitAncMotherCare.TTNO35);
            PROJECTION_MAP.put(VisitAncMotherCare.HOSSERVICE,
                    VisitAncMotherCare.HOSSERVICE);
            PROJECTION_MAP.put(VisitAncMotherCare.RESULTCHECK,
                    VisitAncMotherCare.RESULTCHECK);

        }
    }

    public static class VisitOldter implements BaseColumns {
        public static final String TABLENAME = "ffc_visitoldter";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/oldter");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.oldter";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.oldter";
        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String PID = "pid";
        public static final String SEX = "sex";
        public static final String AGE = "age";
        public static final String WEIGHT = "weight";
        public static final String HEIGHT = "height";
        public static final String WAIST = "waist";
        public static final String CIGA = "ciga";
        public static final String WISKY = "wisky";
        public static final String EXERCISE = "exercise";
        public static final String BIGACCIDENTEVER = "bigaccidentever";
        public static final String TONIC = "tonic";
        public static final String DRUGBYYOURSEFT = "drugbyyourseft";
        public static final String SUGAR = "sugar";
        public static final String SALT = "salt";
        public static final String Q_CONGENITALDISEASE = "q_congenitaldisease";
        public static final String DIAGCODE = "diagcode";

        public static final String DENTALCHECK = "dentalcheck";
        public static final String SLEEPHOUR = "sleephour";
        public static final String SLEEP_Q1 = "sleep_q1";
        // public static final String CLUBMEMBER = " clubmember ";
        public static final String CLUBNAME = "clubname";
        public static final String FUNDED = " funded ";
        public static final String MONEY_FUNDED = "money_funded";
        // public static final String CAREKEEPER = " carekeeper ";
        public static final String CAREKEEPER_NAME = "carekeeper_name";
        public static final String PRESSURE_Q1 = "pressure_q1";
        public static final String PRESSURE_WORK = "pressure_work";
        public static final String PRESSURE_FAMILY = "pressure_family";
        public static final String PRESSURE_MONEY = "pressure_money";
        public static final String PRESSURE_SOCIAL = "pressure_social";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitOldter._ID, " pcucode || visitno  AS "
                    + VisitOldter._ID);
            PROJECTION_MAP.put(VisitOldter.PCUCODE, VisitOldter.PCUCODE);
            PROJECTION_MAP.put(VisitOldter.VISITNO, VisitOldter.VISITNO);
            PROJECTION_MAP.put(VisitOldter.PID, VisitOldter.PID);
            PROJECTION_MAP.put(VisitOldter.WEIGHT, VisitOldter.WEIGHT);
            PROJECTION_MAP.put(VisitOldter.HEIGHT, VisitOldter.HEIGHT);
            PROJECTION_MAP.put(VisitOldter.WAIST, VisitOldter.WAIST);
            PROJECTION_MAP.put(VisitOldter.CIGA, VisitOldter.CIGA);
            PROJECTION_MAP.put(VisitOldter.WISKY, VisitOldter.WISKY);
            PROJECTION_MAP.put(VisitOldter.EXERCISE, VisitOldter.EXERCISE);
            PROJECTION_MAP.put(VisitOldter.BIGACCIDENTEVER,
                    VisitOldter.BIGACCIDENTEVER);
            PROJECTION_MAP.put(VisitOldter.TONIC, VisitOldter.TONIC);
            PROJECTION_MAP.put(VisitOldter.DRUGBYYOURSEFT,
                    VisitOldter.DRUGBYYOURSEFT);
            PROJECTION_MAP.put(VisitOldter.SUGAR, VisitOldter.SUGAR);
            PROJECTION_MAP.put(VisitOldter.SALT, VisitOldter.SALT);
            PROJECTION_MAP.put(VisitOldter.Q_CONGENITALDISEASE,
                    VisitOldter.Q_CONGENITALDISEASE);
            PROJECTION_MAP.put(VisitOldter.DIAGCODE, VisitOldter.DIAGCODE);

            PROJECTION_MAP
                    .put(VisitOldter.DENTALCHECK, VisitOldter.DENTALCHECK);
            PROJECTION_MAP.put(VisitOldter.SLEEPHOUR, VisitOldter.SLEEPHOUR);
            PROJECTION_MAP.put(VisitOldter.SLEEP_Q1, VisitOldter.SLEEP_Q1);
            // PROJECTION_MAP.put(VisitOldter.CLUBMEMBER,VisitOldter.CLUBMEMBER);
            PROJECTION_MAP.put(VisitOldter.CLUBNAME, VisitOldter.CLUBNAME);
            PROJECTION_MAP.put(VisitOldter.FUNDED, VisitOldter.FUNDED);
            PROJECTION_MAP.put(VisitOldter.MONEY_FUNDED,
                    VisitOldter.MONEY_FUNDED);
            // PROJECTION_MAP.put(VisitOldter.CAREKEEPER,VisitOldter.CAREKEEPER);
            PROJECTION_MAP.put(VisitOldter.CAREKEEPER_NAME,
                    VisitOldter.CAREKEEPER_NAME);
            PROJECTION_MAP
                    .put(VisitOldter.PRESSURE_Q1, VisitOldter.PRESSURE_Q1);
            PROJECTION_MAP.put(VisitOldter.PRESSURE_WORK,
                    VisitOldter.PRESSURE_WORK);
            PROJECTION_MAP.put(VisitOldter.PRESSURE_FAMILY,
                    VisitOldter.PRESSURE_FAMILY);
            PROJECTION_MAP.put(VisitOldter.PRESSURE_MONEY,
                    VisitOldter.PRESSURE_MONEY);
            PROJECTION_MAP.put(VisitOldter.PRESSURE_SOCIAL,
                    VisitOldter.PRESSURE_SOCIAL);

        }
    }

    public static class VisitNutrition implements BaseColumns {
        public static final String TABLENAME = "visitnutrition";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/nutrition");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.nutrition";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.nutrition";
        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String WEIGHT = " weight ";
        public static final String TALL = " tall ";
        public static final String HEADCYCLE = " headcycle ";
        public static final String TOOTHNEW = " toothnew ";
        public static final String TOOTHCORRUPT = " toothcorrupt ";
        public static final String NAVEL = " navel ";
        public static final String DATEAPPOINT = " dateappoint ";
        public static final String REMARK = " remark ";
        public static final String NLEVEL = " nlevel ";
        public static final String NLEVEL2 = " nlevel2 ";
        public static final String NLEVEL3 = " nlevel3 ";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitNutrition._ID, " pcucode || visitno  AS "
                    + VisitNutrition._ID);
            PROJECTION_MAP.put(VisitNutrition.PCUCODE, VisitNutrition.PCUCODE);
            PROJECTION_MAP.put(VisitNutrition.VISITNO, VisitNutrition.VISITNO);
            PROJECTION_MAP.put(VisitNutrition.WEIGHT, VisitNutrition.WEIGHT);
            PROJECTION_MAP.put(VisitNutrition.TALL, VisitNutrition.TALL);
            PROJECTION_MAP.put(VisitNutrition.HEADCYCLE,
                    VisitNutrition.HEADCYCLE);
            PROJECTION_MAP
                    .put(VisitNutrition.TOOTHNEW, VisitNutrition.TOOTHNEW);
            PROJECTION_MAP.put(VisitNutrition.TOOTHCORRUPT,
                    VisitNutrition.TOOTHCORRUPT);
            PROJECTION_MAP.put(VisitNutrition.NAVEL, VisitNutrition.NAVEL);
            PROJECTION_MAP.put(VisitNutrition.DATEAPPOINT,
                    VisitNutrition.DATEAPPOINT);
            PROJECTION_MAP.put(VisitNutrition.REMARK, VisitNutrition.REMARK);
            PROJECTION_MAP.put(VisitNutrition.NLEVEL, VisitNutrition.NLEVEL);
            PROJECTION_MAP.put(VisitNutrition.NLEVEL2, VisitNutrition.NLEVEL2);
            PROJECTION_MAP.put(VisitNutrition.NLEVEL3, VisitNutrition.NLEVEL3);
        }
    }

    public static class VisitDiagAppoint implements BaseColumns {
        public static final String TABLENAME = "visitdiagappoint";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/diagappoint");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.diagappoint";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.diagappoint";
        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String DIAGCODE = " diagcode ";
        public static final String APPODATE = " appodate ";
        public static final String APPOTYPE = " appotype ";
        public static final String COMMENT = " comment ";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitDiagAppoint._ID, " pcucode || visitno  AS "
                    + VisitDiagAppoint._ID);
            PROJECTION_MAP.put(VisitDiagAppoint.PCUCODE,
                    VisitDiagAppoint.PCUCODE);
            PROJECTION_MAP.put(VisitDiagAppoint.VISITNO,
                    VisitDiagAppoint.VISITNO);
            PROJECTION_MAP.put(VisitDiagAppoint.DIAGCODE,
                    VisitDiagAppoint.DIAGCODE);
            PROJECTION_MAP.put(VisitDiagAppoint.APPODATE,
                    VisitDiagAppoint.APPODATE);
            PROJECTION_MAP.put(VisitDiagAppoint.APPOTYPE,
                    VisitDiagAppoint.APPOTYPE);
            PROJECTION_MAP.put(VisitDiagAppoint.COMMENT,
                    VisitDiagAppoint.COMMENT);
        }
    }

    public static class VisitDentalCheck implements BaseColumns {
        public static final String TABLENAME = "visitdentalcheck";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/dentalcheck");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.dentalcheck";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.dentalcheck";
        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String TOOTHMILK = " toothmilk ";
        public static final String TOOTHMILKCORRUPT = " toothmilkcorrupt ";
        public static final String TOOTHPERMANENT = " toothpermanent ";
        public static final String TOOTHPERMANENTCORRUPT = " toothpermanentcorrupt ";
        public static final String TARTAR = " tartar ";
        public static final String GUMSTATUS = " gumstatus ";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitDentalCheck._ID, " pcucode || visitno  AS "
                    + VisitDentalCheck._ID);
            PROJECTION_MAP.put(VisitDentalCheck.PCUCODE,
                    VisitDentalCheck.PCUCODE);
            PROJECTION_MAP.put(VisitDentalCheck.VISITNO,
                    VisitDentalCheck.VISITNO);
            PROJECTION_MAP.put(VisitDentalCheck.TOOTHMILK,
                    VisitDentalCheck.TOOTHMILK);
            PROJECTION_MAP.put(VisitDentalCheck.TOOTHMILKCORRUPT,
                    VisitDentalCheck.TOOTHMILKCORRUPT);
            PROJECTION_MAP.put(VisitDentalCheck.TOOTHPERMANENT,
                    VisitDentalCheck.TOOTHPERMANENT);
            PROJECTION_MAP.put(VisitDentalCheck.TOOTHPERMANENTCORRUPT,
                    VisitDentalCheck.TOOTHPERMANENTCORRUPT);
            PROJECTION_MAP
                    .put(VisitDentalCheck.TARTAR, VisitDentalCheck.TARTAR);
            PROJECTION_MAP.put(VisitDentalCheck.GUMSTATUS,
                    VisitDentalCheck.GUMSTATUS);
        }

    }

    public static class VisitPersongrow implements BaseColumns {
        public static final String TABLENAME = "persongrow";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/persongrow");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.persongrow";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.persongrow";
        public static final String DEFAULT_SORTING = "pcucodeperson||pid ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String GROWCODE = " growcode ";
        public static final String AGEMONTHCAN = " agemonthcan ";
        public static final String DATESURVEY = " datesurvey ";
        public static final String ABNORMAL = " abnormal ";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitPersongrow._ID, " pcucode || visitno  AS "
                    + VisitPersongrow._ID);
            PROJECTION_MAP.put(VisitPersongrow.PCUCODEPERSON,
                    VisitPersongrow.PCUCODEPERSON);
            PROJECTION_MAP.put(VisitPersongrow.PID, VisitPersongrow.PID);
            PROJECTION_MAP.put(VisitPersongrow.GROWCODE,
                    VisitPersongrow.GROWCODE);
            PROJECTION_MAP.put(VisitPersongrow.AGEMONTHCAN,
                    VisitPersongrow.AGEMONTHCAN);
            PROJECTION_MAP.put(VisitPersongrow.DATESURVEY,
                    VisitPersongrow.DATESURVEY);
            PROJECTION_MAP.put(VisitPersongrow.ABNORMAL,
                    VisitPersongrow.ABNORMAL);
        }
    }

    public static class PersonDeath implements BaseColumns {
        public static final String TABLENAME = "persondeath";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/persondeath");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.persondeath";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.persondeath";
        public static final String DEFAULT_SORTING = "pcucodeperson||pid ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String DEATHA = "cdeatha";
        public static final String DEATHB = "cdeathb";
        public static final String DEATHC = "cdeathc";
        public static final String DEATHD = "cdeathd";
        public static final String DEATHDISEASE = "odisease";
        public static final String DEATHCAUSE = "deadcause";
        public static final String DELIVERYCONCERN = "deliveryconcern";
        public static final String DEATHDATE = "deaddate";
        public static final String DEATHPLACE = "deadplace";
        public static final String SOURCE = "source";
        public static final String DATEUPDATE = "dateupdate";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonDeath._ID,
                    " pcucodeperson || visitno  AS " + PersonDeath._ID);
            PROJECTION_MAP.put(PersonDeath.PCUCODEPERSON,
                    PersonDeath.PCUCODEPERSON);
            PROJECTION_MAP.put(PersonDeath.PID, PersonDeath.PID);
            PROJECTION_MAP.put(PersonDeath.DEATHA, PersonDeath.DEATHA);
            PROJECTION_MAP.put(PersonDeath.DEATHB, PersonDeath.DEATHB);
            PROJECTION_MAP.put(PersonDeath.DEATHC, PersonDeath.DEATHC);
            PROJECTION_MAP.put(PersonDeath.DEATHD, PersonDeath.DEATHD);
            PROJECTION_MAP.put(PersonDeath.DEATHDISEASE,
                    PersonDeath.DEATHDISEASE);
            PROJECTION_MAP.put(PersonDeath.DEATHCAUSE, PersonDeath.DEATHCAUSE);
            PROJECTION_MAP.put(PersonDeath.DELIVERYCONCERN,
                    PersonDeath.DELIVERYCONCERN);
            PROJECTION_MAP.put(PersonDeath.DEATHDATE, PersonDeath.DEATHDATE);
            PROJECTION_MAP.put(PersonDeath.DEATHPLACE, PersonDeath.DEATHPLACE);
            PROJECTION_MAP.put(PersonDeath.SOURCE, PersonDeath.SOURCE);
            PROJECTION_MAP.put(PersonDeath.DATEUPDATE, PersonDeath.DATEUPDATE);

        }
    }

    public static class VisitEpiAppoint implements BaseColumns {
        public static final String TABLENAME = "visitepiappoint";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/epiappoint");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.epiappoint";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.epiappoint";
        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String PID = "pid";
        public static final String VACCINECODE = " vaccinecode ";
        public static final String APPODATE = " dateappoint ";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitEpiAppoint._ID, " pcucode || visitno  AS "
                    + VisitEpiAppoint._ID);
            PROJECTION_MAP.put(VisitEpiAppoint.PCUCODEPERSON,
                    VisitEpiAppoint.PCUCODEPERSON);
            PROJECTION_MAP
                    .put(VisitEpiAppoint.PCUCODE, VisitEpiAppoint.PCUCODE);
            PROJECTION_MAP.put(VisitEpiAppoint.PID, VisitEpiAppoint.PID);
            PROJECTION_MAP
                    .put(VisitEpiAppoint.VISITNO, VisitEpiAppoint.VISITNO);
            PROJECTION_MAP.put(VisitEpiAppoint.VACCINECODE,
                    VisitEpiAppoint.VACCINECODE);
            PROJECTION_MAP.put(VisitEpiAppoint.APPODATE,
                    VisitEpiAppoint.APPODATE);

        }
    }

    public static class PersonunableType implements BaseColumns,
            DateUpdateColumns {
        public static final String TABLENAME = "personunable1type";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/unabletype");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.unabletype";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.unabletype";
        public static final String DEFAULT_SORTING = "typecode ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String TYPECODE = "typecode";
        public static final String UNABLELEVEL = "unablelevel";
        public static final String DATEFOUND = "datefound";
        public static final String DATEUPDATE = "dateupdate";
        public static final String DISABCAUSE = "disabcause";
        public static final String DIAGCAUSE = "diagcode";
        public static final String DATESTART = "datestartunable";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonunableType._ID,
                    " pcucodeperson || pid  AS " + PersonunableType._ID);
            PROJECTION_MAP.put(PersonunableType.PCUCODEPERSON,
                    PersonunableType.PCUCODEPERSON);
            PROJECTION_MAP.put(PersonunableType.PID, PersonunableType.PID);
            PROJECTION_MAP.put(PersonunableType.TYPECODE,
                    PersonunableType.TYPECODE);
            PROJECTION_MAP.put(PersonunableType.UNABLELEVEL,
                    PersonunableType.UNABLELEVEL);
            PROJECTION_MAP.put(PersonunableType.DATEFOUND,
                    PersonunableType.DATEFOUND);

            PROJECTION_MAP.put(_DATEUPDATE, _DATEUPDATE);

            PROJECTION_MAP.put(PersonunableType.DATEUPDATE,
                    PersonunableType.DATEUPDATE);
            PROJECTION_MAP.put(PersonunableType.DISABCAUSE,
                    PersonunableType.DISABCAUSE);
            PROJECTION_MAP.put(PersonunableType.DIAGCAUSE,
                    PersonunableType.DIAGCAUSE);
            PROJECTION_MAP.put(PersonunableType.DATESTART,
                    PersonunableType.DATESTART);
        }
    }

    public static class PersonunableProblem implements BaseColumns {
        public static final String TABLENAME = "personunable2prob";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/unableproblem");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.unableproblem";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.unableproblem";
        public static final String DEFAULT_SORTING = "unableproblemcode ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String PROBLEMCODE = "unableproblemcode";
        public static final String DATEUPDATE = "dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonunableProblem._ID,
                    " pcucodeperson || pid  AS " + PersonunableProblem._ID);
            PROJECTION_MAP.put(PersonunableProblem.PCUCODEPERSON,
                    PersonunableProblem.PCUCODEPERSON);
            PROJECTION_MAP
                    .put(PersonunableProblem.PID, PersonunableProblem.PID);
            PROJECTION_MAP.put(PersonunableProblem.PROBLEMCODE,
                    PersonunableProblem.PROBLEMCODE);
            PROJECTION_MAP.put(PersonunableProblem.DATEUPDATE,
                    PersonunableProblem.DATEUPDATE);
        }
    }

    public static class PersonunableNeed implements BaseColumns {
        public static final String TABLENAME = "personunable3need";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/unableneed");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.unableneed";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.unableneed";
        public static final String DEFAULT_SORTING = "needcode ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String NEEDCODE = "needcode";
        public static final String DATEUPDATE = "dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonunableNeed._ID,
                    " pcucodeperson || pid  AS " + PersonunableNeed._ID);
            PROJECTION_MAP.put(PersonunableNeed.PCUCODEPERSON,
                    PersonunableNeed.PCUCODEPERSON);
            PROJECTION_MAP.put(PersonunableNeed.PID, PersonunableNeed.PID);
            PROJECTION_MAP.put(PersonunableNeed.NEEDCODE,
                    PersonunableNeed.NEEDCODE);
            PROJECTION_MAP.put(PersonunableNeed.DATEUPDATE,
                    PersonunableNeed.DATEUPDATE);
        }
    }

    public static class PersonunableHelp implements BaseColumns {
        public static final String TABLENAME = "personunable4help";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/unablehelp");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.unablehelp";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.unablehelp";
        public static final String DEFAULT_SORTING = "helpcode ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String HELPCODE = "helpcode";
        public static final String DATEHELP = "datehelp";
        public static final String PROVIDER = "provider";
        public static final String DATEUPDATE = "dateupdate";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PersonunableHelp._ID,
                    " pcucodeperson || pid  AS " + PersonunableHelp._ID);
            PROJECTION_MAP.put(PersonunableHelp.PCUCODEPERSON,
                    PersonunableHelp.PCUCODEPERSON);
            PROJECTION_MAP.put(PersonunableHelp.PID, PersonunableHelp.PID);
            PROJECTION_MAP.put(PersonunableHelp.HELPCODE,
                    PersonunableHelp.HELPCODE);
            PROJECTION_MAP.put(PersonunableHelp.DATEHELP,
                    PersonunableHelp.DATEHELP);
            PROJECTION_MAP.put(PersonunableHelp.PROVIDER,
                    PersonunableHelp.PROVIDER);
            PROJECTION_MAP.put(PersonunableHelp.DATEUPDATE,
                    PersonunableHelp.DATEUPDATE);
        }
    }

    public static class VisitDrugDentalDiag implements BaseColumns {
        public static final String TABLENAME = "visitdrugdentaldiag";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/drugdentaldiag");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.drugdentaldiag";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.drugdentaldiag";
        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String DENTCODE = "dentcode";
        public static final String TOOTHAREA = "tootharea";
        public static final String DIAGCODE = "diagcode";
        public static final String DXTYPE = "dxtype";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitDrugDentalDiag._ID,
                    " pcucode || visitno  AS " + VisitDrugDentalDiag._ID);
            PROJECTION_MAP.put(VisitDrugDentalDiag.PCUCODE,
                    VisitDrugDentalDiag.PCUCODE);
            PROJECTION_MAP.put(VisitDrugDentalDiag.VISITNO,
                    VisitDrugDentalDiag.VISITNO);
            PROJECTION_MAP.put(VisitDrugDentalDiag.DENTCODE,
                    VisitDrugDentalDiag.DENTCODE);
            PROJECTION_MAP.put(VisitDrugDentalDiag.TOOTHAREA,
                    VisitDrugDentalDiag.TOOTHAREA);
            PROJECTION_MAP.put(VisitDrugDentalDiag.DIAGCODE,
                    VisitDrugDentalDiag.DIAGCODE + " AS "
                            + VisitDrugDentalDiag.DIAGCODE);
            PROJECTION_MAP.put(VisitDrugDentalDiag.DXTYPE,
                    VisitDrugDentalDiag.DXTYPE);
        }
    }

    public static class VisitDrugDental implements BaseColumns {
        public static final String TABLENAME = "visitdrugdental";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/drugdental");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit.drugdental";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit.drugdental";
        public static final String DEFAULT_SORTING = "visitno DESC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String DENTCODE = "dentcode";
        public static final String TOOTHAREA = "tootharea";
        public static final String SURFACE = "surface";
        public static final String COMMENT = "comment";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VisitDrugDental._ID, " pcucode || visitno  AS "
                    + VisitDrugDental._ID);
            PROJECTION_MAP
                    .put(VisitDrugDental.PCUCODE, VisitDrugDental.PCUCODE);
            PROJECTION_MAP
                    .put(VisitDrugDental.VISITNO, VisitDrugDental.VISITNO);
            PROJECTION_MAP.put(VisitDrugDental.DENTCODE,
                    VisitDrugDental.DENTCODE);
            PROJECTION_MAP.put(VisitDrugDental.TOOTHAREA,
                    VisitDrugDental.TOOTHAREA);
            PROJECTION_MAP
                    .put(VisitDrugDental.SURFACE, VisitDrugDental.SURFACE);
            PROJECTION_MAP
                    .put(VisitDrugDental.COMMENT, VisitDrugDental.COMMENT);
        }
    }

    public static class Personunable implements BaseColumns {
        public static final String TABLENAME = "personunable";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/unable");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.unable";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.unable";
        public static final String DEFAULT_SORTING = "pid ASC";

        public static final String PCUCODEPERSON = "pcucodeperson";
        public static final String PID = "pid";
        public static final String REGNO = "registerno";
        public static final String DATEREG = "dateregister";
        public static final String DATEEXP = "dateexpire";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Personunable._ID, " pcucodeperson || pid  AS "
                    + Personunable._ID);
            PROJECTION_MAP.put(Personunable.PCUCODEPERSON,
                    Personunable.PCUCODEPERSON);
            PROJECTION_MAP.put(Personunable.PID, Personunable.PID);
            PROJECTION_MAP.put(Personunable.REGNO, Personunable.REGNO);
            PROJECTION_MAP.put(Personunable.DATEREG, Personunable.DATEREG);
            PROJECTION_MAP.put(Personunable.DATEEXP, Personunable.DATEEXP);
        }
    }

    public static class NCDPerson implements BaseColumns {
        public static final String TABLENAME = "ncd_person";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/ncdperson");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ncdperson";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ncdperson";
        public static final String DEFAULT_SORTING = "pid ASC";

        public static final String PCUCODE = "pcucode";
        public static final String PID = "pid";
        public static final String CID = "cid";
        public static final String HD = "hn";
        public static final String PRENAME = "prename";
        public static final String NAME = "name";
        public static final String LNAME = "lname";
        public static final String BIRTH = "birth";
        public static final String SEX = "sex";
        public static final String HEIGHT = "height";
        public static final String WEIGHT = "weight";
        public static final String WAIST = "waist";
        public static final String HOUSE = "house";
        public static final String VILLAGE = "village";
        public static final String TAMBOL = "tambol";
        public static final String AMPUR = "ampur";
        public static final String CHANGWAT = "changwat";
        public static final String DATEUPDATE = "d_update";
        public static final String USERNAME = "user_update";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(NCDPerson._ID, " pid || pid  AS "
                    + NCDPerson._ID);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(CID, CID);
            PROJECTION_MAP.put(HD, HD);
            PROJECTION_MAP.put(PRENAME, PRENAME);
            PROJECTION_MAP.put(NAME, NAME);
            PROJECTION_MAP.put(LNAME, LNAME);
            PROJECTION_MAP.put(BIRTH, BIRTH);
            PROJECTION_MAP.put(SEX, SEX);
            PROJECTION_MAP.put(HEIGHT, HEIGHT);
            PROJECTION_MAP.put(WEIGHT, WEIGHT);
            PROJECTION_MAP.put(WAIST, WAIST);
            PROJECTION_MAP.put(HOUSE, HOUSE);
            PROJECTION_MAP.put(VILLAGE, VILLAGE);
            PROJECTION_MAP.put(TAMBOL, TAMBOL);
            PROJECTION_MAP.put(AMPUR, AMPUR);
            PROJECTION_MAP.put(CHANGWAT, CHANGWAT);
            PROJECTION_MAP.put(DATEUPDATE, DATEUPDATE);
            PROJECTION_MAP.put(USERNAME, USERNAME);
        }
    }

    public static class NCDPersonNCD implements BaseColumns { // NCDPersonType
        public static final String TABLENAME = "ncd_person_ncd";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/ncdperson_type");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_type";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_type";
        public static final String DEFAULT_SORTING = "pid ASC";

        public static final String PCUCODE = "pcucode";
        public static final String PID = "pid";
        public static final String CHRONIC_FLAG = "chronic_flag";
        public static final String CHRONIC_START_DATE = "chronic_start_date";
        public static final String DATEUPDATE = "d_update";
        public static final String USERNAME = "user_update";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(NCDPersonNCD._ID, " pid || pid  AS "
                    + NCDPersonNCD._ID);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(CHRONIC_FLAG, CHRONIC_FLAG);
            PROJECTION_MAP.put(CHRONIC_START_DATE, CHRONIC_START_DATE);
            PROJECTION_MAP.put(DATEUPDATE, DATEUPDATE);
            PROJECTION_MAP.put(USERNAME, USERNAME);
        }
    }

    public static class NCDPersonNCDHist implements BaseColumns { // NCDPersonNCDHist
        public static final String TABLENAME = "ncd_person_ncd_hist";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/ncdperson_patienttype");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_patienttype";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_patienttype";
        public static final String DEFAULT_SORTING = "pid ASC";

        public static final String PCUCODE = "pcucode";
        public static final String PID = "pid";
        public static final String DM_FLAG = "dm_flag";
        public static final String HBP_FLAG = "hbp_flag";
        public static final String HAS_SYMPTOM = "has_symptom";
        public static final String DATEUPDATE = "d_update";
        public static final String USERNAME = "user_update";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(NCDPersonNCDHist._ID, " pid || pid  AS "
                    + NCDPersonNCDHist._ID);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(DM_FLAG, DM_FLAG);
            PROJECTION_MAP.put(HBP_FLAG, HBP_FLAG);
            PROJECTION_MAP.put(HAS_SYMPTOM, HAS_SYMPTOM);
            PROJECTION_MAP.put(DATEUPDATE, DATEUPDATE);
            PROJECTION_MAP.put(USERNAME, USERNAME);
        }
    }

    public static class NCDPersonNCDHistDetail implements BaseColumns { // NCDPersonNCDHistDetail
        public static final String TABLENAME = "ncd_person_ncd_hist_detail";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/ncdperson_symptom");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_symptom";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_symptom";
        public static final String DEFAULT_SORTING = "pid ASC";

        public static final String PCUCODE = "pcucode";
        public static final String PID = "pid";
        public static final String ORGAN = "org_code";
        public static final String REMARK = "remark";
        public static final String DATEUPDATE = "d_update";
        public static final String USERNAME = "user_update";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(NCDPersonNCDHist._ID, " pid || pid  AS "
                    + NCDPersonNCDHist._ID);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(ORGAN, ORGAN);
            PROJECTION_MAP.put(REMARK, REMARK);
            PROJECTION_MAP.put(DATEUPDATE, DATEUPDATE);
            PROJECTION_MAP.put(USERNAME, USERNAME);
        }
    }

    public static class NCDPersonNCDScreen implements BaseColumns {
        public static final String TABLENAME = "ncd_person_ncd_screen";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/ncdperson_screen");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_screen";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_screen";
        public static final String DEFAULT_SORTING = "pid ASC";

        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String PID = "pid";
        public static final String NO = "no";// �������� = = 1 ��觷ء�ѹ ���
        public static final String AGE = "age_year";
        public static final String SCREEN_DATE = "screen_date";
        public static final String HEIGHT = "height";
        public static final String WEIGHT = "weight";
        public static final String WAIST = "waist";
        public static final String HBP_H1 = "hbp_s1";
        public static final String HBP_L1 = "hbp_d1";
        public static final String HBP_H2 = "hbp_s2";
        public static final String HBP_L2 = "hbp_d2";
        public static final String SCREEN_Q1 = "screen_q1";
        public static final String SCREEN_Q2 = "screen_q2";
        public static final String SCREEN_Q3 = "screen_q3";
        public static final String SCREEN_Q4 = "screen_q4";
        public static final String SCREEN_Q5 = "screen_q5";
        public static final String SCREEN_Q6 = "screen_q6";
        public static final String BLACKARMPIT = "blackarmpit";
        public static final String BSL = "bsl";
        public static final String BMI = "bmi";
        public static final String RESULT_DM = "result_new_dm";
        public static final String RESULT_HBP = "result_new_hbp";
        public static final String RESULT_WAIST = "result_new_waist";
        public static final String RESULT_OBESITY = "result_new_obesity";
        public static final String SMOKE = "smoke";
        public static final String ALCOHOL = "alcohol";
        public static final String HTFAM = "htfamily";
        public static final String BSTEST = "bstest";
        public static final String SERVICEPLACE = "servplace";

        public static final String DATEUPDATE2 = "dateupdate"; // date time
        public static final String DATEUPDATE = "d_update"; // dateonly
        public static final String USERNAME = "user_update";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(NCDPersonNCDScreen._ID, " visitno || pid  AS "
                    + NCDPersonNCDScreen._ID);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(VISITNO, VISITNO);
            PROJECTION_MAP.put(NO, NO);
            PROJECTION_MAP.put(AGE, AGE);
            PROJECTION_MAP.put(SCREEN_DATE, SCREEN_DATE);
            PROJECTION_MAP.put(HEIGHT, HEIGHT);
            PROJECTION_MAP.put(WEIGHT, WEIGHT);
            PROJECTION_MAP.put(WAIST, WAIST);
            PROJECTION_MAP.put(HBP_H1, HBP_H1);
            PROJECTION_MAP.put(HBP_L1, HBP_L1);
            PROJECTION_MAP.put(HBP_H2, HBP_H2);
            PROJECTION_MAP.put(HBP_L2, HBP_L2);
            PROJECTION_MAP.put(SCREEN_Q1, SCREEN_Q1);
            PROJECTION_MAP.put(SCREEN_Q2, SCREEN_Q2);
            PROJECTION_MAP.put(SCREEN_Q3, SCREEN_Q3);
            PROJECTION_MAP.put(SCREEN_Q4, SCREEN_Q4);
            PROJECTION_MAP.put(SCREEN_Q5, SCREEN_Q5);
            PROJECTION_MAP.put(SCREEN_Q6, SCREEN_Q6);
            PROJECTION_MAP.put(BLACKARMPIT, BLACKARMPIT);
            PROJECTION_MAP.put(BSL, BSL);
            PROJECTION_MAP.put(BMI, BMI);
            PROJECTION_MAP.put(RESULT_DM, RESULT_DM);
            PROJECTION_MAP.put(RESULT_HBP, RESULT_HBP);
            PROJECTION_MAP.put(RESULT_WAIST, RESULT_WAIST);
            PROJECTION_MAP.put(RESULT_OBESITY, RESULT_OBESITY);
            PROJECTION_MAP.put(SMOKE, SMOKE);
            PROJECTION_MAP.put(ALCOHOL, ALCOHOL);
            PROJECTION_MAP.put(HTFAM, HTFAM);
            PROJECTION_MAP.put(BSTEST, BSTEST);
            PROJECTION_MAP.put(SERVICEPLACE, SERVICEPLACE);
            PROJECTION_MAP.put(DATEUPDATE, DATEUPDATE);
            PROJECTION_MAP.put(DATEUPDATE2, DATEUPDATE2);
            PROJECTION_MAP.put(USERNAME, USERNAME);
        }
    }

    public static class NCDScreenJOIN implements BaseColumns {
        public static final String TABLENAME = NCDPersonNCDScreen.TABLENAME
                + " LEFT JOIN " + NCDPersonNCD.TABLENAME
                + " ON ncd_person_ncd.pid = ncd_person_ncd_screen.pid "
                + " LEFT JOIN " + NCDPersonNCDHist.TABLENAME
                + " ON ncd_person_ncd_hist.pid = ncd_person_ncd_screen.pid ";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/ncdperson_screen_join");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_screen_join";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ncdperson_screen_join";
        public static final String DEFAULT_SORTING = "pid ASC";

        // SCREEN
        public static final String PCUCODE = "ncd_person_ncd_screen.pcucode";
        public static final String PID = "ncd_person_ncd_screen.pid";
        public static final String NO = "ncd_person_ncd_screen.no";
        public static final String VISITNO = "visitno";
        public static final String HEIGHT = "height";
        public static final String WEIGHT = "weight";
        public static final String WAIST = "waist";
        public static final String HBP_H1 = "hbp_s1";
        public static final String HBP_L1 = "hbp_d1";
        public static final String HBP_H2 = "hbp_s2";
        public static final String HBP_L2 = "hbp_d2";
        public static final String SCREEN_Q1 = "screen_q1";
        public static final String SCREEN_Q2 = "screen_q2";
        public static final String SCREEN_Q3 = "screen_q3";
        public static final String SCREEN_Q4 = "screen_q4";
        public static final String SCREEN_Q5 = "screen_q5";
        public static final String SCREEN_Q6 = "screen_q6";
        public static final String BLACKARMPIT = "blackarmpit";
        public static final String BSL = "bsl";
        public static final String BMI = "bmi";
        public static final String SMOKE = "smoke";
        public static final String ALCOHOL = "alcohol";
        public static final String HTFAM = "htfamily";
        public static final String BSTEST = "bstest";
        public static final String SERVICEPLACE = "servplace";

        // NCD
        public static final String CHRONIC_FLAG = "chronic_flag";
        // HIST
        public static final String DM_FLAG = "dm_flag";
        public static final String HBP_FLAG = "hbp_flag";
        public static final String HAS_SYMPTOM = "has_symptom";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(NCDPersonNCDScreen._ID,
                    " pcucode || pid || no  AS " + NCDPersonNCDScreen._ID);
            PROJECTION_MAP.put(PCUCODE, PCUCODE);
            PROJECTION_MAP.put(PID, PID);
            PROJECTION_MAP.put(NO, NO);
            PROJECTION_MAP.put(VISITNO, VISITNO);
            PROJECTION_MAP.put(HEIGHT, HEIGHT);
            PROJECTION_MAP.put(WEIGHT, WEIGHT);
            PROJECTION_MAP.put(WAIST, WAIST);
            PROJECTION_MAP.put(HBP_H1, HBP_H1);
            PROJECTION_MAP.put(HBP_L1, HBP_L1);
            PROJECTION_MAP.put(HBP_H2, HBP_H2);
            PROJECTION_MAP.put(HBP_L2, HBP_L2);
            PROJECTION_MAP.put(SCREEN_Q1, SCREEN_Q1);
            PROJECTION_MAP.put(SCREEN_Q2, SCREEN_Q2);
            PROJECTION_MAP.put(SCREEN_Q3, SCREEN_Q3);
            PROJECTION_MAP.put(SCREEN_Q4, SCREEN_Q4);
            PROJECTION_MAP.put(SCREEN_Q5, SCREEN_Q5);
            PROJECTION_MAP.put(SCREEN_Q6, SCREEN_Q6);
            PROJECTION_MAP.put(BLACKARMPIT, BLACKARMPIT);
            PROJECTION_MAP.put(BSL, BSL);
            PROJECTION_MAP.put(BMI, BMI);
            PROJECTION_MAP.put(SMOKE, SMOKE);
            PROJECTION_MAP.put(ALCOHOL, ALCOHOL);
            PROJECTION_MAP.put(HTFAM, HTFAM);
            PROJECTION_MAP.put(BSTEST, BSTEST);
            PROJECTION_MAP.put(SERVICEPLACE, SERVICEPLACE);
            PROJECTION_MAP.put(CHRONIC_FLAG, CHRONIC_FLAG);
            PROJECTION_MAP.put(DM_FLAG, DM_FLAG);
            PROJECTION_MAP.put(HBP_FLAG, HBP_FLAG);
            PROJECTION_MAP.put(HAS_SYMPTOM, HAS_SYMPTOM);

        }

    }


    public static class FFC506RADIUS implements BaseColumns {
        public static final String TABLENAME = "ffc_506radius";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/ffc_506radius");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.ffc_506radius";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.ffc_506radius";
        public static final String DEFAULT_SORTING = "visitno ASC";
        public static final String ID = "id";
        public static final String VISITNO = "visitno";
        public static final String RADIUS = "radius";
        public static final String COLORCODE = "colorcode";
        public static final String LEVEL = "level";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(FFC506RADIUS.ID, FFC506RADIUS.ID);
            PROJECTION_MAP.put(FFC506RADIUS.VISITNO, FFC506RADIUS.VISITNO);
            PROJECTION_MAP.put(FFC506RADIUS.RADIUS, FFC506RADIUS.RADIUS);
            PROJECTION_MAP.put(FFC506RADIUS.COLORCODE, FFC506RADIUS.COLORCODE);
            PROJECTION_MAP.put(FFC506RADIUS.LEVEL, FFC506RADIUS.LEVEL);
        }

    }

    public static class Visit506_Person implements BaseColumns {
        public static final String TABLENAME = Visit.TABLENAME
                + " INNER JOIN " + Person.TABLENAME + " ON visit.pid = person.pid"
                + " INNER JOIN " + House.TABLENAME + " ON house.hcode = person.hcode"
                + " INNER JOIN " + Village.TABLENAME + " ON house.villcode = village.villcode"
                + " INNER JOIN " + VisitDiag506address.TABLENAME + " ON visit.visitno = visitdiag506address.visitno"
                + " LEFT  JOIN " + "cdisease ON visitdiag506address.diagcode = cdisease.diseasecode"
                + " LEFT  JOIN " + "ffc_506radius ON visit.visitno = ffc_506radius.visitno";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit506_person");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit506_person";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit506_person";
        public static final String DEFAULT_SORTING = "visitno ASC";
        public static final String VISIT_NO = Visit.TABLENAME + "." + Visit.NO;
        public static final String PID = Person.TABLENAME + "." + Person.PID;
        public static final String FIRST_NAME = Person.TABLENAME + "." + Person.FIRST_NAME;
        public static final String LAST_NAME = Person.TABLENAME + "." + Person.LAST_NAME;
        public static final String HCODE = House.TABLENAME + "." + House.HCODE;
        public static final String HNO = House.TABLENAME + "." + House.HNO;
        public static final String XGIS = House.TABLENAME + "." + House.X_GIS;
        public static final String YGiS = House.TABLENAME + "." + House.Y_GIS;
        public static final String VILLCODE = Village.TABLENAME + "." + Village.VILLCODE;
        public static final String VILLNO = Village.TABLENAME + "." + Village.VILLNO;
        public static final String VILLNAME = Village.TABLENAME + "." + Village.VILLNAME;
        public static final String STATUS = VisitDiag506address.TABLENAME + "." + VisitDiag506address.STATUS;
        public static final String SICKDATESTART = VisitDiag506address.TABLENAME + "." + VisitDiag506address.SICKDATESTART;
        public static final String LATITUDE = VisitDiag506address.TABLENAME + "." + VisitDiag506address.LATITUDE;
        public static final String LONGITUDE = VisitDiag506address.TABLENAME + "." + VisitDiag506address.LONGITUDE;
        public static final String CDISEASENAME = "cdisease" + "." + "diseasenamethai";
        public static final String RADIUS = FFC506RADIUS.TABLENAME + "." + FFC506RADIUS.RADIUS;
        public static final String COLORCODE = FFC506RADIUS.TABLENAME + "." + FFC506RADIUS.COLORCODE;
        public static final String LEVEL = FFC506RADIUS.TABLENAME + "." + FFC506RADIUS.LEVEL;
        public static final String PCUCODEPERSON = Person.TABLENAME + "." + Person.PCUPERSONCODE;
        public static final String NAME = Person.TABLENAME + "." + Person.PCUPERSONCODE;
        public static final String _NAME = "name";
        public static final String CODE506 = "cdisease" + "." + "code506";
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put("_id", HCODE + " AS " + " _id");
            PROJECTION_MAP.put(Person.PCUPERSONCODE, PCUCODEPERSON + " AS " + Person.PCUPERSONCODE);
            PROJECTION_MAP.put(Visit.NO, VISIT_NO + " AS " + Visit.NO);
            PROJECTION_MAP.put(Person.PID, PID + " AS " + Person.PID);
            PROJECTION_MAP.put(Person.FIRST_NAME, FIRST_NAME + " AS " + Person.FIRST_NAME);
            PROJECTION_MAP.put("name", FIRST_NAME + " AS " + "name");
            PROJECTION_MAP.put(Person.LAST_NAME, LAST_NAME + " AS " + Person.LAST_NAME);
            PROJECTION_MAP.put(House.HCODE, HCODE + " AS " + House.HCODE);
            PROJECTION_MAP.put(House.HNO, HNO + " AS " + House.HNO);
            PROJECTION_MAP.put(House.X_GIS, XGIS + " AS " + House.X_GIS);
            PROJECTION_MAP.put(House.Y_GIS, YGiS + " AS " + House.Y_GIS);
            PROJECTION_MAP.put(Village.VILLCODE, VILLCODE + " AS " + Village.VILLCODE);
            PROJECTION_MAP.put(Village.VILLNO, VILLNO + " AS " + Village.VILLNO);
            PROJECTION_MAP.put(Village.VILLNAME, VILLNAME + " AS " + Village.VILLNAME);
            PROJECTION_MAP.put(VisitDiag506address.SICKDATESTART, SICKDATESTART + " AS " + VisitDiag506address.SICKDATESTART);
            PROJECTION_MAP.put(VisitDiag506address.STATUS, STATUS + " AS " + VisitDiag506address.STATUS);
            PROJECTION_MAP.put(VisitDiag506address.LATITUDE, LATITUDE + " AS " + VisitDiag506address.LATITUDE);
            PROJECTION_MAP.put(VisitDiag506address.LONGITUDE, LONGITUDE + " AS " + VisitDiag506address.LONGITUDE);
            PROJECTION_MAP.put("diseasenamethai", CDISEASENAME + " AS " + "diseasenamethai");
            PROJECTION_MAP.put(FFC506RADIUS.RADIUS, RADIUS + " AS " + FFC506RADIUS.RADIUS);
            PROJECTION_MAP.put(FFC506RADIUS.COLORCODE, COLORCODE + " AS " + FFC506RADIUS.COLORCODE);
            PROJECTION_MAP.put(FFC506RADIUS.LEVEL, LEVEL + " AS " + FFC506RADIUS.LEVEL);
            PROJECTION_MAP.put("code506", CODE506 + " AS " + "code506");
            PROJECTION_MAP.put(_NAME, "person.fname || ' ' || person.lname AS " + _NAME);

        }
    }

    public static class GET_ADDRESS implements BaseColumns {
        public static final String TABLENAME = "personaddresscontact "
                + " INNER JOIN "
                + "cprovince"
                + " ON personaddresscontact.provcode =cprovince.provcode"
                + " INNER JOIN "
                + "village"
                + " ON village.postcode = personaddresscontact.postcode";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/get_address");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.get_address";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.get_address";
        public static final String DEFAULT_SORTING = "provcode ASC";
        public static final String PROVCODE = "cprovince.provcode";
        public static final String DISTNAME = "cprovince.provname";
        public static final String LATITUDE = "village.latitude";
        public static final String LONGITUDE = "village.longitude";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put("provcode", PROVCODE + " AS provcode");
            PROJECTION_MAP.put("provname", DISTNAME + " AS provname");
            PROJECTION_MAP.put("latitude", LATITUDE + " AS latitude");
            PROJECTION_MAP.put("longitude", LONGITUDE + " AS longitude");
        }
    }

    public static class VISITLABSUGARBLOOD implements BaseColumns {
        public static final String TABLENAME = "visitlabsugarblood";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visitlabsugarblood");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visitlabsugarblood";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visitlabsugarblood";
        public static final String DEFAULT_SORTING = "visitno ASC";
        public static final String PCUCODE = "pcucode";
        public static final String VISITNO = "visitno";
        public static final String SUGARNUMDIGIT = "sugarnumdigit";
        public static final String FOODSUSPEND = "foodsuspend";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.PCUCODE, VISITLABSUGARBLOOD.PCUCODE);
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.VISITNO, VISITLABSUGARBLOOD.VISITNO);
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.SUGARNUMDIGIT, VISITLABSUGARBLOOD.SUGARNUMDIGIT);
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.FOODSUSPEND, VISITLABSUGARBLOOD.FOODSUSPEND);
        }
    }

    public static class VISIT_VISITLABSUGARBLOOD implements BaseColumns {
        public static final String TABLENAME = Visit.TABLENAME + " INNER JOIN "
                + VISITLABSUGARBLOOD.TABLENAME + " ON "
                + Visit.TABLENAME + "." + Visit.NO + " = " + VISITLABSUGARBLOOD.TABLENAME + "." + VISITLABSUGARBLOOD.VISITNO;
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/visit/visit_visitlabsugarblood");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.visit_visitlabsugarblood";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.visit_visitlabsugarblood";
        public static final String DEFAULT_SORTING = "visitno ASC";
        public static final String PCUCODE = VISITLABSUGARBLOOD.TABLENAME + "." + VISITLABSUGARBLOOD.PCUCODE;
        public static final String VISITNO = VISITLABSUGARBLOOD.TABLENAME + "." + VISITLABSUGARBLOOD.VISITNO;
        public static final String VISITDATE = Visit.TABLENAME + "." + Visit.DATE;
        public static final String PID = Visit.TABLENAME + "." + Visit.PID;
        public static final String SUGARNUMDIGIT = VISITLABSUGARBLOOD.TABLENAME + "." + VISITLABSUGARBLOOD.SUGARNUMDIGIT;
        public static final String FOODSUSPEND = VISITLABSUGARBLOOD.TABLENAME + "." + VISITLABSUGARBLOOD.FOODSUSPEND;
        public static final String WEIGHT = Visit.TABLENAME + "." + Visit.WEIGHT;
        public static final String PRESSURE = Visit.TABLENAME + "." + Visit.PRESSURE;
        public static final String PULSE = Visit.TABLENAME + "." + Visit.PULSE;

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.PCUCODE, PCUCODE + " AS " + VISITLABSUGARBLOOD.PCUCODE);
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.VISITNO, VISITNO + " AS " + VISITLABSUGARBLOOD.VISITNO);
            PROJECTION_MAP.put(Visit.PID, PID + " AS " + Visit.PID);
            PROJECTION_MAP.put(Visit.DATE, VISITDATE + " AS " + Visit.DATE);
            PROJECTION_MAP.put(Visit.WEIGHT, WEIGHT + " AS " + Visit.WEIGHT);
            PROJECTION_MAP.put(Visit.PRESSURE, PRESSURE + " AS " + Visit.PRESSURE);
            PROJECTION_MAP.put(Visit.PULSE, PULSE + " AS " + Visit.PULSE);
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.SUGARNUMDIGIT, SUGARNUMDIGIT + " AS " + VISITLABSUGARBLOOD.SUGARNUMDIGIT);
            PROJECTION_MAP.put(VISITLABSUGARBLOOD.FOODSUSPEND, FOODSUSPEND + " AS " + VISITLABSUGARBLOOD.FOODSUSPEND);
        }
    }

    public static class PersonRiskBlackBall implements BaseColumns {
        public static final String TABLENAME = Person.TABLENAME
                + " INNER JOIN " + House.TABLENAME + " ON person.hcode = house.hcode"
                + " INNER JOIN " + Village.TABLENAME + " ON village.villcode = house.villcode"
                + " INNER JOIN " + Chronic.TABLENAME + " ON person.pid= personchronic.pid"
                + " INNER JOIN " + "cdisease" + " ON cdisease.diseasecode=personchronic.chroniccode"
                + " INNER JOIN " + "cdiseasechronic" + " ON cdiseasechronic.groupcode=cdisease.codechronic";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/personriskblackball");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.personriskblackball";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.personriskblackball";

        public static final String DEFAULT_SORTING = "hcode ASC";
        public static final String FNAME = Person.TABLENAME + "." + Person.FIRST_NAME;
        public static final String LNAME = Person.TABLENAME + "." + Person.LAST_NAME;
        public static final String PCUDODE = Person.TABLENAME + "." + Person.PCUPERSONCODE;
        public static final String PID = Person.TABLENAME + "." + Person.PID;
        public static final String HNO = House.TABLENAME + "." + House.HNO;
        public static final String XGIS = House.TABLENAME + "." + House.X_GIS;
        public static final String YGIS = House.TABLENAME + "." + House.Y_GIS;
        public static final String BIRTH = Person.TABLENAME + "." + Person.BIRTH;
        public static final String HCODE = House.TABLENAME + "." + House.HCODE;
        public static final String VILLNAME = Village.TABLENAME + "." + Village.VILLNAME;
        public static final String DISCHARGETYPE = "person.dischargetype";
        public static final String TYPELIVE = "person.typelive";
        public static final String VILLCODE = House.TABLENAME + "." + House.VILLCODE;
        public static final String TYPEDISCHART = Chronic.TABLENAME + "." + Chronic.TYPE_DISCHART;
        public static final String GROUPCODE = "cdiseasechronic.groupcode";
        public static final String VILLNO = Village.TABLENAME + "." + Village.VILLNO;
        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(" distinct "," distinct ");
            PROJECTION_MAP.put(Person.FIRST_NAME, FNAME + " AS " + Person.FIRST_NAME);

            PROJECTION_MAP.put(Person.LAST_NAME, LNAME + " AS " + Person.LAST_NAME);
            PROJECTION_MAP.put(Person.PID, PID + " AS " + Person.PID);
            PROJECTION_MAP.put(Person.PCUPERSONCODE, PCUDODE + " AS " + Person.PCUPERSONCODE);
            PROJECTION_MAP.put(Person.BIRTH, BIRTH + " AS " + Person.BIRTH);
            PROJECTION_MAP.put(House.HCODE, HCODE + " AS " + House.HCODE);
            PROJECTION_MAP.put(House.HNO, HNO + " AS " + House.HNO);
            PROJECTION_MAP.put(House.X_GIS, XGIS + " AS " + House.X_GIS);
            PROJECTION_MAP.put(House.Y_GIS, YGIS + " AS " + House.Y_GIS);
            PROJECTION_MAP.put(House.VILLCODE, VILLCODE + " AS " + House.VILLCODE);
            PROJECTION_MAP.put(Village.VILLNO, VILLNO + " AS " + Village.VILLNO);
            PROJECTION_MAP.put(Village.VILLNAME, VILLNAME + " AS " + Village.VILLNAME);
            PROJECTION_MAP.put(Chronic.TYPE_DISCHART, TYPEDISCHART + " AS " + Chronic.TYPE_DISCHART);
            PROJECTION_MAP.put("person.dischargetype", DISCHARGETYPE + " AS " + "person.dischargetype");
            PROJECTION_MAP.put("person.typelive", TYPELIVE + " AS " + "person.typelive");
            PROJECTION_MAP.put("cdiseasechronic.groupcode", GROUPCODE + " AS " + "cdiseasechronic.groupcode");
        }
    }

    public static class PersonRiskRedGrayBall implements BaseColumns {
        public static final String TABLENAME = Person.TABLENAME
                + " INNER JOIN " + House.TABLENAME + " ON person.hcode = house.hcode"
                + " INNER JOIN " + Village.TABLENAME + " ON village.villcode = house.villcode"
                + " INNER JOIN " + Chronic.TABLENAME + " ON person.pid= personchronic.pid"
                + " INNER JOIN " + "cdisease" + " ON cdisease.diseasecode=personchronic.chroniccode"
                + " INNER JOIN " + "cdiseasechronic" + " ON cdiseasechronic.groupcode=cdisease.codechronic"
                + " INNER JOIN " + "ncd_person_ncd_screen" + " ON ncd_person_ncd_screen.pid = person.pid"
                + " INNER JOIN " + "visitlabchcyhembmsse" + " ON visitlabchcyhembmsse.pid = person.pid";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/personriskredgrayball");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.personriskredgrayball";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.personriskredgrayball";

        public static final String DEFAULT_SORTING = "hcode ASC";
        public static final String FNAME = Person.TABLENAME + "." + Person.FIRST_NAME;
        public static final String LNAME = Person.TABLENAME + "." + Person.LAST_NAME;
        public static final String PID = Person.TABLENAME + "." + Person.PID;
        public static final String HNO = House.TABLENAME + "." + House.HNO;
        public static final String XGIS = House.TABLENAME + "." + House.X_GIS;
        public static final String YGIS = House.TABLENAME + "." + House.Y_GIS;
        public static final String BIRTH = Person.TABLENAME + "." + Person.BIRTH;
        public static final String HCODE = House.TABLENAME + "." + House.HCODE;
        public static final String VILLNAME = Village.TABLENAME + "." + Village.VILLNAME;
        public static final String PCUDODE = Person.TABLENAME + "." + Person.PCUPERSONCODE;
        public static final String DISCHARGETYPE = "c person.dischargetype";
        public static final String TYPELIVE = "person.typelive";
        public static final String VILLCODE = House.TABLENAME + "." + House.VILLCODE;
        public static final String TYPEDISCHART = Chronic.TABLENAME + "." + Chronic.TYPE_DISCHART;
        public static final String GROUPCODE = "cdiseasechronic.groupcode";
        public static final String VILLNO = Village.TABLENAME + "." + Village.VILLNO;
        public static final String BSL = "ncd_person_ncd_screen.bsl";
        public static final String HBPS1 = "ncd_person_ncd_screen.hbp_s1";
        public static final String HBPD1 = "ncd_person_ncd_screen.hbp_d1";
        public static final String HBPS2 = "ncd_person_ncd_screen.hbp_s2";
        public static final String HBPD2 = "ncd_person_ncd_screen.hbp_d2";
        public static final String LABRESULTDIGIT = "visitlabchcyhembmsse.labresultdigit";
        public static final String LABCODE = "visitlabchcyhembmsse.labcode";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();

            PROJECTION_MAP.put(Person.FIRST_NAME, FNAME + " AS " + Person.FIRST_NAME);
            PROJECTION_MAP.put(Person.LAST_NAME, LNAME + " AS " + Person.LAST_NAME);
            PROJECTION_MAP.put(Person.PID, PID + " AS " + Person.PID);
            PROJECTION_MAP.put(Person.BIRTH, BIRTH + " AS " + Person.BIRTH);
            PROJECTION_MAP.put(House.HCODE, HCODE + " AS " + House.HCODE);
            PROJECTION_MAP.put(House.HNO, HNO + " AS " + House.HNO);
            PROJECTION_MAP.put(House.X_GIS, XGIS + " AS " + House.X_GIS);
            PROJECTION_MAP.put(House.Y_GIS, YGIS + " AS " + House.Y_GIS);
            PROJECTION_MAP.put(House.VILLCODE, VILLCODE + " AS " + House.VILLCODE);
            PROJECTION_MAP.put(Village.VILLNO, VILLNO + " AS " + Village.VILLNO);
            PROJECTION_MAP.put(Village.VILLNAME, VILLNAME + " AS " + Village.VILLNAME);
            PROJECTION_MAP.put(Person.PCUPERSONCODE, PCUDODE + " AS " + Person.PCUPERSONCODE);
            PROJECTION_MAP.put(Chronic.TYPE_DISCHART, TYPEDISCHART + " AS " + Chronic.TYPE_DISCHART);
            PROJECTION_MAP.put("person.dischargetype", DISCHARGETYPE + " AS " + "person.dischargetype");
            PROJECTION_MAP.put("person.typelive", TYPELIVE + " AS " + "person.typelive");
            PROJECTION_MAP.put("groupcode", GROUPCODE + " AS " + "	groupcode");
            PROJECTION_MAP.put("bsl", BSL + " AS " + "bsl");
            PROJECTION_MAP.put("hbp_s1", HBPS1 + " AS " + "hbp_s1");
            PROJECTION_MAP.put("hbp_d1", HBPD1 + " AS " + "hbp_d1");
            PROJECTION_MAP.put("hbp_s2", HBPS2 + " AS " + "hbp_s2");
            PROJECTION_MAP.put("hbp_d2", HBPD2 + " AS " + "hbp_d2");
            PROJECTION_MAP.put("labresultdigit", LABRESULTDIGIT + " AS " + "labresultdigit");
            PROJECTION_MAP.put("labcode", LABCODE + " AS " + "labcode");

        }
    }

    public static class PersonNCDBall implements BaseColumns {
        public static final String TABLENAME = Person.TABLENAME
                + " INNER JOIN " + House.TABLENAME + " ON person.hcode = house.hcode"
                + " INNER JOIN " + Village.TABLENAME + " ON village.villcode = house.villcode"
                + " LEFT JOIN " + "ncd_person_ncd_screen" + " ON ncd_person_ncd_screen.pid = person.pid"
                + " LEFT JOIN " + Chronic.TABLENAME + " ON person.pid= personchronic.pid"
                + " LEFT JOIN " + "cdisease" + " ON cdisease.diseasecode=personchronic.chroniccode"
                + " LEFT JOIN " + "cdiseasechronic" + " ON cdiseasechronic.groupcode=cdisease.codechronic"
                + " LEFT JOIN " + "visitlabchcyhembmsse" + " ON visitlabchcyhembmsse.pid = person.pid";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/personncdball");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.personncdball";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.personncdball";

        public static final String DEFAULT_SORTING = "hcode ASC";
        public static final String FNAME = Person.TABLENAME + "." + Person.FIRST_NAME;
        public static final String LNAME = Person.TABLENAME + "." + Person.LAST_NAME;
        public static final String PID = Person.TABLENAME + "." + Person.PID;
        public static final String HNO = House.TABLENAME + "." + House.HNO;
        public static final String XGIS = House.TABLENAME + "." + House.X_GIS;
        public static final String YGIS = House.TABLENAME + "." + House.Y_GIS;
        public static final String BIRTH = Person.TABLENAME + "." + Person.BIRTH;
        public static final String BSL = "ncd_person_ncd_screen.bsl";
        public static final String HBPS1 = "ncd_person_ncd_screen.hbp_s1";
        public static final String HBPD1 = "ncd_person_ncd_screen.hbp_d1";
        public static final String HBPS2 = "ncd_person_ncd_screen.hbp_s2";
        public static final String HBPD2 = "ncd_person_ncd_screen.hbp_d2";
        public static final String GROUPCODE = "cdiseasechronic.groupcode";
        public static final String PCUDODE = Person.TABLENAME + "." + Person.PCUPERSONCODE;
        public static final String HCODE = House.TABLENAME + "." + House.HCODE;
        public static final String VILLNAME = Village.TABLENAME + "." + Village.VILLNAME;
        public static final String VILLCODE = House.TABLENAME + "." + House.VILLCODE;
        public static final String VILLNO = Village.TABLENAME + "." + Village.VILLNO;
        public static final String LABCODE = "visitlabchcyhembmsse.labcode";
        public static final String LABRESULTDIGIT = "visitlabchcyhembmsse.labresultdigit";

        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Person.FIRST_NAME, FNAME + " AS " + Person.FIRST_NAME);
            PROJECTION_MAP.put(Person.LAST_NAME, LNAME + " AS " + Person.LAST_NAME);
            PROJECTION_MAP.put(Person.PID, PID + " AS " + Person.PID);
            PROJECTION_MAP.put(Person.PCUPERSONCODE, PCUDODE + " AS " + Person.PCUPERSONCODE);
            PROJECTION_MAP.put(Person.BIRTH, BIRTH + " AS " + Person.BIRTH);
            PROJECTION_MAP.put(House.HCODE, HCODE + " AS " + House.HCODE);
            PROJECTION_MAP.put(House.HNO, HNO + " AS " + House.HNO);
            PROJECTION_MAP.put("bsl", BSL + " AS " + "bsl");
            PROJECTION_MAP.put("hbp_s1", HBPS1 + " AS " + "hbp_s1");
            PROJECTION_MAP.put("hbp_d1", HBPD1 + " AS " + "hbp_d1");
            PROJECTION_MAP.put("hbp_s2", HBPS2 + " AS " + "hbp_s2");
            PROJECTION_MAP.put("hbp_d2", HBPD2 + " AS " + "hbp_d2");
            PROJECTION_MAP.put("groupcode", GROUPCODE + " AS " + "	groupcode");
            PROJECTION_MAP.put("labcode", LABCODE + " AS " + "labcode");
            PROJECTION_MAP.put(House.X_GIS, XGIS + " AS " + House.X_GIS);
            PROJECTION_MAP.put(House.Y_GIS, YGIS + " AS " + House.Y_GIS);
            PROJECTION_MAP.put(House.VILLCODE, VILLCODE + " AS " + House.VILLCODE);
            PROJECTION_MAP.put(Village.VILLNO, VILLNO + " AS " + Village.VILLNO);
            PROJECTION_MAP.put(Village.VILLNAME, VILLNAME + " AS " + Village.VILLNAME);
            PROJECTION_MAP.put("labresultdigit", LABRESULTDIGIT + " AS " + "labresultdigit");
        }
    }

    public static class PersonHouse implements BaseColumns {
        public static final String TABLENAME = Person.TABLENAME
                + " INNER JOIN " + House.TABLENAME + " ON person.hcode = house.hcode"
                + " INNER JOIN " + Village.TABLENAME + " ON house.villcode = village.villcode";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + PersonProvider.AUTHORITY + "/person/personhouse");
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.ffc.person.personhouse";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.ffc.person.personhouse";

        //	public static final String PCUCODE = VISITLABSUGARBLOOD.TABLENAME +"." +VISITLABSUGARBLOOD.PCUCODE;
        public static final String DEFAULT_SORTING = "hcode ASC";
        public static final String FNAME = Person.TABLENAME + "." + Person.FIRST_NAME;
        public static final String LNAME = Person.TABLENAME + "." + Person.LAST_NAME;
        public static final String XGIS = House.TABLENAME + "." + House.X_GIS;
        public static final String YGIS = House.TABLENAME + "." + House.Y_GIS;
        public static final String BIRTH = Person.TABLENAME + "." + Person.BIRTH;
        public static final String DISCHARGETYPE = "person.dischargetype";
        public static final String TYPELIVE = "person.typelive";
        public static final String VILLCODE = House.TABLENAME + "." + House.VILLCODE;
        public static final String TYPEDISCHART = Chronic.TABLENAME + "." + Chronic.TYPE_DISCHART;
        public static final String GROUPCODE = "cdiseasechronic.groupcode";
        public static final String VILLNAME = Village.TABLENAME + "." + Village.VILLNAME;
        public static final String VILLNO = Village.TABLENAME + "." + Village.VILLNO;
        public static final String HNO = House.TABLENAME + "." + House.HNO;
        public static final String HCODE = House.TABLENAME + "." + House.HCODE;


        protected static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(Person.FIRST_NAME, FNAME + " AS " + Person.FIRST_NAME);
            PROJECTION_MAP.put(Person.LAST_NAME, LNAME + " AS " + Person.LAST_NAME);
            PROJECTION_MAP.put(House.X_GIS, XGIS + " AS " + House.X_GIS);
            PROJECTION_MAP.put(House.Y_GIS, YGIS + " AS " + House.Y_GIS);
            PROJECTION_MAP.put(House.HNO, YGIS + " AS " + House.Y_GIS);
            PROJECTION_MAP.put(House.HCODE, HCODE + " AS " + House.HCODE);
            PROJECTION_MAP.put(Village.VILLNAME, VILLNAME + " AS " + Village.VILLNAME);
            PROJECTION_MAP.put(Village.VILLNO, VILLNO + " AS " + Village.VILLNO);
        }
    }

}
