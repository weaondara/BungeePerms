package net.alpenblock.bungeeperms.platform.bungee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Color;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class BungeePlugin extends Plugin implements PlatformPlugin
{

    private static BungeePlugin instance;

    public static BungeePlugin getInstance()
    {
        return instance;
    }

    private BungeeConfig config;

    //platform dependend parts
    private BungeeEventListener listener;
    private BungeeNotifier notifier;
    private PluginMessageSender pmsender;

    private BungeePerms bungeeperms;

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
        notifier = new BungeeNotifier(config);
        pmsender = new BungeePluginMessageSender();

        bungeeperms = new BungeePerms(this, config, pmsender, notifier, listener);
        bungeeperms.onLoad();
    }

    @Override
    public void onEnable()
    {
        BungeeCord.getInstance().registerChannel(BungeePerms.CHANNEL);
        bungeeperms.onEnable();
    }

    @Override
    public void onDisable()
    {
        bungeeperms.onDisable();
        BungeeCord.getInstance().unregisterChannel(BungeePerms.CHANNEL);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        return bungeeperms.getCommandHandler().onCommand(new BungeeSender(sender), cmd.getName(), label, args);
    }

    private void loadcmds()
    {
        BungeeCord.getInstance().getPluginManager().registerCommand(this,
                                                                    new Command("bungeeperms", null, "bp")
                                                                    {
                                                                        @Override
                                                                        public void execute(final CommandSender sender, final String[] args)
                                                                        {
                                                                            final Command cmd = this;
                                                                            BungeeCord.getInstance().getScheduler().runAsync(instance, new Runnable()
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
    public String getName()
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
        CommandSender sender;
        if (name.equalsIgnoreCase("console"))
        {
            sender = BungeeCord.getInstance().getConsole();
        }
        else
        {
            sender = BungeeCord.getInstance().getPlayer(name);
        }

        Sender s = null;

        if (sender != null)
        {
            s = new BungeeSender(sender);
        }

        return null;
    }

    @Override
    public Sender getPlayer(UUID uuid)
    {
        CommandSender sender;
        if (UUID.fromString("00000000-0000-0000-0000-000000000000").equals("uuid"))
        {
            sender = BungeeCord.getInstance().getConsole();
        }
        else
        {
            sender = BungeeCord.getInstance().getPlayer(uuid);
        }

        Sender s = null;

        if (sender != null)
        {
            s = new BungeeSender(sender);
        }

        return null;
    }

    @Override
    public Sender getConsole()
    {
        return new BungeeSender(BungeeCord.getInstance().getConsole());
    }

    @Override
    public List<Sender> getPlayers()
    {
        List<Sender> senders = new ArrayList<>();

        for (ProxiedPlayer pp : BungeeCord.getInstance().getPlayers())
        {
            senders.add(new BungeeSender(pp));
        }

        return senders;
    }

}
