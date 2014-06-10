package br.inova.mobile.map;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.constants.Constants;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.task.TaskActivity;

public class BaseMapDownload extends AsyncTask<String, String, String> {
        String               tileSourcePath = GeoMap.tileSourcePath;
        
        String               filePath       = null;
        
        String               message        = null;
        
        List<String>         mapLevels      = Arrays.asList("12", "13", "14", "15", "16", "17", "20");
        
        private TaskActivity taskActivity;
        
        public BaseMapDownload(TaskActivity taskActivity) {
                this.taskActivity = taskActivity;
        }
        
        public boolean compareSizeOfRemoteFile(
                                               String remoteUrl,
                                               long localFileSize) throws IOException {
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
        
        @Override
        protected String doInBackground(String... urls) {
                
                for (String mapLevel : mapLevels) {
                        String url = urls[0] + "?level=" + mapLevel;
                        downloadZipFiles(url, mapLevel);
                }
                
                /*
                 * try { this.unzip(filePath, tileSourcePath); } catch
                 * (Exception e) { message =
                 * "Ocorreu um erro ao descompactar o arquivo."; StringWriter
                 * errors = new StringWriter(); e.printStackTrace(new
                 * PrintWriter(errors));
                 * ExceptionHandler.saveLogFile(errors.toString()); }
                 */
                
                return message;
        }
        
        public void downloadZipFiles(String url, String mapLevel) {
                try {
                        filePath = tileSourcePath + "bauru_" + mapLevel + ".zip";
                        File baseMapZip = new File(filePath);
                        
                        if (!baseMapZip.exists()) {
                                this.getRemoteBaseMap(url, mapLevel);
                        }
                        else {
                                long localFileSize = baseMapZip.length();
                                boolean isSameSize = this.compareSizeOfRemoteFile(url, localFileSize);
                                
                                if (!isSameSize) {
                                        this.getRemoteBaseMap(url, mapLevel);
                                }
                        }
                }
                catch (Exception e) {
                        message = "Ocorreu um erro ao baixar o arquivo.";
                        ExceptionHandler.saveLogFile(e);
                }
        }
        
        /**
         * Get remote file using URLConnection.
         * 
         * @param remoteURL
         *                The remote url of the file.
         * 
         * @throws IOException
         */
        public void getRemoteBaseMap(String remoteUrl, String mapLevel) throws IOException {
                final DownloadManager downloadManager = (DownloadManager) taskActivity.getSystemService(Context.DOWNLOAD_SERVICE);
                
                String url = Utility.getServerUrl() + Constants.ZIP_REST + Constants.LEVEL_QUERY_STRING + mapLevel;
                Request request = new Request(Uri.parse(url));
                
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bauru_" + mapLevel + ".zip");
                
                final long enqueue = downloadManager.enqueue(request);
                
                BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                                String action = intent.getAction();
                                
                                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                                        Query query = new Query();
                                        query.setFilterById(enqueue);
                                        
                                        Cursor c = downloadManager.query(query);
                                        
                                        if (c.moveToNext()) {
                                                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                                
                                                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                                                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                                        
                                                        File file = null;
                                                        
                                                        try {
                                                                file = new File(new URI(uriString));
                                                        }
                                                        catch (Exception e) {
                                                                e.printStackTrace();
                                                        }
                                                        
                                                        if (file != null || !file.exists()) {
                                                                String inputPath = file.getPath();
                                                                String fileName = file.getName();
                                                                
                                                                Utility.moveFile(inputPath, tileSourcePath, fileName);
                                                                
                                                                sendActionMountedEvent();
                                                        }
                                                }
                                        }
                                }
                        }
                };
                
                taskActivity.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                
                publishProgress("Iniciado download: " + mapLevel + ".zip ");
        }
        
        @Override
        protected void onPostExecute(String message) {
                //taskActivity.hideLoadingMask();
                
                if (message != null) {
                        Utility.showToast(message, Toast.LENGTH_LONG, taskActivity);
                }
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                //taskActivity.showLoadingMask("Carregando, aguarde...");
        }
        
        @Override
        protected void onProgressUpdate(String... progress) {
                super.onProgressUpdate(progress);
                Utility.showToast(progress[0], Toast.LENGTH_LONG, taskActivity);
                //taskActivity.onProgressUpdate(progress);
        }
        
        /**
         * In order for the MapView to start loading a new tile archive file,
         * you need the MapTileFileArchiveProvider class to call its
         * findArchiveFiles() function. As it is coded now, this only happens
         * when the MapTileFileArchiveProvider class is constructed, and when
         * the system sends a ACTION_MEDIA_UNMOUNTED/ACTION_MEDIA_MOUNTED
         * notification.
         * 
         * */
        public void sendActionMountedEvent() {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                
                if (currentapiVersion < android.os.Build.VERSION_CODES.KITKAT) {
                        Uri uri = Uri.parse("file://" + this.tileSourcePath);
                        taskActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, uri));
                }
                else {
                        // TODO: won't work.
                        GeoMap.mapView.invalidate();
                }
        }
}
