package dbxprts.terminaltrak;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class RegistrarParoActivity extends AppCompatActivity {
    TextView idBL, horaInicio, horaFin;
    Spinner idSegmento, idCausaParo;
    ImageView horaInicioCalendar, horaFinCalendar;
    Button registrarParo;
    SublimePickerFragment.Callback mFragmentCallbackHoraInicio;
    SublimePickerFragment.Callback mFragmentCallbackHoraFin;
    HashMap<String, Integer> causasParoWithID = new HashMap<>();
    ArrayList<String> segmentos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_paro);
        setToolbar();
        setGUIElements();
        setGUIBehavior();
        new setGUI().execute();
    }

    public String buildURL() {
        String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.nuevo_paro?";
        String vIdBL = getIntent().getExtras().getString("id_bl");
        String vSegmento = idSegmento.getSelectedItem().toString();
        int vCausaParo = causasParoWithID.get(idCausaParo.getSelectedItem().toString());

        String finalURL = (baseurl
                + "p_id_bl=" + vIdBL
                + "&p_id_segmento=" + vSegmento
                + "&p_id_causa_paro=" + vCausaParo
                + "&p_hora_inicio=" + horaInicio.getText().toString().replaceAll(" ", ",")
                + "&p_hora_fin=" + horaFin.getText().toString().replaceAll(" ", ",")
                + "&p_usuario_android=" + ((LastRecordedLocation) this.getApplication()).getActiveUser());

        Log.e("", finalURL);

        return finalURL;
    }

    public void setToolbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Registrar Paro");
        }
    }

    public void setGUIValues(){
        ArrayAdapter<String> segmentosAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, segmentos);

        idSegmento.setAdapter(segmentosAdapter);

        ArrayList<String> arrayCausasParoSpinner = new ArrayList<>();
        arrayCausasParoSpinner.add(0, "-SELECCIONA-");
        arrayCausasParoSpinner.addAll(causasParoWithID.keySet());
        ArrayAdapter<String> causasParoAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arrayCausasParoSpinner);

        idCausaParo.setAdapter(causasParoAdapter);
    }

    public void setGUIElements(){
        idBL = (TextView) findViewById(R.id.id_bl);
        horaInicio = (TextView) findViewById(R.id.hora_inicio);
        horaFin = (TextView) findViewById(R.id.hora_fin);
        idSegmento = (Spinner) findViewById(R.id.id_segmento);
        idCausaParo = (Spinner) findViewById(R.id.id_causa_paro);
        horaInicioCalendar = (ImageView) findViewById(R.id.hora_inicio_calendar);
        horaFinCalendar = (ImageView) findViewById(R.id.hora_fin_calendar);
        registrarParo = (Button) findViewById(R.id.registrar_paro);

        idBL.setText(getIntent().getExtras().getString("id_bl"));
    }

    public void setGUIBehavior(){
        mFragmentCallbackHoraInicio = new SublimePickerFragment.Callback() {
            @Override
            public void onCancelled() {

            }

            @Override
            public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                                int hourOfDay, int minute,
                                                SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                                String recurrenceRule) {

                String year = String.valueOf(selectedDate.getStartDate().get(Calendar.YEAR));
                int month = selectedDate.getStartDate().get(Calendar.MONTH) + 1;
                String day = String.valueOf(selectedDate.getStartDate().get(Calendar.DAY_OF_MONTH));
                String vMinute = String.valueOf(minute);
                String vHour = String.valueOf(hourOfDay);
                String finalMonth = "";
                if(Integer.parseInt(day)<10){
                    day = "0"+day;
                }
                if(month<10){
                    finalMonth = "0"+month;
                } else {
                    finalMonth = String.valueOf(month);
                }
                if(Integer.parseInt(vMinute)<10){
                    vMinute = "0"+vMinute;
                }
                if(Integer.parseInt(vHour)<10){
                    vHour = "0"+vHour;
                }

                horaInicio.setText(day+"/"+finalMonth+"/"+year+" "+vHour+":"+vMinute);
            }
        };
        horaInicioCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DialogFragment to host SublimePicker
                SublimePickerFragment pickerFrag = new SublimePickerFragment();
                pickerFrag.setCallback(mFragmentCallbackHoraInicio);

                // Valid options

                pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                pickerFrag.show(getSupportFragmentManager(), "SUBLIME_PICKER");
            }
        });

        mFragmentCallbackHoraFin = new SublimePickerFragment.Callback() {
            @Override
            public void onCancelled() {

            }

            @Override
            public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                                int hourOfDay, int minute,
                                                SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                                String recurrenceRule) {

                String year = String.valueOf(selectedDate.getStartDate().get(Calendar.YEAR));
                int month = selectedDate.getStartDate().get(Calendar.MONTH) + 1;
                String day = String.valueOf(selectedDate.getStartDate().get(Calendar.DAY_OF_MONTH));
                String vMinute = String.valueOf(minute);
                String vHour = String.valueOf(hourOfDay);
                String finalMonth = "";
                if(Integer.parseInt(day)<10){
                    day = "0"+day;
                }
                if(month<10){
                    finalMonth = "0"+month;
                } else {
                    finalMonth = String.valueOf(month);
                }
                if(Integer.parseInt(vMinute)<10){
                    vMinute = "0"+vMinute;
                }
                if(Integer.parseInt(vHour)<10){
                    vHour = "0"+vHour;
                }

                horaFin.setText(day+"/"+finalMonth+"/"+year+" "+vHour+":"+vMinute);
            }
        };
        horaFinCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DialogFragment to host SublimePicker
                SublimePickerFragment pickerFrag = new SublimePickerFragment();
                pickerFrag.setCallback(mFragmentCallbackHoraFin);

                // Valid options

                pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                pickerFrag.show(getSupportFragmentManager(), "SUBLIME_PICKER");
            }
        });

        registrarParo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CallAPI().execute();
            }
        });
    }

    private class setGUI extends AsyncTask<Void, Void, Void> {
        private String urlSegmento = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/paro/spinner/segmento/";
        private String urlCausaParo = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/paro/spinner/causa";

        ProgressDialog progress = new ProgressDialog(RegistrarParoActivity.this);

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
            String TAG = "SPINNERS INFORMATION";
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStrSegmento = sh.makeServiceCall(urlSegmento+getIntent().getExtras().getString("id_bl"));
            String jsonStrCausaParo= sh.makeServiceCall(urlCausaParo);

            Log.e(TAG, "Response from spinner Segmento url: " + jsonStrSegmento);
            Log.e(TAG, "Response from spinner Causa Paro url: " + jsonStrCausaParo);

            if (jsonStrSegmento != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrSegmento);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject empleado = items.getJSONObject(x);
                        //Getting items in Object Node
                        String segmento = empleado.getString("segmento");
                        segmentos.add(segmento);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            if (jsonStrCausaParo != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrCausaParo);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject cliente = items.getJSONObject(x);
                        //Getting items in Object Node
                        String causa_paro = cliente.getString("causa_paro");
                        String id_causa_paro = cliente.getString("id_causa_paro");
                        causasParoWithID.put(causa_paro, Integer.parseInt(id_causa_paro));
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setGUIValues();
            progress.dismiss();
        }
    }

    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(RegistrarParoActivity.this);

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
            Toast.makeText(getApplicationContext(), "Se registró con éxito el paro", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
