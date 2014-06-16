package br.inova.mobile.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.util.Log;

public class RestTemplateFactory extends RestTemplate {
        
        public RestTemplateFactory() {
                
                Integer timeout = 600 * 1000;
                
                if (getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
                        Log.i("HTTP", "HttpUrlConnection is used, TIMEOUT: " + timeout);
                        ((SimpleClientHttpRequestFactory) getRequestFactory()).setConnectTimeout(timeout);
                        ((SimpleClientHttpRequestFactory) getRequestFactory()).setReadTimeout(timeout);
                }
                else if (getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
                        Log.i("HTTP", "HttpClient is used, TIMEOUT: " + timeout);
                        ((HttpComponentsClientHttpRequestFactory) getRequestFactory()).setConnectTimeout(timeout);
                        ((HttpComponentsClientHttpRequestFactory) getRequestFactory()).setReadTimeout(timeout);
                }
                
                // Add converters, Note I use the Jackson Converter, I removed the http form converter because it is not needed when posting String, used for multipart forms.
                getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                getMessageConverters().add(new StringHttpMessageConverter());
                
                setErrorHandler(new RestTemplateErrorHandler());
        }
}
