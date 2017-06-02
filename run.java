package ccim.iar.ui.main;


import java.util.ArrayList;     // https://www.tutorialspoint.com/java/util/java_util_arraylist.htm
import java.util.Date;			// https://www.tutorialspoint.com/java/util/java_util_arraylist.htm
import org.apache.logging.log4j.LogManager;   		//https://l;ogging.apache.org/log4j/2.x/log4j-api/apidocs/org/apache/logging/log4j/LogManager.html
													// examples: http://www.programcreek.com/java-api-examples/index.php?api=org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;



 


// import other classes we wrote so we can call them    
import ccim.iar.ui.screen.Assessment;
import ccim.iar.ui.screen.Person;
import ccim.iar.ui.screen.SearchCriteria;
import ccim.iar.ui.screen.User;
import ccim.iar.ui.screen.util;

public class run {

	//public static Properties config_properties = new Properties(); // define config_properties to be of the tyoe Properties
	private static final Logger log = LogManager.getLogger(run.class); // reason for private final variable is that it CANNOT be accessed from any anonymous inner subclass

	public static void main(String[] args) throws Exception { //THROWS: any excewption that;s thrown out must be specifies
															  
		
		//InputStream input = new FileInputStream(configfile); // this calls config.properties to get the input
															// This creates a FileInputStream by opening a connection to an actual file (config.properties) named by the path name configfile in the file system.
		
		//config_properties.load(input); // first we defined the property (line: public static Properties config_properties = new Properties(); )
									   // then we import the file (line: InputStream input = new FileInputStream(configfile))
									   // loan the data from the file to a defined variable (line config_properties.load(input);)
									   // then close it by next line
		//input.close();

		// define more public variables who are the input from the file we input
		int testDurationMinutes = 10; 
		int testCycles = 5;
		// refer to the  util class, and call the function with the class util using the input
		util.thinkTime = 0;
		util.screenshotFolder ="screenshots";
		util.takeScreenshot = util.getBoolean("no", false);
		
		// this log.info creates the logs below in the console window
		log.info("Starting test case_" + (new Date()));
		log.info("test Duration (minutes) = " + testDurationMinutes + ", test Cycles = " + testCycles);
		
		// set the property of the system 
		// webdriver.ie.driver is now set to whatever the input is after webdriver.ie.driver= (in config.properties)
		System.setProperty("webdriver.chrome.driver", "C:/Selenium_test_case/cmdline/Selenium/DriverServer/IEDriverServerWin32.exe");


		WebDriver driver=new ChromeDriver();

		try {
			
		
		//* init
		User user = new User();
		Person person = new Person();
		Assessment assessment = new Assessment();

		//* user login
		// using the function in user.java
		user.login(driver,"10.21.202.123","Serena", "Seren@2017");
		util.takeScreenshot(driver, "user.login");

		//* person search
		// using the function in Person,java 
		SearchCriteria sc = new SearchCriteria();
		sc.addField(SearchCriteria.USE_HCN, "YES");
		sc.addField(SearchCriteria.HCN, "8058101455");
		sc.addField(SearchCriteria.LAST_NAME, "Skova");
		sc.addField(SearchCriteria.FIRST_NAME, "Linele");
		sc.addField(SearchCriteria.DOB, "30-Apr-1980");
		sc.addField(SearchCriteria.SEX, "F");
		sc.addField(SearchCriteria.STREET,"37 Linele Street");
		sc.addField(SearchCriteria.POSTAL_CODE, "Y2N 6L2");
		sc.addField(SearchCriteria.UNIT, "211");
		sc.addField(SearchCriteria.PHONE, "411-678-975");
		
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
			//if we find anyone

			// person details
			ArrayList<ArrayList<String>> assessmentList = person.getDetails(driver, 0);
			util.takeScreenshot(driver, "person.details");

			int totalassessments = assessmentList.size();

			if (totalassessments > 0) //there is at least one assessment
				{
				// starts test case cycles
				String details = Assessment.DETAIL;
				for (int testi = 0; testi < totalassessments; testi++) {

					long currentTime = System.currentTimeMillis();
					log.debug("test(" + (testi + 1) + "), time to finish (sec) = " + (endTime - currentTime) / 1000);
					if (currentTime > endTime)
						break;
	 				int irow = testi % totalassessments;
		//			int count = assessmentList.get(irow).size(); // 5,6,7

					details = (details == Assessment.DETAIL) ? details = Assessment.SUMMARY
							: (details == Assessment.SUMMARY && testi== 7) ? Assessment.OUTCOMES : Assessment.DETAIL;
					assessment.view(driver,irow, details);

					//util.takeScreenshot(driver, "view." + details + "_cycle_" + (iCycle + 1));
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
