package net.alpenblock.bungeeperms.platform;

import java.util.UUID;

public interface Sender
{

    public void sendMessage(String message);

    public void sendMessage(MessageEncoder encoder);

    public String getName();

    public UUID getUUID();

    public String getServer();

    public String getWorld();

    public boolean isConsole();

    public boolean isPlayer();

    public boolean isOperator();
}
