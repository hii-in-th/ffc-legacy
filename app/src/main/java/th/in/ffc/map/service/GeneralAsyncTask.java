package th.in.ffc.map.service;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import th.in.ffc.R;

public class GeneralAsyncTask extends AsyncTask<Runnable, String, String> {

    private Activity act;
    private ProgressDialog mProgressDialog;
    private String msg;
    private Handler handler;
    private int success_number, fail_number;

    public static final String TAG = "GAT-Dialog";

    public GeneralAsyncTask(Activity act, String msg, Handler handler, int success_number, int fail_number) {
        this.act = act;
        if (msg == null)
            this.msg = act.getResources().getString(R.string.wait_msg);
        else
            this.msg = msg;
        this.handler = handler;
        this.success_number = success_number;
        this.fail_number = fail_number;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

//		if(act instanceof FragmentActivity){
//			FragmentActivity fa = (FragmentActivity)act;
//			Fragment f = fa.getSupportFragmentManager().findFragmentByTag(TAG);
//			if(f == null){
//				Bundle args = new Bundle();
//				args.putString("msg", msg);
//				
//				f = Fragment.instantiate(fa, ProgressFragmentDialog.class.getName());
//				f.setArguments(args);
//				FragmentTransaction ft = fa.getSupportFragmentManager().beginTransaction();
//				ft.add(f, TAG);
//				ft.commit();
//			}
//		}else{
        mProgressDialog = new ProgressDialog(act);
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(false);
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
//		}
    }

    @Override
    protected String doInBackground(Runnable... params) {
        Thread r = new Thread(params[0]);
        r.start();
        try {
            r.join();
        } catch (InterruptedException e) {
            Log.d("TAG!", "Thread error 1");
            e.printStackTrace();
            if (handler != null)
                handler.sendEmptyMessage(fail_number);

            return null;
        }
        if (handler != null)
            handler.sendEmptyMessage(success_number);
        Log.d("TAG!", "Thread finished 2");
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
//		if(act instanceof FragmentActivity){
//			FragmentActivity fa = (FragmentActivity)act;
//			Fragment f = fa.getSupportFragmentManager().findFragmentByTag(TAG);
//			if(f != null){
//				
//				FragmentTransaction ft = fa.getSupportFragmentManager().beginTransaction();
//				ft.remove(f);
//				ft.commit();
//			}
//		}else{
        mProgressDialog.dismiss();
//		}
    }

    public static class ProgressFragmentDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getArguments().getString("msg"));
            mProgressDialog.setCancelable(false);

            return mProgressDialog;
        }
    }

}
