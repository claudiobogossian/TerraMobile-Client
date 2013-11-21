package br.org.funcate.baurudigital.server.photo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import br.org.funcate.baurudigital.server.form.FormException;
import br.org.funcate.baurudigital.server.form.FormService;
import br.org.funcate.baurudigital.server.user.User;
import br.org.funcate.baurudigital.server.user.UserException;
import br.org.funcate.baurudigital.server.user.UserService;

public class PhotoService {

	public static void savePhotos(List<Photo> photos, String userHash)
			throws PhotoException {
		User user;
		try {
			user = UserService.getUserByHash(userHash);
		} catch (UserException e) {
			throw new PhotoException(
					"Não foi possível obter as task solicitadas pois o usuário solicitado não existe.",
					e);
		}
		new PhotoDAO().save(photos);
	}
	
	public static Photo retrievePhotoFromDisk() throws IOException, FormException
	{
		String filePath = "/dados/temp/1379293393_519856642_1-leite-pregomim-pept-Vila-Formosa.jpg";
		
		
		RandomAccessFile f = new RandomAccessFile(filePath, "r");
		byte[] b = new byte[(int)f.length()];
		f.read(b);
		Photo photo = new Photo();
		photo.setBlob(b);
		photo.setPath(filePath);
		photo.setForm(FormService.getForm(54));
		
		return photo;
		
	}
	
	public static Photo retrieve(int id)
			throws PhotoException {
		return new PhotoDAO().retrieve(id);
	}

}
