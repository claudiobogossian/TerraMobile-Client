package br.org.funcate.baurudigital.server.util;

import java.io.IOException;
import java.util.Properties;

import br.org.funcate.baurudigital.server.model.exception.ConfigException;



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
		/*
try {
        uri = url.toURI();
} catch (URISyntaxException e1) {
        e1.printStackTrace();
        JOptionPane.showMessageDialog(null,
                        "Falha ao carregar o arquivo .properties");
}

file = new File(uri);
fis = null;

try {
        fis = new FileInputStream(file);
        properties.load(fis);
        fis.close();
} catch (IOException e) {
        System.err.println(e.getMessage());
        JOptionPane.showMessageDialog(null,
                        "Falha ao ler o arquivo .properties");
}*/

}
