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

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author alex
 */
public class PermissionsResolverTest
{

    PermissionsResolver resolver;

    public PermissionsResolverTest()
    {
        resolver = new PermissionsResolver();
    }

    @Test
    public void testHas1()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas2()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.test1.test2"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas3()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.test1.test2"));
        perms.add(testperm("test.test1.test2"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas4()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.test1.test2"));
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.test1.test2"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas5()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas6()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas7()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas8()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.test1.test2"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas9()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.test1.*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas10()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas11()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.*"));
        perms.add(testperm("-test.test1.test2"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas12()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.*"));
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas13()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.*"));
        perms.add(testperm("-test.test1.*"));
        perms.add(testperm("*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHas14()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.*"));
        perms.add(testperm("-*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest1()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2.test3"));
        perms.add(testperm("test.test1.test2.*"));
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-test.*"));
        perms.add(testperm("test.test1.test2"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest2()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2.test3"));
        perms.add(testperm("test.test1.test2.*"));
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-test.*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest3()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest4()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest5()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1."));
        perms.add(testperm("-*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest6()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test1.test2"));
        perms.add(testperm("-*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest7()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.test2"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest8()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.test2"));
        perms.add(testperm("*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest9()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest10()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.*";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest11()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.*"));
        perms.add(testperm("-*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest12()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("*"));
        perms.add(testperm("-*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest13()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("-test.test1.test2"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest14()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest15()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.*"));
        perms.add(testperm("-test.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest16()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test1.*"));
        perms.add(testperm("-test1.*"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest17()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2.test3"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest18()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test2.*"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasBest19()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.test2"));
        perms.add(testperm("test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex1()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.(test2|test3)"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex2()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.(test)2|3"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex3()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.(test1|test3)"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex4()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.(test1|*)"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex5()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.(test1|test3|*)"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex6()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.#####.test2"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex7()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.####.test2"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex8()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.####.test2"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex9()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.#####.test2"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex10()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.*"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex11()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex12()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex13()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.*"));
        perms.add(testperm("test.test1.*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex14()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex15()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test*.t"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegex16()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest1()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1"));
        perms.add(testperm("-test.test1.(test2|test3)"));
        perms.add(testperm("test.test1.(test2|test3)"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest2()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.(test)2|3"));
        perms.add(testperm("-test.test1.(test)2|3"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest3()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1"));
        perms.add(testperm("test.test1.(test1|test3)"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest4()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.test"));
        perms.add(testperm("test.test1.(test1|*)"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest5()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.(test1|test3|*)"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest6()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.#####.test2"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest7()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.####.test2"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest8()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.####.test2"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest9()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.#####.test*"));
        perms.add(testperm("test.#####.test2"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest10()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.*"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest11()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest12()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest13()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.*"));
        perms.add(testperm("test.test1.*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest14()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test*"));

        assertTrue(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest15()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.test*.t"));

        assertNull(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest16()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    @Test
    public void testHasRegexBest17()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<BPPermission> perms = new ArrayList<>();
        perms.add(testperm("-test.test1.test2"));
        perms.add(testperm("test.test1.*"));

        assertFalse(resolver.hasPerm(perms, perm));
    }

    //sorts
    @Test
    public void testSortNormalBest()
    {
        List<BPPermission> perms = new ArrayList();
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-test.test1.*"));
        List<BPPermission> ex = new ArrayList();
        ex.add(testperm("-test.test1.*"));
        ex.add(testperm("test.test1.*"));
        List<BPPermission> res = PermissionsResolver.sortNormalBest(perms);
        assertEquals(ex, res);
    }

    @Test
    public void testSortRegexBest()
    {
        List<BPPermission> perms = new ArrayList();
        perms.add(testperm("test.test1.*"));
        perms.add(testperm("-test.test1.test2"));
        perms.add(testperm("test.test1.(test2|*)"));
        perms.add(testperm("-test.test1.*"));
        perms.add(testperm("test.test1.test2"));
        perms.add(testperm("test.##.test2"));
        perms.add(testperm("-test.test1.(test2|*)"));
        perms.add(testperm("-test.##.test2"));
        List<BPPermission> ex = new ArrayList();
        ex.add(testperm("test.test1.(test2|*)"));
        ex.add(testperm("-test.test1.(test2|*)"));
        ex.add(testperm("test.test1.*"));
        ex.add(testperm("-test.test1.*"));
        ex.add(testperm("test.##.test2"));
        ex.add(testperm("-test.##.test2"));
        ex.add(testperm("-test.test1.test2"));
        ex.add(testperm("test.test1.test2"));
        List<BPPermission> res = PermissionsResolver.sortRegexBest(perms);

//        for (BPPermission e : ex)
//            System.out.println(e.getPermission());
//        System.out.println("---");
//        for (BPPermission e : res)
//            System.out.println(e.getPermission());
        assertEquals(ex, res);
    }

    private BPPermission testperm(String testtest1test2)
    {
        return new BPPermission(testtest1test2, "", false, null, null, null, null);
    }
}
