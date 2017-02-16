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
				if (args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("n")){
					//permission check here
					String desc = "";
					for (int i=1; i < args.length; i++){
						desc += args[i] + " ";
					}
					int x = getRDatabase().createTicket((Player) sender, desc);
					return x == 0;
				}
				if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")){
					//permission check here
					if (args.length < 3){
						String id = args[1];
						Map<String, String> rs = getRDatabase().getTicketLocation(id);
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
						//TODO: Stub for Hotspots
					}
				}
				if (args[0].equalsIgnoreCase("view")||args[0].equalsIgnoreCase("v")){
					//permission check here
					if (args.length < 3){
						sender.sendMessage("Usage: /ticket view <comment|hotspot|info> ID");
					}else{
						if (args[1].equalsIgnoreCase("info")){
							String id = args[2];
							Map<String, String> rs = getRDatabase().getTicketInfo(id);
							rs.putAll(getRDatabase().getTicketLocation(id));
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
							//TODO: Stub
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
				if (args[0].equalsIgnoreCase("close")||args[0].equalsIgnoreCase("c")){
					String id = args[1];
					// TODO: Return message to user and verify for no ID
					return getRDatabase().closeTicket(id) == 0;
				}
				if (args[0].equalsIgnoreCase("comment")||args[0].equalsIgnoreCase("com")){
					String id = args[1];
					String comment = "";
					for (int i = 2; i < args.length; i++){
						comment += args[i] + " ";
					}
					return getRDatabase().addComment(id, comment) == 0;
				}
				if (args[0].equalsIgnoreCase("hotspot")||args[0].equalsIgnoreCase("hs")){
					//TODO: Stub
				}
				if (args[0].equalsIgnoreCase("purge")){
					//TODO: Stub
				}
				if (args[0].equalsIgnoreCase("list")||args[0].equalsIgnoreCase("l")){
					if (args.length < 2){
						sender.sendMessage("Usage: /ticket list <open|all|unassigned|mine>");
					}else{
						if (args[1].equalsIgnoreCase("open")){
							Map<String, String> rs = getRDatabase().getTicketHeaders();
							//TODO: There is no pagination!
							sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");
							for(Map.Entry<String, String> entry: rs.entrySet()){
								sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
							}
							sender.sendMessage(ChatColor.GOLD + "--------------------<>--------------------");							
						}
						if (args[1].equalsIgnoreCase("unassigned")){
							//TODO: Stub
						}
						if (args[1].equalsIgnoreCase("mine")){
							//TODO: Stub
						}
						if (args[1].equalsIgnoreCase("all")){
							//TODO: Stub
						}
					}	
				}
			}
		}
		return true;
	}
}