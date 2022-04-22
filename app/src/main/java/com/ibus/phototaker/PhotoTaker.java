package com.ibus.phototaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import th.in.ffc.map.FGActivity;
import th.in.ffc.map.value.MARKER_TYPE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class PhotoTaker {

    public static final String CODE_VERSION = "1.0";
    public static final String ACTION_CROP_IMAGE = "com.android.camera.action.CROP";
    public static final String TEMP_PREFIX = "tmp_";
    public static final int IMAGE_CAPTURE = 0x00005012;
    public static final int CROP_IMAGE = 0x00003012;
    public static final int PICK_FROM_FILE = 0x00004012;
    public static final String TAG = "PhotoTaker";
    private static final int CAMERA_PERM_CODE = 101 ;

    public int outputX = 240;
    public int outputY = 240;
    public int aspectX = 1;
    public int aspectY = 1;
    public boolean return_data = true;
    public boolean scale = true;
    public boolean faceDetection = true;

    protected String mOutput;
    protected String mTemp;
    protected File mDirectory;
    private Activity mActivity;
    private OnCropFinishListener mOnFinishListener;
    private OnNotFoundCropIntentListener mNotFoundCropIntentListener;
    private Uri mCropUri;
    private File camera_temp_file = null;
    private File fileOut = null;

    public PhotoTaker(Activity activity) {
        //this(activity, "/sdcard/", "PhotoTaker.jpg");
        this(activity, "/sdcard/Android/data/th.in.ffc/temps/", "PhotoTaker.jpg");

    }

    // Base Constructor
    public PhotoTaker(Activity activity, String path, String name) {
        mActivity = activity;
        setOutput(path, name);

        if (!TextUtils.isEmpty(FGActivity.getTempDir())) {
            File file = new File(FGActivity.getTempDir());
            if (file != null)
                file.mkdirs();
        }

    }

    public PhotoTaker(Activity activity, String path, String name, OnCropFinishListener listener) {
        this(activity, path, name);
        mOnFinishListener = listener;
    }

    public PhotoTaker(Activity activity, OnCropFinishListener listener) {
        this(activity);
        mOnFinishListener = listener;
    }

    public void setOutput(String path, String name) {
        mDirectory = createDirectory(path);
        mOutput = name;
        mTemp = TEMP_PREFIX.concat(name);
    }

    public void setOutput(String name) {
        mOutput = name;
        mTemp = TEMP_PREFIX.concat(name);
    }

    public void setCropfinishListener(OnCropFinishListener listener) {
        mOnFinishListener = listener;
    }

    public void setNotFoundCropIntentListener(OnNotFoundCropIntentListener listener) {
        mNotFoundCropIntentListener = listener;
    }

    private MediaUriFinder.MediaScannedListener mScanner = new MediaUriFinder.MediaScannedListener() {

        @Override
        public boolean OnScanned(Uri uri) {
            /*
			 * Start Crop Activity with URI that we get once scanned if not
			 * found Support Crop Activity then run OnNotFoundCropIntent()
			 */
            if (!doCropImage(uri) && mNotFoundCropIntentListener != null)
                mNotFoundCropIntentListener.OnNotFoundCropIntent(mDirectory.getAbsolutePath(), mCropUri);
            return false;
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case IMAGE_CAPTURE:
                    Log.e(TAG, "blayzupe IMAGE_CAPTURE");
                    final File tempFile = createTempFile();
                    Log.d(TAG, "blayzupe tempfile=" + tempFile.getAbsolutePath());

                    // Create MediaUriScanner to find your Content URI of File
                    MediaUriFinder.create(mActivity, tempFile.getAbsolutePath(), mScanner);
                    break;
                case PICK_FROM_FILE:
                    Log.e(TAG, "blayzupe PICK_IMAGE");
                    Uri dataUri = data.getData();

                    if (dataUri != null) {
                        if (dataUri.getScheme().trim().equalsIgnoreCase("content"))
                            doCropImage(data.getData());

                            // if Scheme URI is File then scan for content then Crop it!
                        else if (dataUri.getScheme().trim().equalsIgnoreCase("file")) {
                            Log.d(TAG, "blayzupe search for Media Content of path=" + dataUri.getPath());
                            MediaUriFinder.create(mActivity, dataUri.getPath(), mScanner);
                        }
                    } else {
                        Log.e(TAG, "blayzupe DATA IS NULL");
                    }
                    break;
                case CROP_IMAGE: {

                    Log.e(TAG, "blayzupe CROP_IMAGE");
                    if (camera_temp_file != null && camera_temp_file.exists())
                        camera_temp_file.delete();

                    Bundle extras = data.getExtras();
                    if (extras != null) {

                        // get data to Bitmap then write to file
                        Bitmap croppedImg = extras.getParcelable("data");
                        File output = createTempFile();
                        boolean writed = writeBitmapToFile(croppedImg, output);

                        // when write Cropped image finish, run OnCrop finish
                        if (writed && mOnFinishListener != null)
                            mOnFinishListener.OnCropFinsh(output.getAbsolutePath(), mCropUri);
                    }
                }
                break;
                default:
                    Log.e(TAG, "blayzupe Result some thing");
                    break;
            }// end Switch case
        } else {
            Log.e(TAG, "blayzupe Result Not OK!!!");
        }
    }

    public boolean doCropImage(Uri uri) {
        try {
            // If filter out file Scheme URI.
            if (uri.getScheme().trim().equalsIgnoreCase("file")) {
                Log.e(TAG, "blayzupe doCropImage(Uri) " + "Support only \'Content\' Scheme URI");
                return false;
            }

            // set CropUri for use in onActivityResult Method.
            mCropUri = uri;
            Log.w(TAG, "blayzupe Start doCropImage(Uri uri) " + "uri=" + uri.toString());

            Intent intent = new Intent(PhotoTaker.ACTION_CROP_IMAGE);
            intent.setType("image/*");
            intent.setData(uri);

            // Check for Crop Intent that Support Content URI.
            List<ResolveInfo> list = mActivity.getPackageManager().queryIntentActivities(intent, 0);
            int size = list.size();
            if (size > 0) {

                // Set Parameter(Extra) for Crop Intent.
                Log.w(TAG, "blayzupe Found Crop Intent");
                intent.putExtra("noFaceDetection", !faceDetection);
                intent.putExtra("aspectX", aspectX);
                intent.putExtra("aspectY", aspectY);
                intent.putExtra("outputX", outputX);
                intent.putExtra("outputY", outputY);
                intent.putExtra("scale", scale);
                intent.putExtra("return-data", return_data);

                mActivity.startActivityForResult(intent, CROP_IMAGE);
                return true;
            } else {
                Log.e(TAG, "blayzupe doCropImage(Uri)" + " Not Found Support Crop Activity");
                Log.e(TAG, "URI : " + uri.toString());
                // Don't forget to delete your temp file;
                camera_temp_file.delete();
                return false;
            }
        } catch (ActivityNotFoundException anfe) {
            Log.e(TAG, "blayzupe doCropImage(Uri) " + "Not Found Support Crop Activity");
            anfe.printStackTrace();
            return false;
        }
    }

    public boolean doImageCapture() {
        try {
            createTempFile();

            // Take Image for Camera and write to tempfile
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
            intent.putExtra("return-data", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getCameraTempFileUri());

            mActivity.startActivityForResult(intent, IMAGE_CAPTURE);
            return true;
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
            return false;
        }
    }
    private void askCameraPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.CAMERA},CAMERA_PERM_CODE );
        }else {
//            openCamera();
            doImageCapture();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mActivity.startActivityForResult(intent,CAMERA_PERM_CODE);
    }

    //    private fun askCameraPermission() {
//        if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this@MainActivity,
//            arrayOf(Manifest.permission.CAMERA), CAMERA_PERM_CODE)
//        }
//        else {
//            openCamera()
//        }
//    }
//
//    private fun openCamera() {
//        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        Toast.makeText(this,"Open camera",Toast.LENGTH_SHORT).show();
//        startActivityForResult(intent,CAMERA_PERM_CODE)
//    }
    public boolean doPickImage() {
        try {
            Log.d(TAG, "blayzupe doPickImage() START");

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");

            // Use external Crop Intent if found
            if (findCropActivity()) {
                Log.d(TAG, "blayzupe doPickImage() Found");
                intent.putExtra("return-data", return_data);

                mActivity.startActivityForResult(intent, PICK_FROM_FILE);

                // Use Internal Crop method of GET_CONTENT intent
                // This is more Risk method
            } else {
                Log.d(TAG, "blayzupe doPickImage() Not found crop activity");
                intent.putExtra("crop", "true");
                intent.putExtra("noFaceDetection", !faceDetection);
                intent.putExtra("aspectX", aspectX);
                intent.putExtra("aspectY", aspectY);
                intent.putExtra("outputX", outputX);
                intent.putExtra("outputY", outputY);
                intent.putExtra("scale", scale);
                intent.putExtra("return-data", return_data);

                mActivity.startActivityForResult(intent, CROP_IMAGE);
            }
            return true;
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
            return false;
        }
    }

    public void doShowDialog() {
        final CharSequence[] items = {"Take from Camera", "Select from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity,android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        doImageCapture();
                        askCameraPermission();
                        break; // Take from Camera
                    case 1:
                        doPickImage();
                        break; // From gallery
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean findCropActivity() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = mActivity.getPackageManager().queryIntentActivities(intent, 0);
        return (list.size() > 0) ? true : false;
    }

    private File createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private File createTempFile() {
        // clearCameraTempFile();

        camera_temp_file = new File(FGActivity.getTempDir(), UUID.randomUUID().toString() + ".jpg");

        // Log.d("TAG!", camera_temp_file.getAbsolutePath());

        return camera_temp_file;
    }

    public Uri getCameraTempFileUri() {
        if (camera_temp_file == null)
            return null;
        return Uri.fromFile(camera_temp_file);
        //return FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", camera_temp_file);
    }

    public File getCameraTempFile() {
        return camera_temp_file;
    }

    private boolean writeBitmapToFile(Bitmap bitmap, File file) {
        try {
            FileOutputStream fops = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 100, fops);
            fops.flush();
            fops.close();
            fops = null;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // private boolean clearCameraTempFile() {
    // if (camera_temp_file != null)
    // {
    // boolean result = camera_temp_file.delete();
    // camera_temp_file = null;
    // return result;
    // }
    // return false;
    // }

    public boolean writeBitmapToFile(MARKER_TYPE type, String filename, Bitmap pic) {
        if (pic == null)
            return false;

        File myFilesDir = new File(FGActivity.getPictureDir() + type);
        myFilesDir.mkdirs();

        File fileOut = new File(myFilesDir, filename + ".jpg");

        // Log.d(TAG,
        // myFilesDir.getAbsolutePath()+" || "+myFilesDir.isDirectory()+" || "+myFilesDir.exists());
        return writeBitmapToFile(pic, fileOut);
    }

    public Activity getActivity() {
        return mActivity;
    }

    public void setActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void setOutput(File output) {
        fileOut = output;
    }

    public File getOutput() {
        return fileOut;
    }

    public static void clearCache() {

        File dir = new File(FGActivity.getTempDir());

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }

        PhotoTaker pt = FGActivity.fgsys.getPhotoTaker();
        if (pt != null)
            pt.setOutput((File) null);
    }

    @Override
    protected void finalize() throws Throwable {
        mActivity = null;
        mCropUri = null;
        mOnFinishListener = null;
        mNotFoundCropIntentListener = null;
        super.finalize();
    }

    public static interface OnCropFinishListener {
        public boolean OnCropFinsh(String path, Uri uri);
    }

    public static interface OnNotFoundCropIntentListener {
        public boolean OnNotFoundCropIntent(String path, Uri uri);
    }
}
