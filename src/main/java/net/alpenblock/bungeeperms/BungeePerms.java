package net.alpenblock.bungeeperms;

import java.util.logging.Logger;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.EventListener;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;

@Getter
public class BungeePerms
{

    public final static String CHANNEL = "bungeeperms";
    
    @Getter
    private static BungeePerms instance;
    @Getter
    private static Logger logger;

    private final PlatformPlugin plugin;
    private final BPConfig config;
    private final Debug debug;

    private final PermissionsManager permissionsManager;
    private final CommandHandler commandHandler;
    private final PermissionsChecker permissionsChecker;
    private final PluginMessageSender pluginMessageSender;
    private final NetworkNotifier networkNotifier;
    private final EventListener eventListener;

    public BungeePerms(PlatformPlugin plugin, BPConfig config, PluginMessageSender pluginMessageSender, NetworkNotifier networkNotifier, EventListener eventListener)
    {
        //static
        instance = this;
        logger = plugin.getLogger();
        
        //basic
        this.plugin = plugin;
        this.config = config;
        debug = new Debug(plugin, config.getConfig(), "BP");
        
        //adv
        permissionsManager = new PermissionsManager(config, debug);
        permissionsChecker = new PermissionsChecker();
        commandHandler = new CommandHandler(plugin, permissionsChecker, config);
        this.pluginMessageSender = pluginMessageSender;
        this.networkNotifier = networkNotifier;
        this.eventListener = eventListener;
    }

    public void onLoad()
    {
        //static
        //check for config file existance
//        File f = new File(plugin.getPluginFolder(), "/config.yml");
//        if (!f.exists() | !f.isFile())
//        {
//            bc.getLogger().info("[BungeePerms] no config file found -> copy packed default config.yml to data folder ...");
//            f.getParentFile().mkdirs();
//            try
//            {
//                //file öffnen
//                ClassLoader cl = this.getClass().getClassLoader();
//                URL url = cl.getResource("config.yml");
//                if (url != null)
//                {
//                    URLConnection connection = url.openConnection();
//                    connection.setUseCaches(false);
//                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(connection.getInputStream());
//                    defConfig.save(f);
//                }
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            bc.getLogger().info("[BungeePerms] copied default config.yml to data folder");
//        }
    }

    public void onEnable()
    {
        logger.info("Activating BungeePerms ...");
        permissionsManager.enable();
        eventListener.enable();
    }

    public void onDisable()
    {
        logger.info("Deactivating BungeePerms ...");
        eventListener.disable();
        permissionsManager.disable();
    }
}
