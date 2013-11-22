package br.org.funcate.baurudigital.tools.extraction.domain.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.org.funcate.baurudigital.server.address.Address;

public class SourceAddressDAO {

	public List<Address> getAddressByBlock(String blockId)
	{
		List<Address> addressList = new ArrayList<Address>();
		 try {
			 	Class.forName("net.sourceforge.jtds.jdbc.Driver");
			 
		        Connection con = DriverManager.getConnection("jdbc:jtds:sqlserver://geodb4;instance=sql2012;DatabaseName=Bauru_420","sa","S4geo");
		        Statement stmt = con.createStatement();
		        ResultSet rs = stmt.executeQuery("select * from view_fct_endereco_imovel_bogo where imo_inscricao like '"+blockId+"%'");
		        
		        while (rs.next()) {
		        	
		        	Address address = new Address(); 
		        			
		        	//address.setIdImo(rs.getInt("imo_id"));     
		        	//address.setIdLog(rs.getInt("log_id"));
		        	//address.setCodLog(rs.getString("log_codlogradouro"));
		        	address.setFeatureId(rs.getString("imo_inscricao"));
		        	address.setName(rs.getString("endereco"));
		        	address.setNumber(rs.getString("numero"));
		        	address.setPostalCode(rs.getString("cep"));      
		        	address.setExtra(rs.getString("complemento"));
		        	address.setState(rs.getString("estado"));		        	                                              
		        	address.setCity(rs.getString("municipio"));
		        	address.setNeighborhood(rs.getString("bairro"));
		        	addressList.add(address);
				}
		        
		    } catch(SQLException e) {
		        e.printStackTrace();
		    } catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return addressList;
	}

}