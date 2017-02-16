package main;

import java.io.File;
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
				if (args.length > 0){
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
					}
					if (args[0].equalsIgnoreCase("view")||args[0].equalsIgnoreCase("v")){
						//permission check here
						if (args.length < 2){
							sender.sendMessage("Usage: /ticket view ID");
						}else{
							String id = args[1];
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
					}
				}else{
					Map<String, String> rs = getRDatabase().getTicketHeaders();
					//TODO: There is no pagination!
					for(Map.Entry<String, String> entry: rs.entrySet()){
						sender.sendMessage(ChatColor.RED + "[" + entry.getKey() + "] " + ChatColor.GOLD + entry.getValue());
					}
				}
			}
		}
		return true;
	}
}