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
package net.alpenblock.bungeeperms.uuid;

import com.google.gson.Gson;
import com.mojang.api.http.BasicHttpClient;
import com.mojang.api.http.HttpBody;
import com.mojang.api.http.HttpClient;
import com.mojang.api.http.HttpHeader;
import com.mojang.api.profiles.Profile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HttpProfileRepository
{

    private static final Gson gson = new Gson();
    private final HttpClient client;

    public HttpProfileRepository()
    {
        client = BasicHttpClient.getInstance();
    }

    public Profile[] findProfilesOfUsers(String names[])
    {
        try
        {
            HttpBody body = new HttpBody(gson.toJson(names));
            List<HttpHeader> headers = new ArrayList<>();
            headers.add(new HttpHeader("Content-Type", "application/json"));

            Profile[] result = post(new URL("https://api.mojang.com/profiles/minecraft"), body, headers);

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Profile[0];
        }
    }

    private Profile[] post(URL url, HttpBody body, List<HttpHeader> headers) throws IOException
    {
        return gson.fromJson(client.post(url, body, headers), Profile[].class);
    }
}
