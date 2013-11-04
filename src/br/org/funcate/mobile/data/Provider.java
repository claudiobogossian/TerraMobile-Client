package br.org.funcate.mobile.data;

import java.util.HashMap;

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

public class Provider extends ContentProvider {

	public static final String TAG = "#PROVIDER";

	public static final String AUTHORITY = "br.org.funcate.mobile.data.Provider";

	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_DADOS = "dados";
	private static final int DADOS_CODE = 1;

	private DBHelper mHelper;
	private static final UriMatcher mMatcher;
	private static HashMap<String, String> dadosProjection;

	static {
		dadosProjection = new HashMap<String, String>();
		dadosProjection.put(Dados.ID, Dados.ID);
		dadosProjection.put(Dados.FOT, Dados.FOT);
		dadosProjection.put(Dados.DAT, Dados.DAT);
		dadosProjection.put(Dados.LAT, Dados.LAT);
		dadosProjection.put(Dados.LON, Dados.LON);
		dadosProjection.put(Dados.NUM, Dados.NUM);
		dadosProjection.put(Dados.IF1, Dados.IF1);
		dadosProjection.put(Dados.IF2, Dados.IF2);
		dadosProjection.put(Dados.LOG, Dados.LOG);
		dadosProjection.put(Dados.CEP, Dados.CEP);
		dadosProjection.put(Dados.CID, Dados.CID);
		dadosProjection.put(Dados.EST, Dados.EST);
	}

	static {
		mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mMatcher.addURI(AUTHORITY, TABLE_DADOS, DADOS_CODE);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Log.i(TAG, ":::DELETANDO REGISTROS:::");
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;
		switch (mMatcher.match(uri)) {
		case DADOS_CODE:
			count = db.delete(TABLE_DADOS, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Erro na URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mMatcher.match(uri)) {
		case DADOS_CODE:
			return Dados.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Erro na URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Log.i(TAG, ":::INSERINDO REGISTROS::: " + uri + " ::: " +
		// values.toString());
		switch (mMatcher.match(uri)) {
		case DADOS_CODE:
			SQLiteDatabase db2 = mHelper.getWritableDatabase();
			long rowId2 = db2.insert(TABLE_DADOS, null, values);
			if (rowId2 > 0) {
				Uri noteUri = ContentUris.withAppendedId(Dados.CONTENT_URI,
						rowId2);
				getContext().getContentResolver().notifyChange(noteUri, null);
				return noteUri;
			}
		default:
			throw new IllegalArgumentException("Erro na URI: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mHelper = new DBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Log.i(TAG, ":::EXECUTANDO QUERY:::");
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		SQLiteDatabase database = mHelper.getReadableDatabase();
		Cursor cursor;
		switch (mMatcher.match(uri)) {
		case DADOS_CODE:
			builder.setTables(TABLE_DADOS);
			builder.setProjectionMap(dadosProjection);
			break;
		default:
			throw new IllegalArgumentException("Erro na URI: " + uri);
		}
		cursor = builder.query(database, projection, selection, selectionArgs,
				null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// Log.i(TAG, ":::ATUALIZANDO REGISTROS:::");
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;
		switch (mMatcher.match(uri)) {
		case DADOS_CODE:
			count = db.update(TABLE_DADOS, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Erro na URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	public static final class Dados implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Provider.AUTHORITY + "/dados");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.funcate.dadostype";
		public static final String ID = "_id";
		public static final String FOT = "fot";
		public static final String DAT = "dat";
		public static final String LAT = "lat";
		public static final String LON = "lon";
		public static final String NUM = "num";
		public static final String IF1 = "if1";
		public static final String IF2 = "if2";
		public static final String LOG = "log";
		public static final String CEP = "cep";
		public static final String CID = "cid";
		public static final String EST = "est";
	}

	private static class DBHelper extends SQLiteOpenHelper {
		DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			String sql = "CREATE TABLE " + TABLE_DADOS + " (" + Dados.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + Dados.FOT
					+ " TINYTEXT," + Dados.DAT + " DATETIME," + Dados.LAT
					+ " TINYTEXT," + Dados.LON + " TINYTEXT," + Dados.LOG
					+ " TEXT," + Dados.NUM + " INTEGER," + Dados.IF1 + " TEXT,"
					+ Dados.IF2 + " TEXT," + Dados.CEP + " INTEGER,"
					+ Dados.CID + " TEXT," + Dados.EST + " TEXT);";
			db.execSQL(sql);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}
}