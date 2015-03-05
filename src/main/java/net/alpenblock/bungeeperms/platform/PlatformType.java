package net.alpenblock.bungeeperms.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlatformType
{

    Bukkit("bukkit.conf.yml"),
    BungeeCord("bungee.conf.yml");

    private final String defaultConfigPath;
}
