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
package net.alpenblock.bungeeperms.io.mysql2;

public enum EntityType
{

    User(0),
    Group(1),
    Version(2);

    private final int code;

    private EntityType(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static EntityType getByCode(int code)
    {
        for (EntityType et : values())
        {
            if (et.getCode() == code)
            {
                return et;
            }
        }
        return null;
    }
}
