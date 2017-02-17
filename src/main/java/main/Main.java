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
	static final String logo = ChatColor.RED + "[" + ChatColor.GOLD + "UltimateTickets" + ChatColor.RED + "] " + ChatColor.GOLD;

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
						sender.sendMessage(ChatColor.RED + "/ticket list open" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows open tickets");
						sender.sendMessage(ChatColor.RED + "/ticket list claimed" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Shows tickets you claimed");
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
					if (sender.hasPermission("ultimateTickets.label")){
						sender.sendMessage(ChatColor.RED + "/ticket label <ID> <One-word-label>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Give a label to the ticket");
					}
					if (sender.hasPermission("ultimateTickets.purge")){
						sender.sendMessage(ChatColor.RED + "/ticket purge" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Purge all closed tickets, to free up the database");
					}
				}else{
					if (args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("n")){
						String desc = "";
						for (int i=1; i < args.length; i++){
							desc += args[i] + " ";
						}
						int x = getRDatabase().createTicket((Player) sender, desc);
						if (x==0){
							sender.sendMessage(logo + "There was an error while creating your ticket, please talk to an administrator");
							return false;
						}else{
							sender.sendMessage(logo + "Your ticket has been sent successfully, check /ticket l mine for your open tickets");
							return true;
						}
					}
					if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")){
						if (sender.hasPermission("ultimateTickets.teleport")){
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
								sender.sendMessage(logo+"You have been teleported to the selected location");
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
								sender.sendMessage(logo+"You have been teleported to the selected location");
								return true;
							}
						}
					}
					if (args[0].equalsIgnoreCase("view")||args[0].equalsIgnoreCase("v")){
						if (args.length < 3){
							sender.sendMessage("Usage: /ticket view <comment|hotspot|info> ID");
						}else{
							if (args[1].equalsIgnoreCase("info")){
								String id = args[2];
								Map<String, String> rs = getRDatabase().getTicketInfo(id);
								if (sender.hasPermission("ultimateTickets.viewall") || rs.get("Owner") == ((Player) sender).getName()){
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
							}
							if (args[1].equalsIgnoreCase("hotspot")){
								String id = args[2];
								Map<String, String> rs = getRDatabase().getTicketInfo(id);
								if (sender.hasPermission("ultimateTickets.viewall") || rs.get("Owner") == ((Player) sender).getName()){
									ArrayList<String> cmts = getRDatabase().getHotSpots(id);
									for (String item: cmts){
										sender.sendMessage(ChatColor.GOLD + item);
									}
								}
							}
							if (args[1].equalsIgnoreCase("comment")){
								String id = args[2];
								Map<String, String> rs = getRDatabase().getTicketInfo(id);
								if (sender.hasPermission("ultimateTickets.viewall") || rs.get("Owner") == ((Player) sender).getName()){
									ArrayList<String> cmts = getRDatabase().getComments(id);
									for (String item: cmts){
										sender.sendMessage(ChatColor.GOLD + item);
									}
								}
							}
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("claim")){
						if (sender.hasPermission("ultimateTickets.assign")){
							if (getRDatabase().setTicket("Assignee=\"" + ((Player) sender).getName() + "\" WHERE ID=" + args[1])==0){
								sender.sendMessage(logo + "You have claimed this ticket");
							}else{
								sender.sendMessage(logo + "The ticket could not be claimed.");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to claim tickets");
						}
					}
					if (args[0].equalsIgnoreCase("label")){
						if (sender.hasPermission("ultimateTickets.label")){
							if (getRDatabase().setTicket("Label=\"" + args[2] + "\" WHERE ID=" + args[1])==0){
								sender.sendMessage(logo + "You have labeled this ticket");
							}else{
								sender.sendMessage(logo + "The ticket could not be labeled.");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to label tickets");
						}
					}
					if (args[0].equalsIgnoreCase("assign")||args[0].equalsIgnoreCase("a")){
						if (sender.hasPermission("ultimateTickets.assign")){
							if (getRDatabase().setTicket("Assignee=\"" + args[2] + "\" WHERE ID=" + args[1])==0){
								sender.sendMessage(logo + "You have assigned this ticket");
							}else{
								sender.sendMessage(logo + "The ticket could not be assigned.");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to assign tickets");
						}
					}
					if (args[0].equalsIgnoreCase("close")||args[0].equalsIgnoreCase("c")){
						if(sender.hasPermission("ultimateTickets.close")){
							if(getRDatabase().setTicket("Status=\"Closed\" WHERE ID=" + args[1])==0){
								sender.sendMessage(logo + "Ticket closed");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to close this ticket");
						}
					}
					if (args[0].equalsIgnoreCase("comment")||args[0].equalsIgnoreCase("com")){
						String id = args[1];
						Map<String, String> rs = getRDatabase().getTicketInfo(id);
						if (sender.hasPermission("ultimateTickets.commentOthers") || rs.get("Owner") == ((Player) sender).getName()){
							String comment = "";
							for (int i = 2; i < args.length; i++){
								comment += args[i] + " ";
							}
							int x=getRDatabase().addComment(id, ((Player) sender).getName(), comment);
							if (x==0){
								sender.sendMessage(logo + "Your comment has been added to ticket #" + id);
								return true;
							}else{
								sender.sendMessage(logo + "There has been an error, your comment might have been lost");
								return false;
							}
						}else{
							sender.sendMessage(logo + "You can't comment other people's tickets, check your ticket ID");
						}
					}
					if (args[0].equalsIgnoreCase("hotspot")||args[0].equalsIgnoreCase("hs")){
						String id = args[1];
						Map<String, String> rs = getRDatabase().getTicketInfo(id);
						if (sender.hasPermission("ultimateTickets.commentOthers") || rs.get("Owner") == ((Player) sender).getName()){
							String comment = "";
							for (int i = 2; i < args.length; i++){
								comment += args[i] + " ";
							}
							int x = getRDatabase().addHotSpot(id, ((Player) sender), comment);
							if (x==0){
								sender.sendMessage(logo + "Your hotspot has been added to ticket #" + id);
								return true;
							}else{
								sender.sendMessage(logo + "There has been an error, your hotspot might have been lost");
								return false;
							}
						}
					}
					if (args[0].equalsIgnoreCase("purge")){
						if (sender.hasPermission("ultimateTickets.purge")){
							/*if (args.length == 1){
								sender.sendMessage(logo + "This will purge the database of all closed tickets, type '/ticket purge force' to force the removal");
							}else{
								if (args[1].equalsIgnoreCase("force")){
									getRDatabase().purgeTickets();
									sender.sendMessage(logo + "The Ticket database has been forcibly purged of closed tickets");
								}
							}*/
							sender.sendMessage(logo + "This command has been disabled due to bugs");
						}
					}
					if (args[0].equalsIgnoreCase("list")||args[0].equalsIgnoreCase("l")){
						if (args.length < 2){
							sender.sendMessage("Usage: /ticket list <open|all|unassigned|mine|claimed>");
						}else{
							if (args[1].equalsIgnoreCase("open")){
								if (sender.hasPermission("ultimateTickets.listall")){
									Map<String, String> rs = getRDatabase().getTicketHeaders("Status='Open'");
									//TODO: There is no pagination!
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									for(Map.Entry<String, String> entry: rs.entrySet()){
										sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
									}
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								}else{
									sender.sendMessage("You don't have permission to list open tickets");
								}
							}
							if (args[1].equalsIgnoreCase("unassigned")){
								if (sender.hasPermission("ultimateTickets.listall")){
									Map<String, String> rs = getRDatabase().getTicketHeaders("Assignee IS NULL AND Status='Open'");
									//TODO: There is no pagination!
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									for(Map.Entry<String, String> entry: rs.entrySet()){
										sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
									}
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");							
								}else{
									sender.sendMessage("You don't have permission to list unassigned tickets");
								}
							}
							if (args[1].equalsIgnoreCase("claimed")){
								if (sender.hasPermission("ultimateTickets.listall")){
									Map<String, String> rs=getRDatabase().getTicketHeaders("Status='Open' AND Assignee =\"" + ((Player) sender).getName() + "\"" );
									//TODO: There is no pagination!
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									for(Map.Entry<String, String> entry: rs.entrySet()){
										sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
									}
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								}else{
									sender.sendMessage("You don't have permission to list claimed tickets");
								}
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
								if (sender.hasPermission("ultimateTickets.listall")){
									//Workaround-ish
									Map<String, String> rs = getRDatabase().getTicketHeaders("1 = 1");
									//TODO: There is no pagination!
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									for(Map.Entry<String, String> entry: rs.entrySet()){
										sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
									}
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								}else{
									sender.sendMessage("You don't have permission to list all tickets");
								}
							}
						}	
					}
				}
			}
		}
		return true;
	}
}