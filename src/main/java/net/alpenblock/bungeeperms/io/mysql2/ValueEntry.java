package net.alpenblock.bungeeperms.io.mysql2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ValueEntry
{

    private String value;
    private String server;
    private String world;
}
