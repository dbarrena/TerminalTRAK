package dbxprts.terminaltrak;

import android.app.Activity;
import android.app.ProgressDialog;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Diego on 05/09/2017.
 */

public class PlaceholderFragment extends Fragment implements SearchView.OnQueryTextListener {
    private String TAG = LoginActivity.class.getSimpleName();

    private SearchView mSearchView;
    private ListView mListView;
    HashMap<String, String> BLS = new HashMap<>();
    Multimap<String, String> BLS_Specs = ArrayListMultimap.create();
    Multimap<String, String> inventarioMap = ArrayListMultimap.create();
    Multimap<String, String> carrosMap = ArrayListMultimap.create();
    Multimap<String, String> planTrabajoMap = ArrayListMultimap.create();
    // URL to get contacts JSON
    private static String bls_url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/BL";
    private static String inventario_url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/gmap/carros";
    private static String carros_url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/carros";
    private static String plan_trabajo_url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/plan/";
    CustomAdapter adapter;
    private static final String ARG_TAB_NUMBER = "tab_number";
    int currentTab;

    public PlaceholderFragment() {

    }

    public static PlaceholderFragment newInstance(int tabNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
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
            new populateMap(bls_url, "bl").execute();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        } else if (currentTab == 1) {
            mListView.invalidateViews();
            BLS.clear();
            new populateMap(inventario_url, "inventario").execute();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else if (currentTab == 2) {
            mListView.invalidateViews();
            carrosMap.clear();
            new populateMap(carros_url, "carros").execute();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else if (currentTab == 3) {
            mListView.invalidateViews();
            planTrabajoMap.clear();
            String activeUser = ((LastRecordedLocation) getActivity().getApplication()).getActiveUser();
            new populateMap(plan_trabajo_url + activeUser, "plan").execute();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Busqueda");
        mSearchView.setFocusable(false);
        mSearchView.setIconified(false);
        mSearchView.clearFocus();
    }

    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            ((CustomAdapter) mListView.getAdapter()).getFilter().filter(null);
        } else {
            ((CustomAdapter) mListView.getAdapter()).getFilter().filter(newText);
        }
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(getActivity());
        String url;
        String selector;

        public populateMap(String url, String selector) {
            this.url = url;
            this.selector = selector;
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
                        //FRAGMENTO BL'S
                        if (this.selector.equals("bl")) {
                            String id = BL.getString("id_bl");
                            String name = BL.getString("bl");
                            String consignatario = BL.getString("consignatario");
                            String producto = BL.getString("producto");
                            //METHOD FOR STRIPPING STRING
                            int iend = name.indexOf("T");
                            if (iend != -1) {
                                String BLName = name.substring(0, iend);
                                BLS.put(id, BLName);
                            }

                            BLS_Specs.put(id, consignatario);
                            BLS_Specs.put(id, producto);

                        } else if (this.selector.equals("carros") || this.selector.equals("inventario")) {
                            String id_detalle_bl = BL.getString("id_detalle_bl");
                            String producto = BL.getString("producto");
                            String plataforma = BL.getString("plataforma");
                            //FRAGMENTO CARROS ACTIVOS
                            if (this.selector.equals("carros")) {
                                carrosMap.put(id_detalle_bl, plataforma);
                                carrosMap.put(id_detalle_bl, producto);
                                //FRAGMENTO INVENTARIO
                            } else if (this.selector.equals("inventario")) {
                                inventarioMap.put(id_detalle_bl, plataforma);
                                inventarioMap.put(id_detalle_bl, producto);
                            }
                            //FRAGMENTO PLAN
                        } else if (this.selector.equals("plan")) {
                            String id_plan_trabajo = BL.getString("id_plan_trabajo");
                            String cantidad = BL.getString("cantidad");
                            String id_operacion = BL.getString("id_operacion");
                            String operacion = BL.getString("operacion");
                            String id_cliente = BL.getString("id_cliente");
                            String nombre_cliente = BL.getString("nombre_cliente");
                            String descripcion_actividad = BL.getString("descripcion_actividad");

                            planTrabajoMap.put(id_plan_trabajo, cantidad);
                            planTrabajoMap.put(id_plan_trabajo, id_operacion);
                            planTrabajoMap.put(id_plan_trabajo, operacion);
                            planTrabajoMap.put(id_plan_trabajo, id_cliente);
                            planTrabajoMap.put(id_plan_trabajo, nombre_cliente);
                            planTrabajoMap.put(id_plan_trabajo, descripcion_actividad);

                        }

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            populateListView(selector);
            setupSearchView();
            progress.dismiss();
        }
    }

    public void setListItemBehavior(final List<HashMap<String, String>> listItems, String selector) {
        if (selector.equals("bl")) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    HashMap<String, String> resultsMap = listItems.get(position);
                    String id_BL = resultsMap.get("Second Line");
                    String name = resultsMap.get("First Line");
                    Intent i = new Intent(getActivity(), SecondFormActivity.class);
                    i.putExtra("id_BL", id_BL);
                    i.putExtra("name", name);
                    startActivity(i);
                }
            });
        } else if (selector.equals("inventario") || selector.equals("carros"))  {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    HashMap<String, String> resultsMap = listItems.get(position);
                    String name = resultsMap.get("First Line");
                    String id_BL = resultsMap.get("Second Line");
                    Intent i = new Intent(getActivity(), ThirdFormActivity.class);
                    i.putExtra("id_BL", id_BL);
                    i.putExtra("id_detalle_bl", id_BL);
                    i.putExtra("name", name);
                    startActivity(i);
                }
            });
        } else if (selector.equals("plan")){
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    HashMap<String, String> resultsMap = listItems.get(position);
                    String operacion = resultsMap.get("First Line");
                    String id_plan_trabajo = resultsMap.get("Second Line");
                    String shit = resultsMap.get("Third Line");
                    String id_operacion = resultsMap.get("Fourth Line");
                    String id_cliente = resultsMap.get("Fifth Line");
                    String descripcion_actividad = resultsMap.get("Sixth Line");
                    String cantidad = resultsMap.get("Seventh Line");
                    String nombre_cliente = resultsMap.get("Eighth Line");
                    Intent i = new Intent(getActivity(), PlanTrabajoActivity.class);
                    i.putExtra("operacion", operacion);
                    i.putExtra("id_plan_trabajo", id_plan_trabajo);
                    i.putExtra("id_operacion", id_operacion);
                    i.putExtra("id_cliente", id_cliente);
                    i.putExtra("descripcion_actividad", descripcion_actividad);
                    i.putExtra("cantidad", cantidad);
                    i.putExtra("nombre_cliente", nombre_cliente);

                    startActivityForResult(i,10001);
                }
            });
        }
    }

    public void populateListView(String selector) {
        List<HashMap<String, String>> listItems = new ArrayList<>();
        adapter = new CustomAdapter(getActivity(), listItems, R.layout.list_item,
                new String[]{"First Line", "Third Line"},
                new int[]{R.id.text1, R.id.text2});
        if (selector.equals("bl")) {
            Iterator it = BLS.entrySet().iterator();
            while (it.hasNext()) {
                HashMap<String, String> resultsMap = new HashMap<>();
                Map.Entry pair = (Map.Entry) it.next();
                String key = pair.getKey().toString();
                int first = pair.getValue().toString().indexOf("-");
                String year = pair.getValue().toString().substring(0, first);
                String monthAndDay = pair.getValue().toString().substring(5);
                String[] split = monthAndDay.split("-");
                String month = split[0];
                String day = split[1];
                resultsMap.put("First Line", day + "-" + month + "-" + year);
                resultsMap.put("Second Line", pair.getKey().toString());
                String consignatario = Iterables.get(BLS_Specs.get(key), 0);
                String producto = Iterables.get(BLS_Specs.get(key), 1);
                resultsMap.put("Third Line", producto + " " + consignatario + "\n ID: " + pair.getKey().toString());
                listItems.add(resultsMap);
                mListView.setTextFilterEnabled(true);
                mListView.setAdapter(adapter);
                setListItemBehavior(listItems, selector);
            }
        } else if (selector.equals("inventario")) {
            for (String key : inventarioMap.keySet()) {
                String plataforma = Iterables.get(inventarioMap.get(key), 0);
                String producto = Iterables.get(inventarioMap.get(key), 1);
                HashMap<String, String> resultsMap = new HashMap<>();
                resultsMap.put("First Line", plataforma);
                resultsMap.put("Third Line", producto + "\n ID: " + key);
                resultsMap.put("Second Line", key);
                listItems.add(resultsMap);
                mListView.setTextFilterEnabled(true);
                mListView.setAdapter(adapter);
                setListItemBehavior(listItems, selector);
            }
        } else if (selector.equals("carros")) {
            for (String key : carrosMap.keySet()) {
                String plataforma = Iterables.get(carrosMap.get(key), 0);
                String producto = Iterables.get(carrosMap.get(key), 1);
                HashMap<String, String> resultsMap = new HashMap<>();
                resultsMap.put("First Line", plataforma);
                resultsMap.put("Third Line", producto + "\n ID: " + key);
                resultsMap.put("Second Line", key);
                listItems.add(resultsMap);
                mListView.setTextFilterEnabled(true);
                mListView.setAdapter(adapter);
                setListItemBehavior(listItems, selector);
            }
        } else if (selector.equals("plan")) {
            for (String key : planTrabajoMap.keySet()) {
                String cantidad = Iterables.get(planTrabajoMap.get(key), 0);
                String id_operacion = Iterables.get(planTrabajoMap.get(key), 1);
                String operacion = Iterables.get(planTrabajoMap.get(key), 2);
                String id_cliente = Iterables.get(planTrabajoMap.get(key), 3);
                String nombre_cliente = Iterables.get(planTrabajoMap.get(key), 4);
                String descripcion_actividad = Iterables.get(planTrabajoMap.get(key), 5);
                HashMap<String, String> resultsMap = new HashMap<>();
                resultsMap.put("First Line", operacion);
                resultsMap.put("Second Line", key);
                resultsMap.put("Third Line", "Cantidad: " + cantidad + "\nCliente: "+ nombre_cliente +"\nID: " + key);
                resultsMap.put("Fourth Line", id_operacion);
                resultsMap.put("Fifth Line", id_cliente);
                resultsMap.put("Sixth Line", descripcion_actividad);
                resultsMap.put("Seventh Line", cantidad);
                resultsMap.put("Eighth Line", nombre_cliente);
                listItems.add(resultsMap);
                mListView.setTextFilterEnabled(true);
                mListView.setAdapter(adapter);
                setListItemBehavior(listItems, selector);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {Log.e(TAG, "fragment result called");
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001)) {
            PlaceholderFragment fragment = (PlaceholderFragment) getFragmentManager().getFragments().get(3);
            getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
        }
    }
}

