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
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TabCompleter;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PlatformType;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;

@Plugin(id = "BungeePerms", name = "BungeePerms", version = "1", authors = {"wea_ondara", "AuroraRainbow"})
@Getter
public class VelocityPlugin implements PlatformPlugin
{

    @Getter
    private static VelocityPlugin instance;
   
    @Getter
    private static final ChannelIdentifier ci;
    static{
        String[] split = BungeePerms.CHANNEL.split(":");
        ci = MinecraftChannelIdentifier.create(split[0], split[1]);
    }
    
    @Inject
    private ProxyServer proxyServer;

    private ProxyConfig config;

    //platform dependend parts
    private VelocityEventListener listener;
    private VelocityEventDispatcher dispatcher;
    private VelocityNotifier notifier;
    private PluginMessageSender pmsender;

    private BungeePerms bungeeperms;

    private final PlatformType platformType = PlatformType.Velocity;

    @Override
    public void onLoad()
    {
        //static
        instance = this;

        //metrics
        startMetrics();

        //load config
        Config conf = new Config(this, "/config.yml");
        conf.load();
        config = new ProxyConfig(conf);
        config.load();

        //register commands
        loadcmds();

        listener = new VelocityEventListener(config);
        dispatcher = new VelocityEventDispatcher();
        notifier = new VelocityNotifier(config);
        pmsender = new VelocityPluginMessageSender();

        bungeeperms = new BungeePerms(this, config, pmsender, notifier, listener, dispatcher);
        bungeeperms.load();
        bungeeperms.getPermissionsResolver().registerProcessor(new GroupProcessor());
    }

    @Override
    public void onEnable()
    {
        proxyServer.getChannelRegistrar().register(ci);
        bungeeperms.enable();
    }

    @Override
    public void onDisable()
    {
        bungeeperms.disable();
        proxyServer.getChannelRegistrar().unregister(ci);
    }

    public boolean onCommand(CommandSource sender, Command cmd, String label, String[] args)
    {
        return bungeeperms.getCommandHandler().onCommand(new VelocitySender(sender), "bungeeperms", label, args);
    }

    public List<String> onTabComplete(CommandSource sender, CommandSource cmd, String label, String[] args)
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
        return this.getPluginName();
    }

    @Override
    public String getVersion()
    {
        return this.getVersion();
    }

    @Override
    public String getAuthor()
    {
        return this.getAuthor();
    }

    @Override
    public String getPluginFolderPath()
    {
        return this.getPluginFolderPath();
    }

    @Override
    public File getPluginFolder()
    {
        return this.getPluginFolder();
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
    public int registerRepeatingTask(Runnable r, long delay, long interval)
    {
        return proxyServer.getScheduler().buildTask(this, r).repeat(interval, TimeUnit.MILLISECONDS).delay(delay, TimeUnit.MILLISECONDS).schedule();
    }

    @Override
    public int runTaskLater(Runnable r, long delay)
    {
        return proxyServer.getScheduler().buildTask(this, r).delay(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int runTaskLaterAsync(Runnable r, long delay)
    {
        return proxyServer.getScheduler().buildTask(this, r).delay(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelTask(int id)
    {
        proxyServer.getScheduler().cancel(id);
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

    @Override
    public Logger getLogger() {
        return this.getLogger();
    }

    private class CmdExec extends Command implements TabExecutor
    {

        public CmdExec(String name, String permission, String... aliases)
        {
            super(name, permission, aliases);
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
                        sender.sendMessage(Color.Error + "[BungeePerms] Command not found");
                }
            };
            if (config.isAsyncCommands())
                proxyServer.getScheduler().runAsync(instance, r);
            else
                r.run();
        }

        @Override
        public Iterable<String> onTabComplete(CommandSource sender, String[] args)
        {
            return VelocityPlugin.this.onTabComplete(sender, this, "", args);
        }
    }
}
