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
