package ticketmain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import mkremins.fanciful.FancyMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.cache.SQLiteCache;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;
import com.sk89q.squirrelid.resolver.ProfileService;

public class Main extends JavaPlugin{
	static final String logo = ChatColor.RED + "[" + ChatColor.GOLD + "UltimateTickets" + ChatColor.RED + "] " + ChatColor.GOLD;
	static final String defaultPermissionMessage = ChatColor.RED + " You don't have permissions to execute this command!";
	static Configuration config;
	static boolean sendEmail;
	static SQLiteCache cache = null;
	static ConfigurationSection labelsConfig = null;
	static Random rng = new Random();
	static ChatColor[] visibleColors = {ChatColor.GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.BLUE, ChatColor.DARK_GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE};

    private Database db;

    public void onEnable(){
    	File f = getDataFolder();
		if (!f.exists()){
			f.mkdir();
			saveResource("config.yml", false);
		}
		config = this.getConfig(); //loads the config
		sendEmail = config.getBoolean("sendMail");
        this.db = new SQLite(this);
        this.db.load();
        File file = new File(getDataFolder(), "UUIDcache.sqlite");
        try {
			cache = new SQLiteCache(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        labelsConfig = config.getConfigurationSection("customLabels");
    }
    
    public boolean reloadConfigCommand(){
		reloadConfig();
    	config = this.getConfig(); //loads the config
		sendEmail = config.getBoolean("sendMail");
        this.db = new SQLite(this);
        this.db.load();
        File file = new File(getDataFolder(), "UUIDcache.sqlite");
        try {
			cache = new SQLiteCache(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        labelsConfig = config.getConfigurationSection("customLabels");
        return true;
    }

	public Database getRDatabase() {
	        return this.db;
	}
	
	public static ChatColor getRandomColor(ChatColor lastColor) {
		ChatColor color = visibleColors[rng.nextInt(12)];
		while (color == lastColor){
			color = visibleColors[rng.nextInt(12)];
		}
		return color;
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
						sender.sendMessage(ChatColor.RED + "/ticket assign <ID> <Text>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Assign a ticket to another staff player");
						sender.sendMessage(ChatColor.RED + "/ticket claim <ID>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Assign a ticket to yourself");
					}
					if (sender.hasPermission("ultimateTickets.label")){
						sender.sendMessage(ChatColor.RED + "/ticket label <ID> <One-word-label>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Give a label to the ticket");
					}
					if (sender.hasPermission("ultimateTickets.purge")){
						sender.sendMessage(ChatColor.RED + "/ticket purge" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Purge all closed tickets, to free up the database");
					}
					if (sender.hasPermission("ultimateTickets.search")){
						sender.sendMessage(ChatColor.RED + "/ticket search <owner|label> <text>" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "Search among the tickets, by owner or label");
					}
					if (sender.hasPermission("ultimateTickets.reopen")){
						sender.sendMessage(ChatColor.RED + "/ticket reopen <ID>" + ChatColor.DARK_PURPLE +  " - " + ChatColor.GOLD + "Reopen a closed ticket");
					}
					if (sender.hasPermission("ultimateTickets.selfCaching")){
						sender.sendMessage(ChatColor.RED + "/ticket cachemyname" + ChatColor.DARK_PURPLE +  " - " + ChatColor.GOLD + "Force the caching of your UUID and Nickname, use in case of <Unknown player> in the 'assignee' field");
					}
					if (sender.hasPermission("ultimateTickets.reload")){
						sender.sendMessage(ChatColor.RED + "/ticket reload" + ChatColor.DARK_PURPLE +  " - " + ChatColor.GOLD + "Reloads the Configuration files");
					}
					if (sender.hasPermission("ultimateTickets.viewInfo")){
						sender.sendMessage(ChatColor.RED + "/ticket version" + ChatColor.DARK_PURPLE + " - " + ChatColor.GOLD + "View Information about this plugin");
					}
				}else{
					if (args[0].equalsIgnoreCase("version")){
						if (sender.hasPermission("ultimateTicket.viewInfo")){
							sender.sendMessage(ChatColor.GOLD + "---------------<>---------------");
							sender.sendMessage(logo + " - " + ChatColor.GOLD + "Yet another SQLite Ticket Manager Plugin");
							sender.sendMessage(ChatColor.GOLD + "Proudly brought to you by: Penaz");
							sender.sendMessage(ChatColor.GOLD + "Version: 0.0.2 - SNAPSHOT");
							sender.sendMessage(ChatColor.GOLD + "---------------<>---------------");
						}else{
							sender.sendMessage(logo + defaultPermissionMessage);
						}
					}
					if (args[0].equalsIgnoreCase("reload")){
						if (sender.hasPermission("ultimateTickets.reload")){
							reloadConfigCommand();
							getLogger().info(logo + "Configuration Reloaded");
							sender.sendMessage(logo + "Configuration Reloaded");
						}else{
							sender.sendMessage(logo + defaultPermissionMessage);
						}
					}
					if (args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("n")){
						String desc = "";
						for (int i=1; i < args.length; i++){
							desc += args[i] + " ";
						}
						if (desc!=""){
							int x = getRDatabase().createTicket((Player) sender, desc);
							if (x==0){
								sender.sendMessage(logo + "There was an error while creating your ticket, please talk to an administrator");
								return false;
							}else{
								sender.sendMessage(logo + "Your ticket has been sent successfully, check /ticket l mine for your open tickets");
								for (Player p: getServer().getOnlinePlayers()){
									if (p.hasPermission("ultimateTickets.newTicketWarn")){
										p.sendMessage(logo + "A new ticket has been posted! Check /ticket view open.");
									}
								}
								return true;
							}
						}else{
							sender.sendMessage(logo + "Your ticket description is empty! Please try again.");
						}
					}
					if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")){
						if (sender.hasPermission("ultimateTickets.teleport")){
							if (args.length < 3){
								String id = args[1];
								Map<String, String> rs = getRDatabase().getTicketLocation(id,"");
								//getLogger().info(rs.toString());
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
								//getLogger().info(rs.toString());
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
								//getLogger().info("Does the caller have property of the ticket? " + rs.get("Owner").equalsIgnoreCase(((Player) sender).getName()));
								//getLogger().info("Does the caller have the viewAll Permission? " + sender.hasPermission("ultimateTickets.viewall"));
								//getLogger().info("Will the player view the ticket info? " + (rs.get("Owner").equalsIgnoreCase(((Player) sender).getName()) || sender.hasPermission("ultimateTickets.viewall")));
								if (rs.get("Owner").equalsIgnoreCase(((Player) sender).getUniqueId().toString()) || sender.hasPermission("ultimateTickets.viewall")){
									rs.putAll(getRDatabase().getTicketLocation(id,""));
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									String playerName = "";
									String assignee = "";
									try{
										playerName = Bukkit.getPlayer(UUID.fromString(rs.get("Owner"))).getName() + "[Online]";
									}catch (NullPointerException e){
										Profile profile = cache.getIfPresent(UUID.fromString(rs.get("Owner")));
										if (profile == null){
											playerName = "<UNKNOWN PLAYER>";
										}else{
											playerName = profile.getName() + "[LKN]";
										}
									}
									try{
										assignee = Bukkit.getPlayer(UUID.fromString(rs.get("Assignee"))).getName() + "[Online]";
									}catch (NullPointerException e){
										Profile profile = cache.getIfPresent(UUID.fromString(rs.get("Assignee")));
										if (profile == null){
											assignee = "<UNKNOWN PLAYER>";
										}else{
											assignee = profile.getName() + "[LKN]";
										}
									}catch (IllegalArgumentException e){
										assignee="<Nobody>";
									}
									sender.sendMessage(ChatColor.RED + "ID: " + ChatColor.GOLD + rs.get("ID") + "      " + ChatColor.RED + "Owner: " + ChatColor.GOLD + playerName);
									sender.sendMessage(ChatColor.RED + "Status: " + ChatColor.GOLD + rs.get("Status") + "      " + ChatColor.RED + "Assigned to: " + ChatColor.GOLD + assignee);
									sender.sendMessage(ChatColor.RED + "Label: " + ChatColor.GOLD + rs.get("Label"));
									sender.sendMessage(ChatColor.RED + "Sent from: " + ChatColor.GOLD + rs.get("World") + ":" + rs.get("x") + "," + rs.get("y") + "," + rs.get("z"));
									sender.sendMessage(ChatColor.RED + "Description: " + ChatColor.GOLD + rs.get("Description"));
									sender.sendMessage(ChatColor.GOLD + "------------------------------------------");
									sender.sendMessage(ChatColor.RED + "Comments: " + ChatColor.GOLD + rs.get("ComN") + "      " + ChatColor.RED + "HotSpots: " + ChatColor.GOLD + rs.get("HSN"));
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									FancyMessage view = new FancyMessage("View: ").color(ChatColor.GOLD).then("[Comments]").color(ChatColor.RED).command("/tkt v comment " + id).tooltip("View this ticket's comments");
									view.then(" ").then("[HotSpots]").color(ChatColor.AQUA).command("/tkt v hotspot " + id).tooltip("View this ticket's teleportable hotspots");
									view.send(sender);
									FancyMessage add = new FancyMessage("Add: ").color(ChatColor.GOLD).then("[Comment]").color(ChatColor.LIGHT_PURPLE).suggest("/tkt com " + id +" ").tooltip("Add a new comment");
									add.then(" ").then("[HotSpot]").color(ChatColor.DARK_GREEN).suggest("/tkt hs " + id + " ").tooltip("Add a teleportable hotspot where you're standing");
									add.send(sender);
									if (sender.hasPermission("ultimateTickets.assign")){
										FancyMessage assignments = new FancyMessage("Assignments: ").color(ChatColor.GOLD).then("[Claim]").color(ChatColor.DARK_RED).command("/tkt claim " + id).tooltip("Claim this ticket");
										assignments.then(" ").then("[Assign to other]").color(ChatColor.DARK_PURPLE).suggest("/tkt assign " + id + " ").tooltip("Assign this ticket");
										assignments.send(sender);
									}
									if (sender.hasPermission("ultimateTickets.label")){
										FancyMessage labels = new FancyMessage("Label: ").color(ChatColor.GOLD);
										ChatColor lastColor = null;
										for (String lbl: labelsConfig.getKeys(false)){
											lastColor= getRandomColor(lastColor);
											labels.then(" ").then("[" + lbl + "]").color(lastColor).command("/tkt label " + id + " " + lbl).tooltip(labelsConfig.getString(lbl));
										}
										labels.then(" ").then("[Custom]").color(ChatColor.GRAY).suggest("/tkt label " + id + " ").tooltip("Use a custom label");
										labels.send(sender);
									}
									if (sender.hasPermission("ultimateTickets.teleport") && sender.hasPermission("ultimateTickets.close")){
										FancyMessage actions = new FancyMessage("Actions: ").color(ChatColor.GOLD).then("[Teleport]").color(ChatColor.DARK_PURPLE).command("/tkt tp " + id).tooltip("Teleport to the ticket place");
										actions.then(" ").then("[Close]").color(ChatColor.DARK_GRAY).command("/tkt c " + id).tooltip("Close this Ticket");
										actions.then(" ").then("[Reopen]").color(ChatColor.DARK_AQUA).command("/tkt ro " + id).tooltip("Reopen This ticket (if closed)");
										actions.send(sender);
									}
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									return true;
								}else{
									sender.sendMessage(logo +"Seems you're trying to touch a ticket that is not yours, check your ID, if you're sure this ticket is yours, contact the Administrators");
								}
							}
							if (args[1].equalsIgnoreCase("hotspot")){
								String id = args[2];
								Map<String, String> rs = getRDatabase().getTicketInfo(id);
								//if (rs.get("Owner").equalsIgnoreCase(((Player) sender).getName()) || sender.hasPermission("ultimateTickets.viewall")){
								if (rs.get("Owner").equalsIgnoreCase(((Player) sender).getUniqueId().toString()) || sender.hasPermission("ultimateTickets.viewall")){
									Map<String, String> cmts = getRDatabase().getHotSpots(id);
									if (cmts.size() == 0){
										sender.sendMessage(logo + "There are no hotspots available for this ticket");
									}else{
										sender.sendMessage(logo + "Available Hotspots:");
										for (Entry<String, String> item: cmts.entrySet()){
											FancyMessage msg = new FancyMessage("[" + item.getKey() + "] ").color(ChatColor.RED).command("/tkt tp " + id + " " + item.getKey()).tooltip("Teleport to this HotSpot").then(item.getValue()).color(ChatColor.GOLD);
											msg.send(sender);
											//sender.sendMessage(ChatColor.GOLD + item);
										}
									}
									return true;
								}else{
									sender.sendMessage(logo +"Seems you're trying to touch a ticket that is not yours, check your ID, if you're sure this ticket is yours, contact the Administrators");
								}
							}
							if (args[1].equalsIgnoreCase("comment")){
								String id = args[2];
								Map<String, String> rs = getRDatabase().getTicketInfo(id);
								//if (rs.get("Owner").equalsIgnoreCase(((Player) sender).getName()) || sender.hasPermission("ultimateTickets.viewall")){
								if (rs.get("Owner").equalsIgnoreCase(((Player) sender).getUniqueId().toString()) || sender.hasPermission("ultimateTickets.viewall")){
									getLogger().info("Getting comments for ticket: " + rs.toString());
									ArrayList<String> cmts = getRDatabase().getComments(id);
									if (cmts.size() == 0){
										sender.sendMessage(logo + "There are no comments available for this ticket");
									}else{
										sender.sendMessage(logo + "Available Comments:");
										for (String item: cmts){
											sender.sendMessage(ChatColor.GOLD + item);
										}
									}
									return true;
								}else{
									sender.sendMessage(logo +"Seems you're trying to touch a ticket that is not yours, check your ID, if you're sure this ticket is yours, contact the Administrators");
								}
							}
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("claim")){
						if (sender.hasPermission("ultimateTickets.assign")){
							//if (getRDatabase().setTicket("Assignee=\"" + ((Player) sender).getName() + "\" WHERE ID=" + args[1])==0){
							if (getRDatabase().setTicket("Assignee=\"" + ((Player) sender).getUniqueId().toString() + "\" WHERE ID=" + args[1])==0){
								sender.sendMessage(logo + "You have claimed this ticket");
								if (sendEmail){
									Map<String, String> rs = getRDatabase().getTicketInfo(args[1]);
									getServer().dispatchCommand(getServer().getConsoleSender(), "mail send " + rs.get("Owner") + " Your ticket with ID " + args[1] + " has been claimed, it will be fixed as soon as possible.");
								}
							}else{
								sender.sendMessage(logo + "The ticket could not be claimed.");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to claim tickets");
						}
					}
					if (args[0].equalsIgnoreCase("cachemyname")){
						if (sender.hasPermission("ultimateTickets.selfCaching")){
							Player pl=(Player) sender;
							cache.put(new Profile(pl.getUniqueId(), pl.getName()));
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
							try{
								ProfileService resolver = HttpRepositoryService.forMinecraft();
								Profile profile = resolver.findByName(args[2]);
								if (profile != null){
									cache.put(profile);
									if (getRDatabase().setTicket("Assignee=\"" + profile.getUniqueId().toString() + "\" WHERE ID=" + args[1])==0){
										sender.sendMessage(logo + "You have assigned this ticket");
									}else{
										sender.sendMessage(logo + "The ticket could not be assigned. Database error occurred.");
									}
								}else{
									sender.sendMessage(logo + "The ticket could not be assigned. The name doesn't exist, control your command.");
								}
							}catch (Exception e){
								sender.sendMessage(logo + "The ticket could not be assigned. Internal error occurred.");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to assign tickets");
						}
					}
					if (args[0].equalsIgnoreCase("close")||args[0].equalsIgnoreCase("c")){
						if(sender.hasPermission("ultimateTickets.close")){
							if(getRDatabase().setTicket("Status=\"Closed\" WHERE ID=" + args[1])==0){
								sender.sendMessage(logo + "Ticket closed");
								if (sendEmail){
									Map<String, String> rs = getRDatabase().getTicketInfo(args[1]);
									getServer().dispatchCommand(getServer().getConsoleSender(), "mail send " + rs.get("Owner") + " Your ticket with ID " + args[1] + " has been closed, hopefully everything is fixed for you.");
								}
							}else{
								sender.sendMessage(logo + "There has been an error in closing the ticket, are you sure you typed the correct arguments?");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to close this ticket");
						}
					}
					if (args[0].equalsIgnoreCase("reopen")||args[0].equalsIgnoreCase("ro")){
						if(sender.hasPermission("ultimateTickets.reopen")){
							if(getRDatabase().setTicket("Status=\"Open\" WHERE ID=" + args[1])==0){
								sender.sendMessage(logo + "Ticket Reopened");
								if (sendEmail){
									Map<String, String> rs = getRDatabase().getTicketInfo(args[1]);
									getServer().dispatchCommand(getServer().getConsoleSender(), "mail send " + rs.get("Owner") + " Your ticket with ID " + args[1] + " has been reopened.");
								}
							}else{
								sender.sendMessage(logo + "There has been an error in reopening the ticket, are you sure you typed the correct arguments?");
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to close this ticket");
						}
					}
					if (args[0].equalsIgnoreCase("comment")||args[0].equalsIgnoreCase("com")){
						String id = args[1];
						Map<String, String> rs = getRDatabase().getTicketInfo(id);
						//if (sender.hasPermission("ultimateTickets.commentOthers") || rs.get("Owner").equals(((Player) sender).getName())){
						if (sender.hasPermission("ultimateTickets.commentOthers") || rs.get("Owner").equals(((Player) sender).getUniqueId().toString())){
							String comment = "";
							for (int i = 2; i < args.length; i++){
								comment += args[i] + " ";
							}
							//int x=getRDatabase().addComment(id, ((Player) sender).getName(), comment);
							int x=getRDatabase().addComment(id, ((Player) sender).getUniqueId().toString(), comment);
							if (x==0){
								sender.sendMessage(logo + "Your comment has been added to ticket #" + id);
								//if (sendEmail && !rs.get("Owner").equalsIgnoreCase(((Player) sender).getName())){
								if (sendEmail && !rs.get("Owner").equalsIgnoreCase(((Player) sender).getUniqueId().toString())){
									getServer().dispatchCommand(getServer().getConsoleSender(), "mail send " + rs.get("Owner") + " Your ticket with ID " + args[1] + " has been commented, check it with /tkt v comment " + args[1] + ".");
								}
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
						//if (sender.hasPermission("ultimateTickets.commentOthers") || rs.get("Owner").equals(((Player) sender).getName())){
						if (sender.hasPermission("ultimateTickets.commentOthers") || rs.get("Owner").equals(((Player) sender).getUniqueId().toString())){
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
							if (args.length == 1){
								sender.sendMessage(logo + "This will purge the database of all closed tickets, type '/ticket purge force' to force the removal");
							}else{
								if (args[1].equalsIgnoreCase("force")){
									boolean done = getRDatabase().purgeTickets();
									if (done){
										sender.sendMessage(logo + "The Ticket database has been forcibly purged of closed tickets and comments");
									}else{
										sender.sendMessage(logo + "Ticket purge failed, please check console and logs for more information about the errors");
									}
								}
							}
							return true;
						}
					}
					if (args[0].equalsIgnoreCase("search") || args[0].equalsIgnoreCase("s")){
						if (sender.hasPermission("ultimateTickets.search")){
							if (args.length < 3){
								sender.sendMessage("Usage: /ticket search <owner|label> ID");
							}else{
								if (args[1].equalsIgnoreCase("owner")){
									sender.sendMessage(logo + "Fetching UUID for '" + args[2] + "', please wait");
									ProfileService resolver = HttpRepositoryService.forMinecraft();
									Profile owner = null;
									try {
										owner = resolver.findByName(args[2]);
									} catch (IOException e) {
										sender.sendMessage(logo + "There has been an exception while resolving the username, please check the logs or console for more information.");
										e.printStackTrace();
										return true;
									} catch (InterruptedException e) {
										sender.sendMessage(logo + "There has been an exception while resolving the username, please check the logs or console for more information.");
										e.printStackTrace();
										return true;
									}
									if (owner == null){
										sender.sendMessage(logo + "This username doesn't exist!");
										return true;
									}
									Map<String, String> rs = getRDatabase().getTicketHeaders("Status='Open' AND Owner LIKE '%" + owner.getUniqueId() + "%'");
									//TODO: There is no pagination!
									if (rs.size() == 0){
										sender.sendMessage(logo + "There are no tickets that match this search criteria");
									}else{
										sender.sendMessage(ChatColor.GOLD + "Tickets found that match your criteria:");
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
										for(Map.Entry<String, String> entry: rs.entrySet()){
											FancyMessage msg = new FancyMessage("[" + entry.getKey() + "] ").color(ChatColor.RED).command("/tkt v info " + entry.getKey()).tooltip("View this ticket's info").then(entry.getValue()).color(ChatColor.GOLD);
											msg.send(sender);
											//sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
										}
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									}
								}
								if (args[1].equalsIgnoreCase("label")){
									if (args[1].equalsIgnoreCase("label")){
										Map<String, String> rs = getRDatabase().getTicketHeaders("Status='Open' AND Label LIKE '%" + args[2] + "%'");
										if (rs.size() == 0){
											sender.sendMessage(logo + "There are no tickets that match this search criteria");
										}else{
											//TODO: There is no pagination!
											sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
											for(Map.Entry<String, String> entry: rs.entrySet()){
												FancyMessage msg = new FancyMessage("[" + entry.getKey() + "] ").color(ChatColor.RED).command("/tkt v info " + entry.getKey()).tooltip("View this ticket's info").then(entry.getValue()).color(ChatColor.GOLD);
												msg.send(sender);
												//sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
											}
											sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
										}
									}									
								}
							}
						}else{
							sender.sendMessage(logo + "You don't have permission to search through tickets.");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("list")||args[0].equalsIgnoreCase("l")){
						if (args.length < 2){
							sender.sendMessage("Usage: /ticket list <open|all|unassigned|mine|claimed>");
						}else{
							if (args[1].equalsIgnoreCase("open")){
								if (sender.hasPermission("ultimateTickets.listall")){
									Map<String, String> rs = getRDatabase().getTicketHeaders("Status='Open'");
									if (rs.size() == 0){
										sender.sendMessage(logo + "There are no tickets open! Good Job!");
									}else{
										//TODO: There is no pagination!
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
										for(Map.Entry<String, String> entry: rs.entrySet()){
											FancyMessage msg = new FancyMessage("[" + entry.getKey() + "] ").color(ChatColor.RED).command("/tkt v info " + entry.getKey()).tooltip("View this ticket's info").then(entry.getValue()).color(ChatColor.GOLD);
											msg.send(sender);
											//sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
										}
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									}
								}else{
									sender.sendMessage("You don't have permission to list open tickets");
								}
							}
							if (args[1].equalsIgnoreCase("unassigned")){
								if (sender.hasPermission("ultimateTickets.listall")){
									Map<String, String> rs = getRDatabase().getTicketHeaders("Assignee IS NULL AND Status='Open'");
									if (rs.size() == 0){
										sender.sendMessage(logo + "There are no unassigned tickets! Nice job on keeping everything clean and organized!");
									}else{
										//TODO: There is no pagination!
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
										for(Map.Entry<String, String> entry: rs.entrySet()){
											FancyMessage msg = new FancyMessage("[" + entry.getKey() + "] ").color(ChatColor.RED).command("/tkt v info " + entry.getKey()).tooltip("View this ticket's info").then(entry.getValue()).color(ChatColor.GOLD);
											msg.send(sender);
											//sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
										}
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");							
									}
								}else{
									sender.sendMessage("You don't have permission to list unassigned tickets");
								}
							}
							if (args[1].equalsIgnoreCase("claimed")){
								if (sender.hasPermission("ultimateTickets.listall")){
									//Map<String, String> rs=getRDatabase().getTicketHeaders("Status='Open' AND Assignee =\"" + ((Player) sender).getName() + "\"" );
									Map<String, String> rs=getRDatabase().getTicketHeaders("Status='Open' AND Assignee =\"" + ((Player) sender).getUniqueId().toString() + "\"" );
									if (rs.size() == 0){
										sender.sendMessage(logo + "There are no tickets assigned to you");
									}else{
										//TODO: There is no pagination!
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
										for(Map.Entry<String, String> entry: rs.entrySet()){
											FancyMessage msg = new FancyMessage("[" + entry.getKey() + "] ").color(ChatColor.RED).command("/tkt v info " + entry.getKey()).tooltip("View this ticket's info").then(entry.getValue()).color(ChatColor.GOLD);
											msg.send(sender);
											//sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
										}
										sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									}
								}else{
									sender.sendMessage("You don't have permission to list claimed tickets");
								}
							}
							if (args[1].equalsIgnoreCase("mine")){
								//Map<String, String> rs=getRDatabase().getTicketHeaders("Owner =\"" + ((Player) sender).getName() + "\" ORDER BY Status DESC" );
								Map<String, String> rs=getRDatabase().getTicketHeaders("Owner =\"" + ((Player) sender).getUniqueId().toString() + "\" ORDER BY Status DESC" );
								if (rs.size() == 0){
									sender.sendMessage(logo + "You have no tickets");
								}else{
									//TODO: There is no pagination!
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									for(Map.Entry<String, String> entry: rs.entrySet()){
										FancyMessage msg = new FancyMessage("[" + entry.getKey() + "] ").color(ChatColor.RED).command("/tkt v info " + entry.getKey()).tooltip("View this ticket's info").then(entry.getValue()).color(ChatColor.GOLD);
										msg.send(sender);
										//sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
									}
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								}
							}
							if (args[1].equalsIgnoreCase("all")){
								if (sender.hasPermission("ultimateTickets.listall")){
									//Workaround-ish
									Map<String, String> rs = getRDatabase().getTicketHeaders("1 = 1");
									//TODO: There is no pagination!
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
									for(Map.Entry<String, String> entry: rs.entrySet()){
										FancyMessage msg = new FancyMessage("[" + entry.getKey() + "] ").color(ChatColor.RED).command("/tkt v info " + entry.getKey()).tooltip("View this ticket's info").then(entry.getValue()).color(ChatColor.GOLD);
										msg.send(sender);
										//sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
									}
									sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
								}else{
									sender.sendMessage("You don't have permission to list all tickets");
								}
							}
						}	
					}
				}
			}else{
				sender.sendMessage(logo + "Only players can execute this command!");
			}
		}
		return true;
	}
}