package main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

//import Database; // import the database class.
//import Main; // import your main class


public class SQLite extends Database{
    String dbname;
    public SQLite(Main instance){
        super(instance);
        dbname = "Tickets"; // Set the table name here e.g player_kills
    }

  /*  public String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS table_name (" + // make sure to put your table name in here too.
            "`player` varchar(32) NOT NULL," + // This creates the different colums you will save data too. varchar(32) Is a string, int = integer
            "`kills` int(11) NOT NULL," +
            "`total` int(11) NOT NULL," +
            "PRIMARY KEY (`player`)" +  // This is creating 3 colums Player, Kills, Total. Primary key is what you are going to use as your indexer. Here we want to use player so
            ");"; // we can search by player, and get kills and total. If you some how were searching kills it would provide total and player.
*/
    public String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS Tickets(" +
    "`ID` INTEGER PRIMARY KEY," + 
    "`Owner` VARCHAR(40) NOT NULL," +
    "`Description` VARCHAR(255) NOT NULL," +
    "`Assignee` VARCHAR(255)," +
    "`Status` VARCHAR(255),"+
    "`Label` VARCHAR(255)," + 
    "`World` VARCHAR(255),"+
    "`x` INTEGER NOT NULL," +
    "`y` INTEGER NOT NULL," + 
    "`z` INTEGER NOT NULL," + 
    "`pitch` INTEGER NOT NULL,"+
    "`yaw` INTEGER NOT NULL);"+
    "CREATE TABLE IF NOT EXISTS Comments(" + 
    "`ID` INTEGER PRIMARY KEY,"+
    "`TicketID` INTEGER REFERENCES Tickets(ID),"+
    "`Commenter` VARCHAR(255)," +
    "`Text` VARCHAR(255));"+
    "CREATE TABLE IF NOT EXISTS HotSpots("+
    "`ID` INTEGER PRIMARY KEY,"+
    "`TicketID` INTEGER REFERENCES Tickets(ID),"+
    "`Commenter` VARCHAR(255)," +
    "`Text` VARCHAR(255),"+
    "`World` VARCHAR(255),"+
    "`x` INTEGER NOT NULL,"+
    "`y` INTEGER NOT NULL,"+
    "`z` INTEGER NOT NULL,"+
    "`pitch` INTEGER NOT NULL,"+
    "`yaw` INTEGER NOT NULL);";

    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }
    public void load() {
        connection = getSQLConnection(); 
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}