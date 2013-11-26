package br.org.funcate.baurudigital.tools.extraction.domain.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import br.org.funcate.baurudigital.server.address.Address;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
/**
 *  This class was created to extract data from original database to this application model database
 * @author bogo
 *
 */
public class SourceAddressDAO {

	public List<Address> getAddressByBlock(String blockId) throws IOException, ParseException, MismatchedDimensionException, TransformException, FactoryException
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
/*		        	Coordinate lower = new Coordinate(rs.getDouble("lower_x"), rs.getDouble("lower_y"));
		        	Coordinate upper = new Coordinate(rs.getDouble("upper_x"), rs.getDouble("upper_y"));

		        	Coordinate[] coords = {lower, upper};
*/		        	
		        	
		        	GeometryBuilder gb = new GeometryBuilder();
		        	Polygon box = gb.box(rs.getDouble("lower_x"), rs.getDouble("lower_y"), rs.getDouble("upper_x"), rs.getDouble("upper_y"));
		        	
		        	
		        	
		        	CoordinateReferenceSystem toCRS = CRS.decode("EPSG:4326");
		            CoordinateReferenceSystem fromCRS = CRS.decode("EPSG:29192");
		            boolean lenient = true; // allow for some error due to different datums
		            MathTransform transform = CRS.findMathTransform(fromCRS, toCRS, lenient);
		        	Polygon p = (Polygon) JTS.transform(box, transform);
		        	address.setCoordx(p.getCentroid().getX());
		        	address.setCoordy(p.getCentroid().getY());
		        	/*		        	byte[] b = rs.getBytes("spatial_data");
		     
		        	WKBReader r = new WKBReader();
		        	Geometry g = r.read(b);
		        	System.out.println(g.getLength());
		        	System.out.println(g.getArea());
*/
		        	
		        	
/*		        	BufferedReader br = null;
		    		StringBuilder sb = new StringBuilder();
		     
		    		String line;
		     
	    			br = new BufferedReader(new InputStreamReader(is));
	    			while ((line = br.readLine()) != null) {
	    				sb.append(line);
	    			}
	    			
	    			System.out.println(sb);
*/		    			
		        	
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
