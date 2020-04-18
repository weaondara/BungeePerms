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
package net.alpenblock.bungeeperms.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.ChatColor;

public abstract class MessageEncoder
{

    public MessageEncoder(MessageEncoder original)
    {
    }

    public MessageEncoder(String text)
    {
    }

    public abstract MessageEncoder append(String text);

    public abstract MessageEncoder color(ChatColor color);

    public abstract MessageEncoder bold(boolean bold);

    public abstract MessageEncoder italic(boolean italic);

    public abstract MessageEncoder underlined(boolean underlined);

    public abstract MessageEncoder strikethrough(boolean strikethrough);

    public abstract MessageEncoder obfuscated(boolean obfuscated);

    public abstract MessageEncoder event(ClickEvent clickEvent);

    public abstract MessageEncoder event(HoverEvent hoverEvent);

    public MessageEncoder reset()
    {
        color(ChatColor.RESET);
        event((ClickEvent) null);
        event((HoverEvent) null);
        return this;
    }

    public abstract BaseComponent[] create();

    @AllArgsConstructor
    @Getter
    public static class ClickEvent
    {

        private final Action action;
        private final String value;

        public enum Action
        {

            OPEN_URL,
            OPEN_FILE,
            RUN_COMMAND,
            SUGGEST_COMMAND
        }
    }

    @AllArgsConstructor
    @Getter
    public static class HoverEvent
    {

        private final Action action;
        private final MessageEncoder value;

        public enum Action
        {

            SHOW_TEXT,
            SHOW_ACHIEVEMENT,
            SHOW_ITEM
        }
    }

    @AllArgsConstructor
    @Getter
    public static class BaseComponent
    {

        private final Object component;
    }
}
