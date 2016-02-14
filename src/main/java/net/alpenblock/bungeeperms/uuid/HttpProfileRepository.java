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
        client=BasicHttpClient.getInstance();
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
