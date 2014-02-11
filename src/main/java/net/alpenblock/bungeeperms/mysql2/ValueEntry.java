package net.alpenblock.bungeeperms.mysql2;

public class ValueEntry 
{
    private String value;
    private String server;
    private String world;

    public ValueEntry(String value, String server, String world) {
        this.value = value;
        this.server = server;
        this.world = world;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
