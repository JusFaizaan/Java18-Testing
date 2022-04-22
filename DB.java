package uk.ac.mmu.advprog.hackathon;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles database access from within your web service
 * @author You, Mainly!
 */
public class DB implements AutoCloseable {
	
	//allows us to easily change the database used
	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/AMI.db";
	
	//Allow us to access ArrayLists and store the necessary data
	private ArrayList<String> xmlFrequency = new ArrayList<>(); 
	private ArrayList<String> xmlValue = new ArrayList<>(); 
	
	//allows us to re-use the connection between queries if desired
	private Connection connection = null;
	
	/**
	 * Creates an instance of the DB object and connects to the database
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
		}
		catch (SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Returns the number of entries in the database, by counting rows
	 * @return The number of entries in the database, or -1 if empty
	 */
	public int getNumberOfEntries() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM ami_data");
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	
	/**
	 * Returns the final signal produced by filtering out the unnecessary data
	 * @return The final signal produced
	 * @param RequestSignalID representing the ArrayList for the function
	 */
	public String getLastSignalDisplayed(String RequestSignalID){
		String LastSignal= null; 
		try {
			PreparedStatement s = connection.prepareStatement("SELECT signal_value FROM ami_data WHERE signal_id = ? AND NOT signal_value = 'OFF'AND NOT signal_value = 'NR' AND NOT signal_value = 'BLNK' ORDER BY datetime DESC LIMIT 1;");
			s.setString(1, RequestSignalID);
			ResultSet results = s.executeQuery(); 
			while(results.next()) { 
				LastSignal = results.getString("signal_value"); 
				
			}
			
		}
		catch (SQLException sqle) {
			error(sqle);
			
		} 
		return LastSignal;

	}
	
	/**
	 * Returns the most frequently used signal by filtering through the signals and passes it into the xmlFrequency ArayList
	 * @return The most frequent signals
	 * @param mainFrequency Representing the ArrayList for the function- Applies the correct digits in the SQL query
	 */
	public ArrayList<String> frequentlyUsed(String mainFrequency){
		String Frequency = null; 
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(signal_value) AS frequency, signal_value FROM ami_data WHERE signal_id LIKE '"+mainFrequency+"%' GROUP BY signal_value ORDER BY frequency DESC;"); 
			while(results.next()) { 
			Frequency = results.getString("frequency");
			xmlFrequency.add(Frequency); 
			}
			
		}
		catch (SQLException sqle) {
			error(sqle);
			
		} 
		return xmlFrequency;

	}
	
	/**
	 * Returns the values used by filtering through the signals and passes it into the xmlValue ArrayList
	 * @return The values used 
	 * @param mainValue Used to represent the ArrayList for the function- Applies the correct digits in the SQL query
	 */
	public ArrayList<String>valueUsed(String mainValue){ 
		String Frequency = null; 
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(signal_value) AS frequency, signal_value FROM ami_data WHERE signal_id LIKE '"+mainValue+"%' GROUP BY signal_value ORDER BY frequency DESC;"); 
			while(results.next()) { 
			Frequency = results.getString("signal_value");
			xmlValue.add(Frequency); 
			}
			
		}
		catch (SQLException sqle) {
			error(sqle);
			
		} 
		return xmlValue; 

	}
	
	
	/**
	 * Takes data from the xmlFrequency and xmlValue ArrayLists and adds them to the corresponding nodes
	 * @return The data from the ArrayLists into an XML format
	 */
	public Writer xmlFormatting() {
		Writer xmlCode = null;
		DocumentBuilderFactory xml = DocumentBuilderFactory.newInstance();
		try {
			Document document = xml.newDocumentBuilder().newDocument();
			Element SignalT = document.createElement("SignalTrends");
			document.appendChild(SignalT);

			for(int i= 0; i<xmlValue.size(); i++) {
				Element Signal = document.createElement("Signal");
				SignalT.appendChild(Signal);
				
				Element Value = document.createElement("Value");
				Signal.appendChild(Value); 
				Value.setTextContent(xmlValue.get(i));

				Element Frequency = document.createElement("Frequency");
				Signal.appendChild(Frequency);
				Frequency.setTextContent(xmlFrequency.get(i));
			}
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Writer output = new StringWriter();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(output));
			xmlCode = output;  
			
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xmlCode; 
		
		
		
		
	}
	
	
	/**
	 * Closes the connection to the database, required by AutoCloseable interface.
	 */
	@Override
	public void close() {
		try {
			if ( !connection.isClosed() ) {
				connection.close();
			}
		}
		catch(SQLException sqle) {
			error(sqle);
		}
	}

	/**
	 * Prints out the details of the SQL error that has occurred, and exits the programme
	 * @param sqle Exception representing the error that occurred
	 */
	private void error(SQLException sqle) {
		System.err.println("Problem Opening Database! " + sqle.getClass().getName());
		sqle.printStackTrace();
		System.exit(1);
	}
}
