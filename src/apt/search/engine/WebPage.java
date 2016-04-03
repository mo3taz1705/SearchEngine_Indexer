package apt.search.engine;

import org.jsoup.nodes.Document;

public class WebPage {
	
	private Document doc;
	private String url;
	private int id;
	
	WebPage(String url, int id){
		this.url = url;
		this.id = id;
	}
	
	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	
}
