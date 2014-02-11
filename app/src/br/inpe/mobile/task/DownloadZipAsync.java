package br.inpe.mobile.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.map.GeoMap;

public class DownloadZipAsync extends AsyncTask<String, String, String> {
	private long enqueue;
	private DownloadManager downloadManager;
	
	String tileSourcePath = GeoMap.tileSourcePath;
	String filePath = null;
	
	String message = null;	
	List<String> mapLevels = Arrays.asList("12", "13", "14", "15", "16");
	
	private TaskActivity taskActivity;

	public DownloadZipAsync(TaskActivity taskActivity) {
		this.taskActivity = taskActivity;
	}

	@Override
	protected String doInBackground(String... urls) {
		
		//TODO: vai pegar os mapLevels do servidor?
		// server.getArrayDeMapLevels
		
		for (String mapLevel : mapLevels) {
			String url = urls[0] + "?level=" + mapLevel;
			downloadZipFiles(url, mapLevel);
		}
		
		/*
		try {
			this.unzip(filePath, tileSourcePath);
		} catch (Exception e) {
			message = "Ocorreu um erro ao descompactar o arquivo.";
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			ExceptionHandler.saveLogFile(errors.toString());
		}*/

		return message;
	}

	public void downloadZipFiles(String url, String mapLevel) {
		try {
			filePath = tileSourcePath + "bauru_" + mapLevel + ".zip";
			File baseMapZip = new File(filePath);

			if (!baseMapZip.exists()) {
				this.getRemoteBaseMap(url, mapLevel);
			} else {
				long localFileSize = baseMapZip.length();
				boolean isSameSize = this.compareSizeOfRemoteFile(url, localFileSize);

				if (!isSameSize) {
					this.getRemoteBaseMap(url, mapLevel);
				}
			}
		} catch (Exception e) {
			message = "Ocorreu um erro ao baixar o arquivo.";
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			ExceptionHandler.saveLogFile(errors.toString());
		}
	}
	
	public boolean compareSizeOfRemoteFile(String remoteUrl, long localFileSize) throws IOException {
		boolean isSameSize = false;

		URL url = new URL(remoteUrl);
		URLConnection conexion = url.openConnection();
		conexion.connect();

		long remoteFileSize = conexion.getContentLength();

		if (localFileSize == remoteFileSize) {
			isSameSize = true;
		}

		return isSameSize;
	}

	/**
	 * Get remote file using URLConnection.
	 * 
	 * @param remoteURL
	 *            The remote url of the file.
	 * 
	 * @throws IOException
	 */
	public void getRemoteBaseMap(String remoteUrl, String mapLevel) throws IOException {
		downloadManager = (DownloadManager) taskActivity.getSystemService(Context.DOWNLOAD_SERVICE);
		
		String url = Utility.hostUrl + "rest/tiles/zip?level=" + mapLevel;
		
		Request request = new Request(Uri.parse(url));
		request.setDestinationInExternalPublicDir("osmdroid", "bauru_" + mapLevel + ".zip");
				
		publishProgress("Iniciado download: " + mapLevel +".zip ");
		
		enqueue = downloadManager.enqueue(request);
	}

	/**
	 * Unzip files.
	 * 
	 * @param zipfile
	 *            The path of the zip file
	 * @param location
	 *            The new Location (path) that you want to unzip the file
	 * @throws IOException
	 */
	private void unzip(String zipFile, String outputFolder) throws IOException {
		int progress = 0;

		ZipFile zip = new ZipFile(zipFile);
		publishProgress("Descompactando mapa de base...", "0", "" + zip.size());

		FileInputStream fin = new FileInputStream(zipFile);
		ZipInputStream zin = new ZipInputStream(fin);
		ZipEntry ze = null;

		while ((ze = zin.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				Utility.dirChecker(outputFolder + ze.getName());
			} else {
				progress++;
				publishProgress("Descompactando mapa de base...", "" + progress);

				FileOutputStream fout = null;
				try {
					fout = new FileOutputStream(outputFolder + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}
				} catch (Exception e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					ExceptionHandler.saveLogFile(errors.toString());
				} finally {
					zin.closeEntry();
					if (fout != null) {
						fout.close();
					}
				}
			}
		}

		zin.close();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//taskActivity.showLoadingMask("Carregando, aguarde...");
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
		Utility.showToast(progress[0] , Toast.LENGTH_LONG, taskActivity);
		//taskActivity.onProgressUpdate(progress);
	}

	@Override
	protected void onPostExecute(String message) {
		//taskActivity.hideLoadingMask();

		if (message != null) {
			Utility.showToast(message, Toast.LENGTH_LONG, taskActivity);
		}
	}
}
