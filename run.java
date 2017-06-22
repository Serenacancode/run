package ccim.iar.ui.main;

import java.util.ArrayList;
import java.util.Date;			// https://www.tutorialspoint.com/java/util/java_util_arraylist.htm

import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.LogManager;   		//https://l;ogging.apache.org/log4j/2.x/log4j-api/apidocs/org/apache/logging/log4j/LogManager.html
													// examples: http://www.programcreek.com/java-api-examples/index.php?api=org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import ccim.iar.ui.screen.Assessment; 
import ccim.iar.ui.screen.PageMap;
import ccim.iar.ui.screen.Person;
import ccim.iar.ui.screen.SearchCriteria;
import ccim.iar.ui.screen.SearchResults;
import ccim.iar.ui.screen.User;
import ccim.iar.ui.screen.report;
import ccim.iar.ui.screen.util;


public class run { 
	//public static Properties config_properties = new Properties(); // define config_properties to be of the tyoe Properties
	private static final Logger log = LogManager.getLogger(run.class); // reason for private final variable is that it CANNOT be accessed from any anonymous inner subclass
	private long startTime;
	 
	public WebDriver driver = null;

	public WebDriver getDriver() {
		return driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

 

	public static void main(String[] args) throws Exception {  

		run run = new run();
		try {
			run.init("C:/Projects/testcase/SOAPUI/run.properties");	
 
			// pageMap.getPageMap(driver);
			PageMap pageMap = new PageMap(true,true,true,userinput.pagesource_folder);
			
			WebDriver driver = run.getDriver();
			driver.manage().window().maximize();
			
			User user = new User();	//* user login 
			user.login(driver,userinput.portal_domain,userinput.portal_username,userinput.portal_password);
			   /*String pmapfilename = "Iframe__2__2__1__0__.html";
			        log.debug("... getting to : " + pmapfilename);
			        if (PageMap.gotoFrame(driver, "Iframe__2__2__1__0__.html", "1")) {
			                        //driver.switchTo().frame("SearchFilter");
			                        //PageMap.continuetoFrame(driver,"0");
			                        System.out.println(driver.findElement(By.tagName("Body")).getAttribute("outerHTML"));
			                        //String frameSrc = driver.getAttribute("src");
			                        log.debug("Done. Success");
			                        
			        } else {
			                        log.debug("Done. gotoFrame unsuccess");
			        }
					*/
		
	if (userinput.upload_assessment)
			 
		 	{log.info("start uploading assessment");
			if (report.upload(driver)) 
			System.out.println("uploaded assessment " + Assessment.Transaction_id+", obtained infomation");}
  		
		if (userinput.upload_assessment||userinput.search_person)	
		{	
			log.info("start searching person"); 
			SearchCriteria searchcriteria = new SearchCriteria();
			searchcriteria.addField(SearchCriteria.USE_HCN, userinput.search_use_hcn);
			searchcriteria.addField(SearchCriteria.HCN, userinput.search_hcn);
			searchcriteria.addField(SearchCriteria.LAST_NAME, userinput.search_last_name);
			searchcriteria.addField(SearchCriteria.FIRST_NAME, userinput.search_first_name);
			searchcriteria.addField(SearchCriteria.DOB, userinput.search_dob);
			searchcriteria.addField(SearchCriteria.SEX, userinput.search_sex);
			searchcriteria.addField(SearchCriteria.STREET,userinput.search_street);
			searchcriteria.addField(SearchCriteria.POSTAL_CODE, userinput.search_postal_code);
			searchcriteria.addField(SearchCriteria.UNIT, userinput.search_unit);
			searchcriteria.addField(SearchCriteria.PHONE, userinput.search_phone);
 
		
			//first search the person 
			 Person person = new Person();
			 int persons = person.search(driver, searchcriteria);        
			 if (persons > 0) 			
				{ //click the first person
					util.logActionStart(driver, "click on the first person");
					driver.switchTo().defaultContent(); // you are now outside frames
					driver.switchTo().frame("ConcertoContext");
					driver.switchTo().frame("application-content");
					driver.switchTo().frame(0);
					driver.switchTo().frame("SearchResults");//		
					log.info("going to click the first row");// WebElement searchResultsBody = driver.findElement(By.id("search-results-body"));
					WebElement searchResultsRow = driver.findElement(By.id("row0"));
					searchResultsRow.click();						
					// To get more details about the person, we first store all the assessment detail of this person. 	
						
					if (!person.getDetails(driver, 0)) {
							log.error(person.getErrorMessage());
							return;
						}
					
					SearchResults xx = new SearchResults();
				
					log.info("working on xx");
					xx = person.assessmentList;
					log.info(xx);
					
					util.takeScreenshot(driver, "person.details");		
					int totalassessments = xx.resultRows();				
					log.info("find " + totalassessments+ " assessment(s)");
					
					if (totalassessments > 0)  
					{
					for (int testi = 0; testi < totalassessments; testi++) 
					{// starts test case cycles
						
						int irow = testi % totalassessments;
						String details = Assessment.DETAIL;
						WebElement t = driver.findElement(By.cssSelector("#row"+testi+" > td.yui-dt-first > div"));
					if ( t.getText().contains("CHA"))
					{			log.info("Viewing CHA details");
		
								details = (details == Assessment.DETAIL) ? details = Assessment.SUMMARY
										: (details == Assessment.SUMMARY && testi== 7) ? Assessment.OUTCOMES : Assessment.DETAIL;
								log.info("Viewing assessment");
								Assessment.view(driver,irow, details);
								Assessment.viewall(driver,userinput.assessment_viewall||userinput.upload_assessment);
								Assessment.printpage(driver,userinput.assessment_print); 
								util.takeScreenshot(driver, "view." + details + "_cycle_" + (testi + 1));
								Assessment.backToAssessmentListing(driver);
								}
					
					if ( t.getText().contains("PS"))				
					{
								log.info("Viewing PS details");
								Assessment.view(driver,irow, Assessment.DETAIL);
								Assessment.viewall(driver,userinput.assessment_viewall||userinput.upload_assessment);
								Assessment.printpage(driver,userinput.assessment_print); 
								util.takeScreenshot(driver, "view." + details + "_cycle_" + (testi + 1));
								Assessment.backToAssessmentListing(driver);}
					

					 
			 }
					}}
			 
		/*};
		
					
					
					
					 
					if (person.isSuccess())
				 
					//util.takeScreenshot(driver, "person.details");		
			 
					 // save/store the list of assessment,
 
					{
												
						int totalassessments = person.assessmentList.resultRows();			
						log.info("find " + totalassessments+ " assessment(s)");
					
						if (totalassessments > 0)   // if there is at least one assessment, view them one by one and go back to the list after viewing.
							{
							
							int i;
							for ( i = 0; i < totalassessments; i ++)
							{
								if(person.getDetails(driver, i))
									log.info("stored test "+ i);
							}
							log.info("stored all the search result"); 
								for (int testi = 0; testi < totalassessments; testi++) 
								{
									
									String details = Assessment.DETAIL;
								WebElement t = driver.findElement(By.cssSelector("#row"+testi+" > td.yui-dt-first > div"));
						
								if ( t.getText().contains("CHA"))
								{			log.info("Viewing CHA details"); 
											details = (details == Assessment.DETAIL) ? details = Assessment.SUMMARY
												: (details == Assessment.SUMMARY && testi== 7) ? Assessment.OUTCOMES : Assessment.DETAIL;
											log.info("Viewing assessment");
											Assessment.view(driver,testi, details);
											util.takeScreenshot(driver, "view." + details + "_cycle_" + (testi + 1));
											Assessment.backToAssessmentListing(driver);}
							
								else if ( t.getText().contains("PS"))				
								{ 			log.info("Viewing PS details");
											Assessment.view(driver,testi, Assessment.DETAIL);
											util.takeScreenshot(driver, "view." + details + "_cycle_" + (testi + 1));
											Assessment.backToAssessmentListing(driver);}
								
								else log.info("test " +testi +"open failed");
								}
								log.info("ended loop");
							}
						else log.info("no assessments found");
						log.info("Number Of Assessments: " + totalassessments);
						log.debug("Assessment List: " + person.assessmentList.getResults());
						log.debug("Assessment Details: " + person.assessmentList.getResultDetails());
		
			}	
					if (persons > 0) {
						if (!person.getDetails(driver, 0)) {
							log.error(person.getErrorMessage());
							return;
						}
						// pageMap.getPageMap(driver); // get page map
					}*/ else {
						return;					}					};
		
		log.info("logging out");
		user.logout(driver);// Logout
 																
	    }catch (Exception ex) {
	    	log.error("Exception: " + ex.getMessage());
	    	log.error(ex);
		}
		run.cleanup();
	}
	public void init (String configfile ) throws Exception {

		 
		//InputStream input = new FileInputStream(configfile); // this calls config.properties to get the input
// This creates a FileInputStream by opening a connection to an actual file (config.properties) named by the path name configfile in the file system.

//config_properties.load(input); // first we defined the property (line: public static Properties config_properties = new Properties(); )
							   // then we import the file (line: InputStream input = new FileInputStream(configfile))
							   // loan the data from the file to a defined variable (line config_properties.load(input);)
							   // then close it by next line
//input.close();
		int testDurationMinutes = userinput.test_duration;
		int testCycles = userinput.test_cycles;
		util.thinkTime = userinput.think_time;
		util.screenshotFolder =userinput.screenshot_folder;
		util.takeScreenshot = userinput.take_screenshots; 
		Date starts_time = new Date();
		log.info("Starting test case_" + starts_time);
		log.info("test Duration (minutes) = " + testDurationMinutes + ", test Cycles = " + testCycles);
		log.info("Starting test case_" + starts_time);
		// set the property of the system 
		// webdriver.ie.driver is now set to whatever the input is after webdriver.ie.driver= (in config.properties)
		System.setProperty("webdriver.ie.driver",userinput.webdriver_ie_driver);
		log.info("webdriver.ie.driver: " + System.getProperty("webdriver.ie.driver"));
		// make the change in the system 
		// name the current time as start time, notes this returns the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC(coordinated universal time).
		WebDriver driver = new InternetExplorerDriver();  
		setDriver(driver);
		
		
		startTime = System.currentTimeMillis();
		 long endTime = startTime + testDurationMinutes * 60 * 1000;
		log.debug(startTime);
		log.debug(endTime); 
		log.info("webdriver.ie.driver: " + System.getProperty("webdriver.ie.driver"));
		
		log.info("initilize done...");
		
		
		
		
		




	}

	
	public void cleanup() throws Exception {
		driver.close();// Close the browser
		long actualendtime = System.currentTimeMillis();
		long total_time =( actualendtime - startTime)/1000;		
		log.info("Test case finished " + new Date());
		log.info("Test duration: " + total_time + "s");
		
	
	}
	
	
}
