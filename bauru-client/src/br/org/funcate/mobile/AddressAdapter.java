package br.org.funcate.mobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import br.org.funcate.mobile.data.ProviderAddress;

public class AddressAdapter extends SimpleCursorAdapter implements Filterable {

	@SuppressWarnings("unused")
	private static final String TAG = "#AddressAdapter";

	private final Context context;
	private final int layout;

	public AddressAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to) {
		super(context, layout, c, from, to);
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
		TextView name_text1 = (TextView) v.findViewById(R.id.itemlog);
		name_text1.setText(c.getString(c
				.getColumnIndex(ProviderAddress.Lograd.LOG)));
		TextView name_text2 = (TextView) v.findViewById(R.id.itemcep);
		name_text2.setText("CEP: "
				+ Utility.correctNull(c.getString(c
						.getColumnIndex(ProviderAddress.Lograd.CEP))));
	}

	@SuppressLint("DefaultLocale")
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		StringBuilder buffer = null;
		String[] args = null;
		if (constraint != null) {
			buffer = new StringBuilder();
			buffer.append("LOWER(");
			buffer.append(ProviderAddress.Lograd.LOG);
			buffer.append(") GLOB ?");
			args = new String[] { "*" + constraint.toString().toLowerCase()
					+ "*" };
		}

		Cursor c = context.getContentResolver().query(
				ProviderAddress.Lograd.CONTENT_URI, null,
				buffer == null ? null : buffer.toString(), args,
						ProviderAddress.Lograd.LOG + " ASC");
		return c;
	}
}
