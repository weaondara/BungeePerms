package com.mojang.api.profiles;

import java.util.UUID;

import net.md_5.bungee.Util;

public class Profile {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public UUID getUUID() {
    	return Util.getUUID(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
