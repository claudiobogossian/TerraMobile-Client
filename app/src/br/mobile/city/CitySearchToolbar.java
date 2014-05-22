package br.mobile.city;

import java.sql.SQLException;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.map.GeoMap;
import br.inpe.mobile.R;

public class CitySearchToolbar {
        
        protected static final String LOG_TAG = "#CITYSEARCHTOOLBAR";
        // widgets
        private AutoCompleteTextView  cityAutoCompleteTextView;
        private LayoutInflater        inflater;
        
        private GeoMap                geoMap;
        private CitySearchToolbar     self    = this;
        
        private View                  citiesToolbar;
        
        public CitySearchToolbar(GeoMap geomap) {
                this.geoMap = geomap;
                CityDao.generateCities(geomap);
                this.createInflatorForSearchToolbar();
        }
        
        /**
         * Creates the inflator Layout to show the cities search toolbar.
         */
        public void createInflatorForSearchToolbar() {
                inflater = LayoutInflater.from(geoMap.getBaseContext());
                citiesToolbar = inflater.inflate(R.layout.cities_search_toolbar, null);
                LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                geoMap.addContentView(citiesToolbar, layoutParamsControl);
                
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
        
        /**
         * 
         * Creates the auto Complete Text View to user search the cityes.
         * 
         * @author Paulo Luan
         * */
        public void setAutoCompleteAdapterPropertiers(Cursor cursor) {
                cityAutoCompleteTextView = (AutoCompleteTextView) geoMap.findViewById(R.id.search_city_autocomplete);
                CityAdapter cityAdapter = new CityAdapter(geoMap, R.layout.city_autocomplete_list, cursor, new String[] { "name", "state" }, new int[] { R.id.city_name, R.id.city_state });
                
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
        
        public void createButtonForOpenToolbar() {
                ImageButton btnCloseSearchToolbar = (ImageButton) geoMap.findViewById(R.id.btn_open_search_city_toolbar);
                btnCloseSearchToolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                self.showSearchToolbar();
                        }
                });
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
        
        public void showSearchToolbar() {
                if (citiesToolbar != null) {
                        citiesToolbar.setVisibility(View.VISIBLE);
                }
        }
        
        public void hideSearchToolbar() {
                if (citiesToolbar != null) {
                        citiesToolbar.setVisibility(View.GONE);
                        clearCityAutoCompleteText();
                        hideKeyboard();
                }
        }
        
        public void zoomToCity(Integer cityId) {
                City city = CityDao.getCityById(cityId);
                
                geoMap.controller.setZoom(12);
                geoMap.controller.setCenter(new GeoPoint(city.getLatitude(), city.getLongitude()));
        }
        
        private void clearCityAutoCompleteText() {
                cityAutoCompleteTextView.setText("");
        }
        
        public void hideKeyboard() {
                InputMethodManager mgr = (InputMethodManager) geoMap.getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(cityAutoCompleteTextView.getWindowToken(), 0);
        }
        
}
