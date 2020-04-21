/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.placeholderapi;

import java.util.stream.Collectors;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.BungeePermsAPI;
import net.alpenblock.bungeeperms.PermissionsManager;
import org.bukkit.entity.Player;

public class PlaceholderProvider extends PlaceholderExpansion {
    
    private PermissionsManager p = BungeePerms.getInstance().getPermissionsManager();

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

        if(player == null){
            return "";
        }

        if(identifier.equals("groups")){
            return BungeePermsAPI.userAllGroups(player.getName()).stream().collect(Collectors.joining(", "));
        }
        
        if(identifier.equals("primary_group_name")){
            return BungeePermsAPI.userMainGroup(player.getName());
        }
        
        if(identifier.equals("display_primary_group")){
            return p.getMainGroup(p.getUser(player.getName())).getDisplay();
        }
        
        if(identifier.equals("prefix")){
            return BungeePermsAPI.userPrefix(player.getName(), "", "");
        }
        
        if(identifier.equals("prefix_primary_group")){
            return p.getMainGroup(p.getUser(player.getName())).getPrefix();
        }
        
        if(identifier.equals("suffix")){
            return BungeePermsAPI.userSuffix(player.getName(), "", "");
        }
        
        if(identifier.equals("suffix_primary_group")){
            return p.getMainGroup(p.getUser(player.getName())).getSuffix();
        }
        
        if(identifier.startsWith("in_group_")){       
            return "" + BungeePermsAPI.userInGroup(player.getName(), identifier.substring("in_group_".length()));
        }
        
        if(identifier.startsWith("has_permission_")){       
            return "" + BungeePermsAPI.userHasPermission(player.getName(), identifier.substring("has_permission_".length()), "", "");
        }
        
        return null;
    }
}
