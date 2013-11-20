package br.org.funcate.baurudigital.server.util;

import java.io.IOException;
import java.util.Properties;

import br.org.funcate.baurudigital.user.controller.exception.ConfigException;



public class PropertiesReader {
		
		public static String getProperty(String name) throws ConfigException
		{
			Properties p = new Properties();
			try {

				p.load(PropertiesReader.class.getClassLoader().getResourceAsStream("br/org/funcate/baurudigital/server/util/Application.properties"));
			} catch (IOException e) {
				throw new ConfigException("Não foi possível obter arquivo de configuração da aplicação.", e);
			}
			return p.getProperty(name);
		}

}
