package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
        int status = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO Tickets (Owner, Description, Status, World, x, y, z, pitch, yaw) VALUES (\"" + pl.getName() + "\",\""+description+"\", \"Open\",\""+ pll.getWorld().getName() + "\"," +pll.getX()+","+pll.getY()+","+pll.getZ()+","+pll.getPitch()+","+pll.getYaw()+");");
            status=ps.executeUpdate();
            
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
        return status; 
    }
    
    public Map<String, String> getTicketLocation(String id, String hs){
    	Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            if (hs==""){
            	ps = conn.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM Tickets WHERE ID = \""+id+"\";");
            }else{
            	ps = conn.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM HotSpots WHERE TicketID = \""+id+"\" AND ID=\"" + hs + "\";");
            }
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
    
    public void purgeTickets(){
    	Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
           // ps = conn.prepareStatement("PRAGMA foreign_keys=ON");
           // ps.executeQuery();
           // ps.close();
            //FIXME: Doesn't clean Comment table and hotspot table
            ps = conn.prepareStatement("DELETE FROM Tickets WHERE Status='Closed';");
            ps.executeUpdate();
            
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
    
    
    public int addComment(String id, String pl, String comment){
    	Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO Comments (TicketID, Commenter, Text) VALUES (" + id + ",\"" + pl + "\",\"" + comment + "\");");
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
            ps = conn.prepareStatement("SELECT * FROM Tickets WHERE ID = "+id+";");
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

	public int addHotSpot(String id, Player player, String comment) {
		Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            Location loc = player.getLocation();
            ps = conn.prepareStatement("INSERT INTO HotSpots (TicketID, Commenter, World, x, y, z, pitch, yaw, Text) VALUES (" + id + ",\"" + player.getName() + "\",\"" + loc.getWorld().getName() + "\"," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw() + ",\"" + comment + "\");");
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

	public ArrayList<String> getHotSpots(String id) {
		 Connection conn = null;
	        PreparedStatement ps = null;
	        ResultSet rs = null;
	        try {
	            conn = getSQLConnection();
	            ps = conn.prepareStatement("SELECT ID, Text FROM HotSpots WHERE TicketID = " + id + " ORDER BY ID;");
	            rs = ps.executeQuery();
	            ArrayList<String> res = new ArrayList<String>();
	            while(rs.next()){
	            	res.add(rs.getString("ID") + " - " +rs.getString("Text"));
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