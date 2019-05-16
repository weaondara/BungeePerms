package net.alpenblock.bungeeperms.platform.bukkit;

import lombok.Getter;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.Config;

@Getter
public class BukkitConfig extends BPConfig
{

    private String servername;
    private boolean allowops;
    private boolean superpermscompat;
    
    private boolean standalone;

    public BukkitConfig(Config config)
    {
        super(config);
    }

    @Override
    public void load()
    {
        super.load();
        servername = getConfig().getString("servername", "servername");
        allowops = getConfig().getBoolean("allowops", true);
        superpermscompat = getConfig().getBoolean("superpermscompat", false);
        
        standalone = getConfig().getBoolean("standalone", false);
    }
}
