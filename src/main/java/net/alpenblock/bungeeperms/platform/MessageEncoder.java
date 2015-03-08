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
