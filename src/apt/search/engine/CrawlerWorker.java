package apt.search.engine;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.*;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.trigonic.jrobotx.RobotExclusion;

import java.sql.*;

public class CrawlerWorker implements Runnable {
	
	
	public static final String pages_directory = "E:\\Crawler";
	private final String user_agent = "*";
	private ArrayList<WebPage> to_visit = new ArrayList<WebPage>();
	
	private DatabaseClient database_client = DatabaseClient.GetClient();
	
	
	public CrawlerWorker(){
		initializeSeed();
	}
	
	public CrawlerWorker(int dummy){
		refreshPages();
	}
	
	private void initializeSeed(){
		ResultSet content = database_client.getToVisit();
		try {
			while (content.next()) {
				WebPage webpage = new WebPage(content.getString("URL"), content.getInt("ID"));
				to_visit.add(webpage);
			}
		} catch (SQLException e) {
			System.out.println("InitializeSeed: " + e.getMessage());
		}
	}
	
	private void refreshPages(){
		ResultSet content = database_client.refresh();
		try {
			while (content.next()) {
				WebPage webpage = new WebPage(content.getString("URL"), content.getInt("ID"));
				to_visit.add(webpage);
			}
		} catch (SQLException e) {
			System.out.println("InitializeSeed: " + e.getMessage());
		}
	}
	
	@Override
	public void run(){
		while(!isToVisitEmpty()){
			WebPage webpage = getWebPage();
			if(webpage.getDoc() != null){
				saveFile(webpage);
				getUrls(webpage.getDoc());
			}
		}
	}
	
	private WebPage getToVisit(){
		synchronized(to_visit){
			return to_visit.remove(0);
		}
	}
	
	private  WebPage getWebPage(){
		WebPage current_webpage = getToVisit();
		String current_url =  current_webpage.getUrl();
		Document web_doc = null;
		try {
			web_doc = Jsoup.connect(current_url).userAgent(user_agent).get();
			current_webpage.setDoc(web_doc);
			database_client.updateVisited(current_webpage.getId());		
		} catch (IOException e) {
			System.out.println("Get WebPage: " + e.getMessage());
			if(e.getCause() instanceof TimeoutException){
				synchronized(to_visit){
					to_visit.add(current_webpage);
				}
			}
			
		}
		return current_webpage;
	}
	
	
	private void saveFile(WebPage webpage){
		String file_name = webpage.getId() + ".html";	
		File new_file = new File(pages_directory , file_name);
        try {
			FileUtils.writeStringToFile(new_file, webpage.getDoc().outerHtml(), "UTF-8");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void getUrls(Document web_doc) {
		Elements links = web_doc.select("a[abs:href]");
		for (int i = 0; i < links.size(); i++){
			Element new_element = links.get(i);
			String new_link = new_element.attr("abs:href");
			new_link = new_link.replace("www.", "");
			boolean priority = new_link.toLowerCase().contains(".com") | new_link.toLowerCase().contains(".net");
			if(checkIfHTML(new_element) && checkRobotTxt(new_link))
				addToVisit(new_link, priority);
		}
	}

	private boolean checkRobotTxt(String url){
		RobotExclusion robotExclusion = new RobotExclusion();
		boolean allowed = false;
		try {
			allowed = robotExclusion.allows(new URL(url), user_agent);
		} catch (MalformedURLException e) {
			System.out.println("ROBOT:" + e.getMessage());
		}
		return allowed;
	}
	
	private boolean checkIfHTML(Element url){
		Elements html = url.select("html");
		return html != null;
	}
		
	private void addToVisit(String to_visit_url, boolean priority){
		if(!database_client.exist(to_visit_url)){
			int id = database_client.insertPage(to_visit_url, priority);
			WebPage webpage = new WebPage(to_visit_url, id);
			synchronized(to_visit){
				to_visit.add(webpage);
			}
		}
	}
	
	private boolean isToVisitEmpty(){
		synchronized (to_visit){
			return to_visit.isEmpty();
		}
	}
}
