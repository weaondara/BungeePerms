package net.alpenblock.bungeeperms.io.mysql2;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ValueEntry
{

    public ValueEntry(String value, String server, String world)
    {
        this(value, server, world, null, null);
    }

    private String value;
    private String server;
    private String world;
    private Timestamp start;
    private Integer duration;
}
