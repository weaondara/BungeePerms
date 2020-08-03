/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms;

import java.util.logging.Logger;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.EventDispatcher;
import net.alpenblock.bungeeperms.platform.EventListener;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.alpenblock.bungeeperms.platform.ScheduledTask;
import net.alpenblock.bungeeperms.platform.independend.VersionCheck;

@Getter
public class BungeePerms
{

    public final static String CHANNEL = "bungeeperms:main";

    @Getter
    private static BungeePerms instance;
    @Getter
    private static Logger logger = Logger.getLogger("bungeeperms");

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
    private ScheduledTask cleanupTaskId;

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
        debug = new Debug(plugin, config, "BP");

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
        permissionsResolver.setResolvingMode(config.getResolvingMode());
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
        plugin.runTaskLaterAsync(new Runnable()
        {
            @Override
            public void run()
            {
                VersionCheck.checkForUpdate();
            }
        }, 1000);
    }

    public void disable()
    {
        if (!enabled)
        {
            return;
        }
        enabled = false;

        logger.info("Deactivating BungeePerms ...");
        if (cleanupTaskId != null)
            cleanupTaskId.cancel();
        cleanupTaskId = null;
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
