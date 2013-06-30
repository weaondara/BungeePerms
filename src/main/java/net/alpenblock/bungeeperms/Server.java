package net.alpenblock.bungeeperms;

import java.util.List;

/**
 *
 * @author Alex
 */
public class Server 
{
    private String server;
    private List<String> perms;
    private String display;
    private String prefix;
    private String suffix;
    public Server(String server, List<String> perms, String display, String prefix, String suffix) 
    {
		this.server = server;
		this.perms = perms;
		this.display = display;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	/**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return the perms
     */
    public List<String> getPerms() {
        return perms;
    }

    /**
     * @param perms the perms to set
     */
    public void setPerms(List<String> perms) {
        this.perms = perms;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return display;
    }

    /**
     * @param display the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * @param suffix the suffix to set
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
}
