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
    
    public Map<String, String> getTicketHeaders(String condition){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT ID, Label, Description FROM Tickets WHERE "+ condition +";");
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
    
    public int setTicket(String instruction){
    	Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE Tickets SET "+ instruction + ";");
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
    
/*    public int closeTicket(String id){
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
    
    public int assignTicket(String pl, String id){
	   Connection conn = null;
       PreparedStatement ps = null;
       try {
           conn = getSQLConnection();
           ps = conn.prepareStatement("UPDATE Tickets SET Assignee=\"" + pl + "\" WHERE ID=" + id + ";");
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
    }*/
    
    
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