package net.alpenblock.bungeeperms.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlatformType
{

    Bukkit("bukkit.conf.yml", false),
    BungeeCord("bungee.conf.yml", true);

    private final String defaultConfigPath;
    private final boolean proxy;
}
