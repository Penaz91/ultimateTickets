package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

    private Database db;

    public void onEnable(){
    	File f = getDataFolder();
		if (!f.exists()){
			f.mkdir();
		}
        this.db = new SQLite(this);
        this.db.load();
    }

	public Database getRDatabase() {
	        return this.db;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ticket")) {
			if (sender instanceof Player){
				if (args.length == 0){
					sender.sendMessage(ChatColor.RED + "Usage:");
					sender.sendMessage(ChatColor.RED + "/ticket" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows this list");
					sender.sendMessage(ChatColor.RED + "/ticket new <issue>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Create a new issue");
					if (sender.hasPermission("ultimateTickets.listall")){
						sender.sendMessage(ChatColor.RED + "/ticket list all" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows all tickets, open and closed");
						sender.sendMessage(ChatColor.RED + "/ticket list unassigned" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows all unassigned tickets");
						sender.sendMessage(ChatColor.RED + "/ticket view open" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows open tickets");
						sender.sendMessage(ChatColor.RED + "/ticket view claimed" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows tickets you claimed");
					}
					sender.sendMessage(ChatColor.RED + "/ticket list mine" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows the tickets you opened");
					if (sender.hasPermission("ultimateTickets.teleport")){
						sender.sendMessage(ChatColor.RED + "/ticket teleport <ID> <HS>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Teleports you to a certain ticket or one of its hotspots");
					}
					sender.sendMessage(ChatColor.RED + "/ticket view info <ID>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Check the ticket information");
					sender.sendMessage(ChatColor.RED + "/ticket view comment <ID>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "List all Ticket comments");
					sender.sendMessage(ChatColor.RED + "/ticket view hotspot <ID>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "List all Ticket hotspots");
					sender.sendMessage(ChatColor.RED + "/ticket hotspot <ID> <Comment>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Add a hotspot to your ticket");
					if (sender.hasPermission("ultimateTickets.close")){
						sender.sendMessage(ChatColor.RED + "/ticket close <ID>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Close a ticket");
					}
					if (sender.hasPermission("ultimateTickets.assign")){
						sender.sendMessage(ChatColor.RED + "/ticket assign <ID> <Text>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Assign a ticket to a player or category");
						sender.sendMessage(ChatColor.RED + "/ticket claim <ID>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Assign a ticket to yourself");
					}
					if (sender.hasPermission("ultimateTickets.purge")){
						sender.sendMessage(ChatColor.RED + "/ticket purge" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Purge all closed tickets, to free up the database");
					}
				}else{
					if (args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("n")){
						//TODO: permission check here
						String desc = "";
						for (int i=1; i < args.length; i++){
							desc += args[i] + " ";
						}
						int x = getRDatabase().createTicket((Player) sender, desc);
						return x == 0;
					}
					if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")){
						//TODO: permission check here
						if (args.length < 3){
							String id = args[1];
							Map<String, String> rs = getRDatabase().getTicketLocation(id,"");
							getLogger().info(rs.toString());
							Location l = new Location(getServer().getWorld(rs.get("World")),
									Double.parseDouble(rs.get("x")),
									Double.parseDouble(rs.get("y")),
									Double.parseDouble(rs.get("z")),
									Float.parseFloat(rs.get("yaw")),
									Float.parseFloat(rs.get("pitch")));
							((Player) sender).teleport(l);
							return true;
						}else{
							String id = args[1];
							String hs = args[2];
							Map<String, String> rs = getRDatabase().getTicketLocation(id,hs);
							getLogger().info(rs.toString());
							Location l = new Location(getServer().getWorld(rs.get("World")),
									Double.parseDouble(rs.get("x")),
									Double.parseDouble(rs.get("y")),
									Double.parseDouble(rs.get("z")),
									Float.parseFloat(rs.get("yaw")),
									Float.parseFloat(rs.get("pitch")));
							((Player) sender).teleport(l);
							return true;
						}
					}
					if (args[0].equalsIgnoreCase("view")||args[0].equalsIgnoreCase("v")){
						//TODO: permission check here
						if (args.length < 3){
							sender.sendMessage("Usage: /ticket view <comment|hotspot|info> ID");
						}else{
							if (args[1].equalsIgnoreCase("info")){
								String id = args[2];
								Map<String, String> rs = getRDatabase().getTicketInfo(id);
								rs.putAll(getRDatabase().getTicketLocation(id,""));
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								sender.sendMessage(ChatColor.RED + "ID: " + ChatColor.GOLD + rs.get("ID") + "      " + ChatColor.RED + "Owner: " + ChatColor.GOLD + rs.get("Owner"));
								sender.sendMessage(ChatColor.RED + "Status: " + ChatColor.GOLD + rs.get("Status") + "      " + ChatColor.RED + "Assigned to: " + ChatColor.GOLD + rs.get("Assignee"));
								sender.sendMessage(ChatColor.RED + "Label: " + ChatColor.GOLD + rs.get("Label"));
								sender.sendMessage(ChatColor.RED + "Sent from: " + ChatColor.GOLD + rs.get("World") + ":" + rs.get("x") + "," + rs.get("y") + "," + rs.get("z"));
								sender.sendMessage(ChatColor.RED + "Description: " + ChatColor.GOLD + rs.get("Description"));
								sender.sendMessage(ChatColor.GOLD + "------------------------------------------");
								sender.sendMessage(ChatColor.RED + "Comments: " + ChatColor.GOLD + rs.get("ComN") + "      " + ChatColor.RED + "HotSpots: " + ChatColor.GOLD + rs.get("HSN"));
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
							}
							if (args[1].equalsIgnoreCase("hotspot")){
								String id = args[2];
								ArrayList<String> cmts = getRDatabase().getHotSpots(id);
								for (String item: cmts){
									//TODO: This is just a stub, no verifications etc...
									sender.sendMessage(item);
								}
							}
							if (args[1].equalsIgnoreCase("comment")){
								String id = args[2];
								ArrayList<String> cmts = getRDatabase().getComments(id);
								for (String item: cmts){
									//TODO: This is just a stub, no verifications etc...
									sender.sendMessage(item);
								}
							}
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("claim")){
						//TODO: Stub, needs tests
						//return getRDatabase().assignTicket(((Player) sender).getName(), args[1]) == 0;
						return getRDatabase().setTicket("Assignee=\"" + ((Player) sender).getName() + " WHERE ID=" + args[1] + "\"")==0;
						
					}
					if (args[0].equalsIgnoreCase("assign")){
						//TODO: Stub, needs tests
						//return getRDatabase().assignTicket(args[2], args[1]) == 0;
						return getRDatabase().setTicket("Assignee=\"" + args[2] + " WHERE ID=" + args[1] + "\"")==0;
					}
					if (args[0].equalsIgnoreCase("close")||args[0].equalsIgnoreCase("c")){
						// TODO: Return message to user and verify for no ID
						return getRDatabase().setTicket("Status=\"Closed\" WHERE ID=" + args[1] + "\"")==0;
					}
					if (args[0].equalsIgnoreCase("comment")||args[0].equalsIgnoreCase("com")){
						String id = args[1];
						String comment = "";
						for (int i = 2; i < args.length; i++){
							comment += args[i] + " ";
						}
						return getRDatabase().addComment(id, ((Player) sender).getName(), comment) == 0;
					}
					if (args[0].equalsIgnoreCase("hotspot")||args[0].equalsIgnoreCase("hs")){
						//TODO: Stub
						String id = args[1];
						String comment = "";
						for (int i = 2; i < args.length; i++){
							comment += args[i] + " ";
						}
						return getRDatabase().addHotSpot(id, ((Player) sender), comment) == 0;
					}
					if (args[0].equalsIgnoreCase("purge")){
						//TODO: Stub
					}
					if (args[0].equalsIgnoreCase("list")||args[0].equalsIgnoreCase("l")){
						if (args.length < 2){
							sender.sendMessage("Usage: /ticket list <open|all|unassigned|mine|claimed>");
						}else{
							if (args[1].equalsIgnoreCase("open")){
								//Map<String, String> rs = getRDatabase().getOpenTicketHeaders();
								Map<String, String> rs = getRDatabase().getTicketHeaders("Status='Open'");
								//TODO: There is no pagination!
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								for(Map.Entry<String, String> entry: rs.entrySet()){
									sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
								}
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");							
							}
							if (args[1].equalsIgnoreCase("unassigned")){
								//Map<String, String> rs = getRDatabase().getUnassignedTicketHeaders();
								Map<String, String> rs = getRDatabase().getTicketHeaders("Assignee IS NULL AND Status='Open'");
								//TODO: There is no pagination!
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								for(Map.Entry<String, String> entry: rs.entrySet()){
									sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
								}
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");							
							}
							}
							if (args[1].equalsIgnoreCase("claimed")){
								//Map<String, String> rs = getRDatabase().getPlayerTicketHeaders(((Player) sender));
								Map<String, String> rs=getRDatabase().getTicketHeaders("Status='Open' AND Assignee =\"" + ((Player) sender).getName() + "\"" );
								//TODO: There is no pagination!
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								for(Map.Entry<String, String> entry: rs.entrySet()){
									sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
								}
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
							}
							if (args[1].equalsIgnoreCase("mine")){
								Map<String, String> rs=getRDatabase().getTicketHeaders("Owner =\"" + ((Player) sender).getName() + "\" ORDER BY Status DESC" );
								//TODO: There is no pagination!
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								for(Map.Entry<String, String> entry: rs.entrySet()){
									sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
								}
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
							}
							if (args[1].equalsIgnoreCase("all")){
								//Map<String, String> rs = getRDatabase().getAllTicketHeaders();
								//Workaround-ish
								Map<String, String> rs = getRDatabase().getTicketHeaders("1 = 1");
								//TODO: There is no pagination!
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								for(Map.Entry<String, String> entry: rs.entrySet()){
									sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
								}
								sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
							}
						}	
					}
				}
			}
		return true;
	}
}