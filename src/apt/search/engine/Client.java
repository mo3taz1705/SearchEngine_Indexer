package apt.search.engine;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Client {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter 0 to crawl, 1 to refresh crawling, and anything else to index:");
		int user_input = scanner.nextInt();
		if (user_input == 0) {
			System.out.println("Enter number of threads:");
			int num_threads = scanner.nextInt();
			try {
				Crawler crawler = new Crawler(num_threads);
				crawler.startCrawling();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		else if (user_input == 1){
			System.out.println("Enter number of threads:");
			int num_threads = scanner.nextInt();
			try {
				Crawler crawler = new Crawler(num_threads);
				crawler.refreshCrawling();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		else { // Index
			Indexer indexer = new Indexer();
			String pages_directory = CrawlerWorker.pages_directory;
			indexer.IndexFilesInDirectory(pages_directory);
		}
		scanner.close();
		
	}

}
