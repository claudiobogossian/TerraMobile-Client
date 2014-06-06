package br.inova.mobile.user;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import android.os.AsyncTask;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.rest.RestTemplateFactory;

/**
 * Async class implementation to get users from server.
 * 
 * @author Paulo Luan
 * 
 * @param String
 *                ... urls URL's that will called.
 */
public class DownloadUsers extends AsyncTask<String, String, String> {
        private LoginActivity loginActivity;
        
        public DownloadUsers(LoginActivity loginActivity) {
                this.loginActivity = loginActivity;
        }
        
        @Override
        protected String doInBackground(String... urls) {
                String message = null;
                
                for (String url : urls) {
                        try {
                                ResponseEntity<User[]> response = new RestTemplateFactory().getForEntity(url, User[].class);
                                User[] users = response.getBody();
                                
                                ArrayList<User> list = new ArrayList<User>(Arrays.asList(users));
                                loginActivity.saveUsersIntoLocalSqlite(list);
                        }
                        catch (HttpClientErrorException e) {
                                message = "Erro ao verificar usuários no servidor.";
                                ExceptionHandler.saveLogFile(e);
                        }
                        catch (Exception e) {
                                message = "Erro ao verificar usuários no servidor.";
                                ExceptionHandler.saveLogFile(e);
                        }
                }
                
                return message;
        }
        
        @Override
        protected void onPreExecute() {
                loginActivity.showLoadingMask("Realizando Login...");
        }
        
        @Override
        protected void onProgressUpdate(String... progress) {
                loginActivity.onProgressUpdate(progress);
        }
        
        @Override
        protected void onPostExecute(String message) {
                loginActivity.hideLoadingMask();
                
                if (message != null) {
                        Utility.showToast(message, Toast.LENGTH_LONG, loginActivity);
                }
                
                loginActivity.login();
        }
        
}
