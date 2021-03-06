package ticketmain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.squirrelid.Profile;

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
//            ps = conn.prepareStatement("INSERT INTO Tickets (Owner, Description, Status, World, x, y, z, pitch, yaw) VALUES (\"" + pl.getName() + "\",\""+description+"\", \"Open\",\""+ pll.getWorld().getName() + "\"," +pll.getX()+","+pll.getY()+","+pll.getZ()+","+pll.getPitch()+","+pll.getYaw()+");");
            ps = conn.prepareStatement("INSERT INTO Tickets (Owner, Description, Status, World, x, y, z, pitch, yaw) VALUES (\"" + pl.getUniqueId().toString() + "\",\""+description+"\", \"Open\",\""+ pll.getWorld().getName() + "\"," +pll.getX()+","+pll.getY()+","+pll.getZ()+","+pll.getPitch()+","+pll.getYaw()+");");
            Main.cache.put(new Profile(pl.getUniqueId(), pl.getName()));
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
        Map<String, String> mp = new HashMap<String, String>();
        try {
            conn = getSQLConnection();
            if (hs==""){
            	ps = conn.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM Tickets WHERE ID = \""+id+"\";");
            }else{
            	ps = conn.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM HotSpots WHERE TicketID = \""+id+"\" AND ID=\"" + hs + "\";");
            }
            rs = ps.executeQuery();
            mp.put("World", rs.getString("World"));
            mp.put("x", rs.getString("x"));
            mp.put("y", rs.getString("y"));
            mp.put("z", rs.getString("z"));
            mp.put("pitch", rs.getString("pitch"));
            mp.put("yaw", rs.getString("yaw"));
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed())
                    ps.close();
                if (conn != null)
                    conn.close();
                return mp;
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null; 
    }
    
    public boolean purgeTickets(){
    	Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM Comments WHERE TicketID IN (SELECT Comments.ID from Tickets, Comments where Tickets.ID==Comments.TicketID AND Tickets.Status=='Closed');");
            ps.executeUpdate();
            ps1 = conn.prepareStatement("DELETE FROM HotSpots WHERE TicketID IN (SELECT HotSpots.ID from Tickets, HotSpots where Tickets.ID==HotSpots.TicketID AND Tickets.Status=='Closed');");
            ps1.executeUpdate();
            ps2 = conn.prepareStatement("DELETE FROM Tickets WHERE Status='Closed';");
            ps2.executeUpdate();
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed())
                    ps.close();
                if (ps1 != null && !ps1.isClosed())
                    ps1.close();
                if (ps2 != null && !ps2.isClosed())
                    ps2.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
                return false;
            }
        }
        return true;
    }
    
    public Map<String, String> getTicketHeaders(String condition){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, String> res = new HashMap<String, String>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT ID, Label, Description FROM Tickets WHERE "+ condition +";");
            rs = ps.executeQuery();
            while(rs.next()){
            	String desc = rs.getString("Description");
            	String extra = desc.length()>25 ? "..." : "";
            	String value = rs.getString("Label") + " - " + desc.substring(0, Math.min(desc.length(), 25)) + extra;
            	res.put(rs.getString("ID"), value);
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
                return res;
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
			Collection<? extends Player> onlineList = Bukkit.getServer().getOnlinePlayers();
			Player player = null;
			for (Player Pl:onlineList){
				if (Pl.getUniqueId().equals(UUID.fromString(pl))){
					player = Pl;
				}
			}
			if (player != null){
				Main.cache.put(new Profile(player.getUniqueId(), player.getName()));
			}else{
				Bukkit.getServer().getLogger().info("Could not cache player: "+pl);
			}
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
        Map<String, String> mp = new HashMap<String, String>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM Tickets WHERE ID = "+id+";");
            rs = ps.executeQuery();
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
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null && !ps.isClosed())
                    ps.close();
                if (conn != null)
                    conn.close();
                return mp;
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
        ArrayList<String> res = new ArrayList<String>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT Commenter, Text FROM Comments WHERE TicketID = " + id + " ORDER BY ID;");
            rs = ps.executeQuery();
            while(rs.next()){
            	Bukkit.getLogger().info("Entro");
            	UUID uid = UUID.fromString(rs.getString("Commenter"));
            	Profile prof = Main.cache.getIfPresent(uid);
            	String commenter = null;
            	if (prof==null){
            		commenter = "<Uncached_User>";
            	}else{
            		commenter = prof.getName();
            	}
            	res.add(commenter + ": " + rs.getString("Text"));
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
                Bukkit.getLogger().info("Sto per ritornare il risultato: " + res.toString());
                return res;
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
//            ps = conn.prepareStatement("INSERT INTO HotSpots (TicketID, Commenter, World, x, y, z, pitch, yaw, Text) VALUES (" + id + ",\"" + player.getName() + "\",\"" + loc.getWorld().getName() + "\"," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw() + ",\"" + comment + "\");");
            ps = conn.prepareStatement("INSERT INTO HotSpots (TicketID, Commenter, World, x, y, z, pitch, yaw, Text) VALUES (" + id + ",\"" + player.getUniqueId().toString() + "\",\"" + loc.getWorld().getName() + "\"," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw() + ",\"" + comment + "\");");
            Main.cache.put(new Profile(player.getUniqueId(), player.getName()));
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

	public Map<String, String> getHotSpots(String id) {
		 Connection conn = null;
	        PreparedStatement ps = null;
	        ResultSet rs = null;
	        Map<String, String> res = new HashMap<String, String>();
	        try {
	            conn = getSQLConnection();
	            ps = conn.prepareStatement("SELECT ID, Text FROM HotSpots WHERE TicketID = " + id + " ORDER BY ID;");
	            rs = ps.executeQuery();
	            while(rs.next()){
	            	res.put(rs.getString("ID"), rs.getString("Text"));
	            }
	        } catch (SQLException ex) {
	            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
	        } finally {
	            try {
	                if (ps != null)
	                    ps.close();
	                if (conn != null)
	                    conn.close();
	                return res;
	            } catch (SQLException ex) {
	                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
	            }
	        }
		return null;
	}
}