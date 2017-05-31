package ccim.iar.ui.main;


//import some java package class to use their function for our convenience
import java.io.FileInputStream; // https://www.tutorialspoint.com/java/io/java_io_fileinputstream.htm
import java.io.InputStream;     // https://www.tutorialspoint.com/java/io/inputstream_read.htm
import java.util.ArrayList;     // https://www.tutorialspoint.com/java/util/java_util_arraylist.htm
import java.util.Date;			// https://www.tutorialspoint.com/java/util/java_util_arraylist.htm
import java.util.Properties;	// https://www.tutorialspoint.com/java/util/java_util_properties.htm


import org.apache.logging.log4j.LogManager;   		//https://l;ogging.apache.org/log4j/2.x/log4j-api/apidocs/org/apache/logging/log4j/LogManager.html
													// examples: http://www.programcreek.com/java-api-examples/index.php?api=org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

// import other classes we wrote so we can call them    
import ccim.iar.ui.screen.Assessment;
import ccim.iar.ui.screen.Person;
import ccim.iar.ui.screen.SearchCriteria;
import ccim.iar.ui.screen.User;
import ccim.iar.ui.screen.util;

public class run {

	public static Properties config_properties = new Properties(); // define config_properties to be of the tyoe Properties
	private static final Logger log = LogManager.getLogger(run.class); // reason for private final variable is that it CANNOT be accessed from any anonymous inner subclass

	public static void main(String[] args) throws Exception { //THROWS: any excewption that;s thrown out must be specifies
															  //it is a checked exception and if you don't handle that particular exception, it must be specified in the throws clause, 
		String configfile = args[0];  // args[] contains the supplied command-line arguments as an array or string objects
									  // define string (type) configfile to be the first command line
		
		InputStream input = new FileInputStream(configfile); // this calls config.properties to get the input
															// This creates a FileInputStream by opening a connection to an actual file (config.properties) named by the path name configfile in the file system.
		
		config_properties.load(input); // first we defined the property (line: public static Properties config_properties = new Properties(); )
									   // then we import the file (line: InputStream input = new FileInputStream(configfile))
									   // loan the data from the file to a defined variable (line config_properties.load(input);)
									   // then close it by next line
		input.close();

		// define more public variables who are the input from the file we input
		int testDurationMinutes = Integer.parseInt(config_properties.getProperty("test.duration")); 
		int testCycles = Integer.parseInt(config_properties.getProperty("test.cycles"));
		// refer to the  util class, and call the function with the class util using the input
		util.thinkTime = Integer.parseInt(config_properties.getProperty("think.time"));
		util.screenshotFolder = config_properties.getProperty("screenshot.folder");
		util.takeScreenshot = util.getBoolean(config_properties.getProperty("take.screenshots"), false);
		
		// this log.info creates the logs below in the console window
		log.info("Starting test case_" + (new Date()));
		log.info("test Duration (minutes) = " + testDurationMinutes + ", test Cycles = " + testCycles);
		
		// set the property of the system 
		// webdriver.ie.driver is now set to whatever the input is after webdriver.ie.driver= (in config.properties)
		System.setProperty("webdriver.ie.driver", config_properties.getProperty("webdriver.ie.driver"));
		WebDriver driver = new InternetExplorerDriver();

		try {
			
		
		//* init
		User user = new User();
		Person person = new Person();
		Assessment assessment = new Assessment();

		//* user login
		// using the function in user.java
		user.login(driver, config_properties.getProperty("portal.domain"),
				config_properties.getProperty("portal.username"), config_properties.getProperty("portal.password"));
		util.takeScreenshot(driver, "user.login");

		//* person search
		// using the function in Person,java 
		SearchCriteria sc = new SearchCriteria();
		sc.addField(SearchCriteria.USE_HCN, config_properties.getProperty("search.use_hcn"));
		sc.addField(SearchCriteria.HCN, config_properties.getProperty("search.hcn"));
		sc.addField(SearchCriteria.LAST_NAME, config_properties.getProperty("search.last_name"));
		sc.addField(SearchCriteria.FIRST_NAME, config_properties.getProperty("search.first_name"));
		sc.addField(SearchCriteria.DOB, config_properties.getProperty("search.dob"));
		sc.addField(SearchCriteria.SEX, config_properties.getProperty("search.sex"));
		sc.addField(SearchCriteria.STREET, config_properties.getProperty("search.street"));
		sc.addField(SearchCriteria.POSTAL_CODE, config_properties.getProperty("search.postal_code"));
		sc.addField(SearchCriteria.UNIT, config_properties.getProperty("search.unit"));
		sc.addField(SearchCriteria.PHONE, config_properties.getProperty("search.phone"));
		
		// make the change in the system 
		// name the current time as starttime, notes thie returns the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC(coordinated universal time).
		long startTime = System.currentTimeMillis();
		// name the endtime as the starttime + that, (notes thie is an input from config.properties), 
		// since the start time in measured in millisecond, the testDurationMinutes is measured in minutes, we need to * 60 (into second* 1000 
		long endTime = startTime + testDurationMinutes * 60 * 1000;
		log.debug("startTime = " + startTime);
		log.debug("endTime = " + endTime);

		int persons = person.search(driver, sc);
		util.takeScreenshot(driver, "person.search");

		if (persons > 0) {

			// person details
			ArrayList<ArrayList<String>> assessmentList = person.getDetails(driver, 0);
			util.takeScreenshot(driver, "person.details");

			int assessments = assessmentList.size();

			if (assessments > 0) {

				// start test case cycles
				String details = Assessment.DETAIL;
				for (int iCycle = 0; iCycle < testCycles; iCycle++) {

					long currentTime = System.currentTimeMillis();
					log.debug("Cycle = " + (iCycle + 1) + ", time to finish (sec) = " + (endTime - currentTime) / 1000);
					if (currentTime > endTime)
						break;
					int irow = iCycle % assessments;
					int count = assessmentList.get(irow).size(); // 5,6,7

					details = (details == Assessment.DETAIL) ? details = Assessment.SUMMARY
							: (details == Assessment.SUMMARY && count == 7) ? Assessment.OUTCOMES : Assessment.DETAIL;
					assessment.view(driver, irow, details);

					util.takeScreenshot(driver, "view." + details + "_cycle_" + (iCycle + 1));
					assessment.backToAssessmentListing(driver);
				}
			}
		}

		// Logout
		user.logout(driver);

		// Close the browser
		driver.quit();

		log.info("Test case finished " + (new Date()));
		
		} catch (Exception ex) {
			log.error("Exception: " + ex.getMessage());
			log.error(ex);
			driver.quit();
		}

	}
}
