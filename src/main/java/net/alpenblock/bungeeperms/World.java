package net.alpenblock.bungeeperms;

import java.util.List;
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
public class World
{

    private String world;
    private List<String> perms;
    private String display;
    private String prefix;
    private String suffix;
}
