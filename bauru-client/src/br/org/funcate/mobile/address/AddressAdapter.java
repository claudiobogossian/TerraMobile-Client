package br.org.funcate.mobile.address;

import java.sql.SQLException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.database.DatabaseHelper;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.user.SessionManager;
import br.org.funcate.mobile.user.User;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class AddressAdapter extends CursorAdapter implements Filterable {

	@SuppressWarnings("unused")
	private static final String LOG_TAG = "#AddressAdapter";

	private final Context context;
	private final int layout;
	
	public AddressAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
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
		TextView name_text1 = (TextView) v.findViewById(R.id.item_log);
		name_text1.setText(c.getString(c.getColumnIndex("name")));
		
		TextView name_text2 = (TextView) v.findViewById(R.id.item_cep);
		name_text2.setText("CEP: " + Utility.correctNull(c.getString(c.getColumnIndex("postalCode"))));
		
		TextView txt_number = (TextView) v.findViewById(R.id.item_number);
		txt_number.setText("Número: " + c.getString(c.getColumnIndex("number")));
		
		TextView txt_neighborhood = (TextView) v.findViewById(R.id.item_neighborhood);
		txt_neighborhood.setText("Bairro: " + c.getString(c.getColumnIndex("neighborhood")));
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		String filter = constraint.toString();
		//String rawQuery = "SELECT * FROM address WHERE name = '"+ filter + "' OR number = '" + filter + "'";
		
		Cursor cursor = null;
		
		try {
			cursor = AddressAdapter.getAddressCursor("%" + filter + "%");
		} catch (SQLException e) {
			e.printStackTrace();
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
		
		String userHash = SessionManager.getUserHash();
		userQueryBuilder.where()
			.eq("hash", userHash);
		
		taskQueryBuilder.where().eq("done", Boolean.FALSE);
		taskQueryBuilder.join(userQueryBuilder);
		
		if(propertieFilter != null) {
			addressQueryBuilder.where()
				.like("name", propertieFilter).or()
				.like("number", propertieFilter).or()
				.like("postalCode", propertieFilter);

			addressQueryBuilder.join(taskQueryBuilder);
		} else {
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
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(iterator != null) {
				//iterator.closeQuietly();
			}
		}
		
		return cursor;
	}
}
