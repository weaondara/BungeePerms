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
    PermissionsResolver regexResolver;
    public PermissionsResolverTest()
    {
        resolver=new PermissionsResolver();
        resolver.setUseRegex(false);
        regexResolver=new PermissionsResolver();
        regexResolver.setUseRegex(true);
    }

    @Test
    public void testHas1()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHas2()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHas3()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        perms.add("test.test1.test2");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHas4()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHas5()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        
        assertNull(resolver.has(perms, perm));
    }
    @Test
    public void testHas6()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.*");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHas7()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHas8()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.test1.test2");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHas9()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.test1.*");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHas10()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHas11()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.test2");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHas12()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.*");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHas13()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
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
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-*");
        
        assertFalse(resolver.has(perms, perm));
    }
    
    @Test
    public void testHasRegex1()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test2|test3)");
        
        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testHasRegex2()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test)2|3");
        
        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testHasRegex3()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test1|test3)");
        
        assertNull(regexResolver.has(perms, perm));
    }
    @Test
    public void testHasRegex4()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test1|*)");
        
        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testHasRegex5()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.test1.(test1|test3|*)");
        
        assertFalse(regexResolver.has(perms, perm));
    }
    @Test
    public void testHasRegex6()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.#####.test2");
        
        assertFalse(regexResolver.has(perms, perm));
    }
    @Test
    public void testHasRegex7()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.####.test2");
        
        assertNull(regexResolver.has(perms, perm));
    }

    @Test
    public void testSimplify1()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.size() == 1);
    }
    @Test
    public void testSimplify2()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.test3");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.size()==2);
    }

    @Test
    public void testSimplify4()
    {
        List<String> perms=new ArrayList<>();
//        perms.add("-test.test1.test2");
//        perms.add("test.test1.*");
//        perms.add("-test.test1.test2");


        perms.add("-multiverse.access.vipfarm");
        perms.add("multiverse.access.*");
        perms.add("multiverse.access.*");
        perms.add("-multiverse.access.vipfarm");

        List<String> s=resolver.simplify(perms);
        System.out.println(s);
        assertTrue(s.size()==2);
    }
    
    @Test
    public void testSimplify3()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.test3");


        perms.add("-chatty.join.el");
        perms.add("-chatty.join.mb");
        perms.add("chatty.join");
        perms.add("chatty.join.*");
        perms.add("-chatty.join.mb");
        perms.add("-chatty.join.el");
        perms.add("-chatty.join.mb");
        perms.add("-chatty.join.el");
        perms.add("-chatty.join.mb");
        perms.add("chatty.join");
        perms.add("chatty.join.*");
        perms.add("-chatty.join.mb");
        perms.add("-chatty.join.el");


        List<String> s=resolver.simplify(perms);
        System.out.println(s);
        System.out.println(resolver.has(perms, "chatty.join.mb"));
        assertTrue(s.size()==5);
    }
    @Test
    public void testSimplifyRegex1()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.*");

        List<String> s=regexResolver.simplify(perms);
        assertTrue(s.size()==1);
        assertTrue(s.get(0).equalsIgnoreCase("test.test1.*"));
    }
    @Test
    public void testSimplifyRegex2()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.*");
        perms.add("-test.test1.*");

        List<String> s=regexResolver.simplify(perms);
        assertTrue(s.size() == 1);
        assertFalse(regexResolver.has(s, "test.test1.test2"));
        assertFalse(regexResolver.has(s, "test.test1.test5"));
    }
    @Test
    public void testSimplifyRegex3()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.*");
        perms.add("-test.*");

        List<String> s=regexResolver.simplify(perms);
        assertTrue(s.size() == 1);
        assertFalse(regexResolver.has(s, "test.test1.test2"));
    }
    @Test
    public void testSimplifyRegex4()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*.test2");

        List<String> s=regexResolver.simplify(perms);
        assertTrue(s.size() == 1);
        assertFalse(regexResolver.has(s, "test.test1.test2"));
        assertFalse(regexResolver.has(s, "test.abc.test2"));
        assertNull(regexResolver.has(s, "test.test1.test3"));
    }
    @Test
    public void testSimplifyRegex5()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*.*");

        List<String> s=regexResolver.simplify(perms);
        assertTrue(s.size() == 1);
        assertFalse(regexResolver.has(s, "test.test1.test2"));
        assertFalse(regexResolver.has(s, "test.abc.test2"));
        assertFalse(regexResolver.has(s, "test.test1.test3"));
    }
    @Test
    public void testSimplifyRegex6()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.test(2|3|4)");

        List<String> s=regexResolver.simplify(perms);
        assertTrue(s.size()==1);
        assertTrue(s.get(0).equalsIgnoreCase("test.test1.test(2|3|4)"));
    }
    @Test
    public void testSimplifyRegex7()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.test(2|3|4)");
        perms.add("test.test1.test#");

        List<String> s=regexResolver.simplify(perms);
        assertTrue(s.size()==2);
        assertTrue(s.get(0).equalsIgnoreCase("test.test1.test(2|3|4)"));
        assertTrue(s.get(1).equalsIgnoreCase("test.test1.test#"));
    }

    @Test
    public void testSimplify5()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");

        perms = resolver.simplify(perms);

        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify6()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");

        perms = resolver.simplify(perms);

        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify7()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        perms.add("test.test1.test2");

        perms = resolver.simplify(perms);

        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify8()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");
        perms.add("test.test1.test2");
        perms.add("-test.test1.test2");

        perms = resolver.simplify(perms);

        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify9()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();

        perms = resolver.simplify(perms);

        assertNull(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify10()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.test1.*");

        perms = resolver.simplify(perms);

        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify11()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");

        perms = resolver.simplify(perms);

        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify12()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.test1.test2");

        perms = resolver.simplify(perms);

        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify13()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.test1.*");

        perms = resolver.simplify(perms);

        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify14()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");

        perms = resolver.simplify(perms);

        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify15()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.test2");

        perms = resolver.simplify(perms);

        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify16()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.*");

        perms = resolver.simplify(perms);

        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify17()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-test.test1.*");
        perms.add("*");

        perms = resolver.simplify(perms);

        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testSimplify18()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.*");
        perms.add("-*");

        perms = resolver.simplify(perms);

        assertFalse(resolver.has(perms, perm));
    }

    @Test
    public void testSimplifyRegex8()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test2|test3)");

        perms = regexResolver.simplify(perms);

        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex9()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test)2|3");

        perms = regexResolver.simplify(perms);

        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex10()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test1|test3)");

        perms = regexResolver.simplify(perms);

        assertNull(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex11()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test1|*)");

        perms = regexResolver.simplify(perms);

        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex12()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.test1.(test1|test3|*)");

        perms = regexResolver.simplify(perms);

        assertFalse(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex13()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.#####.test2");

        perms = regexResolver.simplify(perms);

        assertFalse(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex14()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.####.test2");

        perms = regexResolver.simplify(perms);

        assertNull(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex15()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.#####.test2");
        perms.add("test.test1.test2");

        perms = regexResolver.simplify(perms);

        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex16()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*.test2");
        perms.add("test.test1.test2");

        perms = regexResolver.simplify(perms);

        assertTrue(regexResolver.has(perms, perm));
    }
    @Test
    public void testSimplifyRegex17()
    {
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*");
        perms.add("test.test1.test2");

        perms = regexResolver.simplify(perms);

        assertTrue(regexResolver.has(perms, perm));
    }
}
