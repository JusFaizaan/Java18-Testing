package uk.ac.mmu.advprog.hackathon;
import static spark.Spark.get;
import static spark.Spark.port;

import java.util.ArrayList;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles the setting up and starting of the web service
 * You will be adding additional routes to this class, and it might get quite large
 * Feel free to distribute some of the work to additional child classes, like I did with DB
 * @author You, Mainly!
 */
public class AMIWebService {


	
	/**
	 * Main program entry point, starts the web service
	 * @param args not used
	 */
	public static void main(String[] args) {		
		port(8088);
		
		//Simple route so you can check things are working...
		//Accessible via http://localhost:8088/test in your browser
		get("/test", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					return "Number of Entries: " + db.getNumberOfEntries();
				}
			}			
		});
		
		
		get("/lastsignal", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					String Motorway = request.queryParams("signal_id"); 
					String result = db.getLastSignalDisplayed(Motorway);
					if (result != null) {						
						return result;
					}
				}
				return "No results"; 
			}			
		});
		
		
		
		System.out.println("Server up! Don't forget to kill the program when done!");
		
		get("/frequentlyused", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				response.type("text.xml"); 
				try (DB db = new DB()) {
					//While showing that these values appear not to be used removing the following three lines causes the web service to not function
					String MotorwayFreq = request.queryParams("motorway");  
					ArrayList<String> result = db.frequentlyUsed(MotorwayFreq); 
					ArrayList<String> result2 = db.valueUsed(MotorwayFreq); 
					return db.xmlFormatting().toString();
				} 
			}			
		});
				
		
	}

}
