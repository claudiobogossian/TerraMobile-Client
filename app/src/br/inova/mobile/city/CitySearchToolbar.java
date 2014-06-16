package br.inova.mobile.city;

import java.sql.SQLException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import br.inova.mobile.Utility;
import br.inova.mobile.database.DatabaseAdapter;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.map.GeoMap;
import br.inpe.mobile.R;

public class CitySearchToolbar extends AsyncTask<String, String, String> {
        
        protected static final String LOG_TAG = "#CITYSEARCHTOOLBAR";
        // widgets
        private AutoCompleteTextView  cityAutoCompleteTextView;
        
        private GeoMap                geoMap;
        private CitySearchToolbar     self    = this;
        
        private View                  citiesToolbar;
        
        public CitySearchToolbar(GeoMap geoMap) {
                this.geoMap = geoMap;
                this.execute();
        }
        
        private void clearCityAutoCompleteText() {
                cityAutoCompleteTextView.setText("");
        }
        
        public void createButtonForCloseToolbar() {
                ImageButton btnCloseSearchToolbar = (ImageButton) geoMap.findViewById(R.id.btn_close_search_toolbar);
                btnCloseSearchToolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                self.hideSearchToolbar();
                        }
                });
        }
        
        public void createButtonForOpenToolbar() {
                ImageButton btnCloseSearchToolbar = (ImageButton) geoMap.findViewById(R.id.btn_open_search_city_toolbar);
                btnCloseSearchToolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                self.showSearchToolbar();
                        }
                });
        }
        
        /**
         * Creates the inflator Layout to show the cities search toolbar.
         */
        public void createInflatorForSearchToolbar() {
                createButtonForOpenToolbar();
                createButtonForCloseToolbar();
                
                self.createThreadToSearchCitiesAdapter();
        }
        
        /**
         * Database query can be a time consuming task, so its safe to call
         * database query in another thread
         * 
         * @author Paulo Luan
         */
        public void createThreadToSearchCitiesAdapter() {
                new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                                Cursor cursor;
                                
                                try {
                                        cursor = CityAdapter.getCityCursor(null);
                                        setAutoCompleteAdapterPropertiers(cursor);
                                }
                                catch (SQLException e) {
                                        Log.e(self.LOG_TAG, "ERRO AO CRIAR CURSOR!" + e.getMessage());
                                        ExceptionHandler.saveLogFile(e);
                                }
                        }
                });
        }
        
        @Override
        protected String doInBackground(String... params) {
                this.generateCities();
                return null;
        }
        
        public void generateCities() {
                
                long countOfCities = CityDao.getCountOfCities();
                int progress = 0;
                
                Date begin = new Date();
                
                if (countOfCities != 0 && countOfCities != 5565) { // it means that the database is inconsistent.
                        try {
                                DatabaseAdapter.resetCityTable();
                                countOfCities = CityDao.getCountOfCities();
                        }
                        catch (SQLException e) {
                                e.printStackTrace();
                        }
                }
                
                if (countOfCities == 0) {
                        try {
                                String data = Utility.parseAssetFileToString(geoMap, "cities.json");
                                JSONArray cities = new JSONArray(data);
                                
                                publishProgress("Contruíndo banco de dados da aplicação...", "0", "" + cities.length());
                                
                                for (int i = 0; i < cities.length(); i++) {
                                        JSONObject city = (JSONObject) cities.get(i);
                                        
                                        String name = city.getString("name");
                                        String asciiName = city.getString("asciiName");
                                        String state = city.getString("state");
                                        
                                        JSONObject location = city.getJSONObject("location");
                                        Double latitude = location.getDouble("lat");
                                        Double longitude = location.getDouble("lng");
                                        
                                        City cityObject = new City(null, name, asciiName, state, latitude, longitude);
                                        CityDao.saveCity(cityObject);
                                        
                                        Log.i("SAVED CITY", cityObject.getName());
                                        
                                        progress++;
                                        publishProgress("Contruíndo banco de dados da aplicação...", "" + progress);
                                }
                        }
                        catch (Exception exception) {
                                ExceptionHandler.saveLogFile(exception);
                        }
                }
                
                Date end = new Date();
                long diff = end.getTime() - begin.getTime();
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffSeconds = diff / 1000 % 60;
                
                Log.i("TEMPO DE OPERAÇÃO DAS CIDADES: ", diffMinutes + ":" + diffSeconds);
        }
        
        public void hideKeyboard() {
                InputMethodManager mgr = (InputMethodManager) geoMap.getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(cityAutoCompleteTextView.getWindowToken(), 0);
        }
        
        public void hideSearchToolbar() {
                if (citiesToolbar != null) {
                        citiesToolbar.setVisibility(View.GONE);
                        clearCityAutoCompleteText();
                        hideKeyboard();
                }
        }
        
        @Override
        protected void onPostExecute(String result) {
                this.createInflatorForSearchToolbar();
                
                geoMap.hideLoadingMask();
        }
        
        @Override
        protected void onProgressUpdate(String... values) {
                geoMap.onProgressUpdate(values);
        }
        
        /**
         * 
         * Creates the auto Complete Text View to user search the cityes.
         * 
         * @author Paulo Luan
         * */
        public void setAutoCompleteAdapterPropertiers(Cursor cursor) {
                cityAutoCompleteTextView = (AutoCompleteTextView) geoMap.findViewById(R.id.search_city_autocomplete);
                CityAdapter cityAdapter = new CityAdapter(geoMap, R.layout.city_autocomplete_list, cursor, new String[] { "name", "state" }, new int[] { R.id.city_name, R.id.city_state });
                
                citiesToolbar = geoMap.findViewById(R.id.search_toolbar_layout);
                
                cityAutoCompleteTextView.setAdapter(cityAdapter);
                cityAutoCompleteTextView.setHint("Pesquisar cidade...");
                cityAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(
                                                AdapterView<?> adapter,
                                                View view,
                                                int position,
                                                long cityId) {
                                try {
                                        zoomToCity((int) cityId);
                                        hideSearchToolbar();
                                }
                                catch (Exception ex) {
                                        Log.e(LOG_TAG, "Exception onItemClick: " + ex);
                                }
                        }
                });
        }
        
        public void showSearchToolbar() {
                if (citiesToolbar != null) {
                        citiesToolbar.setVisibility(View.VISIBLE);
                }
        }
        
        public void zoomToCity(Integer cityId) {
                City city = CityDao.getCityById(cityId);
                
                MapController controller = geoMap.controller;
                
                controller.setZoom(12);
                controller.setCenter(new GeoPoint(city.getLatitude(), city.getLongitude()));
        }
}
