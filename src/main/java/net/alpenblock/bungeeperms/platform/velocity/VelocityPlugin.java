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
package net.alpenblock.bungeeperms.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.independend.GroupProcessor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Color;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TabCompleter;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PlatformType;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.alpenblock.bungeeperms.platform.ScheduledTask;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;
import net.kyori.text.TextComponent;

@Plugin(id = "bungeeperms", name = "BungeePerms", version = "dev", authors =
{
    "wea_ondara", "AuroraRainbow"
})
@Getter
public class VelocityPlugin implements PlatformPlugin
{

    @Getter
    private static VelocityPlugin instance;

    public static final ChannelIdentifier CHANNEL_ID;

    static
    {
        String[] split = BungeePerms.CHANNEL.split(":");
        CHANNEL_ID = MinecraftChannelIdentifier.create(split[0], split[1]);
    }

    private final ProxyServer proxyServer;
    private final Logger logger;

    private ProxyConfig config;

    //platform dependend parts
    private VelocityEventListener listener;
    private VelocityEventDispatcher dispatcher;
    private VelocityNotifier notifier;
    private PluginMessageSender pmsender;

    private BungeePerms bungeeperms;

    private final PlatformType platformType = PlatformType.Velocity;

    @Inject
    public VelocityPlugin(ProxyServer proxyServer, Logger logger) //onLoad
    {
        //static
        instance = this;

        //init
        this.proxyServer = proxyServer;
        this.logger = logger;

        //metrics
        //startMetrics();

        //load config
        Config conf = new Config(this, "/config.yml");
        conf.load();
        config = new ProxyConfig(conf);
        config.load();

        //register commands
        loadcmds();

        listener = new VelocityEventListener(config, proxyServer);
        dispatcher = new VelocityEventDispatcher();
        notifier = new VelocityNotifier(config);
        pmsender = new VelocityPluginMessageSender();

        bungeeperms = new BungeePerms(this, config, pmsender, notifier, listener, dispatcher);
        bungeeperms.load();
        bungeeperms.getPermissionsResolver().registerProcessor(new GroupProcessor());
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) //onEnable
    {
        proxyServer.getChannelRegistrar().register(CHANNEL_ID);
        bungeeperms.enable();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) //onDisable
    {
        bungeeperms.disable();
        proxyServer.getChannelRegistrar().unregister(CHANNEL_ID);
    }

    public boolean onCommand(CommandSource sender, Command cmd, String label, String[] args)
    {
        return bungeeperms.getCommandHandler().onCommand(new VelocitySender(sender), "bungeeperms", label, args);
    }

    public List<String> onTabComplete(CommandSource sender, Command cmd, String label, String[] args)
    {
        return TabCompleter.tabComplete(new VelocitySender(sender), args);
//        List<String> l = new ArrayList<>();
//        if (!conf.isTabComplete() || args.length == 0)
//            return l;
//
//        for (Player p : getBukkitPlayers())
//        {
//            if (Statics.toLower(p.getName()).startsWith(Statics.toLower(args[args.length - 1])))
//            {
//                l.add(p.getName());
//            }
//        }
//
//        return l;
    }

    private void loadcmds()
    {
        CmdExec cmd = new CmdExec("bungeeperms", null, config.isAliasCommand() ? Statics.array("bp") : new String[0]);
        proxyServer.getCommandManager().register(cmd, config.isAliasCommand() ? Statics.array("bp") : new String[0]);
    }

//plugin info
    @Override
    public String getPluginName()
    {
        Plugin p = this.getClass().getAnnotation(Plugin.class);
        return p.name();
    }

    @Override
    public String getVersion()
    {
        Plugin p = this.getClass().getAnnotation(Plugin.class);
        return p.version();
    }

    @Override
    public String getAuthor()
    {
        Plugin p = this.getClass().getAnnotation(Plugin.class);
        return Arrays.stream(p.authors()).collect(Collectors.joining(", "));
    }

    @Override
    public String getPluginFolderPath()
    {
        return getPluginFolder().getPath();
    }

    @Override
    public File getPluginFolder()
    {
        return new File(System.getProperty("user.dir"), "plugins/BungeePerms");
    }

    @Override
    public VelocitySender getPlayer(String name)
    {
        CommandSource sender = proxyServer.getPlayer(name).get();

        VelocitySender s = null;

        if (sender != null)
        {
            s = new VelocitySender(sender);
        }

        return s;
    }

    @Override
    public VelocitySender getPlayer(UUID uuid)
    {
        CommandSource sender = proxyServer.getPlayer(uuid).get();

        VelocitySender s = null;

        if (sender != null)
        {
            s = new VelocitySender(sender);
        }

        return s;
    }

    @Override
    public VelocitySender getConsole()
    {
        return new VelocitySender(proxyServer.getConsoleCommandSource());
    }

    @Override
    public List<Sender> getPlayers()
    {
        List<Sender> senders = new ArrayList<>();

        for (Player pp : proxyServer.getAllPlayers())
        {
            senders.add(new VelocitySender(pp));
        }

        return senders;
    }

    @Override
    public boolean isChatApiPresent()
    {
        return true;
    }

    @Override
    public MessageEncoder newMessageEncoder()
    {
        return new VelocityMessageEncoder("");
    }

    @Override
    public ScheduledTask registerRepeatingTask(Runnable r, long delay, long interval)
    {
        return new VelocityScheduledTask(proxyServer.getScheduler().buildTask(this, r).repeat(interval, TimeUnit.MILLISECONDS).delay(delay, TimeUnit.MILLISECONDS).schedule());
    }

    @Override
    public ScheduledTask runTaskLater(Runnable r, long delay)
    {
        return new VelocityScheduledTask(proxyServer.getScheduler().buildTask(this, r).delay(delay, TimeUnit.MILLISECONDS).schedule());
    }

    @Override
    public ScheduledTask runTaskLaterAsync(Runnable r, long delay)
    {
        return new VelocityScheduledTask(proxyServer.getScheduler().buildTask(this, r).delay(delay, TimeUnit.MILLISECONDS).schedule());
    }

    @Override
    public Integer getBuild()
    {
        return Statics.getBuild(this);
    }

    private void startMetrics()
    {
        try
        {
            Class c = Class.forName("net.alpenblock.bungeeperms.metrics.bungee.Metrics");
            Constructor cc = c.getConstructor(Plugin.class);
            cc.newInstance(this);
        }
        catch (Exception ex)
        {
            getLogger().severe("Could not start metrics!");
        }
    }

    private class CmdExec implements Command
    {

        public CmdExec(String name, String permission, String... aliases)
        {
        }

        @Override
        public void execute(final CommandSource sender, final String[] args)
        {
            final Command cmd = this;
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    if (!VelocityPlugin.this.onCommand(sender, cmd, "", args))
                        sender.sendMessage(TextComponent.of(Color.Error + "[BungeePerms] Command not found"));
                }
            };
            if (config.isAsyncCommands())
                proxyServer.getScheduler().buildTask(instance, r).schedule();
            else
                r.run();
        }

        @Override
        public List<String> suggest(CommandSource sender, String[] args)
        {
            return VelocityPlugin.this.onTabComplete(sender, this, "", args);
        }
    }
}
