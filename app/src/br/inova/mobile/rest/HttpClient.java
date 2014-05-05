package br.inova.mobile.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import br.inova.mobile.task.Task;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClient {
        
        /**
         * 
         * 
         ***/
        private static String makeRequisition(HttpResponse response) throws IOException {
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
        private static String readResponseToJson(HttpResponse response) {
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
        
        /**
         * 
         * 
         ***/
        private static RestResponseObject jsonStringToRestResponseObject(
                                                                         String json) {
                ObjectMapper mapper = new ObjectMapper();
                RestResponseObject response = null;
                
                try {
                        response = mapper.readValue(json, RestResponseObject.class);
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
                
                return response;
        }
        
        /**
         * 
         * 
         ***/
        private static Task[] jsonStringToTaskObject(String json) {
                ObjectMapper mapper = new ObjectMapper();
                Task[] response = null;
                
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
                
                return response;
        }
        
        private class Get extends AsyncTask<String, String, String> {
                private String url;
                
                public Get(String url) {
                        this.url = url;
                }
                
                @Override
                protected String doInBackground(String... arg0) {
                        Task[] tasks = null;
                        DefaultHttpClient httpClient = null;
                        
                        try {
                                httpClient = new DefaultHttpClient();
                                HttpGet getRequest = new HttpGet(url);
                                getRequest.addHeader("accept", "application/json");
                                
                                HttpResponse response = httpClient.execute(getRequest);
                                
                                String jsonResponse = makeRequisition(response);
                                tasks = jsonStringToTaskObject(jsonResponse);
                        }
                        catch (Exception e) {
                                e.printStackTrace();
                        }
                        finally {
                                httpClient.getConnectionManager().shutdown();
                        }
                        
                        //return Arrays.asList(tasks);
                        return "";
                }
                
        }
        
        private class Post extends AsyncTask<String, String, String> {
                
                private String url, jsonObject;
                
                public Post(String url, String jsonObject) {
                        this.url = url;
                        this.jsonObject = jsonObject;
                }
                
                @Override
                protected String doInBackground(String... arg0) {
                        RestResponseObject restResponseObject = null;
                        DefaultHttpClient httpClient = null;
                        try {
                                httpClient = new DefaultHttpClient();
                                HttpPost postRequest = new HttpPost(url);
                                
                                StringEntity input = new StringEntity(jsonObject);
                                input.setContentType("application/json");
                                postRequest.setEntity(input);
                                
                                HttpResponse response = httpClient.execute(postRequest);
                                
                                String jsonResponse = makeRequisition(response);
                                restResponseObject = jsonStringToRestResponseObject(jsonResponse);
                        }
                        catch (Exception e) {
                                e.printStackTrace();
                        }
                        finally {
                                httpClient.getConnectionManager().shutdown();
                        }
                        
                        //return restResponseObject;
                        return "";
                }
                
        }
        
}
