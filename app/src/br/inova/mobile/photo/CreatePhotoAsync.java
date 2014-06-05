package br.inova.mobile.photo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.support.Base64;

import br.inova.mobile.exception.ExceptionHandler;

public class CreatePhotoAsync {
        
        /**
         * 
         * Returns Base64 String from filePath of a photo.
         * 
         * @param String
         *                filePath The path of the image that you want to get
         *                the base.
         * 
         * */
        public synchronized static String getBytesFromImage(
                                                            final String filePath) {
                System.gc();
                
                String imgString = null;
                
                byte[] bytes;
                byte[] buffer = new byte[8192];
                
                int bytesRead;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                
                try {
                        InputStream inputStream = new FileInputStream(filePath);//You can get an inputStream using any IO API
                        
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                        }
                        
                        bytes = output.toByteArray();
                        imgString = Base64.encodeBytes(bytes);
                }
                catch (IOException exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                catch (OutOfMemoryError exception) {
                        ExceptionHandler.saveLogFile("OutOfMemory ao tentar converter para Base64... " + exception.getLocalizedMessage() + exception.getMessage());
                }
                
                return imgString;
        }
        
}
