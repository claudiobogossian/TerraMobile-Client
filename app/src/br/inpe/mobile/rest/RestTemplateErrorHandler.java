package br.inpe.mobile.rest;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import android.util.Log;

public class RestTemplateErrorHandler implements ResponseErrorHandler {
        
        @Override
        public void handleError(ClientHttpResponse clienthttpresponse) throws IOException {
                
                if (clienthttpresponse.getStatusCode() == HttpStatus.FORBIDDEN) {
                        Log.d(HttpStatus.FORBIDDEN.toString(), " response. Throwing authentication exception");
                        //throw new AuthenticationException();
                }
        }
        
        @Override
        public boolean hasError(ClientHttpResponse clienthttpresponse) throws IOException {
                
                if (clienthttpresponse.getStatusCode() != HttpStatus.OK) {
                        Log.d("Status code: ", clienthttpresponse.getStatusCode().toString());
                        Log.d("Response: ", clienthttpresponse.getStatusText().toString());
                        Log.d("BODY: ", clienthttpresponse.getBody().toString());
                        
                        if (clienthttpresponse.getStatusCode() == HttpStatus.FORBIDDEN) {
                                Log.d("RESTTEMPLATE", "Call returned a error 403 forbidden resposne ");
                                return true;
                        }
                }
                return false;
        }
}