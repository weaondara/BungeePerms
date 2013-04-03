package net.alpenblock.bungeeperms.config;

import java.util.Map;

public class MemoryConfiguration extends MemorySection implements Configuration {
    protected Configuration defaults;
    protected MemoryConfigurationOptions options;

    public MemoryConfiguration() {}
    public MemoryConfiguration(Configuration defaults) 
    {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(String path, Object value) {

        if (defaults == null) {
            defaults = new MemoryConfiguration();
        }

        defaults.set(path, value);
    }

    public void addDefaults(Map<String, Object> defaults) {

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    public void addDefaults(Configuration defaults) {

        addDefaults(defaults.getValues(true));
    }

    public void setDefaults(Configuration defaults) {

        this.defaults = defaults;
    }

    public Configuration getDefaults() {
        return defaults;
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    public MemoryConfigurationOptions options()
    {
        if (options == null) {
            options = new MemoryConfigurationOptions(this);
        }

        return options;
    }
}
