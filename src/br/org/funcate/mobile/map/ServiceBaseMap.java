package br.org.funcate.mobile.map;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import android.os.Environment;

public class ServiceBaseMap {

	RestTemplate restTemplate;

	public ServiceBaseMap() {
		this.restTemplate = new RestTemplate();
	}

	public void getRemoteZipBaseMap(){
		String url = "http://192.168.5.60:8080/bauru-server/rest/tiles/zip";

		final RequestCallback requestCallback = new RequestCallback() {
			@Override
			public void doWithRequest(final ClientHttpRequest request) throws IOException {
				//request.getHeaders().add("Content-type", "application/octet-stream");
				//IOUtils.copy(fis, request.getBody());

				OutputStream body = request.getBody();
			}
			
		};

		final RestTemplate restTemplate = new RestTemplate();
		final HttpMessageConverterExtractor<String> responseExtractor = new HttpMessageConverterExtractor<String>(String.class, restTemplate.getMessageConverters());
		restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
	}

	public boolean unpackZip(){
		String path = "";
		String zipname = "";
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
				filename = ze.getName();

				// Need to create directories if not exists, or it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(path + filename);
					fmd.mkdirs();
					continue;
				}

				FileOutputStream fout = new FileOutputStream(path + filename);

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
		File playNumbersDir = new File(externalStorageDir, "/osmdroid/tiles/"); // extrair aqui... /MapNick/12 ...
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
