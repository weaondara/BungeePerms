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
package net.alpenblock.bungeeperms.platform.bungee;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Color;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TabCompleter;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PlatformType;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.alpenblock.bungeeperms.platform.ScheduledTask;
import net.alpenblock.bungeeperms.platform.independend.GroupProcessor;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

@Getter
public class BungeePlugin extends Plugin implements PlatformPlugin
{

    @Getter
    private static BungeePlugin instance;

    private ProxyConfig config;

    //platform dependend parts
    private BungeeEventListener listener;
    private BungeeEventDispatcher dispatcher;
    private BungeeNotifier notifier;
    private PluginMessageSender pmsender;

    private BungeePerms bungeeperms;

    private final PlatformType platformType = PlatformType.BungeeCord;

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

        listener = new BungeeEventListener(config);
        dispatcher = new BungeeEventDispatcher();
        notifier = new BungeeNotifier(config);
        pmsender = new BungeePluginMessageSender();

        bungeeperms = new BungeePerms(this, config, pmsender, notifier, listener, dispatcher);
        bungeeperms.load();
        bungeeperms.getPermissionsResolver().registerProcessor(new GroupProcessor());
    }

    @Override
    public void onEnable()
    {
        ProxyServer.getInstance().registerChannel(BungeePerms.CHANNEL);
        bungeeperms.enable();
    }

    @Override
    public void onDisable()
    {
        bungeeperms.disable();
        ProxyServer.getInstance().unregisterChannel(BungeePerms.CHANNEL);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        return bungeeperms.getCommandHandler().onCommand(new BungeeSender(sender), cmd.getName(), label, args);
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
    {
        return TabCompleter.tabComplete(new BungeeSender(sender), args);
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
        ProxyServer.getInstance().getPluginManager().registerCommand(this, cmd);
    }

//plugin info
    @Override
    public String getPluginName()
    {
        return this.getDescription().getName();
    }

    @Override
    public String getVersion()
    {
        return this.getDescription().getVersion();
    }

    @Override
    public String getAuthor()
    {
        return this.getDescription().getAuthor();
    }

    @Override
    public String getPluginFolderPath()
    {
        return this.getDataFolder().getAbsolutePath();
    }

    @Override
    public File getPluginFolder()
    {
        return this.getDataFolder();
    }

    @Override
    public Sender getPlayer(String name)
    {
        CommandSender sender = ProxyServer.getInstance().getPlayer(name);

        Sender s = null;

        if (sender != null)
        {
            s = new BungeeSender(sender);
        }

        return s;
    }

    @Override
    public Sender getPlayer(UUID uuid)
    {
        CommandSender sender = ProxyServer.getInstance().getPlayer(uuid);

        Sender s = null;

        if (sender != null)
        {
            s = new BungeeSender(sender);
        }

        return s;
    }

    @Override
    public Sender getConsole()
    {
        return new BungeeSender(ProxyServer.getInstance().getConsole());
    }

    @Override
    public List<Sender> getPlayers()
    {
        List<Sender> senders = new ArrayList<>();

        for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers())
        {
            senders.add(new BungeeSender(pp));
        }

        return senders;
    }

    @Override
    public boolean isChatApiPresent()
    {
        try
        {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            return true;
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    @Override
    public MessageEncoder newMessageEncoder()
    {
        return new BungeeMessageEncoder("");
    }

    @Override
    public ScheduledTask registerRepeatingTask(Runnable r, long delay, long interval)
    {
        return new BungeeScheduledTask(ProxyServer.getInstance().getScheduler().schedule(this, r, delay, interval, TimeUnit.MILLISECONDS));
    }

    @Override
    public ScheduledTask runTaskLater(Runnable r, long delay)
    {
        return new BungeeScheduledTask(ProxyServer.getInstance().getScheduler().schedule(this, r, delay, TimeUnit.MILLISECONDS));
    }

    @Override
    public ScheduledTask runTaskLaterAsync(Runnable r, long delay)
    {
        return new BungeeScheduledTask(ProxyServer.getInstance().getScheduler().schedule(this, r, delay, TimeUnit.MILLISECONDS));
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

    private class CmdExec extends Command implements TabExecutor
    {

        public CmdExec(String name, String permission, String... aliases)
        {
            super(name, permission, aliases);
        }

        @Override
        public void execute(final CommandSender sender, final String[] args)
        {
            final Command cmd = this;
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    if (!BungeePlugin.this.onCommand(sender, cmd, "", args))
                        sender.sendMessage(Color.Error + "[BungeePerms] " + Lang.translate(Lang.MessageType.COMMAND_NOT_FOUND));
                }
            };
            if (config.isAsyncCommands())
                ProxyServer.getInstance().getScheduler().runAsync(instance, r);
            else
                r.run();
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args)
        {
            return BungeePlugin.this.onTabComplete(sender, this, "", args);
        }
    }
}
