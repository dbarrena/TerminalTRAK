package dbxprts.terminaltrak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

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

public class FormActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private String TAG = LoginActivity.class.getSimpleName();

    private SearchView mSearchView;
    private ListView mListView;
    HashMap<String, String> BLS = new HashMap<>();
    Multimap<String, String> inventarioMap = ArrayListMultimap.create();
    // URL to get contacts JSON
    private static String bls_url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/BL";
    private static String inventario_url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/gmap/carros";
    CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        new populateMap(bls_url).execute();
        setTitle("BL' s");
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    private void setupSearchView() {
        mSearchView = (SearchView) findViewById(R.id.search_view);
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
        ProgressDialog progress = new ProgressDialog(FormActivity.this);
        String url;

        public populateMap(String url) {
            this.url = url;
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
                        //METHOD FOR STRIPPING STRING
                        if (this.url.equals(bls_url)) {
                            String id = BL.getString("id_bl");
                            String name = BL.getString("bl");
                            int iend = name.indexOf("T");
                            if (iend != -1) {
                                String BLName = name.substring(0, iend);
                                BLS.put(id, BLName);
                            }
                        } else {
                            String id_detalle_bl = BL.getString("id_detalle_bl");
                            String producto = BL.getString("producto");
                            String plataforma = BL.getString("plataforma");
                            inventarioMap.put(id_detalle_bl, plataforma);
                            inventarioMap.put(id_detalle_bl, producto);
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
            populateListView(this.url);
            setupSearchView();
            progress.dismiss();
        }
    }

    public void setListItemBehavior(final List<HashMap<String, String>> listItems, String url_selector) {
        if (url_selector.equals(bls_url)) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    HashMap<String, String> resultsMap = listItems.get(position);
                    String id_BL = resultsMap.get("Second Line");
                    String name = resultsMap.get("First Line");
                    Intent i = new Intent(getApplicationContext(), SecondFormActivity.class);
                    i.putExtra("id_BL", id_BL);
                    i.putExtra("name", name);
                    startActivity(i);
                }
            });
        } else {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    HashMap<String, String> resultsMap = listItems.get(position);
                    String name = resultsMap.get("First Line");
                    String id_BL = resultsMap.get("Third Line");
                    Intent i = new Intent(getApplicationContext(), ThirdFormActivity.class);
                    i.putExtra("id_BL",id_BL);
                    i.putExtra("name",name);
                    startActivity(i);
                }
            });
        }
    }

    public void populateListView(String url_selector) {
        List<HashMap<String, String>> listItems = new ArrayList<>();
        adapter = new CustomAdapter(this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});
        if (url_selector.equals(bls_url)) {
            Iterator it = BLS.entrySet().iterator();
            while (it.hasNext()) {
                HashMap<String, String> resultsMap = new HashMap<>();
                Map.Entry pair = (Map.Entry) it.next();
                resultsMap.put("First Line", pair.getValue().toString());
                resultsMap.put("Second Line", pair.getKey().toString());
                listItems.add(resultsMap);
                mListView = (ListView) findViewById(R.id.list_view);
                mListView.setTextFilterEnabled(true);
                mListView.setAdapter(adapter);
                setListItemBehavior(listItems, url_selector);
            }
        } else {
            // Iterating over entire Mutlimap
            for(String key : inventarioMap.keySet()) {
                String plataforma = Iterables.get(inventarioMap.get(key), 0);
                String producto = Iterables.get(inventarioMap.get(key), 1);
                HashMap<String, String> resultsMap = new HashMap<>();
                resultsMap.put("First Line", plataforma);
                resultsMap.put("Second Line", producto);
                resultsMap.put("Third Line", key);
                listItems.add(resultsMap);
                mListView = (ListView) findViewById(R.id.list_view);
                mListView.setTextFilterEnabled(true);
                mListView.setAdapter(adapter);
                setListItemBehavior(listItems, url_selector);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        MenuItem calidadItem = menu.findItem(R.id.filter_bl);
        calidadItem.setChecked(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_bl:
                if (!item.isChecked()) {
                    mListView.invalidateViews();
                    BLS.clear();
                    item.setChecked(true);
                    new populateMap(bls_url).execute();
                    setTitle("BL' s");
                    adapter.notifyDataSetChanged();
                }
                return true;
            case R.id.filter_inventario:
                if (!item.isChecked()) {
                    mListView.invalidateViews();
                    inventarioMap.clear();
                    item.setChecked(true);
                    new populateMap(inventario_url).execute();
                    setTitle("Inventario");
                    adapter.notifyDataSetChanged();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
