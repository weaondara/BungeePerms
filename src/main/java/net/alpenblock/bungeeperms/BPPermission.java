package net.alpenblock.bungeeperms;

public class BPPermission 
{
    private String permission;
    private String origin;
    private boolean isGroup;
    private String server;
    private String world;

    public BPPermission(String permission, String origin, boolean isGroup, String server, String world) {
        this.permission = permission;
        this.origin = origin;
        this.isGroup = isGroup;
        this.server = server;
        this.world = world;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }
    
}
