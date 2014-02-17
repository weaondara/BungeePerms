package net.alpenblock.bungeeperms;

import java.util.List;
import java.util.Map;

public class Server 
{
    private String server;
    private List<String> perms;
    private String display;
    private String prefix;
    private String suffix;
    private Map<String, World> worlds;
    public Server(String server, List<String> perms, Map<String, World> worlds, String display, String prefix, String suffix) 
    {
		this.server = server;
		this.perms = perms;
		this.display = display;
		this.prefix = prefix;
		this.suffix = suffix;
		this.worlds = worlds;
	}
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public List<String> getPerms() {
        return perms;
    }
    public void setPerms(List<String> perms) {
        this.perms = perms;
    }
    public String getDisplay() {
        return display;
    }
    public void setDisplay(String display) {
        this.display = display;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getSuffix() {
        return suffix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    public Map<String, World> getWorlds() {
        return worlds;
    }
    public void setWorlds(Map<String, World> worlds) {
        this.worlds = worlds;
    }
    
    @Override
    public String toString()
    {
        String string="server="+server+" "
            +"perms="+perms+" "
            +"display="+display+" "
            +"prefix="+prefix+" "
            +"suffix="+suffix+" "
            +"worlds="+worlds;
        return string;
    }
}
