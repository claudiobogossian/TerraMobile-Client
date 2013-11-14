package br.org.funcate.mobile.map;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Environment;

public class ServiceBaseMap {

	public ServiceBaseMap() {
		// TODO Auto-generated constructor stub
	}
	
	public void getRemoteZipBaseMap(){
		// remote.getBaseMap(user);
	}
	
	public boolean unpackZip(String path, String zipname)
	{       
	     InputStream is;
	     ZipInputStream zis;
	     try 
	     {
	         String filename;
	         is = new FileInputStream(path + zipname);
	         zis = new ZipInputStream(new BufferedInputStream(is));          
	         ZipEntry ze;
	         byte[] buffer = new byte[1024];
	         int count;

	         while ((ze = zis.getNextEntry()) != null) 
	         {
	             // zapis do souboru
	             filename = ze.getName();

	             // Need to create directories if not exists, or
	             // it will generate an Exception...
	             if (ze.isDirectory()) {
	                File fmd = new File(path + filename);
	                fmd.mkdirs();
	                continue;
	             }

	             FileOutputStream fout = new FileOutputStream(path + filename);

	             // cteni zipu a zapis
	             while ((count = zis.read(buffer)) != -1) 
	             {
	                 fout.write(buffer, 0, count);             
	             }

	             fout.close();               
	             zis.closeEntry();
	         }

	         zis.close();
	     } 
	     catch(IOException e)
	     {
	         e.printStackTrace();
	         return false;
	     }

	    return true;
	}
	
	public void saveFile(){
		File externalStorageDir = Environment.getExternalStorageDirectory();
		File playNumbersDir = new File(externalStorageDir, "PlayNumbers");
		File myFile = new File(playNumbersDir, "mysdfile.xml");

		if (!playNumbersDir.exists()) {
		    playNumbersDir.mkdirs();
		}
		if(!myFile.exists()){
		    try {
				myFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
