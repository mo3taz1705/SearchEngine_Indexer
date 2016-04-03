package apt.search.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Crawler{
	
	private int number_of_threads;
		
	public Crawler(int number_of_threads) throws InterruptedException, ExecutionException{
		this.number_of_threads = number_of_threads;
	}
	
	public void startCrawling() throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(number_of_threads);
		CrawlerWorker worker =  new CrawlerWorker();
		
		for(int i=0; i< number_of_threads; i++){
			pool.execute(worker);
		}
		
		pool.shutdown();
		pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
		
	}
	
	public void refreshCrawling() throws InterruptedException{
		ExecutorService pool = Executors.newFixedThreadPool(number_of_threads);
		CrawlerWorker worker =  new CrawlerWorker(1);
		
		for(int i=0; i< number_of_threads; i++){
			pool.execute(worker);
		}
		
		pool.shutdown();
		pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);		
	}
	
}
