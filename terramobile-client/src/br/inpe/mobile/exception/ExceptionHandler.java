package br.inpe.mobile.exception;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import br.inpe.mobile.Main;

public class ExceptionHandler extends Throwable implements java.lang.Thread.UncaughtExceptionHandler {
    private final Activity myContext;
    
    private final String   LINE_SEPARATOR = "\n";
    
    public ExceptionHandler(Activity context) {
        myContext = context;
    }
    
    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());
        
        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);
        
        this.saveLogFile(errorReport.toString());
        
        Intent intent = new Intent(myContext, Main.class);
        intent.putExtra("error", errorReport.toString());
        myContext.startActivity(intent);
        
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
    
    /**
     * Creates a log file with the current date and time.
     * 
     * @param text - the text that will be saved on the log file.
     * @author Paulo Luan
     * 
     * */
    public void saveLogFile(String text) {
        File path = new File(Environment.getExternalStorageDirectory() + "/inpe/" + "/dados" + "/log/");
        
        if (!path.exists()) {
            path.mkdirs();
        }
        
        String fileName = "log_" + new Date().toString() + ".txt";
        File logFile = new File(path, fileName);
        
        if (!logFile.exists()){
            try{
                logFile.createNewFile();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}