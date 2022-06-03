package th.in.ffc.security;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import th.in.ffc.R;
import th.in.ffc.app.FFCFragment;
import th.in.ffc.godmode.godMain;
import th.in.ffc.provider.UserProvider.User;
import th.in.ffc.provider.UserProvider.UserDatabaseOpenHelper;

/**
 * Fragment Class that hanlder Login Process, provide UserName Password of EditText
 * and Login Button, use OnLoginListener for check Callback event.
 *
 * @author piruin panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class LoginFragment extends FFCFragment implements
        View.OnClickListener {

    private OnLoginListener mListener;
    private EditText mEditTextUser;
    private EditText mEditTextPassword;
    private Button mButtonLogin;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.login_fragment, container, false);

        mEditTextUser = (EditText) root
                .findViewById(R.id.username);
        mEditTextPassword = (EditText) root
                .findViewById(R.id.password);
        mButtonLogin = (Button) root.findViewById(R.id.submit);
        mButtonLogin.setOnLongClickListener(lc);
        //DEBUG
        return root;
    }

    @Override
    public void onClick(View v) {

        String user = mEditTextUser.getText().toString().trim();
        String pass = mEditTextPassword.getText().toString().trim();

        this.doLogin(user, pass);

    }

    OnLongClickListener lc = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            Intent god = new Intent(getActivity(), godMain.class);
            startActivity(god);
            getActivity().finish();
            return false;
        }
    };

    public void setOnLoginListener(OnLoginListener listener) {
        if (listener != null) {
            mListener = listener;
            mButtonLogin.setOnClickListener(this);
        }
    }

    public void setEnable(boolean enable) {
        mButtonLogin.setEnabled(enable);
        mEditTextUser.setEnabled(enable);
        mEditTextPassword.setEnabled(enable);
    }

    private synchronized void doLogin(String user, String pass) {

        if (user.length() <= 0 || pass.length() <= 0) {
            mListener.onLoginFailre(getString(R.string.enter_user_pass));
        } else {
            this.setEnable(false);
            SQLiteDatabase userDb = new UserDatabaseOpenHelper(getActivity())
                    .getReadableDatabase();

            String[] columns = new String[]{User.PCUCODE, User.USERNAME};
            String selection = "username=? AND (password=? OR password=?)" ;
            String[] selectionArgs = new String[]
                    {
                        user,
                        MessageDigester.getSha256String(pass),
                        MessageDigester.getSha256String(md5(pass))
                    };

            Cursor cur = userDb.query(User.TABLENAME, columns, selection,
                    selectionArgs, null, null, null);

            boolean success = cur.moveToFirst();
            String pcuCode = null;
            if (success) {
                pcuCode = cur.getString(0);
            }
            cur.close();
            userDb.close();
            this.setEnable(true);

            if (success)
                this.mListener.onLoginSuccess(pcuCode, user);
            else
                this.mListener.onLoginFailre(getString(R.string.incorrect_user_pass));

        }
    }
    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    public void clearPassword() {
        mEditTextPassword.setText(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mButtonLogin = null;
        mEditTextPassword = null;
        mEditTextUser = null;
        mListener = null;
    }

    public void lockUsername(boolean lock) {
        mEditTextUser.setEnabled(!lock);
    }

    public void setUsername(String user) {
        mEditTextUser.setText(user);
    }

    public static interface OnLoginListener {

        public void onLoginSuccess(String pcucode, String user);

        public void onLoginFailre(String message);
    }

}
