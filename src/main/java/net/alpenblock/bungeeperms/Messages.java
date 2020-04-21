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
package net.alpenblock.bungeeperms;

import net.alpenblock.bungeeperms.platform.Sender;

public class Messages
{

    //todo make caps
    public static final String Error = Color.Error + "An error occured! Please report this error on https://github.com/weaondara/BungeePerms/issues . Please include exceptions from console.";
    public static final String TooLessArgs = Color.Error + "Too few arguments!";
    public static final String TooManyArgs = Color.Error + "Too many arguments!";
    public static final String NoRights = Color.Error + "You don't have permission to do that!";

    public static void sendErrorMessage(Sender sender)
    {
        sender.sendMessage(Error);
    }

    public static void sendTooLessArgsMessage(Sender sender)
    {
        sender.sendMessage(TooLessArgs);
    }

    public static void sendTooManyArgsMessage(Sender sender)
    {
        sender.sendMessage(TooManyArgs);
    }

    public static void sendNoRightsMessage(Sender sender)
    {
        sender.sendMessage(NoRights);
    }
}
