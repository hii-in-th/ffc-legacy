package com.blayzupe.phototaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import android.content.Context;
import android.widget.Toast;

public class PhotoTaker {

    public static final String CODE_VERSION = "1.0";
    public static final String ACTION_CROP_IMAGE = "com.android.camera.action.CROP";
    public static final String TEMP_PREFIX = "tmp_";
    public static final int IMAGE_CAPTURE = 10000;
    public static final int CROP_IMAGE = 10001;
    public static final int PICK_FROM_FILE = 10002;
    public static final String TAG = "PhotoTaker";
    private static final int CAMERA_PERM_CODE = 101 ;

    public int outputX = 300;
    public int outputY = 360;
    public int aspectX = 10;
    public int aspectY = 12;
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private android.content.Context context;


    public PhotoTaker(Activity activity) {
        this(activity, "/sdcard/", "PhotoTaker.jpg");
    }

    // Base Constructor
    public PhotoTaker(Activity activity, String path, String name) {
        mActivity = activity;
        setOutput(path, name);

    }
//    public PhotoTaker(Activity activity, String path, String name,Context context) {
//        mActivity = activity;
//        setOutput(path, name);
//        this.context = context;
//    }

    public PhotoTaker(Activity activity, String path, String name,
                      OnCropFinishListener listener) {
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
        mTemp = name.indexOf("tmp_")>-1?name: TEMP_PREFIX.concat(name);
    }

    public void setOutput(String name) {
        mOutput = name;
        mTemp = name.indexOf("tmp_")>-1?name: TEMP_PREFIX.concat(name);
    }

    public void setOutputSize(int x, int y) {
        this.outputX = x;
        this.outputY = y;
    }

    public void setOutputSize(int x, int y, int aspectX, int aspectY) {
        this.setOutputSize(x, y);
        this.aspectX = aspectX;
        this.aspectY = aspectY;
    }


    public void setCropfinishListener(OnCropFinishListener listener) {
        mOnFinishListener = listener;
    }

    public void setNotFoundCropIntentListener(
            OnNotFoundCropIntentListener listener) {
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
                mNotFoundCropIntentListener.OnNotFoundCropIntent(
                        mDirectory.getAbsolutePath(), mCropUri);
            return false;
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case IMAGE_CAPTURE:
                    Log.e(TAG, "blayzupe IMAGE_CAPTURE");

                    final File tempFile = getFile(mDirectory, mTemp);
                    Log.d(TAG, "blayzupe tempfile=" + tempFile.getAbsolutePath());
                    resizeImage(tempFile,mTemp);

//                   File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

                    // Create MediaUriScanner to find your Content URI of File
//                    MediaUriFinder.create(mActivity, tempFile.getAbsolutePath(), mScanner);
                    break;
                case PICK_FROM_FILE:
                    Log.e(TAG, "blayzupe PICK_IMAGE");
                    Uri dataUri = data.getData();
                    try {
                        InputStream iStream = context.getContentResolver().openInputStream(dataUri);
                        byte[] inputData = getBytes(iStream);
                        final File tempFile2 = getFile(mDirectory, mTemp);
                        OutputStream out2 = new FileOutputStream(tempFile2);
                        out2.write(inputData);
                        out2.close();
                        resizeImage(tempFile2,mTemp);
                    }
                    catch (Exception e){
                        Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }

//                    val takedImage = BitmapFactory.decodeFile(photoFile.absolutePath);
//                    imageView.setImageBitmap(takedImage)
//                    if (dataUri != null) {
//                        if (dataUri.getScheme().trim().equalsIgnoreCase("content"))
//                            doCropImage(data.getData());
//
//                            // if Scheme URI is File then scan for content then Crop it!
//                        else if (dataUri.getScheme().trim()
//                                .equalsIgnoreCase("file")) {
//                            Log.d(TAG, "blayzupe search for Media Content of path="
//                                    + dataUri.getPath());
//                            MediaUriFinder.create(mActivity, dataUri.getPath(),
//                                    mScanner);
//                        }
//                    } else {
//                        Log.e(TAG, "blayzupe DATA IS NULL");
//                    }
                    break;
                case CROP_IMAGE: {

                    Log.e(TAG, "blayzupe CROP_IMAGE");
                    getFile(mDirectory, mTemp).delete();

                    Bundle extras = data.getExtras();
                    if (extras != null) {

                        // get data to Bitmap then write to file
                        Bitmap croppedImg = extras.getParcelable("data");
                        File output = getFile(mDirectory, mOutput);
                        boolean writed = writeBitmapToFile(croppedImg, output);

                        // when write Cropped image finish, run OnCrop finish
                        if (writed && mOnFinishListener != null)
                            mOnFinishListener.OnCropFinsh(output.getAbsolutePath(),
                                    mCropUri);
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
    private void resizeImage(File tempFile,String mTemp){
        Bitmap b= BitmapFactory.decodeFile(tempFile.getAbsolutePath());
        Bitmap out = Bitmap.createScaledBitmap(b, 250, 250, false);
//        Bitmap out = Bitmap.createScaledBitmap(b, 250, (int) ((250*b.getHeight())/b.getWidth()), false);
        String[] filename = mTemp.split("_");
        File file = new File(mDirectory, filename[1].indexOf(".jpg")>-1?filename[1]:filename[1]+".jpg");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception e) {}
    }
    private byte[] getBytes(InputStream inputStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        }
        catch (Exception ex) {

        }
        return buffer.toByteArray();
    }
    public boolean doCropImage(Uri uri) {
        try {
            // If filter out file Scheme URI.
            if (uri.getScheme().trim().equalsIgnoreCase("file")) {
                Log.e(TAG, "blayzupe doCropImage(Uri) "
                        + "Support only \'Content\' Scheme URI");
                return false;
            }

            // set CropUri for use in onActivityResult Method.
            mCropUri = uri;
            Log.w(TAG,
                    "blayzupe Start doCropImage(Uri uri) " + "uri="
                            + uri.toString());

            Intent intent = new Intent(PhotoTaker.ACTION_CROP_IMAGE);
            intent.setType("image/*");
            intent.setData(uri);

            // Check for Crop Intent that Support Content URI.
            List<ResolveInfo> list = mActivity.getPackageManager()
                    .queryIntentActivities(intent, 0);
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
                Log.e(TAG, "blayzupe doCropImage(Uri)"
                        + " Not Found Support Crop Activity");
                Log.e(TAG, "URI : " + uri.toString());
                // Don't forget to delete your temp file;
                getFile(mDirectory, mTemp).delete();
                return false;
            }
        } catch (ActivityNotFoundException anfe) {
            Log.e(TAG, "blayzupe doCropImage(Uri) "
                    + "Not Found Support Crop Activity");
            anfe.printStackTrace();
            return false;
        }
    }

    public boolean doImageCapture() {
        try {
            File temp = getFile(mDirectory, mTemp);

            // Take Image for Camera and write to tempfile
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
            intent.putExtra("return-data", true);
            // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(temp));
            Uri uriTmp = FileProvider.getUriForFile(mActivity.getApplicationContext(), mActivity.getApplicationContext().getApplicationContext().getPackageName() + ".provider", temp);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTmp);
            mActivity.startActivityForResult(intent, IMAGE_CAPTURE);
            return true;
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
            return false;
        }
    }
    private void askCameraPermission(){
        if(ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,new String[] {Manifest.permission.CAMERA},IMAGE_CAPTURE );
        }else {
            doImageCapture();
        }
    }
    public boolean doPickImage() {
        try {
            Log.d(TAG, "blayzupe doPickImage() START");

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("return-data", return_data);

            mActivity.startActivityForResult(intent, PICK_FROM_FILE);
            // Use external Crop Intent if found
//            if (findCropActivity()) {
//                Log.d(TAG, "blayzupe doPickImage() Found");
//                intent.putExtra("return-data", return_data);
//
//                mActivity.startActivityForResult(intent, PICK_FROM_FILE);
//
//                // Use Internal Crop method of GET_CONTENT intent
//                // This is more Risk method
//            } else {
//                Log.d(TAG, "blayzupe doPickImage() Not found crop activity");
//                intent.putExtra("crop", "true");
//                intent.putExtra("noFaceDetection", !faceDetection);
//                intent.putExtra("aspectX", aspectX);
//                intent.putExtra("aspectY", aspectY);
//                intent.putExtra("outputX", outputX);
//                intent.putExtra("outputY", outputY);
//                intent.putExtra("scale", scale);
//                intent.putExtra("return-data", return_data);
//
//                mActivity.startActivityForResult(intent, CROP_IMAGE);
//            }
            return true;
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
            return false;
        }
    }

    public void doShowDialog() {
        final CharSequence[] items = {"Take from Camera",
                "Select from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity,android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        // doImageCapture();
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
        List<ResolveInfo> list = mActivity.getPackageManager()
                .queryIntentActivities(intent, 0);
        return (list.size() > 0) ? true : false;
    }

    private File createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private File getFile(File dir, String name) {
        File output = new File(dir, name);
        if (!output.exists()) {
            try {
                output.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return output;
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
