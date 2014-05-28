package br.inova.mobile.city;

import java.sql.SQLException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.TextView;
import br.inova.mobile.database.DatabaseHelper;
import br.inova.mobile.exception.ExceptionHandler;
import br.inpe.mobile.R;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * This class is responsible for the {@link AutoCompleteTextView} of city, that
 * the user input the city text, and the widget will be search the data on the
 * database.
 * 
 * */
public class CityAdapter extends CursorAdapter implements Filterable {
        
        private static final String LOG_TAG = "#CityAdapter";
        
        private final Context       context;
        
        private final int           layout;
        
        public CityAdapter(
                           Context context,
                           int layout,
                           Cursor c,
                           String[] from,
                           int[] to) {
                super(context, c, 0);
                this.context = context;
                this.layout = layout;
        }
        
        @Override
        public View newView(Context ctx, Cursor c, ViewGroup parent) {
                final LayoutInflater inflater = LayoutInflater.from(context);
                View v = inflater.inflate(layout, parent, false);
                return v;
        }
        
        @Override
        public void bindView(View v, Context ctx, Cursor c) {
                TextView txtCityName = (TextView) v.findViewById(R.id.city_name);
                txtCityName.setText("\nCidade:" + c.getString(c.getColumnIndex("name")));
                
                TextView txtState = (TextView) v.findViewById(R.id.city_state);
                txtState.setText("Estado: " + c.getString(c.getColumnIndex("state")));
        }
        
        @SuppressLint("DefaultLocale")
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
                Cursor cursor = null;
                String filter = "";
                
                if (constraint != null) {
                        filter = constraint.toString();
                }
                
                try {
                        cursor = this.getCityCursor("%" + filter + "%");
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return cursor;
        }
        
        public static Cursor getCityCursor(String cityName) throws SQLException {
                Dao<City, Integer> cityDao = DatabaseHelper.getDatabase().getCityDao();
                QueryBuilder<City, Integer> cityQueryBuilder = cityDao.queryBuilder();
                
                CloseableIterator<City> iterator = null;
                Cursor cursor = null;
                
                if (cityName != null) {
                        cityQueryBuilder.where().like("asciiName", cityName);
                }
                
                try {
                        iterator = cityDao.iterator(cityQueryBuilder.prepare());
                        AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
                        cursor = results.getRawCursor();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                finally {
                        if (iterator != null) {
                                // iterator.closeQuietly();
                        }
                }
                
                return cursor;
        }
}
