/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package searchengine_indexer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 *
 * @author Moutaz & Omar
 */
public class SearchEngine_Indexer {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        // TODO code application logic here
        Indexer indexer = new Indexer();
        File input = new File("docTest.htm");
        Document doc = null;
        try {
            doc = Jsoup.parse(input, "UTF-8", "www.whatever.com");
        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        indexer.Index(doc);
        
    }
    
}
