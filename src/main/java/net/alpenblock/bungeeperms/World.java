package net.alpenblock.bungeeperms;

import java.util.List;

public class World 
{
    private String world;
    private List<String> perms;
    private String display;
    private String prefix;
    private String suffix;
    public World(String world, List<String> perms, String display, String prefix, String suffix) 
    {
		this.world = world;
		this.perms = perms;
		this.display = display;
		this.prefix = prefix;
		this.suffix = suffix;
	}

    public String getWorld() {
        return world;
    }
    public void setWorld(String world) {
        this.world = world;
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
    
}
