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
package net.alpenblock.bungeeperms.config;

import java.util.Map;

public class MemoryConfiguration extends MemorySection implements Configuration
{

    protected Configuration defaults;
    protected MemoryConfigurationOptions options;

    public MemoryConfiguration()
    {
    }

    public MemoryConfiguration(Configuration defaults)
    {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(String path, Object value)
    {

        if (defaults == null)
        {
            defaults = new MemoryConfiguration();
        }

        defaults.set(path, value);
    }

    public void addDefaults(Map<String, Object> defaults)
    {

        for (Map.Entry<String, Object> entry : defaults.entrySet())
        {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    public void addDefaults(Configuration defaults)
    {

        addDefaults(defaults.getValues(true));
    }

    public void setDefaults(Configuration defaults)
    {

        this.defaults = defaults;
    }

    public Configuration getDefaults()
    {
        return defaults;
    }

    @Override
    public ConfigurationSection getParent()
    {
        return null;
    }

    public MemoryConfigurationOptions options()
    {
        if (options == null)
        {
            options = new MemoryConfigurationOptions(this);
        }

        return options;
    }
}
