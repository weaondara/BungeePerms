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
package net.alpenblock.bungeeperms.platform.independend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Lang;

public class VersionCheck
{

    public static void checkForUpdate()
    {
        try
        {
            //get current version
            int curVersion = BungeePerms.getInstance().getPlugin().getBuild();

            URL url = new URL("https://ci.wea-ondara.net/job/BungeePerms/api/json");
            URLConnection request = url.openConnection();
            request.connect();
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject j = root.getAsJsonObject();
            int remoteVersion = j.getAsJsonObject("lastSuccessfulBuild").get("number").getAsInt();

            if (remoteVersion > curVersion)
            {
                BungeePerms.getInstance().getPlugin().getConsole().sendMessage("[BungeePerms] " + Lang.translate(Lang.MessageType.UPDATE_AVAILABLE));
            }
        }
        catch (Exception e)
        {
        }
    }
}
