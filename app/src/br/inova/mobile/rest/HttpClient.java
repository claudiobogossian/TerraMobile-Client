package br.inova.mobile.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.util.Log;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.task.Task;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClient {
        
        /**
         * Makes a http request.
         * 
         * @param url
         * @param jsonObject
         * @return
         */
        public static List<Task> doGet(String url, String jsonObject) {
                
                Task[] tasks = null;
                List<Task> returnedTasks = null;
                
                DefaultHttpClient httpClient = null;
                
                try {
                        httpClient = new DefaultHttpClient();
                        HttpGet getRequest = new HttpGet(url);
                        getRequest.addHeader("accept", "application/json");
                        
                        HttpResponse response = httpClient.execute(getRequest);
                        
                        String jsonResponse = parseResponseToJSON(response);
                        tasks = jsonStringToTaskObject(jsonResponse);
                        
                        returnedTasks = Arrays.asList(tasks);
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
                finally {
                        httpClient.getConnectionManager().shutdown();
                }
                
                return returnedTasks;
        }
        
        /**
         * Makes a post request to server.
         * 
         * @param url
         * @param jsonObject
         * @return
         */
        public static Boolean doPost(String url, String jsonObject) {
                DefaultHttpClient httpClient = null;
                
                boolean isSaved = false;
                
                try {
                        httpClient = new DefaultHttpClient();
                        HttpPost postRequest = new HttpPost(url);
                        
                        StringEntity input = new StringEntity(jsonObject, "UTF-8");
                        input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        
                        postRequest.setEntity(input);
                        postRequest.setHeader("Accept", "application/json");
                        postRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
                        postRequest.setHeader("Accept-Encoding", "gzip");
                        
                        long t = System.currentTimeMillis();
                        HttpResponse response = httpClient.execute(postRequest);
                        Log.i("HTTPPostClient", "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");
                        
                        String jsonResponse = parseResponseToJSON(response);
                        
                        if (jsonResponse != null) {
                                isSaved = jsonStringToBoolean(jsonResponse);
                        }
                }
                catch (ClientProtocolException exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                catch (IOException exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        httpClient.getConnectionManager().shutdown();
                }
                
                return isSaved;
        }
        
        /**
         * 
         * 
         ***/
        private static boolean jsonStringToBoolean(String json) {
                ObjectMapper mapper = new ObjectMapper();
                Boolean isSaved = false;
                
                if (json != null) {
                        
                        try {
                                isSaved = mapper.readValue(json, Boolean.class);
                        }
                        catch (JsonParseException e) {
                                e.printStackTrace();
                        }
                        catch (JsonMappingException e) {
                                e.printStackTrace();
                        }
                        catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                
                return isSaved;
        }
        
        /**
         * 
         * 
         ***/
        private static Task[] jsonStringToTaskObject(String json) {
                ObjectMapper mapper = new ObjectMapper();
                Task[] response = null;
                
                if (json != null) {
                        try {
                                response = mapper.readValue(json, Task[].class);
                        }
                        catch (JsonParseException e) {
                                e.printStackTrace();
                        }
                        catch (JsonMappingException e) {
                                e.printStackTrace();
                        }
                        catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                
                return response;
        }
        
        /**
         * 
         * 
         ***/
        private synchronized static String parseResponseToJSON(
                                                               HttpResponse response) throws IOException {
                String jsonResponse = null;
                
                int responseCode = response.getStatusLine().getStatusCode();
                
                if (responseCode == 200) {
                        jsonResponse = readResponseToJson(response);
                }
                
                return jsonResponse;
        }
        
        /**
         * 
         * 
         ***/
        private synchronized static String readResponseToJson(
                                                              HttpResponse response) {
                BufferedReader in = null;
                StringBuffer stringBuffer = null;
                String responseString = null;
                
                try {
                        in = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
                        String inputLine;
                        stringBuffer = new StringBuffer();
                        
                        while ((inputLine = in.readLine()) != null) {
                                stringBuffer.append(inputLine);
                        }
                }
                catch (IOException e) {
                        e.printStackTrace();
                }
                finally {
                        try {
                                in.close();
                        }
                        catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                
                if (response != null) {
                        responseString = stringBuffer.toString();
                }
                
                return responseString;
        }
        
}
