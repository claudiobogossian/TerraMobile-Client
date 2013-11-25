package br.org.funcate.baurudigital.server.form;

/**
 * Keep's state less functions as a service class and comunicate with DAO and others entity's service.  
 * @author bogo
 *
 */
public class FormService {
	/**
	 * Get Form POJO by ID
	 * @param id 
	 * @return Requested Form
	 * @throws FormException
	 */
	public static Form getForm(int id) throws FormException 
	{		
		return new FormDAO().retrieve(id);
	}
}
