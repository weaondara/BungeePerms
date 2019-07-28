package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.alpenblock.bungeeperms.BungeePerms;
import org.bukkit.entity.Player;

public class PlaceholderProvider extends PlaceholderExpansion {

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return BungeePerms.getInstance().getPlugin().getAuthor();
    }

    @Override
    public String getIdentifier(){
        return "bungeeperms";
    }

    @Override
    public String getVersion(){
        return BungeePerms.getInstance().getPlugin().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

//        if(player == null){
//            return "";
//        }
//
//        // %someplugin_placeholder1%
//        if(identifier.equals("placeholder1")){
//            return plugin.getConfig().getString("placeholder1", "value doesnt exist");
//        }
//
//        // %someplugin_placeholder2%
//        if(identifier.equals("placeholder2")){
//            return plugin.getConfig().getString("placeholder2", "value doesnt exist");
//        }
// 
//        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%) 
//        // was provided
        return null;
    }
}
