package br.inpe.mobile.user;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import android.os.AsyncTask;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.exception.ExceptionHandler;

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
                                ResponseEntity<User[]> response = loginActivity.restTemplate.getForEntity(url, User[].class);
                                User[] users = response.getBody();
                                
                                ArrayList<User> list = new ArrayList<User>(Arrays.asList(users));
                                loginActivity.saveUsersIntoLocalSqlite(list);
                        }
                        catch (HttpClientErrorException e) {
                                message = "Erro ao verificar usuários no servidor.";
                                StringWriter errors = new StringWriter();
                                e.printStackTrace(new PrintWriter(errors));
                                ExceptionHandler.saveLogFile(errors.toString());
                        }
                        catch (Exception e) {
                                message = "Erro ao verificar usuários no servidor.";
                                StringWriter errors = new StringWriter();
                                e.printStackTrace(new PrintWriter(errors));
                                ExceptionHandler.saveLogFile(errors.toString());
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
