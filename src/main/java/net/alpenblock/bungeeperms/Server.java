package net.alpenblock.bungeeperms;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class Server
{

    private String server;
    private List<String> perms;
    private Map<String, World> worlds;
    private String display;
    private String prefix;
    private String suffix;
}
