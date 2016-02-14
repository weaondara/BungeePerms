package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.ChatColor;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class BukkitMessageEncoder extends MessageEncoder
{

    public static BaseComponent[] convert(net.md_5.bungee.api.chat.BaseComponent[] components)
    {
        BaseComponent[] ret = new BaseComponent[components.length];
        for (int i = 0; i < components.length; i++)
        {
            ret[i] = new BaseComponent(components[i]);
        }
        return ret;
    }

    public static net.md_5.bungee.api.chat.BaseComponent[] convert(BaseComponent[] components)
    {
        net.md_5.bungee.api.chat.BaseComponent[] ret = new net.md_5.bungee.api.chat.BaseComponent[components.length];
        for (int i = 0; i < components.length; i++)
        {
            ret[i] = (net.md_5.bungee.api.chat.BaseComponent) components[i].getComponent();
        }
        return ret;
    }

    private ComponentBuilder builder;
    net.md_5.bungee.api.chat.BaseComponent[] cache;

    private List<String> list;
    private String current;

    public BukkitMessageEncoder(MessageEncoder original)
    {
        super(original);
        if (!(original instanceof BukkitMessageEncoder))
        {
            throw new IllegalArgumentException("original is not a BukkitMessageEncoder");
        }

        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            builder = ((BukkitMessageEncoder) original).builder;
        }

        list = new ArrayList<>(((BukkitMessageEncoder) original).list);
        current = ((BukkitMessageEncoder) original).current;
    }

    public BukkitMessageEncoder(String text)
    {
        super(text);
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            builder = new ComponentBuilder(text);
        }
        list = new ArrayList<>();
        current = text;
    }

    @Override
    public MessageEncoder append(String text)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            cache = null;
            builder = builder.append(text);
        }

        list.add(current);
        current = text;
        return this;
    }

    @Override
    public MessageEncoder color(ChatColor color)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            cache = null;
            builder = builder.color(net.md_5.bungee.api.ChatColor.valueOf(color.name()));
        }

        current = color + current;

        return this;
    }

    @Override
    public MessageEncoder bold(boolean bold)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            cache = null;
            builder = builder.bold(bold);
        }
        else
        {
            color(ChatColor.BOLD);
            if (bold)
            {
                color(ChatColor.BOLD);
            }
            current = current.replaceAll("" + ChatColor.BOLD, "");
        }

        return this;
    }

    @Override
    public MessageEncoder italic(boolean italic)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            cache = null;
            builder = builder.italic(italic);
        }
        else
        {
            color(ChatColor.ITALIC);
            if (italic)
            {
                color(ChatColor.ITALIC);
            }
            current = current.replaceAll("" + ChatColor.ITALIC, "");
        }

        return this;
    }

    @Override
    public MessageEncoder underlined(boolean underlined)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            cache = null;
            builder = builder.underlined(underlined);
        }
        else
        {
            if (underlined)
            {
                color(ChatColor.UNDERLINE);
            }
            current = current.replaceAll("" + ChatColor.UNDERLINE, "");
        }

        return this;
    }

    @Override
    public MessageEncoder strikethrough(boolean strikethrough)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            cache = null;
            builder = builder.strikethrough(strikethrough);
        }
        else
        {
            if (strikethrough)
            {
                color(ChatColor.STRIKETHROUGH);
            }
            current = current.replaceAll("" + ChatColor.STRIKETHROUGH, "");
        }

        return this;
    }

    @Override
    public MessageEncoder obfuscated(boolean obfuscated)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            cache = null;
            builder = builder.obfuscated(obfuscated);
        }
        else
        {
            if (obfuscated)
            {
                color(ChatColor.MAGIC);
            }
            current = current.replaceAll("" + ChatColor.MAGIC, "");
        }

        return this;
    }

    @Override
    public MessageEncoder event(ClickEvent clickEvent)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            if (clickEvent == null)
            {
                builder = builder.event((net.md_5.bungee.api.chat.ClickEvent) null);
            }
            else
            {
                cache = null;
                net.md_5.bungee.api.chat.ClickEvent.Action action = net.md_5.bungee.api.chat.ClickEvent.Action.valueOf(clickEvent.getAction().name());
                builder = builder.event(new net.md_5.bungee.api.chat.ClickEvent(action, clickEvent.getValue()));
            }
        }

        return this;
    }

    @Override
    public MessageEncoder event(HoverEvent hoverEvent)
    {
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            if (hoverEvent == null)
            {
                builder = builder.event((net.md_5.bungee.api.chat.HoverEvent) null);
            }
            else
            {
                cache = null;
                net.md_5.bungee.api.chat.HoverEvent.Action action = net.md_5.bungee.api.chat.HoverEvent.Action.valueOf(hoverEvent.getAction().name());
                builder = builder.event(new net.md_5.bungee.api.chat.HoverEvent(action, convert(hoverEvent.getValue().create())));
            }
        }

        return this;
    }

    @Override
    public BaseComponent[] create()
    {
        if (cache == null)
        {
            cache = builder.create();
        }
        return convert(cache);
    }

    @Override
    public String toString()
    {
        String ret = "";
        for (String s : list)
        {
            ret += s;
        }
        ret += current;
        return ret;
    }
}
