package br.org.funcate.mobile.task;

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
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import br.org.funcate.mobile.Utility;

public class DownloadZipAsync extends AsyncTask<String, String, String> {
    private ProgressDialog mProgressDialog;
    String                 path     = null;
    String                 filePath = null;

    private TaskActivity   taskActivity;

    public DownloadZipAsync(TaskActivity taskActivity) {
        this.taskActivity = taskActivity;
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            path = Environment.getExternalStorageDirectory() + "/osmdroid/tiles/";
            filePath = path + "base_map.zip";

            File baseMapZip = new File(path);

            if (!baseMapZip.exists()) {
                this.getRemoteBaseMap(urls[0]);
            }
        } catch (Exception e) {
            Utility.showToast("Ocorreu um erro ao baixar o arquivo.", Toast.LENGTH_LONG, taskActivity);
            e.printStackTrace();
        }

        try {
            this.unzip(filePath, path);
        } catch (IOException e) {
            Utility.showToast("Ocorreu um erro ao descompactar o arquivo.", Toast.LENGTH_LONG, taskActivity);
            e.printStackTrace();
        }

        return path;
    }

    /**
     * Get remote file using URLConnection.
     * 
     * @param remoteURL
     *            The remote url of the file.
     * 
     * @throws IOException
     */
    public void getRemoteBaseMap(String remoteUrl) throws IOException {
        int count;

        URL url = new URL(remoteUrl);
        URLConnection conexion = url.openConnection();
        conexion.connect();

        int fileSize = conexion.getContentLength();

        InputStream input = new BufferedInputStream(url.openStream());
        OutputStream output = new FileOutputStream(filePath);

        byte data[] = new byte[1024];
        long total = 0;

        while ((count = input.read(data)) != -1) {
            total += count;
            Integer progress = (int) ((total * 100) / fileSize);
            publishProgress("Baixando mapa de base...", "" + progress);
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();
    }

    /**
     * Unzip files.
     * 
     * @param zipfile
     *            The path of the zip file
     * @param location
     *            The new Location that you want to unzip the file
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
                Utility.dirChecker(ze.getName());
            }
            else {
                progress++;
                publishProgress("Descompactando mapa de base...", "" + progress);

                FileOutputStream fout = new FileOutputStream(outputFolder + ze.getName());
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                zin.closeEntry();
                fout.close();
            }
        }

        zin.close();
    }

    void showDialog(String message) {
        mProgressDialog = new ProgressDialog(taskActivity);
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    void hideDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showDialog("Carregando, aguarde...");
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);

        if (progress.length == 3) {
            mProgressDialog.setMax(Integer.parseInt(progress[2]));
        }

        mProgressDialog.setMessage(progress[0]);
        mProgressDialog.setProgress(Integer.parseInt(progress[1]));
    }

    @Override
    protected void onPostExecute(String path) {
        this.hideDialog();
    }
}
