package org.ndslabs.selenium;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class DataverseDriver implements Runnable{

	public static void main(String[] args) throws Exception {
				
		String host = args[0];
		String nsmap = args[1];
		int iterations = Integer.parseInt(args[2]);
		int sleep = Integer.parseInt(args[3]);
		String file = args[4];
		
		List<String> namespaces = FileUtils.readLines(new File(nsmap), "UTF-8");
		
		List<Thread> threads = new ArrayList<Thread>();
		for (String ns: namespaces) {
			String[] nsport = ns.split(",");
			String namespace=nsport[0];
			String port = nsport[1];
			DataverseDriver dd = new DataverseDriver(host, namespace, port, iterations, sleep, file);
			Thread worker = new Thread(dd);
			worker.setName(namespace);
			worker.start();
			threads.add(worker);
		}
        for (Thread thread: threads) {
            thread.join();
        }
	}

	String namespace = "";
	String port = "";
	int iterations = 0;
	int sleep = 0;
	String host = "";
	String file = "";
	public DataverseDriver(String host, String namespace, String port, int iterations, int sleep, String file) {
		this.namespace =namespace;
		this.port = port;
		this.iterations = iterations;
		this.sleep = sleep;
		this.host = host;
		this.file = file;
	}
	
	public void run() {
		for (int i=0; i<iterations; i++) {
			StopWatch sw = new StopWatch();

			try {
				sw.start();
				runTest(namespace, port, i);					
				sw.stop();

				System.out.println(namespace + "," + i + ",total," + sw.getTime());
			} catch (Exception e) {
				System.out.println(namespace + "," + i + ",error," + e.getMessage());
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(sleep*1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void runTest(String namespace, String port, int test) throws Exception {
		// System.setProperty("webdriver.firefox.profile", "default");
		//WebDriver driver = new FirefoxDriver();
		WebDriver driver = new ChromeDriver();

		try
		{
			String baseUrl = "http://" + host + ":" + port;
			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
	
			WebDriverWait wait = new WebDriverWait(driver, 60);
	
			StopWatch sw = new StopWatch();
			sw.start();		
			driver.navigate().to(baseUrl);
			sw.stop();
			System.out.println(namespace + "," + test + ",navigate," + sw.getTime());
			
			// Login
			Thread.sleep(1000);
			sw.reset();
			sw.start();		
			driver.findElement(By.id("loginForm:credentialsContainer2:0:credValue")).clear();
			driver.findElement(By.id("loginForm:credentialsContainer2:0:credValue")).sendKeys("dataverseAdmin");
			driver.findElement(By.id("loginForm:credentialsContainer2:1:sCredValue")).clear();
			driver.findElement(By.id("loginForm:credentialsContainer2:1:sCredValue")).sendKeys("admin");
	
			driver.findElement(By.id("loginForm:login")).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[@type='button'])[8]")));
			sw.stop();
			System.out.println(namespace + "," + test + ",login," + sw.getTime());
	
	
			// New dataset
			Thread.sleep(1000);
			sw.reset();
			sw.start();
			driver.findElement(By.xpath("(//button[@type='button'])[8]")).click();
			driver.findElement(By.linkText("New Dataset")).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.id("datasetForm:save")));
			sw.stop();
			System.out.println(namespace + "," + test + ",new," + sw.getTime());
	
			Thread.sleep(1000);
			sw.reset();
			sw.start();
			driver.findElement(By.xpath("//span[@id='pre-input-title']/following-sibling::input[@type='text']")).clear();
			driver.findElement(By.xpath("//span[@id='pre-input-title']/following-sibling::input[@type='text']"))
					.sendKeys("Test"+ test);
	
			driver.findElement(By.xpath("//span[@id='pre-input-dsDescription']/following-sibling::textarea")).clear();
			driver.findElement(By.xpath("//span[@id='pre-input-dsDescription']/following-sibling::textarea"))
					.sendKeys("Test");
	
			WebElement checkbox = driver.findElement(By.xpath("//label[text()='Other']"));
			Actions hover = new Actions(driver);
			hover.moveToElement(checkbox);
			hover.click();
			hover.perform();
	
			driver.findElement(By.id("datasetForm:fileUpload_input")).clear();
			// /Users/willis8/Desktop/NDSC5/Iris.jpg
			driver.findElement(By.id("datasetForm:fileUpload_input")).sendKeys(file);
	
			Thread.sleep(2000);
			wait.until(ExpectedConditions.elementToBeClickable(By.id("datasetForm:save")));
			driver.findElement(By.id("datasetForm:save")).click();
	
			wait.until(ExpectedConditions.elementToBeClickable(By.id("editDataSet")));
			sw.stop();
			System.out.println(namespace + "," + test + ",save," + sw.getTime());
			
			// Front page
			Thread.sleep(1000);
			sw.reset();
			sw.start();
			driver.findElement(By.linkText("Root Dataverse")).click();
			sw.stop();
			System.out.println(namespace + "," + test + ",home," + sw.getTime());
	
			// Logout
			Thread.sleep(1000);
			sw.reset();
			sw.start();
			WebElement menu = driver.findElement(By.id("lnk_header_account_dropdown"));
			Actions click = new Actions(driver);
			click.moveToElement(menu).click(driver.findElement(By.xpath("//a[text()='Log Out']"))).perform();
			
			sw.stop();
			System.out.println(namespace + "," + test + ",logout," + sw.getTime());
		} finally {
			driver.quit();
			
		}

	}
}
