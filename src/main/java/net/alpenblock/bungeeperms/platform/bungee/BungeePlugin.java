package net.alpenblock.bungeeperms.platform.bungee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Color;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PlatformType;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.alpenblock.bungeeperms.platform.independend.GroupProcessor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class BungeePlugin extends Plugin implements PlatformPlugin
{

    @Getter
    private static BungeePlugin instance;

    private BungeeConfig config;

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

        //load config
        Config conf = new Config(this, "/config.yml");
        conf.load();
        config = new BungeeConfig(conf);
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

    private void loadcmds()
    {
        ProxyServer.getInstance().getPluginManager().registerCommand(this,
                                                                     new Command("bungeeperms", null, "bp")
                                                                     {
                                                                         @Override
                                                                         public void execute(final CommandSender sender, final String[] args)
                                                                         {
                                                                             final Command cmd = this;
                                                                             ProxyServer.getInstance().getScheduler().runAsync(instance, new Runnable()
                                                                             {
                                                                                 @Override
                                                                                 public void run()
                                                                                 {
                                                                                     if (!BungeePlugin.this.onCommand(sender, cmd, "", args))
                                                                                     {
                                                                                         sender.sendMessage(Color.Error + "[BungeePerms] Command not found");
                                                                                     }
                                                                                 }
                                                                             });
                                                                         }
                                                                     });
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
    public int registerRepeatingTask(Runnable r, long delay, long interval)
    {
        return ProxyServer.getInstance().getScheduler().schedule(this, r, delay, interval, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public void cancelTask(int id)
    {
        ProxyServer.getInstance().getScheduler().cancel(id);
    }
}
