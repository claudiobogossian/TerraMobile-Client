package br.org.funcate.mobile.photo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.client.HttpClientErrorException;

import android.os.AsyncTask;
import android.util.Log;
import br.org.funcate.mobile.task.TaskActivity;

/**
 * Async object implementation to Post Photos to server
 * 
 * @param String
 *            ... urls
 *            URL's that will called.
 * @author Paulo Luan
 */
public class UploadPhotos extends AsyncTask<String, String, List<Photo>> {

    private List<Photo>  photos;
    private String       userHash;
    private TaskActivity taskActivity;

    public UploadPhotos(List<Photo> photos, String userHash, TaskActivity taskActivity) {
        this.photos = photos;
        this.userHash = userHash;
        this.taskActivity = taskActivity;
    }

    @Override
    protected List<Photo> doInBackground(String... urls) {
        List<Photo> response = null;

        for (String url : urls) {
            try {
                publishProgress("Fazendo Upload das fotos...");
                Photo[] responsePhotos = taskActivity.restTemplate.postForObject(url, this.photos, Photo[].class, userHash);
                response = new ArrayList<Photo>(Arrays.asList(responsePhotos));

                if (response != null) {
                    publishProgress("Verificando se existem imagens não utilizadas no aparelho...");
                    PhotoDao.deletePhotos(response);
                }
            } catch (HttpClientErrorException e) {
                String error = e.getResponseBodyAsString();
                e.printStackTrace();
            }
        }
        return response;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... values) {
        taskActivity.setLoadMaskMessage(values[0]);
        Log.i(taskActivity.LOG_TAG, " Progress: " + values);
    }

    @Override
    protected void onPostExecute(List<Photo> result) {
        //TODO: mudar mensagem de feedback

        if (result != null) {
            PhotoDao.deletePhotos(result);
        }

        taskActivity.hideLoadMask();
    }
}
