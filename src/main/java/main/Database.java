package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

//import Error; // YOU MUST IMPORT THE CLASS ERROR, AND ERRORS!!!
//import Errors;
//import Main; // Import main class!


public abstract class Database {
    Main plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.
    //public String table = "Tickets";
    public int tokens = 0;
    public Database(Main instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT date('now');");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
     
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }

    public Integer createTicket(Player pl, String description){
    	Connection conn = null;
        PreparedStatement ps = null;
        Location pll = pl.getLocation();
        try {
            conn = getSQLConnection();
            String qry= "INSERT INTO Tickets (Owner, Description, Status, World, x, y, z, pitch, yaw) VALUES (\"" + pl.getName() + "\",\""+description+"\", \"Open\",\""+ pll.getWorld().getName() + "\"," +pll.getX()+","+pll.getY()+","+pll.getZ()+","+pll.getPitch()+","+pll.getYaw()+");";
            Bukkit.getLogger().info(qry);
            ps = conn.prepareStatement(qry);
            ps.executeUpdate();
            //conn.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed())
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0; 
    }
    
    public Map<String, String> getTicketLocation(String id){
    	Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM Tickets WHERE ID = \""+id+"\";");
            rs = ps.executeQuery();
            Map<String, String> mp = new HashMap<String, String>();
            mp.put("World", rs.getString("World"));
            mp.put("x", rs.getString("x"));
            mp.put("y", rs.getString("y"));
            mp.put("z", rs.getString("z"));
            mp.put("pitch", rs.getString("pitch"));
            mp.put("yaw", rs.getString("yaw"));
            return mp;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed())
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null; 
    }
    
    public Map<String, String> getTicketHeaders(){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT ID, Label, Description FROM Tickets WHERE Status = 'Open';");
            rs = ps.executeQuery();
            Map<String, String> res = new HashMap<String, String>();
            while(rs.next()){
            	String desc = rs.getString("Description");
            	String extra = desc.length()>25 ? "..." : "";
            	String value = rs.getString("Label") + " - " + desc.substring(0, Math.min(desc.length(), 25)) + extra;
            	res.put(rs.getString("ID"), value);
            }
            return res;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null; 
    }
    
    public int closeTicket(String id){
	   Connection conn = null;
       PreparedStatement ps = null;
       try {
           conn = getSQLConnection();
           ps = conn.prepareStatement("UPDATE Tickets SET Status=\"Closed\" WHERE ID=\"" + id + "\";");
           ps.executeUpdate();
       } catch (SQLException ex) {
           plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
       } finally {
           try {
               if (ps != null)
                   ps.close();
               if (conn != null)
                   conn.close();
           } catch (SQLException ex) {
               plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
           }
       }
    	return 0;
    }
    
    public int addComment(String id, String comment){
    	Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            String qry= "INSERT INTO Comments (TicketID, Text) VALUES (" + id + ",\"" + comment + "\");";
            Bukkit.getLogger().info(qry);
            ps = conn.prepareStatement(qry);
            ps.executeUpdate();
            //conn.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed())
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0; 
    }
    
    public Map<String, String> getTicketInfo(String id){
    	Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            String qry = "SELECT * FROM Tickets WHERE ID = "+id+";";
            plugin.getLogger().info(qry);
            ps = conn.prepareStatement(qry);
            rs = ps.executeQuery();
            Map<String, String> mp = new HashMap<String, String>();
            mp.put("ID", rs.getString("ID"));
            mp.put("Owner", rs.getString("Owner"));
            mp.put("Assignee", rs.getString("Assignee") == null ? "Nobody" : rs.getString("Assignee"));
            mp.put("Status", rs.getString("Status"));
            mp.put("Description", rs.getString("Description"));
            mp.put("Label", rs.getString("Label") == null ? "No Labels" : rs.getString("Label"));
            ps.close();
            ps = conn.prepareStatement("SELECT Count(*) as Cnt FROM Comments WHERE TicketID="+ id +";");
            rs = ps.executeQuery();
            mp.put("ComN", rs.getString("Cnt"));
            ps.close();
            ps = conn.prepareStatement("SELECT Count(*) as Cnt FROM HotSpots WHERE TicketID="+ id +";");
            rs = ps.executeQuery();
            mp.put("HSN", rs.getString("Cnt"));
            plugin.getLogger().info(mp.toString());
            return mp;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed())
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null; 
    }
    // These are the methods you can use to get things out of your database. You of course can make new ones to return different things in the database.
    // This returns the number of people the player killed.
    /*public Integer getTokens(String string) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE player = '"+string+"';");
     
            rs = ps.executeQuery();
            conn.close();
            while(rs.next()){
                if(rs.getString("player").equalsIgnoreCase(string.toLowerCase())){ // Tell database to search for the player you sent into the method. e.g getTokens(sam) It will look for sam.
                    return rs.getInt("kills"); // Return the players amount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0; 
    }
    // Exact same method here, Except as mentioned above i am looking for total!
    public Integer getTotal(String string) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE player = '"+string+"';");
     
            rs = ps.executeQuery();
            conn.close();
            while(rs.next()){
                if(rs.getString("player").equalsIgnoreCase(string.toLowerCase())){
                    return rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0; 
    }

// Now we need methods to save things to the database
    public void setTokens(Player player, Integer tokens, Integer total) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + table + " (player,kills,total) VALUES(?,?,?)"); // IMPORTANT. In SQLite class, We made 3 colums. player, Kills, Total.
            ps.setString(1, player.getName().toLowerCase());                                             // YOU MUST put these into this line!! And depending on how many
                                                                                                         // colums you put (say you made 5) All 5 need to be in the brackets
                                                                                                         // Seperated with comma's (,) AND there needs to be the same amount of
                                                                                                         // question marks in the VALUES brackets. Right now i only have 3 colums
                                                                                                         // So VALUES (?,?,?) If you had 5 colums VALUES(?,?,?,?,?)
                                                                                                  
            ps.setInt(2, tokens); // This sets the value in the database. The colums go in order. Player is ID 1, kills is ID 2, Total would be 3 and so on. you can use
                                  // setInt, setString and so on. tokens and total are just variables sent in, You can manually send values in as well. p.setInt(2, 10) <-
                                  // This would set the players kills instantly to 10. Sorry about the variable names, It sets their kills to 10 i just have the variable called
                                  // Tokens from another plugin :/
            ps.setInt(3, total);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;         
    }*/


    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }

	public ArrayList<String> getComments(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT Text FROM Comments WHERE TicketID = " + id + " ORDER BY ID;");
            rs = ps.executeQuery();
            ArrayList<String> res = new ArrayList<String>();
            while(rs.next()){
            	res.add(rs.getString("Text"));
            }
            return res;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
		return null;
	}
}