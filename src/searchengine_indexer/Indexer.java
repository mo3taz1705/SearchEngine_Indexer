/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package searchengine_indexer;

/**
 *
 * @author Moutaz and Omar
 */

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.tartarus.snowball.ext.englishStemmer;

public class Indexer {
    protected static final String[] frequentWords = {"a", "an", "the"};
    protected static final String[] wordTableNames = {"word", "word2"};
    
    private int createdTablePosition = 0;
    //private static final Pattern UNDESIRABLES = Pattern.compile("[][(){},.;!?<>%\\]");
    //Containing Tags
    private final int TITLE = 1, META = 2, HEADER = 3, BODY = 4;
    
    private static String processWord(String word) {
        word = Normalizer.normalize(word, Normalizer.Form.NFC); //transforms Unicode text into an equivalent composed form
        word = word.replaceAll("\\p{Punct}","");
        word = word.trim();
        word = word.replaceAll("\u00A0", "");  //remove Non-breaking space
        word = stemWord(word);
        word = word.toLowerCase();
        return word;
    }
    
    private static String stemWord(String word){
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(word);
        if(stemmer.stem()){
            return stemmer.getCurrent();
        }else{
            return word;
        }
    }
    
    public Indexer() {
        
    }
    
    public void Index(Document doc) {
        DatabaseClient databaseClient = DatabaseClient.GetClient();
        //int page_id = databaseClient.AddPage(doc.baseUri(), 0, 0);
        int page_id = databaseClient.AddPageProcedure(doc.baseUri(), 0, 0);
        createdTablePosition = createNewTable();
        
        String title = GetTagsText(doc, "title").toString();
        String meta = GetMeta(doc).toString();
        String headers = GetHeaders(doc).toString();
        RemoveHeaders(doc);
        String body = GetTagsText(doc, "body").toString();
        
//        System.out.println(doc.baseUri());
//        System.out.println(title);
//        System.out.println(meta);
//        System.out.println(headers);
        
        int nextPosition = 0;
        nextPosition = StoreWords(page_id, title, TITLE, nextPosition);
        nextPosition = StoreWords(page_id, meta, META, nextPosition);
        nextPosition = StoreWords(page_id, headers, HEADER, nextPosition);
        nextPosition = StoreWords(page_id, body, BODY, nextPosition);
        
        dropOldTable();
    }
    
    private StringBuffer GetTagsText(Document doc, String tag) {
        Elements elements = doc.getElementsByTag(tag);
        StringBuffer textBuffer = new StringBuffer();
        for (Element element : elements)
            textBuffer.append(" " + element.text());
       
        return textBuffer;
    }
    
    private StringBuffer GetTagsAttr(Document doc, String tag, String attr) {
        Elements elements = doc.getElementsByTag(tag);
        StringBuffer textBuffer = new StringBuffer();
        for (Element element : elements)
            textBuffer.append(" " + element.attr(attr));
        
        return textBuffer;
    }
    
    private StringBuffer GetMeta(Document doc) {
        Elements metas = doc.getElementsByTag("meta");
        StringBuffer textBuffer = new StringBuffer();
        for (Element meta : metas) {
            if (meta.attr("name").equals("description") || meta.attr("name").equals("keywords"))
                textBuffer.append(" " + meta.attr("content"));
        }
        return textBuffer;
    }
    
    private StringBuffer GetHeaders(Document doc) {
        
        StringBuffer textBuffer = new StringBuffer();
        for(int i = 1; i < 7; i++){
            textBuffer.append(" " + GetTagsText(doc, "h" + i));
        }
        return textBuffer;
    }
    
    private void RemoveHeaders(Document doc) {
        for(int i = 1; i < 7; i++){
            String tag = "h" + i;
            Elements headerElements = doc.getElementsByTag(tag);
            headerElements.remove();
        }
        
    }
    
    private int GetOutboundLinks(Document doc){
        int outboundLinksNumber = 0;
        Elements outboundLinks = doc.getElementsByTag("a");
        for(Element outboundLink : outboundLinks){
            String href = outboundLink.attr("href");
            if( href.length() > 1 && !(href.charAt(0)=='#') )
                outboundLinksNumber++;
        }
        return outboundLinksNumber;
    }
    
    private int StoreWords(int page_id, String words, int containing_tag, int position /*start position*/) {
        DatabaseClient databaseClient = DatabaseClient.GetClient();
        Scanner scanner = new Scanner(words);
        while (scanner.hasNext()) {
            String word = scanner.next();
            word = Indexer.processWord(word);
            
            if (word.length() == 0) continue;
            
            boolean isFrequent = false;
            for (String frequentWord : Indexer.frequentWords) {
                if (word.equals(frequentWord)) {
                    isFrequent = true;
                    break;
                }
            }
            if (isFrequent) continue;
            
            databaseClient.AddWord(word, page_id, position, containing_tag, wordTableNames[createdTablePosition]);
            position += 1;
        } 
        return position;
    }
    
    private int createNewTable(){
        DatabaseClient databaseClient = DatabaseClient.GetClient();
        
        if(!databaseClient.isTableExist(wordTableNames[0])){
            databaseClient.createTable(wordTableNames[0]);
            return 0;
        }else if(!databaseClient.isTableExist(wordTableNames[1])){
            databaseClient.createTable(wordTableNames[1]);
            return 1;
        }else{  //in case both tables are exist
            databaseClient.dropTable(wordTableNames[0]);
            databaseClient.dropTable(wordTableNames[1]);
            
            databaseClient.createTable(wordTableNames[0]);
            return 0;
        }
    }
    
    private void dropOldTable(){
        DatabaseClient databaseClient = DatabaseClient.GetClient();
        
        databaseClient.dropTable(wordTableNames[1 - createdTablePosition]);
    }
}