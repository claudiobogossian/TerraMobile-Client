package br.inova.mobile.address;

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
import br.inova.mobile.Utility;
import br.inova.mobile.database.DatabaseHelper;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.task.Task;
import br.inova.mobile.user.SessionManager;
import br.inova.mobile.user.User;
import br.inpe.mobile.R;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * This class is responsible for the {@link AutoCompleteTextView} of address,
 * that the user input the address text, and the widget will be search the data
 * on the database.
 * 
 * */
public class AddressAdapter extends CursorAdapter implements Filterable {
        
        private static final String LOG_TAG = "#AddressAdapter";
        
        private final Context       context;
        
        private final int           layout;
        
        public AddressAdapter(
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
        public void bindView(View v, Context ctx, Cursor c) {
                TextView txt_logradouro = (TextView) v.findViewById(R.id.item_log);
                txt_logradouro.setText("\n" + c.getString(c.getColumnIndex("name")));
                
                TextView txt_number = (TextView) v.findViewById(R.id.item_number);
                txt_number.setText("Número: " + c.getString(c.getColumnIndex("number")));
                
                TextView txt_lote = (TextView) v.findViewById(R.id.item_lote);
                txt_lote.setText("Lote: " + Utility.correctNull(c.getString(c.getColumnIndex("featureId"))) + "\n");
        }
        
        @Override
        public View newView(Context ctx, Cursor c, ViewGroup parent) {
                final LayoutInflater inflater = LayoutInflater.from(context);
                View v = inflater.inflate(layout, parent, false);
                return v;
        }
        
        @SuppressLint("DefaultLocale")
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
                String filter = constraint.toString();
                // String rawQuery = "SELECT * FROM address WHERE name = '"+ filter +
                // "' OR number = '" + filter + "'";
                
                Cursor cursor = null;
                
                try {
                        cursor = this.getAddressCursor("%" + filter + "%");
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return cursor;
        }
        
        public static Cursor getAddressCursor(String propertieFilter) throws SQLException {
                Dao<Task, Integer> taskDao = DatabaseHelper.getDatabase().getTaskDao();
                Dao<User, Integer> userDao = DatabaseHelper.getDatabase().getUserDao();
                Dao<Address, Integer> addressDao = DatabaseHelper.getDatabase().getAddressDao();
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                QueryBuilder<Address, Integer> addressQueryBuilder = addressDao.queryBuilder();
                
                String userHash = SessionManager.getInstance().getUserHash();
                userQueryBuilder.where().eq("hash", userHash);
                
                // taskQueryBuilder.where().eq("done", Boolean.FALSE);
                taskQueryBuilder.join(userQueryBuilder);
                
                if (propertieFilter != null) {
                        addressQueryBuilder.where().like("name", propertieFilter).or().like("number", propertieFilter).or().like("featureId", propertieFilter).or().like("postalCode", propertieFilter);
                        
                        addressQueryBuilder.join(taskQueryBuilder);
                }
                else {
                        addressQueryBuilder.query();
                }
                
                CloseableIterator<Address> iterator = null;
                Cursor cursor = null;
                
                try {
                        // when you are done, prepare your query and build an iterator
                        iterator = addressDao.iterator(addressQueryBuilder.prepare());
                        // get the raw results which can be cast under Android
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
