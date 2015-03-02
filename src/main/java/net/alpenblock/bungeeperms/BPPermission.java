package net.alpenblock.bungeeperms;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BPPermission
{

    private String permission;
    private String origin;
    private boolean isGroup;
    private String server;
    private String world;
}
