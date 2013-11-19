package br.org.funcate.mobile.map;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import br.org.funcate.mobile.Utility;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class ServiceBaseMap {

	RestTemplate restTemplate;

	public ServiceBaseMap() {
		this.restTemplate = new RestTemplate();
	}

	public void getRemoteZipBaseMap(){
		String url = "http://192.168.5.60:8080/bauru-server/rest/tiles/zip";

		final RequestCallback requestCallback = new RequestCallback() {
			@Override
			public void doWithRequest(final ClientHttpRequest request) throws IOException {
				//request.getHeaders().add("Content-type", "application/octet-stream");
				//IOUtils.copy(fis, request.getBody());

				OutputStream body = request.getBody();
			}

		};

		final RestTemplate restTemplate = new RestTemplate();
		final HttpMessageConverterExtractor<String> responseExtractor = new HttpMessageConverterExtractor<String>(String.class, restTemplate.getMessageConverters());
		restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
	}

	public boolean unpackZip(){

		File externalStorageDir = Environment.getExternalStorageDirectory();		
		String path = externalStorageDir + "/osmdroid/tiles/";

		String zipname = "";
		InputStream is;
		ZipInputStream zis;

		try 
		{
			String filename;
			is = new FileInputStream(path + zipname);
			zis = new ZipInputStream(new BufferedInputStream(is));          
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) 
			{
				filename = ze.getName();

				// Need to create directories if not exists, or it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(path + filename);
					fmd.mkdirs();
					continue;
				}

				FileOutputStream fout = new FileOutputStream(path + filename);

				while ((count = zis.read(buffer)) != -1) 
				{
					fout.write(buffer, 0, count);             
				}

				fout.close();               
				zis.closeEntry();
			}

			zis.close();
		} 
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void saveFile(){
		File externalStorageDir = Environment.getExternalStorageDirectory();
		File playNumbersDir = new File(externalStorageDir, "/osmdroid/tiles/"); // extrair aqui... /MapNick/12 ...
		File myFile = new File(playNumbersDir, "mysdfile.xml");

		if (!playNumbersDir.exists()) {
			playNumbersDir.mkdirs();
		}
		if(!myFile.exists()){
			try {
				myFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}





	private void startDownload() {
		String url = "http://192.168.5.60:8080/bauru-server/rest/tiles/zip";
		//String url = "http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg";
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
