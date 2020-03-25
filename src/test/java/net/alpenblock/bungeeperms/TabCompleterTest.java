package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class TabCompleterTest
{

    public TabCompleterTest()
    {
    }

    @Test
    public void testNoArgs()
    {
        List<String> ex = Statics.list("cleanup", "debug", "demote", "format", "group", "groups", "help", "migrate", "overview", "promote", "reload", "user", "users", "uuid");
        List<String> is = TabCompleter.tabComplete(null, Statics.array());
        assertEquals(ex, is);
    }

    @Test
    public void testHelp()
    {
        List<String> ex = Statics.list("help");
        List<String> is = TabCompleter.tabComplete(null, Statics.array("help"));
        assertEquals(ex, is);

        is = TabCompleter.tabComplete(null, Statics.array("he"));
        assertEquals(ex, is);

        is = TabCompleter.tabComplete(null, Statics.array("help", ""));
        assertEquals(new ArrayList(), is);
    }

    @Test
    public void testReload()
    {
        List<String> ex = Statics.list("reload");
        List<String> is = TabCompleter.tabComplete(null, Statics.array("reload"));
        assertEquals(ex, is);

        is = TabCompleter.tabComplete(null, Statics.array("reloa"));
        assertEquals(ex, is);

        is = TabCompleter.tabComplete(null, Statics.array("reload", ""));
        assertEquals(new ArrayList(), is);
    }

    @Test
    public void testGroups()
    {
        List<String> ex = Statics.list("groups");
        List<String> is = TabCompleter.tabComplete(null, Statics.array("groups"));
        assertEquals(ex, is);

        ex = Statics.list("group", "groups");
        is = TabCompleter.tabComplete(null, Statics.array("group"));
        assertEquals(ex, is);

        is = TabCompleter.tabComplete(null, Statics.array("groups", ""));
        assertEquals(new ArrayList(), is);
    }

    @Test
    public void testGroup()
    {
        List<String> ex = Statics.list("group", "groups");
        List<String> is = TabCompleter.tabComplete(null, Statics.array("group"));
        assertEquals(ex, is);

        is = TabCompleter.tabComplete(null, Statics.array("group", ""));
        assertEquals(new ArrayList(), is);

        ex = Statics.list("addinherit", "addperm", "addtimedinherit", "addtimedperm", "create", "default", "delete", "display", "has", "info", "ladder", "list", "listonly", "prefix", "rank", "removeinherit", "removeperm", "removetimedinherit", "removetimedperm", "suffix", "users", "weight");
        is = TabCompleter.tabComplete(null, Statics.array("group", "", ""));
        assertEquals(ex, is);
    }

    @Test
    public void testUser()
    {
        List<String> ex = Statics.list("user", "users");
        List<String> is = TabCompleter.tabComplete(null, Statics.array("user"));
        assertEquals(ex, is);

        is = TabCompleter.tabComplete(null, Statics.array("user", ""));
        assertEquals(new ArrayList(), is);

        ex = Statics.list("addgroup", "addperm", "addtimedgroup", "addtimedperm", "delete", "display", "groups", "has", "info", "list", "listonly", "prefix", "removegroup", "removeperm", "removetimedgroup", "removetimedperm", "setgroup", "suffix");
        is = TabCompleter.tabComplete(null, Statics.array("user", "", ""));
        assertEquals(ex, is);
    }
}
