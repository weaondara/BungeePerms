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

import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.ChatColor;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class VelocityMessageEncoder extends MessageEncoder
{

    public static BaseComponent[] convert(Component components)
    {
        BaseComponent[] ret = new BaseComponent[components.children().size()];
        for (int i = 0; i < components.children().size(); i++)
        {
            ret[i] = new BaseComponent(components.children().get(i));
        }
        return ret;
    }

    public static Component convert(BaseComponent[] components)
    {
        Component ret = Component.text("");
        List<Component> children = new ArrayList(components.length);
        for (int i = 0; i < components.length; i++)
        {
            children.add((Component) components[i].getComponent());
        }
        ret.children(children);
        return ret;
    }

    private ComponentBuilder builder;
    private Component cache;

    private List<String> list;
    private String current;

    public VelocityMessageEncoder(MessageEncoder original)
    {
        super(original);
        if (!(original instanceof VelocityMessageEncoder))
        {
            throw new IllegalArgumentException("original is not a VelocityMessageEncoder");
        }

        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            builder = ((VelocityMessageEncoder) original).builder;
        }

        list = new ArrayList<>(((VelocityMessageEncoder) original).list);
        current = ((VelocityMessageEncoder) original).current;
    }

    public VelocityMessageEncoder(String text)
    {
        super(text);
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            builder = Component.text().content(text);
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
            builder = builder.append(Component.text(text));
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
            builder = builder.color(NamedTextColor.NAMES.value(color.name()));
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
            builder = builder.decoration(TextDecoration.BOLD, bold);
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
            builder = builder.decoration(TextDecoration.ITALIC, italic);
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
            builder = builder.decoration(TextDecoration.UNDERLINED, underlined);
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
            builder = builder.decoration(TextDecoration.STRIKETHROUGH, strikethrough);
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
            builder = builder.decoration(TextDecoration.OBFUSCATED, obfuscated);
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
                builder = builder.clickEvent(null);
            }
            else
            {
                cache = null;
                net.kyori.adventure.text.event.ClickEvent.Action action = net.kyori.adventure.text.event.ClickEvent.Action.valueOf(clickEvent.getAction().name());
                builder = builder.clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(action, clickEvent.getValue()));
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
                builder = builder.hoverEvent(null);
            }
            else
            {
                cache = null;
                net.kyori.adventure.text.event.HoverEvent.Action action = net.kyori.adventure.text.event.HoverEvent.Action.NAMES.value(hoverEvent.getAction().name());
                builder = builder.hoverEvent(net.kyori.adventure.text.event.HoverEvent.hoverEvent(action, convert(hoverEvent.getValue().create())));
            }
        }

        return this;
    }

    @Override
    public BaseComponent[] create()
    {
        if (cache == null)
        {
            cache = builder.build();
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
