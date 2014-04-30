package net.alpenblock.bungeeperms.uuid;

import com.google.gson.Gson;
import com.mojang.api.http.BasicHttpClient;
import com.mojang.api.http.HttpBody;
import com.mojang.api.http.HttpClient;
import com.mojang.api.http.HttpHeader;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileCriteria;
import com.mojang.api.profiles.ProfileSearchResult;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpProfileRepository
{
    private static final Gson gson = new Gson();
    private final HttpClient client;

    public HttpProfileRepository() 
    {
        client=BasicHttpClient.getInstance();
    }

    public Profile[] findProfilesByCriteria(ProfileCriteria criteria)
    {
        try 
        {
            HttpBody body = new HttpBody(gson.toJson(criteria));
            List<HttpHeader> headers = new ArrayList<>();
            headers.add(new HttpHeader("Content-Type", "application/json"));
            List<Profile> profiles = new ArrayList<>();
            
            ProfileSearchResult result = post(new URL("https://api.mojang.com/profiles/page/1"), body, headers);
            if (result.getSize() > 0)
            {
                profiles.addAll(Arrays.asList(result.getProfiles()));
            }
            
            return profiles.toArray(new Profile[profiles.size()]);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return new Profile[0];
        }
    }

    private ProfileSearchResult post(URL url, HttpBody body, List<HttpHeader> headers) throws IOException
    {
        return gson.fromJson(client.post(url, body, headers), ProfileSearchResult.class);
    }
}
