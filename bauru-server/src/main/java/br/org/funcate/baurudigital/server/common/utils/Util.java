package br.org.funcate.baurudigital.server.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import br.org.funcate.baurudigital.server.common.exception.UtilException;

public class Util {

	public static String generateHashMD5(String text) throws UtilException {
		String result = "";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");

			md.update(text.getBytes());
			byte[] hashMd5 = md.digest();
			result = stringHexa(hashMd5);

		} catch (NoSuchAlgorithmException e) {
			throw new UtilException(
					"Não foi possível codificar o texto solicitado.", e);
		}

		return result;
	}

	private static String stringHexa(byte[] bytes) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
			int parteBaixa = bytes[i] & 0xf;
			if (parteAlta == 0)
				s.append('0');
			s.append(Integer.toHexString(parteAlta | parteBaixa));
		}
		return s.toString();
	}
}
