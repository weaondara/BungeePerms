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
        resolver=new PermissionsResolver();
        resolver.setUseRegex(false);
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
        if(!resolver.isUseRegex()){return;}
        
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test2|test3)");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHasRegex2()
    {
        if(!resolver.isUseRegex()){return;}
        
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test)2|3");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHasRegex3()
    {
        if(!resolver.isUseRegex()){return;}
        
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test1|test3)");
        
        assertNull(resolver.has(perms, perm));
    }
    @Test
    public void testHasRegex4()
    {
        if(!resolver.isUseRegex()){return;}
        
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.(test1|*)");
        
        assertTrue(resolver.has(perms, perm));
    }
    @Test
    public void testHasRegex5()
    {
        if(!resolver.isUseRegex()){return;}
        
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.test1.(test1|test3|*)");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHasRegex6()
    {
        if(!resolver.isUseRegex()){return;}
        
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.#####.test2");
        
        assertFalse(resolver.has(perms, perm));
    }
    @Test
    public void testHasRegex7()
    {
        if(!resolver.isUseRegex()){return;}
        
        String perm="test.test1.test2";
        List<String> perms=new ArrayList<>();
        perms.add("-test.####.test2");
        
        assertNull(resolver.has(perms, perm));
    }

    @Test
    public void testSimplify1()
    {
        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.size()==1);
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
        assertTrue(s.size()==2);
    }
    @Test
    public void testSimplifyRegex1()
    {
        if(!resolver.isUseRegex()){return;}

        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.*");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.size()==1);
        assertTrue(s.get(0).equalsIgnoreCase("test.test1.*"));
    }
    @Test
    public void testSimplifyRegex2()
    {
        if(!resolver.isUseRegex()){return;}

        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.*");
        perms.add("-test.test1.*");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.isEmpty());
    }
    @Test
    public void testSimplifyRegex3()
    {
        if(!resolver.isUseRegex()){return;}

        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.*");
        perms.add("-test.*");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.isEmpty());
    }
    @Test
    public void testSimplifyRegex4()
    {
        if(!resolver.isUseRegex()){return;}

        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*.test2");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.isEmpty());
    }
    @Test
    public void testSimplifyRegex5()
    {
        if(!resolver.isUseRegex()){return;}

        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("-test.*.*");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.isEmpty());
    }
    @Test
    public void testSimplifyRegex6()
    {
        if(!resolver.isUseRegex()){return;}

        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.test(2|3|4)");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.size()==1);
        assertTrue(s.get(0).equalsIgnoreCase("test.test1.test(2|3|4)"));
    }
    @Test
    public void testSimplifyRegex7()
    {
        if(!resolver.isUseRegex()){return;}

        List<String> perms=new ArrayList<>();
        perms.add("test.test1.test2");
        perms.add("test.test1.test(2|3|4)");
        perms.add("test.test1.test#");

        List<String> s=resolver.simplify(perms);
        assertTrue(s.size()==2);
        assertTrue(s.get(0).equalsIgnoreCase("test.test1.test(2|3|4)"));
        assertTrue(s.get(1).equalsIgnoreCase("test.test1.test#"));
    }
}
