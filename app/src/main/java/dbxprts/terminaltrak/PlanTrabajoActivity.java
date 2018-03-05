package dbxprts.terminaltrak;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

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
import java.util.Collections;
import java.util.HashMap;

public class PlanTrabajoActivity extends AppCompatActivity {
    ImageView horaInicioCalendar, horaFinCalendar;
    TextView horaInicio, horaFin, cantidad, descripcionActividad;
    HashMap<String, Integer> empleadosWithID = new HashMap<>();
    HashMap<String, Integer> clientesWithID = new HashMap<>();
    HashMap<String, Integer> operacionesWithID = new HashMap<>();
    Spinner turnoSpinner, programoSpinner, realizoSpinner, supervisorSpinner, operacionSpinner;
    TextView idPlan;
    SearchableSpinner clienteSpinner;
    SublimePickerFragment.Callback mFragmentCallbackHoraInicio;
    SublimePickerFragment.Callback mFragmentCallbackHoraFin;
    Button confirmarCambios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getValuesFromIntent("id_plan_trabajo") != null) {
            setContentView(R.layout.plan_trabajo_existing);
            new setGUIIfExisting().execute();
        } else {
            setContentView(R.layout.activity_plan_trabajo);
            new setGUI().execute();
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Plan Trabajo");
        }
    }

    public void setGUIValues() {
        turnoSpinner = (Spinner) findViewById(R.id.turno);
        programoSpinner = (Spinner) findViewById(R.id.id_programo);
        realizoSpinner = (Spinner) findViewById(R.id.id_realizo);
        supervisorSpinner = (Spinner) findViewById(R.id.id_supervisor);
        operacionSpinner = (Spinner) findViewById(R.id.id_operacion);
        clienteSpinner = (SearchableSpinner) findViewById(R.id.id_cliente);
        horaInicio = (TextView) findViewById(R.id.hora_inicio);
        horaFin = (TextView) findViewById(R.id.hora_fin);
        cantidad = (TextView) findViewById(R.id.cantidad);
        descripcionActividad = (TextView) findViewById(R.id.descripcion_actividad);
        horaInicioCalendar = (ImageView) findViewById(R.id.hora_inicio_calendar);
        horaFinCalendar = (ImageView) findViewById(R.id.hora_fin_calendar);
        confirmarCambios = (Button) findViewById(R.id.confirmar_cambios);

        //Populating "programo, realizo and supervisor spinner"
        ArrayList<String> arrayEmpleadosSpinner = new ArrayList<>();
        arrayEmpleadosSpinner.add(0, "-SELECCIONA-");
        arrayEmpleadosSpinner.addAll(empleadosWithID.keySet());
        Collections.sort(arrayEmpleadosSpinner, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> empleadosAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arrayEmpleadosSpinner);

        programoSpinner.setAdapter(empleadosAdapter);
        realizoSpinner.setAdapter(empleadosAdapter);
        supervisorSpinner.setAdapter(empleadosAdapter);

        //Populating "operacion spinner"
        ArrayList<String> arrayOperacionSpinner = new ArrayList<>();
        arrayOperacionSpinner.add(0, "-SELECCIONA-");
        arrayOperacionSpinner.addAll(operacionesWithID.keySet());
        Collections.sort(arrayOperacionSpinner, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> operacionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arrayOperacionSpinner);

        operacionSpinner.setAdapter(operacionAdapter);

        //Populating "cliente spinner"
        ArrayList<String> arrayClienteSpinner = new ArrayList<>();
        arrayClienteSpinner.add(0, "-SELECCIONA-");
        arrayClienteSpinner.addAll(clientesWithID.keySet());
        Collections.sort(arrayClienteSpinner, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> clienteAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arrayClienteSpinner);

        clienteSpinner.setAdapter(clienteAdapter);

        clienteSpinner.setTitle("Elige el cliente.");
        clienteSpinner.setPositiveButton("Cerrar");

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
                if (Integer.parseInt(day) < 10) {
                    day = "0" + day;
                }
                if (month < 10) {
                    finalMonth = "0" + month;
                } else {
                    finalMonth = String.valueOf(month);
                }
                if (Integer.parseInt(vMinute) < 10) {
                    vMinute = "0" + vMinute;
                }
                if (Integer.parseInt(vHour) < 10) {
                    vHour = "0" + vHour;
                }

                horaInicio.setText(day + "/" + finalMonth + "/" + year + " " + vHour + ":" + vMinute);
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
                if (Integer.parseInt(day) < 10) {
                    day = "0" + day;
                }
                if (month < 10) {
                    finalMonth = "0" + month;
                } else {
                    finalMonth = String.valueOf(month);
                }
                if (Integer.parseInt(vMinute) < 10) {
                    vMinute = "0" + vMinute;
                }
                if (Integer.parseInt(vHour) < 10) {
                    vHour = "0" + vHour;
                }

                horaFin.setText(day + "/" + finalMonth + "/" + year + " " + vHour + ":" + vMinute);
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
        confirmarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CallAPI().execute();
            }
        });

    }

    public void setGUIValuesIfExisting(PlanTrabajoExistente planTrabajo) {
        idPlan = (TextView) findViewById(R.id.id_plan);
        horaInicio = (TextView) findViewById(R.id.hora_inicio);
        horaFin = (TextView) findViewById(R.id.hora_fin);
        cantidad = (TextView) findViewById(R.id.cantidad);
        descripcionActividad = (TextView) findViewById(R.id.descripcion_actividad);
        horaInicioCalendar = (ImageView) findViewById(R.id.hora_inicio_calendar);
        horaFinCalendar = (ImageView) findViewById(R.id.hora_fin_calendar);
        confirmarCambios = (Button) findViewById(R.id.confirmar_cambios);
        horaInicioCalendar = (ImageView) findViewById(R.id.hora_inicio_calendar);
        horaFinCalendar = (ImageView) findViewById(R.id.hora_fin_calendar);

        idPlan.setText(getValuesFromIntent("id_plan_trabajo"));
        horaInicio.setText(planTrabajo.getHora_inicio());
        horaFin.setText(planTrabajo.getHora_fin());
        cantidad.setText(planTrabajo.getCantidad());
        descripcionActividad.setText(planTrabajo.getDescripcion_actividad());

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
                if (Integer.parseInt(day) < 10) {
                    day = "0" + day;
                }
                if (month < 10) {
                    finalMonth = "0" + month;
                } else {
                    finalMonth = String.valueOf(month);
                }
                if (Integer.parseInt(vMinute) < 10) {
                    vMinute = "0" + vMinute;
                }
                if (Integer.parseInt(vHour) < 10) {
                    vHour = "0" + vHour;
                }

                horaInicio.setText(day + "/" + finalMonth + "/" + year + " " + vHour + ":" + vMinute);
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
                if (Integer.parseInt(day) < 10) {
                    day = "0" + day;
                }
                if (month < 10) {
                    finalMonth = "0" + month;
                } else {
                    finalMonth = String.valueOf(month);
                }
                if (Integer.parseInt(vMinute) < 10) {
                    vMinute = "0" + vMinute;
                }
                if (Integer.parseInt(vHour) < 10) {
                    vHour = "0" + vHour;
                }

                horaFin.setText(day + "/" + finalMonth + "/" + year + " " + vHour + ":" + vMinute);
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

        confirmarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ModifyPlanTrabajo().execute();
            }
        });
    }

    public String buildURL() {
        String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.nuevo_plan_trabajo?";
        String vTurno = turnoSpinner.getSelectedItem().toString();
        int vProgramo = empleadosWithID.get(programoSpinner.getSelectedItem().toString());
        int vSupervisor = empleadosWithID.get(programoSpinner.getSelectedItem().toString());
        String vCantidad = cantidad.getText().toString();
        int vOperacion = operacionesWithID.get(operacionSpinner.getSelectedItem().toString());
        int vCliente = clientesWithID.get(clienteSpinner.getSelectedItem().toString());
        String vDescripcionActividad = descripcionActividad.getText().toString();
        /*Date vHoraInicio = null;
        Date vHoraFin = null;
        try {
            vHoraInicio = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(horaInicio.getText().toString());
            vHoraFin = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(horaFin.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        String finalURL = (baseurl
                + "p_turno=" + vTurno
                + "&p_id_programo=" + vProgramo
                + "&p_id_supervisor=" + vSupervisor
                + "&p_cantidad=" + vCantidad
                + "&p_id_operacion=" + vOperacion
                + "&p_id_cliente=" + vCliente
                + "&p_descripcion_actividad=" + vDescripcionActividad.replaceAll(" ", "%20")
                + "&p_hora_inicio=" + horaInicio.getText().toString().replaceAll(" ", ",")
                + "&p_hora_fin=" + horaFin.getText().toString().replaceAll(" ", ",")
                + "&p_usuario_android=" + ((LastRecordedLocation) this.getApplication()).getActiveUser());

        return finalURL;
    }

    public String buildURLExistingPlan() {
        String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_plan_trabajo?";
        String v_id_plan = idPlan.getText().toString();
        String vCantidad = this.cantidad.getText().toString();
        String vDescripcionActividad = descripcionActividad.getText().toString();


        String finalURL = (baseurl
                + "p_id_plan_trabajo=" + v_id_plan
                + "&p_cantidad=" + vCantidad
                + "&p_descripcion_actividad=" + vDescripcionActividad.replaceAll(" ", "%20")
                + "&p_hora_inicio=" + horaInicio.getText().toString().replaceAll(" ", ",")
                + "&p_hora_fin=" + horaFin.getText().toString().replaceAll(" ", ","));

        return finalURL;
    }

    public String getValuesFromIntent(String requiredValue) {
        Bundle extras = getIntent().getExtras();
        String value = null;
        if (extras != null) {
            switch (requiredValue) {
                case "id_plan_trabajo":
                    value = extras.getString("id_plan_trabajo");
                    break;
                case "operacion":
                    value = extras.getString("operacion");
                    break;
                case "id_operacion":
                    value = extras.getString("id_operacion");
                    break;
                case "nombre_cliente":
                    value = extras.getString("nombre_cliente");
                    break;
                case "id_cliente":
                    value = extras.getString("id_cliente");
                    break;
                case "cantidad":
                    value = extras.getString("cantidad");
                    break;
                case "descripcion_actividad":
                    value = extras.getString("descripcion_actividad");
                    break;
            }
        }

        return value;
    }

    //Elementos graficos en la pantalla de plan trabajo existente
    private class setGUIIfExisting extends AsyncTask<Void, Void, Void> {
        private String url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/plan/existing/";

        ProgressDialog progress = new ProgressDialog(PlanTrabajoActivity.this);

        PlanTrabajoExistente planTrabajo = new PlanTrabajoExistente();

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
            String TAG = "PLAN INFORMATION";
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStrEmpleados = sh.makeServiceCall(url+getValuesFromIntent("id_plan_trabajo"));

            Log.e(TAG, "Response plan trabajo: " + jsonStrEmpleados);

            if (jsonStrEmpleados != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrEmpleados);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject empleado = items.getJSONObject(x);
                        //Getting items in Object Node
                        String id_plan_trabajo = empleado.getString("id_plan_trabajo");
                        String cantidad = empleado.getString("cantidad");
                        String hora_inicio = empleado.getString("hora_inicio");
                        String hora_fin = empleado.getString("hora_fin");
                        String descripcion_actividad = empleado.getString("descripcion_actividad");

                        planTrabajo.setCantidad(cantidad);
                        planTrabajo.setHora_inicio(hora_inicio);
                        planTrabajo.setHora_fin(hora_fin);
                        planTrabajo.setDescripcion_actividad(descripcion_actividad);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setGUIValuesIfExisting(planTrabajo);
            progress.dismiss();
        }
    }

    //Elementos graficos en la pantalla de nuevo plan trabajo
    private class setGUI extends AsyncTask<Void, Void, Void> {
        private String urlEmpleados = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/plan/spinner/empleados";
        private String urlClientes = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/plan/spinner/clientes";
        private String urlOperaciones = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/plan/spinner/operaciones";

        ProgressDialog progress = new ProgressDialog(PlanTrabajoActivity.this);

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
            String jsonStrEmpleados = sh.makeServiceCall(urlEmpleados);
            String jsonStrClientes = sh.makeServiceCall(urlClientes);
            String jsonStrOperaciones = sh.makeServiceCall(urlOperaciones);

            Log.e(TAG, "Response from spinner Empleados url: " + jsonStrEmpleados);
            Log.e(TAG, "Response from spinner Clientes url: " + jsonStrClientes);
            Log.e(TAG, "Response from Spinner Operaciones url: " + jsonStrOperaciones);

            if (jsonStrEmpleados != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrEmpleados);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject empleado = items.getJSONObject(x);
                        //Getting items in Object Node
                        String nombre_empleado_json = empleado.getString("nombre_empleado");
                        String id_empleado_json = empleado.getString("id_empleado");
                        empleadosWithID.put(nombre_empleado_json, Integer.parseInt(id_empleado_json));
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            if (jsonStrClientes != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrClientes);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject cliente = items.getJSONObject(x);
                        //Getting items in Object Node
                        String cliente_json = cliente.getString("cliente");
                        String id_cliente_json = cliente.getString("id_cliente");
                        clientesWithID.put(cliente_json, Integer.parseInt(id_cliente_json));
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            if (jsonStrOperaciones != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStrOperaciones);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject operacion = items.getJSONObject(x);
                        //Getting items in Object Node
                        String operacion_json = operacion.getString("operacion");
                        String id_operacion_json = operacion.getString("id_operacion");
                        operacionesWithID.put(operacion_json, Integer.parseInt(id_operacion_json));
                    }
                    Log.e(TAG, "Operaciones: " + operacionesWithID);

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

    //Metodo POST para nuevo plan trabajo
    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(PlanTrabajoActivity.this);

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
            Log.e("HTTP POST ", "SUCCESS");
            Toast.makeText(getApplicationContext(), "Se registró con éxito el plan de trabajo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Metodo POST para modificar plan trabajo existente
    public class ModifyPlanTrabajo extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(PlanTrabajoActivity.this);

        public ModifyPlanTrabajo() {
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

            String urlString = buildURLExistingPlan(); // URL to call

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
            Log.e("HTTP POST ", "SUCCESS");
            Toast.makeText(getApplicationContext(), "Se registró con éxito el plan de trabajo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Objeto de plan trabajo
    public class PlanTrabajoExistente {
        String id_plan_trabajo;
        String cantidad;
        String hora_inicio;
        String hora_fin;
        String descripcion_actividad;

        public String getId_plan_trabajo() {
            return id_plan_trabajo;
        }

        public void setId_plan_trabajo(String id_plan_trabajo) {
            this.id_plan_trabajo = id_plan_trabajo;
        }

        public String getCantidad() {
            return cantidad;
        }

        public void setCantidad(String cantidad) {
            this.cantidad = cantidad;
        }

        public String getHora_inicio() {
            /*String fecha = hora_inicio.substring(0, hora_inicio.indexOf("T")).replaceAll("-", "/");
            String hora = hora_inicio.substring(hora_inicio.indexOf("T") + 1).replaceAll("Z", "");
            return fecha + " " + hora;*/
            return hora_inicio;
        }

        public void setHora_inicio(String hora_inicio) {
            this.hora_inicio = hora_inicio;
        }

        public String getHora_fin() {
            return hora_fin;
        }

        public void setHora_fin(String hora_fin) {
            this.hora_fin = hora_fin;
        }

        public String getDescripcion_actividad() {
            return descripcion_actividad;
        }

        public void setDescripcion_actividad(String descripcion_actividad) {
            this.descripcion_actividad = descripcion_actividad;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
