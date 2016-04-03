package apt.search.engine;

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
            DB_NAME = "crawler",
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
            //Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e){
            System.err.println("catched exception");
        }
        this.db_connection = connection;
    }

    public int AddPage(String url, int outbound_links, double page_rank) {
        int page_id = -1;
        String insert_query = "INSERT INTO `crawler`.`page`" +
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
        String query = "CALL `crawler`.`add_page_sp`('" + url + "', " + outbound_links + ", " + page_rank + ");";
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
        String insert_query = "INSERT INTO `crawler`.`" + table_name + "`" +
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
        //System.out.println("word added " + word);
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
        String create_query = "CREATE TABLE `crawler`.`" + table_name + "` (\n" +
                                "  `page_id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                                "  `word` VARCHAR(100) NOT NULL,\n" +
                                "  `position` INT(11) NOT NULL,\n" +
                                "  `containing_tag` ENUM('TITLE','META','HEADER','BODY') NOT NULL,\n" +
                                "  PRIMARY KEY (`page_id`, `word`, `position`),\n" +
                                "  CONSTRAINT `" + table_name + "_page_fk`\n" +
                                "    FOREIGN KEY (`page_id`)\n" +
                                "    REFERENCES `crawler`.`pages` (`ID`)\n" +
                                "    ON DELETE CASCADE\n" +
                                "    ON UPDATE CASCADE)\n" +
                                "ENGINE = InnoDB\n" +
                                "DEFAULT CHARACTER SET = utf8;";
        try {
            Statement statement = db_connection.createStatement();
            statement.execute(create_query);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /*
     * Crawler Methods
     */
        
    public ResultSet getToVisit(){
    	
    	String query = "SELECT URL, ID FROM Pages WHERE visited = false;";
    	
    	Statement statement;
    	ResultSet result = null;
		try {
			statement = db_connection.createStatement();
			result = statement.executeQuery(query);
		} catch (SQLException e) {
			Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, e);
		}
		return result;
    }
    
    public int insertPage(String current_url, boolean priority){
    	String insert_query = "INSERT INTO Pages " +
                "(URL," +
                "priority) " +
                "VALUES" + 
                "('" + current_url + "', " + priority + ");";
    	
		try {
			
	    	PreparedStatement statement = db_connection.prepareStatement(insert_query, Statement.RETURN_GENERATED_KEYS);
			int result = statement.executeUpdate();
			if(result != 0){
				ResultSet generatedKeys = statement.getGeneratedKeys();
				if(generatedKeys.next()){
					return generatedKeys.getInt(1);
				}
			}
		} catch (SQLException e) {
			Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, e);
		}
		return 0;
    }
    
    public void updateVisited(int id){
    	String update_query = "UPDATE Pages SET visited = true WHERE ID =  " + id + " ;";
    	
    	Statement statement;
		try {
			statement = db_connection.createStatement();
			statement.execute(update_query);
		} catch (SQLException e) {
			Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, e);
		}
    }
    
    public void updateCount(String url){
    	String update_query = "UPDATE Pages SET refcount = refcount + 1 WHERE URL =  '" + url + "' ;";
    	
    	Statement statement;
		try {
			statement = db_connection.createStatement();
			statement.execute(update_query);
		} catch (SQLException e) {
			Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, e);
		}
    }
    
    public boolean exist(String url){
    	String query = "SELECT ID FROM Pages WHERE URL = '" + url + "' ;";
    	
    	Statement statement;
    	ResultSet result = null;
		try {
			statement = db_connection.createStatement();
			result = statement.executeQuery(query);

			if(result.next()){
				updateCount(url);
				return true;
			}
			else return false;
		} catch (SQLException e) {
			Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, e);
		}
		return false;
    }    
    
    public ResultSet refresh(){
    	String update_query = "UPDATE Pages SET visited = false; SELECT URL, ID FROM Pages ORDER BY refcount DESC, priority DESC LIMIT 1000;";
    	
    	Statement statement;
    	ResultSet result = null;
		try {
			statement = db_connection.createStatement();
			result = statement.executeQuery(update_query);
		} catch (SQLException e) {
			Logger.getLogger(DatabaseClient.class.getName()).log(Level.SEVERE, null, e);
		}
		return result;
    }
    
}