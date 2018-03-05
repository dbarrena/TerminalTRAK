package dbxprts.terminaltrak;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SecondFormActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, PlaceholderDetalleBLFragmento.OnFragmentInteractionListener {
    private String TAG = LoginActivity.class.getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    boolean segmentoIsInJSON = true;
    private SearchView mSearchView;
    private ListView mListView;
    TextView carrosPorFaseSnack;
    HashMap<String, String> BLS = new HashMap<>();
    HashMap<String, String> carrosPorFases = new HashMap<>();
    ArrayList<String> Segmentos = new ArrayList<>();
    ArrayList<String> Bloques = new ArrayList<>();
    ArrayList<String> Fases = new ArrayList<>();
    // URL to get contacts JSON
    private String url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/BL/detail/";
    private String carros_por_fase_url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/tabla_fases/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_form);
        setTitle("");
        setToolBarBehavior();
        //new SecondFormActivity.populateMap().execute();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_descarga);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("BL's");
        setCarrosPorFaseSnack();
        setFloatActionButtonSettings();

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /*
     * This method creates the tabs in the toolbar to change between
     * detalle BL fragments "Detalle BL, Descargas en proceso"
     *(Implementing Fragments isn't even necessary because the client
     * asked to have "Descargas en proceso" on Main Screen but I'll
     * leave it like that bc you never know)
     */
    public void setToolBarBehavior(){
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container_descarga);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle("BL's");
                        break;
                    case 1:
                        getSupportActionBar().setTitle("BL's Descargas Activas");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_descarga);
        tabLayout.setupWithViewPager(mViewPager);

        final int[] ICONS = new int[]{
                R.drawable.icon_3,
                R.drawable.icon_1
        };

        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] mFragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[2];
            mFragments[0] = PlaceholderDetalleBLFragmento.newInstance(0, getIntent().getExtras().getString("id_BL"), getValuesFromIntent("name"));
            mFragments[1] = PlaceholderDetalleBLFragmento.newInstance(1, getIntent().getExtras().getString("id_BL"), getValuesFromIntent("name"));
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return null;
                case 1:
                    return null;
            }
            return null;
        }
    }

    public String getValuesFromIntent(String requiredValue){
        Bundle extras = getIntent().getExtras();
        String value = null;
        if (extras != null) {
            switch (requiredValue) {
                case "id":
                    value = extras.getString("id_BL");
                    break;
                case "name":
                    value = extras.getString("name");
                    break;
            }
        }

        return value;
    }

    public void populatesCarrosPorFases(Dialog dialog){
        TableLayout ll = (TableLayout) dialog.findViewById(R.id.CarrosTable);
        ll.setColumnStretchable(2, true);

        Iterator it = carrosPorFases.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            ll.addView(addTableRow(pair.getKey().toString(), pair.getValue().toString()));
        }
    }

    public TableRow addTableRow(String faseValue, String carroValue){
        TableRow row= new TableRow(this);
        TableRow.LayoutParams textParam = new TableRow.LayoutParams();
        row.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView fase = new TextView(this);
        TextView carro = new TextView(this);

        textParam.weight = 1;
        textParam.width = 0;
        textParam.height = TableRow.LayoutParams.WRAP_CONTENT;
        textParam.bottomMargin = 20;

        fase.setGravity(Gravity.CENTER);
        fase.setTextSize(16);
        fase.setLayoutParams(textParam);
        carro.setGravity(Gravity.CENTER);
        carro.setTextSize(16);
        carro.setLayoutParams(textParam);


        fase.setText(faseValue);
        carro.setText(carroValue);

        row.addView(fase);
        row.addView(carro);

        return row;
    }

    public void setCarrosPorFaseSnack(){
        carrosPorFaseSnack = (TextView) findViewById(R.id.fasesCarros);
        carrosPorFaseSnack.setSelected(true);
        new getCarrosPorFase().execute();
    }

    private class getCarrosPorFase extends AsyncTask<Void, Void, Void> {
        String carrosPorFase = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(carros_por_fase_url+getValuesFromIntent("id"));
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject fases = items.getJSONObject(x);
                        //Getting items in Object Node
                        String fase = fases.getString("ultima_fase");
                        String carros = fases.getString("carros");
                        carrosPorFase += "  " + fase + ": " + carros + " Carros ";
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            carrosPorFaseSnack.setText(carrosPorFase);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Behavior for menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filtrar_segmento:
                // Do something
                CharSequence[] cs = Segmentos.toArray(new CharSequence[Segmentos.size()]);
                new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setTitle("Filtrar por segmento")
                        .setSingleChoiceItems(cs,0,null)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ListView lw = ((AlertDialog)dialog).getListView();
                                Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                                ((CustomAdapter)mListView.getAdapter()).getFilter().filter("segmento:"+checkedItem.toString());
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_menu_sort_by_size)
                        .show();
                return true;
            case R.id.filtrar_bloque:
                CharSequence[] cs2 = Bloques.toArray(new CharSequence[Bloques.size()]);
                new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setTitle("Filtrar por bloque")
                        .setSingleChoiceItems(cs2,0,null)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ListView lw = ((AlertDialog)dialog).getListView();
                                Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                                ((CustomAdapter)mListView.getAdapter()).getFilter().filter("bloque:"+checkedItem.toString());
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_menu_sort_by_size)
                        .show();
                return true;
            case R.id.filtrar_fase:
                CharSequence[] cs3 = Fases.toArray(new CharSequence[Fases.size()]);
                new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setTitle("Filtrar por fase")
                        .setSingleChoiceItems(cs3,0,null)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ListView lw = ((AlertDialog)dialog).getListView();
                                Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                                ((CustomAdapter)mListView.getAdapter()).getFilter().filter(checkedItem.toString());
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_menu_sort_by_size)
                        .show();
                return true;
            case R.id.borrar_filtros:
                ((CustomAdapter)mListView.getAdapter()).getFilter().filter(null);
                return true;
            case R.id.carros_por_fases:
                new SecondFormActivity.loadFasesTable().execute();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentSetSegmentos(ArrayList<String> Segmentos) {
        if(this.Segmentos.isEmpty()){
            this.Segmentos = Segmentos;
        }
    }

    @Override
    public void onFragmentSetBloques(ArrayList<String> Bloques) {
        if(this.Bloques.isEmpty()){
            this.Bloques = Bloques;
        }
    }

    @Override
    public void onFragmentSetFases(ArrayList<String> Fases) {
        if(this.Fases.isEmpty()){
            this.Fases = Fases;
        }
    }

    @Override
    public void onFragmetSetListView(ListView mListView) {
        this.mListView = mListView;
    }

    //Opciones dentro del float action button
    public void setFloatActionButtonSettings(){
        com.github.clans.fab.FloatingActionButton registrarParoFAB = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.parosFAB);

        registrarParoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplication(), RegistrarParoActivity.class);
                i.putExtra("id_bl", getValuesFromIntent("id"));
                startActivity(i);
            }
        });
    }

    /*Deprecated methods because all of this is made in the fragments, I'll leave them
     *here because if we go back to only one page without fragments this methods are going to be
     *in use again.
     */
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
            ((CustomAdapter)mListView.getAdapter()).getFilter().filter(null);
        } else {
            ((CustomAdapter)mListView.getAdapter()).getFilter().filter(newText);
            //mListView.setFilterText(newText.toString());
        }
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(SecondFormActivity.this);
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
            String jsonStr = sh.makeServiceCall(url+getValuesFromIntent("id"));
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String plataforma = BL.getString("plataforma");
                        String id = BL.getString("id_detalle_bl");
                        String segmento = BL.getString("segmento");
                        String bloque= BL.getString("bloque");
                        String ultima_fase = BL.getString("ultima_fase");
                        if(!Segmentos.contains(segmento)){
                            Segmentos.add(segmento);
                        }
                        if(!Bloques.contains(bloque)){
                            Bloques.add(bloque);
                        }
                        if(!Fases.contains(ultima_fase)){
                            Fases.add(ultima_fase);
                        }
                        String details = "Ultima Fase: "+ultima_fase+"\nSegmento:"+segmento;
                        BLS.put(plataforma,details);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                    segmentoIsInJSON = false;
                    try{
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray items = jsonObj.getJSONArray("items");
                        for(int x = 0; x < items.length(); x++){
                            // Getting JSON Object Node
                            JSONObject BL = items.getJSONObject(x);
                            //Getting items in Object Node
                            String plataforma = BL.getString("plataforma");
                            String id = BL.getString("id_detalle_bl");
                            String details = "ID: "+id;
                            BLS.put(plataforma,details);
                        }

                    } catch (final JSONException f) {
                        Log.e(TAG, "Json parsing error: " + f.getMessage());
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            populateListView();
            setupSearchView();
            progress.dismiss();
            setTitle("BL "+getValuesFromIntent("name"));
            //getSupportActionBar().setSubtitle("TEST");
        }
    }

    private class loadFasesTable extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(SecondFormActivity.this);
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
            String jsonStr = sh.makeServiceCall(carros_por_fase_url+getValuesFromIntent("id"));
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject fases = items.getJSONObject(x);
                        //Getting items in Object Node
                        String fase = fases.getString("ultima_fase");
                        String carros = fases.getString("carros");
                        carrosPorFases.put(fase,carros);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
            // custom dialog
            final Dialog dialog = new Dialog(SecondFormActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.table_dialog);
            dialog.show();
            populatesCarrosPorFases(dialog);
        }
    }

    public void populateListView() {
        List<HashMap<String, String>> listItems = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        Iterator it = BLS.entrySet().iterator();
        HashMap<String, String> resultsMap = new HashMap<>();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            resultsMap.put("First Line", pair.getKey().toString());
            resultsMap.put("Second Line", pair.getValue().toString());
        }
        listItems.add(resultsMap);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setTextFilterEnabled(true);
        mListView.setAdapter(adapter);
        setListItemBehavior(listItems);
    }

    public void setListItemBehavior(final List<HashMap<String, String>> listItems){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                HashMap<String, String> resultsMap = listItems.get(position);
                String id_BL = resultsMap.get("Second Line");
                String name = resultsMap.get("First Line");
                Intent i = new Intent(getApplicationContext(), ThirdFormActivity.class);
                i.putExtra("id_BL",id_BL);
                i.putExtra("BL_simple",getValuesFromIntent("id"));
                i.putExtra("BL_simple_name",getValuesFromIntent("name"));
                i.putExtra("name",name);
                startActivity(i);
            }
        });
    }
}
