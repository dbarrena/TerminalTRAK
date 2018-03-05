package dbxprts.terminaltrak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity {
    private String TAG = LoginActivity.class.getSimpleName();

    EditText userItem;
    EditText passwordItem;
    ImageView loginImage;
    TextView TEST, versionNumber;
    String user;
    String password;
    String id_area;
    Button button;
    View view;
    String versionName;
    Boolean debug = true;
    LastRecordedLocation lastRecordedLocation = new LastRecordedLocation();
    // URL to get contacts JSON
    private static String url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/login/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        view = this.getWindow().getDecorView();
        //view.setBackgroundColor(Color.parseColor("#7237A6"));
        userItem = (EditText) findViewById(R.id.userField);
        passwordItem = (EditText) findViewById(R.id.passwordField);
        passwordItem.setTransformationMethod(new PasswordTransformationMethod());
        button = (Button) findViewById(R.id.buttonData);
        TEST = (TextView) findViewById(R.id.TEST);
        loginImage = (ImageView) findViewById(R.id.loginImage);

        versionName = BuildConfig.VERSION_NAME;
        versionNumber = (TextView) findViewById(R.id.versionNumberLogin);
        versionNumber.setText("Versión: "+versionName);

        loginImage.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        if(debug){
                            user = "DURIEGA";
                            password = "PATINETA";
                            new LoginActivity.validateCredentials().execute();
                        }
                    }
                });

        button.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        user = userItem.getText().toString();
                        password = passwordItem.getText().toString();
                        new LoginActivity.validateCredentials().execute();
                    }
                });
        setEnterKeyBehavior();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class validateCredentials extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(LoginActivity.this);
        String passwordDatabase;
        Boolean correctUser = false;
        Boolean correctPassword = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setTitle("Cargando");
            progress.setMessage("Conectando con base de datos...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url+user.toUpperCase());
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    Log.e(TAG, "JSON JSON JSON: " + items.length());
                    if(items.length() != 0) {
                        correctUser = true;
                        // Getting JSON Object Node
                        JSONObject c = items.getJSONObject(0);
                        //Getting items in Object Node
                        id_area = c.getString("id_area");
                        passwordDatabase = c.getString("pass_android");
                        if (passwordDatabase.equals(password)) {
                            correctPassword = true;
                        }
                    }
                } catch (final JSONException e) {
                    toastOnUI();
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
            if(correctUser && correctPassword){
                setActiveUser();
                Toast.makeText(getApplicationContext(), "Bienvenido. Ver "+versionName, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                i.putExtra("id_area", id_area);
                startActivity(i);
            } else if (correctUser){
                Toast.makeText(getApplicationContext(), "La contraseña no es correcta, intenta de nuevo.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "El usuario no existe, intenta de nuevo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static final String md5(final String s) {
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

    public void setActiveUser(){
        ((LastRecordedLocation) this.getApplication()).setActiveUser(this.user);
        ((LastRecordedLocation) this.getApplication()).setIdArea(this.id_area);
    }

    public String getActiveUser(){
        return ((LastRecordedLocation) this.getApplication()).getActiveUser();
    }

    public void setEnterKeyBehavior(){
        passwordItem.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    user = userItem.getText().toString();
                    password = passwordItem.getText().toString();
                    new LoginActivity.validateCredentials().execute();
                    return true;
                }
                return false;
            }
        });
    }

    public void toastOnUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "El usuario no existe, intenta de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
