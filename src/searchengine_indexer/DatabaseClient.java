/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package searchengine_indexer;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Moutaz and Omar
 */
public class DatabaseClient {
    private static final String 
            SERVER = "jdbc:mysql://localhost/", 
            USER = "root", 
            PASSWORD = "1234", 
            DB_NAME = "search_engine_db",
            JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static DatabaseClient singletonDatabaseClient = null;
    private java.sql.Connection db_connection;
    
    private DatabaseClient(){
        ConnectToDB();
    }
    
    public static DatabaseClient GetClient() {
        if (DatabaseClient.singletonDatabaseClient == null)
            DatabaseClient.singletonDatabaseClient = new DatabaseClient();
        return DatabaseClient.singletonDatabaseClient;
    }
    
    private void ConnectToDB() {
        java.sql.Connection connection = null;
        String server = DatabaseClient.SERVER, 
                user = DatabaseClient.USER, 
                password = DatabaseClient.PASSWORD, 
                db_name = DatabaseClient.DB_NAME;
        try {
            Class.forName(DatabaseClient.JDBC_DRIVER);
            connection = DriverManager.getConnection(server + db_name, user, password);
        } catch (SQLException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e){
            System.err.println("catched exception");
        }
        this.db_connection = connection;
    }

    public int AddPage(String url, int outbound_links, double page_rank) {
        int page_id = -1;
        String insert_query = "INSERT INTO `search_engine_db`.`page`" +
                                "(`url`," +
                                "`outbound_links`," +
                                "`page_rank`)" +
                                "VALUES" + 
                                "('" + url + "', " + outbound_links + ", " + page_rank + ");";
        
        String select_query = "SELECT LAST_INSERT_ID();";
        
        try {
            Statement statement = db_connection.createStatement();
            statement.execute(insert_query);
            
            ResultSet result_set = statement.executeQuery(select_query);
            
            if(result_set.next())
                page_id = result_set.getInt(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return page_id;
    }
    
    public int AddPageProcedure(String url, int outbound_links, double page_rank){
        int page_id = -1;
        String query = "CALL `search_engine_db`.`add_page_sp`('" + url + "', " + outbound_links + ", " + page_rank + ");";
        try {
            Statement statement = db_connection.createStatement();
            
            ResultSet result_set = statement.executeQuery(query);
            
            if(result_set.next())
                page_id = result_set.getInt(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(page_id);
        return page_id;
    }
    
    public void AddWord(String word, int page_id, int position, int containing_tag, String table_name) {
        String insert_query = "INSERT INTO `search_engine_db`.`" + table_name + "`" +
                                "(`word`," +
                                "`page_id`," +
                                "`position`," +
                                "`containing_tag`)" +
                                "VALUES" + 
                                "('" + word + "', " + page_id + ", " + position + ", " + containing_tag + ");";
        
        try {
            Statement statement = db_connection.createStatement();
            statement.execute(insert_query);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("word added " + word);
    }
    
    public Boolean isTableExist(String table_name){
        try {
            DatabaseMetaData databaseMetaData = db_connection.getMetaData();
            ResultSet tablesResultSet = databaseMetaData.getTables(null, null, table_name, null);
            if(tablesResultSet.next()){
                return true;
            }else{
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    } 
    
    public void dropTable(String table_name){
        String drop_query = "DROP TABLE " + table_name + ";";
        try {
            Statement statement = db_connection.createStatement();
            statement.execute(drop_query);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createTable(String table_name){
        String create_query = "CREATE TABLE `search_engine_db`.`" + table_name + "` (\n" +
                                "  `word` VARCHAR(60) NOT NULL,\n" +
                                "  `page_id` INT(11) NOT NULL,\n" +
                                "  `position` INT(11) NOT NULL,\n" +
                                "  `containing_tag` ENUM('TITLE','META','HEADER','BODY') NOT NULL,\n" +
                                "  PRIMARY KEY (`word`, `page_id`, `position`),\n" +
                                "  INDEX `" + table_name + "_page_fk_idx` (`page_id` ASC),\n" +
                                "  CONSTRAINT `" + table_name + "_page_fk`\n" +
                                "    FOREIGN KEY (`page_id`)\n" +
                                "    REFERENCES `search_engine_db`.`page` (`page_id`)\n" +
                                "    ON DELETE CASCADE\n" +
                                "    ON UPDATE CASCADE)\n" +
                                "ENGINE = InnoDB\n" +
                                "DEFAULT CHARACTER SET = ucs2;";
        try {
            Statement statement = db_connection.createStatement();
            statement.execute(create_query);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
