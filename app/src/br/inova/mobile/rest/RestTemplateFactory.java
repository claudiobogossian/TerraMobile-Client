package br.inova.mobile.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.util.Log;

public class RestTemplateFactory extends RestTemplate {
        
        public RestTemplateFactory() {
                if (getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
                        Log.d("HTTP", "HttpUrlConnection is used");
                        ((SimpleClientHttpRequestFactory) getRequestFactory()).setConnectTimeout(1000);
                        ((SimpleClientHttpRequestFactory) getRequestFactory()).setReadTimeout(60 * 1000);
                }
                else if (getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
                        Log.d("HTTP", "HttpClient is used");
                        ((HttpComponentsClientHttpRequestFactory) getRequestFactory()).setConnectTimeout(1000);
                        ((HttpComponentsClientHttpRequestFactory) getRequestFactory()).setReadTimeout(60 * 1000);
                }
                
                // Add converters, Note I use the Jackson Converter, I removed the http form converter because it is not needed when posting String, used for multipart forms.
                getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                getMessageConverters().add(new StringHttpMessageConverter());
                
                setErrorHandler(new RestTemplateErrorHandler());
        }
}
