package dbxprts.terminaltrak;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Diego on 29/09/2017.
 */

public class PlaceholderDetalleBLFragmento extends Fragment implements SearchView.OnQueryTextListener {
    private String TAG = LoginActivity.class.getSimpleName();

    boolean segmentoIsInJSON = true;
    private SearchView mSearchView;
    private ListView mListView;
    HashMap<String, String> BLS = new HashMap<>();
    HashMap<String, String> BLS_ID = new HashMap<>();

    ArrayList<String> Segmentos = new ArrayList<>();
    ArrayList<String> Bloques = new ArrayList<>();
    ArrayList<String> Fases = new ArrayList<>();

    HashMap<String, String> BL_descarga_on_process_fecha = new HashMap<>();
    HashMap<String, String> BL_descarga_on_process_id_fase = new HashMap<>();

    // URL to get contacts JSON
    private String url_detalle_bl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/BL/detail/";
    private String url_bl_descarga = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/descarga_fin/";

    CustomAdapter adapter;
    private static final String ARG_TAB_NUMBER = "tab_number";
    private static final String ARG_BL_SIMPLE = "bl_simple";
    private static final String ARG_BL_SIMPLE_NAME = "bl_simple_number";
    int currentTab;
    OnFragmentInteractionListener dataPasser;

    public PlaceholderDetalleBLFragmento() {
    }

    public static PlaceholderDetalleBLFragmento newInstance(int tabNumber, String bl_simple, String bl_simple_name) {
        PlaceholderDetalleBLFragmento fragment = new PlaceholderDetalleBLFragmento();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_NUMBER, tabNumber);
        args.putString(ARG_BL_SIMPLE, bl_simple);
        args.putString(ARG_BL_SIMPLE_NAME, bl_simple_name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnFragmentInteractionListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentTab = getArguments().getInt(ARG_TAB_NUMBER);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mSearchView = (SearchView) rootView.findViewById(R.id.search_view);
        mListView = (ListView) rootView.findViewById(R.id.list_view);
        if (currentTab == 0) {
            mListView.invalidateViews();
            BLS.clear();
            new populateMap(url_detalle_bl).execute();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        } else if (currentTab == 1) {
            mListView.invalidateViews();
            BL_descarga_on_process_fecha.clear();
            new populateMap(url_bl_descarga).execute();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        return rootView;
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(getActivity());
        String url;
        String url_selector;

        public populateMap(String url) {
            this.url = url + getArguments().getString(ARG_BL_SIMPLE);
            url_selector = url;
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
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        if (this.url_selector.equals(url_detalle_bl)) {
                            String plataforma = BL.getString("plataforma");
                            String id = BL.getString("id_detalle_bl");
                            String segmento = BL.getString("segmento");
                            String bloque = BL.getString("bloque");
                            String ultima_fase = BL.getString("ultima_fase");
                            String posicion = BL.getString("posicion");
                            if (!Segmentos.contains(segmento)) {
                                Segmentos.add(segmento);
                            }
                            if (!Bloques.contains(bloque)) {
                                Bloques.add(bloque);
                            }
                            if (!Fases.contains(ultima_fase)) {
                                Fases.add(ultima_fase);
                            }
                            String details = "Ultima Fase: " + ultima_fase + "\nSegmento: " + segmento + "\nPosición: " + posicion;
                            BLS.put(plataforma, details);
                            BLS_ID.put(plataforma, id);
                        } else {
                            String fecha_inicio = BL.getString("fecha_inicio");
                            String id = BL.getString("id_detalle_bl");
                            String plataforma = BL.getString("plataforma");
                            String id_fase = BL.getString("id_fase");
                            BLS_ID.put(plataforma, id);
                            BL_descarga_on_process_fecha.put(plataforma, "Fecha inicio: " + fecha_inicio);
                            BL_descarga_on_process_id_fase.put(plataforma, id_fase);
                        }

                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                    if (this.url_selector.equals(url_detalle_bl)) {
                        segmentoIsInJSON = false;
                        try {
                            JSONObject jsonObj = new JSONObject(jsonStr);

                            // Getting JSON Array node
                            JSONArray items = jsonObj.getJSONArray("items");
                            for (int x = 0; x < items.length(); x++) {
                                // Getting JSON Object Node
                                JSONObject BL = items.getJSONObject(x);
                                //Getting items in Object Node
                                String plataforma = BL.getString("plataforma");
                                String id = BL.getString("id_detalle_bl");
                                String details = "ID: " + id;
                                BLS.put(plataforma, details);
                                BLS_ID.put(plataforma, id);
                            }

                        } catch (final JSONException f) {
                            Log.e(TAG, "Json parsing error: " + f.getMessage());
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            populateListView(this.url_selector);
            setupSearchView();
            progress.dismiss();
            getArguments().getString(ARG_BL_SIMPLE_NAME);
            //getSupportActionBar().setSubtitle("TEST");
            dataPasser.onFragmentSetBloques(Bloques);
            dataPasser.onFragmentSetFases(Fases);
            dataPasser.onFragmentSetSegmentos(Segmentos);
        }
    }

    public void populateListView(String url_selector) {
        List<HashMap<String, String>> listItems = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(getActivity(), listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        if (url_selector.equals(url_detalle_bl)) {
            Iterator it = BLS.entrySet().iterator();
            while (it.hasNext()) {
                HashMap<String, String> resultsMap = new HashMap<>();
                Map.Entry pair = (Map.Entry) it.next();
                resultsMap.put("First Line", pair.getKey().toString());
                resultsMap.put("Second Line", pair.getValue().toString() + "\n ID: " + BLS_ID.get(pair.getKey()));
                resultsMap.put("Third Line", BLS_ID.get(pair.getKey()));
                listItems.add(resultsMap);
            }
            //Ordenar lista alfabeticamente
            Collections.sort(listItems, new Comparator<HashMap<String, String>>() {
                @Override
                public int compare(HashMap<String, String> firstMap, HashMap<String, String> secondMap) {
                    return firstMap.get("First Line").compareTo(secondMap.get("First Line"));
                }
            });

            mListView.setTextFilterEnabled(true);
            mListView.setAdapter(adapter);
            setListItemBehavior(listItems, "no");
            dataPasser.onFragmetSetListView(mListView);
        } else {
            Iterator it = BL_descarga_on_process_fecha.entrySet().iterator();
            while (it.hasNext()) {
                HashMap<String, String> resultsMap = new HashMap<>();
                Map.Entry pair = (Map.Entry) it.next();
                resultsMap.put("First Line", pair.getKey().toString());
                resultsMap.put("Second Line", pair.getValue().toString() + "\n ID: " + BLS_ID.get(pair.getKey()));
                resultsMap.put("Third Line", BLS_ID.get(pair.getKey()));
                listItems.add(resultsMap);
                mListView.setTextFilterEnabled(true);
                mListView.setAdapter(adapter);
                setListItemBehavior(listItems, "si");
            }
        }
    }

    public void setListItemBehavior(final List<HashMap<String, String>> listItems, final String selector) {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                HashMap<String, String> resultsMap = listItems.get(position);
                String id_BL = getArguments().getString(ARG_BL_SIMPLE);
                final String name = resultsMap.get("First Line");
                final String id_detalle_bl = resultsMap.get("Third Line");

                if (selector.equals("no")) {
                    Intent i = new Intent(getActivity(), ThirdFormActivity.class);
                    i.putExtra("id_BL", id_BL);
                    i.putExtra("id_detalle_bl", resultsMap.get("Third Line"));
                    i.putExtra("BL_simple", getArguments().getString(ARG_BL_SIMPLE));
                    i.putExtra("BL_simple_name", getArguments().getString(ARG_BL_SIMPLE_NAME));
                    i.putExtra("name", name);
                    i.putExtra("descarga", selector);
                    startActivity(i);
                } else {
                    DateFormat formatFecha = new SimpleDateFormat("d_MM_yyyy");
                    final String fecha = formatFecha.format(Calendar.getInstance().getTime());
                    DateFormat formatHora = new SimpleDateFormat("HH:mm");
                    final String hora = formatHora.format(Calendar.getInstance().getTime());

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());
                    alertDialogBuilder
                            .setTitle("Deseas terminar esta fase de descarga?")
                            .setMessage("ID: " + id_detalle_bl + "\n \n Fecha: " + fecha + "\n \n Hora: " + hora)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new CallAPI().execute(BL_descarga_on_process_id_fase.get(name));
                                }
                            })
                            .setNegativeButton("Cancelar",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            }
        });
    }

    public String buildURL(String vIdFase) {
        String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_fase_descarga?";

        String finalURL = (baseurl
                + "p_id_fase=" + vIdFase);

        return finalURL;
    }

    //This one is to make a POST request to the server on the "Descargas en proceso" tab when a list item is clicked
    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(getContext());

        public CallAPI() {
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

            String urlString = buildURL(params[0]); // URL to call

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
            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Busqueda");
        mSearchView.setFocusable(false);
        mSearchView.setIconified(false);
        mSearchView.clearFocus();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            ((CustomAdapter) mListView.getAdapter()).getFilter().filter(null);
        } else {
            ((CustomAdapter) mListView.getAdapter()).getFilter().filter(newText);
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentSetSegmentos(ArrayList<String> Segmentos);

        public void onFragmentSetBloques(ArrayList<String> Bloques);

        public void onFragmentSetFases(ArrayList<String> Fases);

        public void onFragmetSetListView(ListView mListView);
    }
}
