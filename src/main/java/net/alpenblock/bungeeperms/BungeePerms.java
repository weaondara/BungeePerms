package net.alpenblock.bungeeperms;

import java.util.logging.Logger;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.EventDispatcher;
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
    private final EventDispatcher eventDispatcher;
    private final PermissionsResolver permissionsResolver;
    private final CleanupTask cleanupTask;
    private int cleanupTaskId = -1;

    private boolean enabled;

    public BungeePerms(PlatformPlugin plugin, BPConfig config, PluginMessageSender pluginMessageSender,
            NetworkNotifier networkNotifier, EventListener eventListener, EventDispatcher eventDispatcher)
    {
        //static
        instance = this;
        logger = plugin.getLogger();

        //basic
        this.plugin = plugin;
        this.config = config;
        debug = new Debug(plugin, config.getConfig(), "BP");

        //extract packed files
        FileExtractor.extractAll();
        Lang.load(plugin.getPluginFolderPath() + "/lang/" + Statics.localeString(config.getLocale()) + ".yml"); //early load needed

        //adv
        permissionsManager = new PermissionsManager(plugin, config, debug);
        permissionsChecker = new PermissionsChecker();
        commandHandler = new CommandHandler(plugin, permissionsChecker, config);
        this.pluginMessageSender = pluginMessageSender;
        this.networkNotifier = networkNotifier;
        this.eventListener = eventListener;
        this.eventDispatcher = eventDispatcher;
        permissionsResolver = new PermissionsResolver();
        cleanupTask = new CleanupTask();
    }

    public void load()
    {
        Lang.load(plugin.getPluginFolderPath() + "/lang/" + Statics.localeString(config.getLocale()) + ".yml");
        permissionsResolver.setUseRegex(config.isUseRegexPerms());
    }

    public void enable()
    {
        if (enabled)
        {
            return;
        }
        enabled = true;

        logger.info("Activating BungeePerms ...");
        permissionsManager.enable();
        eventListener.enable();
        cleanupTaskId = plugin.registerRepeatingTask(cleanupTask, 0, config.getCleanupInterval() * 1000);
    }

    public void disable()
    {
        if (!enabled)
        {
            return;
        }
        enabled = false;

        logger.info("Deactivating BungeePerms ...");
        plugin.cancelTask(cleanupTaskId);
        cleanupTaskId = -1;
        eventListener.disable();
        permissionsManager.disable();
    }

    public void reload(boolean notifynetwork)
    {
        disable();
        load();
        permissionsManager.reload();
        if (notifynetwork)
        {
            networkNotifier.reloadAll("");
        }
        enable();
    }
}
