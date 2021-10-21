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
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.PostOrder;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Color;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TabCompleter;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PlatformType;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.alpenblock.bungeeperms.platform.ScheduledTask;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;
import net.kyori.adventure.text.Component;

@Plugin(id = "bungeeperms", name = "BungeePerms", version = "@version@", authors =
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
    public VelocityPlugin(ProxyServer proxyServer, org.slf4j.Logger logger) //onLoad
    {
        //static
        instance = this;

        //init
        this.proxyServer = proxyServer;
        this.logger = new L4JWrapper(logger);

        //metrics
        //startMetrics();
        
        //load config
        Config conf = new Config(this, "/config.yml");
        conf.load();
        config = new ProxyConfig(conf);
        config.load();

        //register commands
        loadcmds();

        listener = new VelocityEventListener(proxyServer, config);
        dispatcher = new VelocityEventDispatcher(proxyServer);
        notifier = new VelocityNotifier(proxyServer, config);
        pmsender = new VelocityPluginMessageSender(proxyServer);

        bungeeperms = new BungeePerms(this, config, pmsender, notifier, listener, dispatcher);
        bungeeperms.load();
        bungeeperms.getPermissionsResolver().registerProcessor(new GroupProcessor());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onEnable(ProxyInitializeEvent event) //onEnable
    {
        //metrics
        startMetrics();
        
        proxyServer.getChannelRegistrar().register(CHANNEL_ID);
        bungeeperms.enable();
    }

    @Subscribe(order = PostOrder.LAST)
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
        proxyServer.getCommandManager().register("bungeeperms", cmd, config.isAliasCommand() ? Statics.array("bp") : new String[0]);
    }

//plugin info
    @Override
    public String getPluginName()
    {
        return proxyServer.getPluginManager().fromInstance(this).get().getDescription().getName().get();
//        Plugin p = this.getClass().getAnnotation(Plugin.class);
//        return p.name();
    }

    @Override
    public String getVersion()
    {
        return proxyServer.getPluginManager().fromInstance(this).get().getDescription().getVersion().get();
//        Plugin p = this.getClass().getAnnotation(Plugin.class);
//        return p.version();
    }

    @Override
    public String getAuthor()
    {
        return String.join(", ", proxyServer.getPluginManager().fromInstance(this).get().getDescription().getAuthors());
//        Plugin p = this.getClass().getAnnotation(Plugin.class);
//        return Arrays.stream(p.authors()).collect(Collectors.joining(", "));
    }

    @Override
    public String getPluginFolderPath()
    {
        return getPluginFolder().getPath();
    }

    @Override
    public File getPluginFolder()
    {
//        File file = new File(proxyServer.getPluginManager().fromInstance(this).get().getDescription().getSource().get().toFile().getParentFile(), getPluginName());
//        System.out.println("file: "+file.getAbsolutePath());
//        return file;
        return new File(System.getProperty("user.dir"), "plugins/BungeePerms");
    }

    @Override
    public VelocitySender getPlayer(String name)
    {
        CommandSource sender = proxyServer.getPlayer(name).orElse(null);

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
        CommandSource sender = proxyServer.getPlayer(uuid).orElse(null);

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
            Class cpd = Class.forName("net.alpenblock.bungeeperms.metrics.velocity.Metrics$PluginData");
            Object plugindata = cpd.getConstructor(Object.class, ProxyServer.class, org.slf4j.Logger.class).newInstance(this, proxyServer, ((L4JWrapper)logger).parent);
            Class c = Class.forName("net.alpenblock.bungeeperms.metrics.velocity.Metrics");
            Constructor cc = c.getConstructor(cpd);
            cc.newInstance(plugindata);
        }
        catch (Exception ex)
        {
            getLogger().severe("Could not start metrics!");
        }
    }

    private class CmdExec implements SimpleCommand
    {

        public CmdExec(String name, String permission, String... aliases)
        {
        }

        @Override
        public void execute(Invocation invocation)
        {
            final Command cmd = this;
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    if (!VelocityPlugin.this.onCommand(invocation.source(), cmd, "", invocation.arguments()))
                        invocation.source().sendMessage(Component.text(Color.Error + "[BungeePerms] " + Lang.translate(Lang.MessageType.COMMAND_NOT_FOUND)));
                }
            };
            if (config.isAsyncCommands())
                proxyServer.getScheduler().buildTask(instance, r).schedule();
            else
                r.run();
        }

        @Override
        public List<String> suggest(Invocation invocation)
        {
            return VelocityPlugin.this.onTabComplete(invocation.source(), this, "", invocation.arguments());
        }
    }

    private static class L4JWrapper extends Logger
    {

        private final org.slf4j.Logger parent;

        public L4JWrapper(org.slf4j.Logger parent)
        {
            super(parent.getName(), null);
            this.parent = parent;
        }

        @Override
        public void info(String msg)
        {
            parent.info(msg);
        }

        @Override
        public void warning(String msg)
        {
            parent.warn(msg);
        }

        @Override
        public void severe(String msg)
        {
            parent.error(msg);
        }
    }
}
