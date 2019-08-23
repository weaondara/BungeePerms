/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHas2()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHas3()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        perms.add("test.test1.test2");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHas4()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHas5()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHas6()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHas7()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHas8()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.test1.test2");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHas9()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.test1.*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHas10()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHas11()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHas12()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHas13()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.*");
        perms.add("*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHas14()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest1()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2.test3");
        perms.add("test.test1.test2.*");
        perms.add("test.test1.*");
        perms.add("-test.*");
        perms.add("test.test1.test2");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest2()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2.test3");
        perms.add("test.test1.test2.*");
        perms.add("test.test1.*");
        perms.add("-test.*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest3()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest4()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest5()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.");
        perms.add("-*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest6()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test1.test2");
        perms.add("-*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest7()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest8()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.test2");
        perms.add("*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest9()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.*");
        perms.add("-*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest10()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.*";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest11()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.*");
        perms.add("-*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest12()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("*");
        perms.add("-*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest13()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest14()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.*");
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest15()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.*");
        perms.add("-test.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest16()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test1.*");
        perms.add("-test1.*");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest17()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2.test3");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasBest18()
    {
        resolver.setUseRegex(false);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test2.*");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex1()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.(test2|test3)");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex2()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.(test)2|3");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex3()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.(test1|test3)");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex4()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.(test1|*)");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex5()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.(test1|test3|*)");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex6()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.#####.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex7()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.####.test2");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex8()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.####.test2");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex9()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.#####.test2");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex10()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.*");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex11()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex12()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex13()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.*");
        perms.add("test.test1.*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex14()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex15()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test*.t");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegex16()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.SEQUENTIAL);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.*");
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest1()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1");
        perms.add("-test.test1.(test2|test3)");
        perms.add("test.test1.(test2|test3)");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest2()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.(test)2|3");
        perms.add("-test.test1.(test)2|3");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest3()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1");
        perms.add("test.test1.(test1|test3)");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest4()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.test");
        perms.add("test.test1.(test1|*)");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest5()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.(test1|test3|*)");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest6()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.#####.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest7()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.####.test2");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest8()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.####.test2");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest9()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.#####.test*");
        perms.add("test.#####.test2");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest10()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.*");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest11()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest12()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest13()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("-test.test1.*");
        perms.add("test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest14()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test*");

        assertTrue(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest15()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.test*.t");

        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testHasRegexBest16()
    {
        resolver.setUseRegex(true);
        resolver.setResolvingMode(PermissionsResolver.ResolvingMode.BESTMATCH);

        String perm = "test.test1.test2";
        List<String> perms = new ArrayList<>();
        perms.add("test.test1.*");
        perms.add("-test.test1.*");

        assertFalse(resolver.has(perms, perm));
    }

    //sorts
    @Test
    public void testSortNormalBest()
    {
        List<String> perms = new ArrayList();
        perms.add("test.test1.*");
        perms.add("-test.test1.*");
        List<String> ex = new ArrayList();
        ex.add("-test.test1.*");
        ex.add("test.test1.*");
        List<String> res = PermissionsResolver.sortNormalBest(perms);
        assertEquals(ex, res);
    }

    @Test
    public void testSortRegexBest()
    {
        List<String> perms = new ArrayList();
        perms.add("test.test1.*");
        perms.add("-test.test1.test2");
        perms.add("test.test1.(test2|*)");
        perms.add("-test.test1.*");
        perms.add("test.test1.test2");
        perms.add("-test.test1.(test2|*)");
        List<String> ex = new ArrayList();
        ex.add("-test.test1.test2");
        ex.add("test.test1.test2");
        ex.add("-test.test1.*");
        ex.add("test.test1.*");
        ex.add("-test.test1.(test2|*)");
        ex.add("test.test1.(test2|*)");
        List<String> res = PermissionsResolver.sortRegexBest(perms);
        assertEquals(ex, res);
    }
}
