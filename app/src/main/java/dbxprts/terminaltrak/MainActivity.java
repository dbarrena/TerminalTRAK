package dbxprts.terminaltrak;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = LoginActivity.class.getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    HashMap<String, String> BLS_ID = new HashMap<>();
    HashMap<String, String> BL_descarga_on_process_fecha = new HashMap<>();
    HashMap<String, String> BL_descarga_on_process_id_fase = new HashMap<>();

    com.github.clans.fab.FloatingActionButton actualizarFAB;
    com.github.clans.fab.FloatingActionButton descargasActivasFAB;
    com.github.clans.fab.FloatingActionButton nuevoPlanTrabajoActivasFAB;

    String baseURL = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/ferro/descargas_activas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFloatActionButtonSettings();
        setToolBarBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BL's");
    }

    //Opciones dentro del float action button
    public void setFloatActionButtonSettings(){
        actualizarFAB = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.actualizarFAB);
        descargasActivasFAB = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.descargaFAB);
        nuevoPlanTrabajoActivasFAB = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.nuevoPlanFAB);

        actualizarFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setToolBarBehavior();
            }
        });

        descargasActivasFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new populateMap().execute();
            }
        });

        nuevoPlanTrabajoActivasFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplication(), PlanTrabajoActivity.class);
                startActivityForResult(i,10001);
            }
        });

    }

    //Aqui se crean las tabs para cambiar entre fragmentos
    public void setToolBarBehavior() {
        /* Para agregar una nueva tab tienes que:
         * cambiar setOffscreenPageLimit (105)
         * agregar case a onPageSelected (114)
         * agregar icono en ICONS[] (141)
         * agregar tabLayout (Recuerda cambiar los numeros en getTab y ICON, si no null pointer) (149)
         * agregar fragmento en SectionsPagerAdapter (Recuerda cambiar el tamaño de array y verifica los numeros) (183)
         */
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle("BL's");
                        nuevoPlanTrabajoActivasFAB.setVisibility(View.GONE);
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Inventario");
                        nuevoPlanTrabajoActivasFAB.setVisibility(View.GONE);
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Carros Activos");
                        nuevoPlanTrabajoActivasFAB.setVisibility(View.GONE);
                        break;
                    case 3:
                        getSupportActionBar().setTitle("Plan Trabajo");
                        nuevoPlanTrabajoActivasFAB.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        final int[] ICONS = new int[]{
                R.drawable.icon_3,
                R.drawable.icon_1,
                R.drawable.icon_4,
                R.drawable.plan_icon
        };

        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);
    }

    public String getPermissions(){
        return getIntent().getExtras().getString("id_area");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] mFragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[4];
            mFragments[0] = PlaceholderFragment.newInstance(0);
            mFragments[1] = PlaceholderFragment.newInstance(1);
            mFragments[2] = PlaceholderFragment.newInstance(2);
            mFragments[3] = PlaceholderFragment.newInstance(3);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return null;
                case 1:
                    return null;
                case 2:
                    return null;
                case 3:
                    return null;
            }
            return null;
        }
    }

    public void openDescargasActivasDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Descargas Activas");

        alertDialog.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        List<HashMap<String, String>> listItems = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        Iterator it = BL_descarga_on_process_fecha.entrySet().iterator();
        while (it.hasNext()) {
            HashMap<String, String> resultsMap = new HashMap<>();
            Map.Entry pair = (Map.Entry) it.next();
            resultsMap.put("First Line", pair.getKey().toString());
            resultsMap.put("Second Line", pair.getValue().toString() + "\n ID: " + BLS_ID.get(pair.getKey()));
            resultsMap.put("Third Line", BLS_ID.get(pair.getKey()));
            listItems.add(resultsMap);
        }

        ListView lv = (ListView) convertView.findViewById(R.id.dialog_list_view);
        lv.setAdapter(adapter);

        setListItemBehavior(listItems, lv);

        alertDialog.show();
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(MainActivity.this);

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
            String jsonStr = sh.makeServiceCall(baseURL);

            Log.e(TAG, "Response from descargas activas url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node

                        String fecha_inicio = BL.getString("fecha_inicio");
                        String id = BL.getString("id_detalle_bl");
                        String plataforma = BL.getString("plataforma");
                        String id_fase = BL.getString("id_fase");
                        BLS_ID.put(plataforma, id);
                        BL_descarga_on_process_fecha.put(plataforma, "Fecha inicio: " + fecha_inicio);
                        BL_descarga_on_process_id_fase.put(plataforma, id_fase);

                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 'e': " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            openDescargasActivasDialog();
            progress.dismiss();
        }
    }

    //Descargas activas dialog list item behavior, build url and call api
    public void setListItemBehavior(final List<HashMap<String, String>> listItems, ListView mListView) {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                HashMap<String, String> resultsMap = listItems.get(position);
                final String name = resultsMap.get("First Line");
                final String id_detalle_bl = resultsMap.get("Third Line");

                DateFormat formatFecha = new SimpleDateFormat("d_MM_yyyy");
                final String fecha = formatFecha.format(Calendar.getInstance().getTime());
                DateFormat formatHora = new SimpleDateFormat("HH:mm");
                final String hora = formatHora.format(Calendar.getInstance().getTime());

                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                        MainActivity.this);
                alertDialogBuilder
                        .setTitle("Deseas terminar esta fase de descarga?")
                        .setMessage("ID: " + id_detalle_bl + "\n \n Fecha: " + fecha + "\n \n Hora: " + hora)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new MainActivity.CallAPI().execute(BL_descarga_on_process_id_fase.get(name));
                            }
                        })
                        .setNegativeButton("Cancelar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                    }
                                });

                // create alert dialog
                android.app.AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });
    }

    public String buildURL(String vIdFase) {
        String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_fase_descarga?";

        String finalURL = (baseurl
                + "p_id_fase=" + vIdFase);

        return finalURL;
    }

    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(MainActivity.this);

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
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {Log.e(TAG, "fragment result called");
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001)) {

        }
    }

}
