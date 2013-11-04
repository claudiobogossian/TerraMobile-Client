package br.org.funcate.mobile.data;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class ProviderAddress extends ContentProvider {

	private static final String TAG = "#ProviderAddress";

	public static final String AUTHORITY = "br.org.funcate.mobile.data.ProviderAddress";

	private static final String DATABASE_NAME = "address.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_LOGRAD = "logradouros";
	private static final int LOGRAD_CODE = 1;

	private DBHelper mHelper;
	private static final UriMatcher mMatcher;
	private static HashMap<String, String> logradProjection;

	static {
		logradProjection = new HashMap<String, String>();
		logradProjection.put(Lograd.ID, Lograd.ID);
		logradProjection.put(Lograd.LOG, Lograd.LOG);
		logradProjection.put(Lograd.CEP, Lograd.CEP);
	}

	static {
		mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mMatcher.addURI(AUTHORITY, TABLE_LOGRAD, LOGRAD_CODE);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.i(TAG, ":::DELETANDO REGISTROS:::");
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;
		switch (mMatcher.match(uri)) {
		case LOGRAD_CODE:
			count = db.delete(TABLE_LOGRAD, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mMatcher.match(uri)) {
		case LOGRAD_CODE:
			return Lograd.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.i(TAG, ":::INSERINDO REGISTROS:::");
		Uri noteUri = null;
		switch (mMatcher.match(uri)) {
		case LOGRAD_CODE:
			noteUri = insertAux(TABLE_LOGRAD, values, Lograd.CONTENT_URI);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		return noteUri;
	}

	private Uri insertAux(String table, ContentValues values, Uri content_uri) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		long rowId = db.insert(table, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(content_uri, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		mHelper = new DBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.i(TAG, ":::EXECUTANDO QUERY:::");
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		SQLiteDatabase database = mHelper.getReadableDatabase();
		Cursor cursor;
		switch (mMatcher.match(uri)) {
		case LOGRAD_CODE:
			builder.setTables(TABLE_LOGRAD);
			builder.setProjectionMap(logradProjection);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		cursor = builder.query(database, projection, selection, selectionArgs,
				null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.i(TAG, ":::ATUALIZANDO REGISTROS:::");
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;
		switch (mMatcher.match(uri)) {
		case LOGRAD_CODE:
			count = db.update(TABLE_LOGRAD, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	public static final class Lograd implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ ProviderAddress.AUTHORITY + "/logradouros");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geogames.geogamestype";
		public static final String ID = "_id";
		public static final String LOG = "log";
		public static final String CEP = "cep";
	}

	@SuppressLint("SdCardPath")
	private static class DBHelper extends SQLiteOpenHelper {

		DBHelper(Context context) {
			super(context, "/data/data/" + context.getPackageName()
					+ "/files/databases/" + DATABASE_NAME, null,
					DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}
}