package br.org.funcate.mobile.map;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import br.org.funcate.mobile.Utility;

public class ServiceBaseMap {

	RestTemplate restTemplate;

	public ServiceBaseMap() {
		this.restTemplate = new RestTemplate();
	}

	public void getRemoteZipBaseMap(){
		String url = "http://200.144.100.34:8080/bauru-server/rest/tiles/zip";
		new DownloadZipAsync().execute(url);
	}

	/*
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Downloading file..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}*/

	class DownloadZipAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;
			String path = null;

			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

				InputStream input = new BufferedInputStream(url.openStream());
				File externalStorageDir = Environment.getExternalStorageDirectory();		
				path = externalStorageDir + "/osmdroid/tiles/";
				String filePath = path + "base_map.zip";
				OutputStream output = new FileOutputStream(filePath);

				byte data[] = new byte[1024];
				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress(""+(int)((total*100)/lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {}

			return path;

		}
		protected void onProgressUpdate(String... progress) {
			Log.d("ANDRO_ASYNC",progress[0]);
			//mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String path) {
			if(path != null) {
				Utility.unzip(path + "base_map.zip", path);
			}
			//dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
		}
	}

}
