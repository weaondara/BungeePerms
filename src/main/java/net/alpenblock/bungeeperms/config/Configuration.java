package net.alpenblock.bungeeperms.config;

import java.util.Map;

public interface Configuration extends ConfigurationSection {
    public void addDefault(String path, Object value);
    public void addDefaults(Map<String, Object> defaults);
    public void addDefaults(Configuration defaults);
    public void setDefaults(Configuration defaults);
    public Configuration getDefaults();
    public ConfigurationOptions options();
}