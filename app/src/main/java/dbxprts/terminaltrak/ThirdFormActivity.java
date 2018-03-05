package dbxprts.terminaltrak;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ThirdFormActivity extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    // LogCat tag
    private static final String TAG = ThirdFormActivity.class.getSimpleName();
    TextView latGUI, lngGUI, carro, fecha_inicio, confrontaRegistrada, via_label;
    EditText posicion;
    Spinner tipo_fase, via, liberado_rechazado;
    CheckBox estado_sellos;
    Button btnShowLocation, btnConfirmarCambios, btnConfronta;
    Chronometer descarga_cronometro;
    HashMap<String, Integer> tiposFasesWithID = new HashMap<>();
    List<String> tiposFases = new ArrayList<>();
    HashMap<String, Integer> viasWithID = new HashMap<>();
    String idFaseCreada;

    //VARIABLES FOR LOCATION
    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 10000;
    private static int DISPLACEMENT = 3; // 10 meters
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //STARTS STUFF FOR LOCATION
        if (!isGooglePlayServicesAvailable()) {
            //finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //ENDS STUFF FOR LOCATION
        setContentView(R.layout.activity_third_form);
        setGUIElements();
        setButtonBehavior();
        new setGUI(((LastRecordedLocation) this.getApplication()).getIdArea()).execute();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tiposFasesWithID.put("-SELECCIONA-", 0);
        tiposFases.add("-SELECCIONA-");
    }

    public void setGUIElements() {
        btnShowLocation = (Button) findViewById(R.id.get_location);
        btnConfirmarCambios = (Button) findViewById(R.id.confirmar_cambios);
        latGUI = (TextView) findViewById(R.id.latitude);
        lngGUI = (TextView) findViewById(R.id.longitude);
        confrontaRegistrada = (TextView) findViewById(R.id.confrontaRegistrada);
        carro = (TextView) findViewById(R.id.carro);
        fecha_inicio = (TextView) findViewById(R.id.fecha_inicio);
        tipo_fase = (Spinner) findViewById(R.id.turno);
        via = (Spinner) findViewById(R.id.via);
        estado_sellos = (CheckBox) findViewById(R.id.estado_sellos);
        btnConfronta = (Button) findViewById(R.id.confronta);
        posicion = (EditText) findViewById(R.id.posicion);
        liberado_rechazado = (Spinner) findViewById(R.id.liberado_rechazado);
        via_label = (TextView) findViewById(R.id.via_label);
        descarga_cronometro = (Chronometer) findViewById(R.id.descarga_cronometro);
    }

    public void setGUIValues(int spinnerPos) {
        carro.setText(getValuesFromIntent("name"));
        estado_sellos.setActivated(true);
        DateFormat df = new SimpleDateFormat("d_MM_yyyy,HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        fecha_inicio.setText(date);

        //Populating "tipos fases"
        ArrayList<String> arraySpinner = new ArrayList<>();
        arraySpinner.add(tiposFases.get(0));

        for (int x = 0; x < tiposFases.size(); x++) {
            arraySpinner.add(tiposFases.get(x));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arraySpinner);
        tipo_fase.setAdapter(adapter);

        tipo_fase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString(); //this is your selected item
                if (selectedItem.equals("DESCARGA")) {
                    btnConfirmarCambios.setText("Abrir Fase Descarga");
                }
                if (btnConfirmarCambios.getText().equals("Abrir Fase Descarga") && !selectedItem.equals("DESCARGA")) {
                    btnConfirmarCambios.setText("Registrar Fase");
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Populating "vias"
        ArrayList<String> arrayViasSpinner = new ArrayList<>();
        arrayViasSpinner.add(tiposFases.get(0));
        arrayViasSpinner.addAll(viasWithID.keySet());
        Collections.sort(arrayViasSpinner, String.CASE_INSENSITIVE_ORDER);

        ArrayAdapter<String> viasAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arrayViasSpinner);
        via.setAdapter(viasAdapter);

        //Setting radio button state
        estado_sellos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnConfronta.setVisibility(View.INVISIBLE);
                } else {
                    btnConfronta.setVisibility(View.VISIBLE);
                }
            }
        });

        //Making green tag invisible
        confrontaRegistrada.setVisibility(View.GONE);

        //Listener for Spinner tipo fase
        tipo_fase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString(); //this is your selected item

                if (selectedItem.equals("DESCARGA")) {
                    btnConfirmarCambios.setText("Abrir Fase Descarga");
                }

                if (selectedItem.equals("LIBERACIÓN CALIDAD")) {
                    btnConfirmarCambios.setText("Registrar Fase");
                    liberado_rechazado.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) via_label.getLayoutParams();
                    lp.addRule(RelativeLayout.BELOW, 0);
                    lp.addRule(RelativeLayout.BELOW, liberado_rechazado.getId());
                    via_label.setLayoutParams(lp);
                } else {
                    liberado_rechazado.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) via_label.getLayoutParams();
                    lp.addRule(RelativeLayout.BELOW, 0);
                    lp.addRule(RelativeLayout.BELOW, tipo_fase.getId());
                    via_label.setLayoutParams(lp);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public String spinnerLOV(String spinnerValue, String selector) {
        String result = "";
        switch (selector) {
            case "fase":
                result = tiposFasesWithID.get(spinnerValue).toString();
                break;

            case "via":
                result = viasWithID.get(spinnerValue).toString();
        }
        return result;
    }

    public String buildURL() {
        String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.post?";
        String vCarro = getValuesFromIntent("id_detalle_bl");
        String vTipo_Fase = spinnerLOV(tipo_fase.getSelectedItem().toString(), "fase");
        String vVia = spinnerLOV(via.getSelectedItem().toString(), "via");
        String vPosicion = posicion.getText().toString();
        String vFechaInicio = fecha_inicio.getText().toString();
        String vEstado_Sellos;
        boolean isChecked = ((CheckBox) findViewById(R.id.estado_sellos)).isChecked();
        if (isChecked) {
            vEstado_Sellos = "1";
        } else {
            vEstado_Sellos = "0";
        }
        String vLatitude = latGUI.getText().toString();
        String vLongitude = lngGUI.getText().toString();
        String vEstatus_Calidad = liberado_rechazado.getSelectedItem().toString();

        String finalURL = (baseurl
                + "p_id_detalle_bl=" + vCarro
                + "&p_id_tipo_fase=" + vTipo_Fase
                + "&p_fecha_inicio=" + vFechaInicio
                + "&p_estado_sellos=" + vEstado_Sellos
                + "&p_lati=" + vLatitude
                + "&p_long=" + vLongitude
                + "&p_id_via=" + vVia
                + "&p_posicion=" + vPosicion
                + "&p_usuario_android=" + ((LastRecordedLocation) this.getApplication()).getActiveUser()
                + "&p_estatus_calidad=" + vEstatus_Calidad);

        return finalURL;
    }

    public String getValuesFromIntent(String requiredValue) {
        Bundle extras = getIntent().getExtras();
        String value = null;
        if (extras != null) {
            switch (requiredValue) {
                case "id":
                    //METHOD FOR STRIPPING ID
                    String id = extras.getString("id_BL");
                    String stripped_id;
                    //If JSON has 'segmento'
                    int iend = id.indexOf("Segmento");
                    if (iend != -1) {
                        stripped_id = id.substring(0, iend);
                        value = stripped_id.substring(stripped_id.lastIndexOf(":") + 1);
                        value = value.replaceAll("\\s+", "");
                    } else {
                        value = id.substring(id.lastIndexOf(":") + 1);
                        value = value.replaceAll("\\s+", "");
                    }

                    break;
                case "name":
                    value = extras.getString("name");
                    break;
                case "id_simple":
                    value = extras.getString("BL_simple");
                    break;
                case "name_simple":
                    value = extras.getString("BL_simple_name");
                    break;
                case "id_detalle_bl":
                    value = extras.getString("id_detalle_bl");
                    break;
                case "descarga":
                    value = extras.getString("descarga");
            }
        }

        return value;
    }

    public void setButtonBehavior() {
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI();
            }
        });

        btnConfirmarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinnerLOV(tipo_fase.getSelectedItem().toString(), "fase").equals("0") && latGUI.getText().toString().equals("0.0")) {
                    Toast.makeText(getApplicationContext(), "Selecciona un tipo de fase y obtén localización.", Toast.LENGTH_SHORT).show();
                } else if (spinnerLOV(tipo_fase.getSelectedItem().toString(), "fase").equals("0")) {
                    Toast.makeText(getApplicationContext(), "Selecciona un tipo de fase.", Toast.LENGTH_SHORT).show();
                } else if (latGUI.getText().toString().equals("0.0")) {
                    Toast.makeText(getApplicationContext(), "Obtén la localización.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("created URL: ", buildURL());
                    new ThirdFormActivity.CallAPI().execute();
                }
            }
        });

        btnConfronta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ConfrontaActivity.class);
                i.putExtra("fecha", fecha_inicio.getText());
                startActivity(i);
            }
        });
    }

    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(ThirdFormActivity.this);

        public CallAPI() {
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setMessage("Registrando Fase...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }


        @Override
        protected String doInBackground(String... params) {

            String urlString = buildURL(); // URL to call

            String resultToDisplay = "";

            InputStream in = null;
            try {

                URL url = new URL(urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                in = new BufferedInputStream(urlConnection.getInputStream());


            } catch (Exception e) {

                System.out.println(e.getMessage());

                return e.getMessage();

            }

            try {
                resultToDisplay = IOUtils.toString(in, "UTF-8");
                //to [convert][1] byte stream to a string
                Log.d("HTTP result ", resultToDisplay);
                if (idFaseCreada == null) {
                    idFaseCreada = resultToDisplay.substring(resultToDisplay.indexOf(":") + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultToDisplay;
        }


        @Override
        protected void onPostExecute(String result) {
            //Update the UI
            progress.dismiss();
            Log.d("HTTP POST ", "SUCCESS");
            Toast.makeText(getApplicationContext(), "Se registró con éxito la fase", Toast.LENGTH_SHORT).show();
            if (tipo_fase.getSelectedItem().equals("DESCARGA")) {
                descarga_cronometro.setVisibility(View.VISIBLE);
                descarga_cronometro.start();
                btnConfirmarCambios.setBackgroundColor(Color.RED);
                btnConfirmarCambios.setText("Cerrar Descarga");
                btnConfirmarCambios.setTextColor(Color.WHITE);
                btnConfirmarCambios.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new ThirdFormActivity.CloseFaseDescarga().execute();
                    }
                });
            } else {
                finish();
            }
        }
    }

    public String buildURLDescarga(String id_fase) {
        String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_fase_descarga?";

        String finalURL = (baseurl
                + "p_id_fase=" + id_fase);

        return finalURL;
    }

    private class setGUI extends AsyncTask<Void, Void, Void> {
        private String url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/spinner/";
        private String urlFases = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/tipos_fases/";
        private String urlVias = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/vias";
        int ultima_fase;
        String id_area;
        ProgressDialog progress = new ProgressDialog(ThirdFormActivity.this);

        setGUI(String id_area) {
            this.id_area = id_area;
        }

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
            String jsonStr = sh.makeServiceCall(url + getValuesFromIntent("id"));
            String jsonStrFases = sh.makeServiceCall(urlFases + id_area);
            String jsonStrVias = sh.makeServiceCall(urlVias);
            Log.e(TAG, "Response from spinner order url: " + jsonStr);
            Log.e(TAG, "Response from fases url: " + jsonStrFases);
            Log.e(TAG, "Response from vias url: " + jsonStrVias);

            /*if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String ultima_fase_json = BL.getString("ultima_fase");
                        ultima_fase = Integer.parseInt(ultima_fase_json);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }*/

            if (jsonStrFases != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrFases);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject fase = items.getJSONObject(x);
                        //Getting items in Object Node
                        String tipo_fase_json = fase.getString("tipo_fase");
                        String id_tipo_fase_json = fase.getString("id_tipo_fase");
                        tiposFases.add(tipo_fase_json);
                        tiposFasesWithID.put(tipo_fase_json, Integer.parseInt(id_tipo_fase_json));
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            if (jsonStrVias != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrVias);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject via = items.getJSONObject(x);
                        //Getting items in Object Node
                        String via_json = via.getString("nombre_via");
                        String id_via_json = via.getString("id_via");
                        viasWithID.put(via_json, Integer.parseInt(id_via_json));
                    }
                    Log.e(TAG, "Vias: " + viasWithID);

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setGUIValues(ultima_fase);
            progress.dismiss();
        }
    }

    public class CloseFaseDescarga extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(ThirdFormActivity.this);

        public CloseFaseDescarga() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setMessage("Registrando Fase...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }


        @Override
        protected String doInBackground(String... params) {

            String urlString = buildURLDescarga(idFaseCreada); // URL to call

            String resultToDisplay = "";

            InputStream in = null;
            try {

                URL url = new URL(urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                in = new BufferedInputStream(urlConnection.getInputStream());


            } catch (Exception e) {

                System.out.println(e.getMessage());

                return e.getMessage();

            }

            try {
                resultToDisplay = IOUtils.toString(in, "UTF-8");
                //to [convert][1] byte stream to a string
                Log.d("HTTP result ", resultToDisplay);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultToDisplay;
        }


        @Override
        protected void onPostExecute(String result) {
            //Update the UI
            progress.dismiss();
            Log.d("HTTP POST ", result);
            Toast.makeText(ThirdFormActivity.this, "Se cerró la fase de Descarga", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean isDescargaActiva() {
        boolean flag = false;
        if (getValuesFromIntent("descarga") != null && getValuesFromIntent("descarga").equals("si")) {
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    //METHODS FOR LOCATION

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            latGUI.setText(lat);
            lngGUI.setText(lng);
            if (lastRecordedLocationIsSame(lat, lng)) {
                Toast.makeText(getApplicationContext(), "Esta localización es idéntica a la ultima localización capturada!", Toast.LENGTH_LONG).show();
            } else {
                ((LastRecordedLocation) this.getApplication()).setLastRecordedLocation(lat, lng);
            }
        } else {
            Log.d(TAG, "location is null ...............");
            Toast.makeText(getApplicationContext(), "Espera unos segundos y vuelve a intentar.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean lastRecordedLocationIsSame(String lat, String lng) {
        boolean isSame = false;
        String latitude = ((LastRecordedLocation) this.getApplication()).getLastRecordedLatitude();
        String longitude = ((LastRecordedLocation) this.getApplication()).getLastRecordedLongitude();
        if (latitude != null && longitude != null) {
            if (latitude.equals(lat) && longitude.equals(lng)) {
                isSame = true;
            }
        }
        return isSame;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }
}
