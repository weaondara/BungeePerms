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
        
        networkType = config.getEnumValue("networktype", NetworkType.Global);
        if (networkType == NetworkType.ServerDependend || networkType == NetworkType.ServerDependendBlacklist)
        {
            networkServers = config.getListString("networkservers", Arrays.asList("lobby"));
        }
        else
        {
            //create option if not server dependend
            if(!config.keyExists("networkservers"))
            {
                config.setListString("networkservers", new ArrayList<String>());
            }
        }
    }
}
