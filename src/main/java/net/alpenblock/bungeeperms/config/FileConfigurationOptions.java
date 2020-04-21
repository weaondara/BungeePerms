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

public class FileConfigurationOptions extends MemoryConfigurationOptions
{

    private String header = null;
    private boolean copyHeader = true;

    protected FileConfigurationOptions(MemoryConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    public FileConfiguration configuration()
    {
        return (FileConfiguration) super.configuration();
    }

    @Override
    public FileConfigurationOptions copyDefaults(boolean value)
    {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public FileConfigurationOptions pathSeparator(char value)
    {
        super.pathSeparator(value);
        return this;
    }

    public String header()
    {
        return header;
    }

    public FileConfigurationOptions header(String value)
    {
        this.header = value;
        return this;
    }

    public boolean copyHeader()
    {
        return copyHeader;
    }

    public FileConfigurationOptions copyHeader(boolean value)
    {
        copyHeader = value;

        return this;
    }
}
