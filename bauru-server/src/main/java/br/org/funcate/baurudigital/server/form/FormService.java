package br.org.funcate.baurudigital.server.form;


public class FormService {

	public static Form getForm(int id) throws FormException 
	{		
		return new FormDAO().retrieve(id);
	}
}
