package net.alpenblock.bungeeperms.platform.bungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.Config;

@Getter
public class BungeeConfig extends BPConfig
{

    private NetworkType networkType;
    private List<String> networkServers;

    public BungeeConfig(Config config)
    {
        super(config);
    }

    @Override
    public void load()
    {
        super.load();

        networkType = config.getEnumValue("network.type", NetworkType.Global);
        if (networkType == NetworkType.ServerDependend || networkType == NetworkType.ServerDependendBlacklist)
        {
            networkServers = config.getListString("network.servers", Arrays.asList("lobby"));
        }
        else
        {
            //create option if not server dependend
            if (!config.keyExists("network.servers"))
            {
                config.setListString("network.servers", new ArrayList<String>());
            }
        }
    }

    @Override
    protected void migrate0to1(Config oldconf, Config newconf)
    {
        super.migrate0to1(oldconf, newconf);

        newconf.setEnumValue("network.type", oldconf.getEnumValue("networktype", NetworkType.Global));
        newconf.setListString("network.servers", oldconf.getListString("networkservers", new ArrayList()));
    }
}
