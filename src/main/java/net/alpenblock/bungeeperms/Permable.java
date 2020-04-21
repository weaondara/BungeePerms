/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms;

import java.util.List;

public interface Permable
{

    public String getPrefix();

    public void setPrefix(String prefix);

    public String getSuffix();

    public void setSuffix(String suffix);

    public String getDisplay();

    public void setDisplay(String display);

    public List<String> getGroupsString();

    public void setGroups(List<String> groups);

    public List<TimedValue<String>> getTimedGroupsString();

    public void setTimedGroups(List<TimedValue<String>> groups);

    public List<String> getPerms();

    public void setPerms(List<String> perms);

    public List<TimedValue<String>> getTimedPerms();

    public void setTimedPerms(List<TimedValue<String>> perms);

    public boolean hasTimedPermSet(String perm);
}
