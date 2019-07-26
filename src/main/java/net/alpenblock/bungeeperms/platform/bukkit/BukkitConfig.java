package net.alpenblock.bungeeperms.platform.bukkit;

import lombok.Getter;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
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

        //perms
        servername = config.getString("permissions.servername", "servername");
        allowops = config.getBoolean("permissions.allowops", true);
        superpermscompat = config.getBoolean("permissions.superpermscompat", false);

        standalone = config.getBoolean("network.standalone", false);
    }

    public void setServerName(String servername)
    {
        this.servername = servername;
        config.setString("permissions.servername", servername);
        config.save();
        BungeePerms.getInstance().reload(false);
    }
}
