/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alpenblock.bungeeperms.util;

import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.util.ConcurrentList.ListItr;
import static org.junit.Assert.*;
import org.junit.Test;

public class ConcurrentListTest
{

    private final List<String> l;

    public ConcurrentListTest()
    {
        l = new ConcurrentList();
    }

    @Test
    public void testAdd_GenericType()
    {
        l.add("string 1");

        assertTrue(l.size() == 1);
        assertTrue(l.get(0).equalsIgnoreCase("string 1"));
    }

    @Test
    public void testAdd_int_GenericType()
    {
        l.add("string 1");
        l.add(0, "string 2");

        assertTrue(l.size() == 2);
        assertTrue(l.get(0).equalsIgnoreCase("string 2"));
    }

    @Test
    public void testAddAll_Collection()
    {
        l.add("string 1");
        l.add(0, "string 2");

        List ladd = new ArrayList<>();
        ladd.add("string 3");
        ladd.add("string 4");
        l.addAll(ladd);

        assertTrue(l.size() == 4);
        assertTrue(l.get(2).equalsIgnoreCase("string 3"));
        assertTrue(l.get(3).equalsIgnoreCase("string 4"));
    }

    @Test
    public void testAddAll_int_Collection()
    {
        l.add("string 1");
        l.add(0, "string 2");

        List ladd = new ArrayList<>();
        ladd.add("string 3");
        ladd.add("string 4");
        l.addAll(ladd);

        List ladd2 = new ArrayList<>();
        ladd2.add("string 5");
        ladd2.add("string 6");
        l.addAll(2, ladd2);

        assertTrue(l.size() == 6);
        assertTrue(l.get(2).equalsIgnoreCase("string 5"));
        assertTrue(l.get(3).equalsIgnoreCase("string 6"));
    }

    @Test
    public void testClear()
    {
        l.add("string 1");
        l.add(0, "string 2");

        assertTrue(l.size() == 2);

        l.clear();

        assertTrue(l.size() == 0);
        assertTrue(l.isEmpty());
    }

    @Test
    public void testClone()
    {
        l.add("string 1");
        l.add(0, "string 2");

        List clone = (List) ((ArrayList) l).clone();

        assertFalse(l == clone);
        assertTrue(l.size() == clone.size());

        for (int i = 0; i < l.size(); i++)
        {
            assertTrue(l.get(i) == clone.get(i));
        }

    }

    @Test
    public void testContains()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add(0, "string 2");

        assertTrue(l.contains(string1));
    }

    @Test
    public void testEnsureCapacity()
    {
        ((ArrayList) l).ensureCapacity(0);
    }

    @Test
    public void testGet()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add(0, "string 2");

        assertTrue(l.get(1) == string1);
    }

    @Test
    public void testIndexOf()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add(0, "string 2");

        assertTrue(l.indexOf(string1) == 1);
    }

    @Test
    public void testLastIndexOf()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");
        l.add(string1);

        assertTrue(l.lastIndexOf(string1) == 2);
    }

    @Test
    public void testRemove_int()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        l.remove(0);

        assertTrue(l.size() == 1);
        assertTrue(l.get(0).equalsIgnoreCase("string 2"));
    }

    @Test
    public void testRemove_Object()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        l.remove(string1);

        assertTrue(l.size() == 1);
        assertTrue(l.get(0).equalsIgnoreCase("string 2"));
    }

    @Test
    public void testRemoveAll()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        l.removeAll(l);

        assertTrue(l.isEmpty());
    }

    @Test
    public void testRetainAll()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        l.retainAll(l);

        assertTrue(l.size() == 2);
    }

    @Test
    public void testSet()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        l.set(1, string1);

        assertTrue(l.size() == 2);
        assertTrue(l.get(1) == string1);
    }

    @Test
    public void testSubList()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        List<String> subList = l.subList(0, 1);

        assertTrue(subList.size() == 1);
        assertTrue(subList.get(0) == string1);
    }

    @Test
    public void testToArray_0args()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        Object[] toArray = l.toArray();

        assertTrue(toArray.length == 2);
        assertTrue(toArray[0] == string1);
    }

    @Test
    public void testToArray_GenericType()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        String[] toArray = l.toArray(new String[l.size()]);

        assertTrue(toArray.length == 2);
        assertTrue(toArray[0] == string1);
    }

    @Test
    public void testTrimToSize()
    {
        String string1 = "string 1";
        l.add(string1);
        l.add("string 2");

        ((ArrayList) l).trimToSize();

        assertTrue(l.size() == 2);
    }

    @Test
    @SneakyThrows
    public void testConcurrencyModification()
    {
        System.out.println("tesing concurrency");

        int threadcount = 10;
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadcount; i++)
        {
            threads.add(new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    bombList();
                }
            }));
        }
        for (int i = 0; i < threadcount; i++)
        {
            threads.get(i).start();
        }

        while (true)
        {
            boolean stop = true;
            for (int i = 0; i < threadcount; i++)
            {
                if (threads.get(i).isAlive())
                {
                    stop = false;
                }
            }
            if (stop)
            {
                break;
            }
            Thread.sleep(100);
        }
    }

    private void bombList()
    {
        for (int bla = 0; bla < 1000; bla++)
        {
            l.add("strsffing 1");
            l.add("strdtshing fe1");
            l.add("striesffethtrejrzehtng 1");
            l.add("striwergwergewgrewng 1");
            l.add("gwe wrgrwegwe1");
            l.add("strigrwegng 1");
            l.add("strirewgrfqeafg 1");
            l.add("stefeeffferieffefng 1");
            l.add("strrgeweffeing 1");
            l.add("streffdsfesfeing 1");
            l.add("strergiefefng 1");
            l.add("steriefeng 1");
            l.add("stringrwegrewgrg 1");
            l.add("strefsing 1");
            l.add("srgrgrewgwegrwetri<feefng 1");
            l.add("strrwegrweeefsfffing 1");
            l.add("stgrwerewgrinrwgregg 1");
            l.add("striregweregng 1");
            l.add("strfwegrewgefesgsaging 1");
            l.add("strirgwegwergweng 1");
            l.add("stgrgrregwwegrwegrasgrringfewrweg 1");
            l.add("strrgwrgweing 1");
            l.add("strigrwegrweng 1");
            l.add("strigrwngrgwrewg 1");
            l.add("strgrwegweing 1");

            for (int i = 0; i < l.size(); i++)
            {
                try
                {
                    l.get(i);
                }
                catch (IndexOutOfBoundsException e)
                {
                }
            }

            l.clear();
        }
    }

    @Test
    public void testItr()
    {
        int count = 10;
        for (int i = 0; i < count; i++)
        {
            l.add("s" + i);
        }

        int i = 0;

        for (String s : l)
        {
            assertTrue(s.equalsIgnoreCase("s" + i++));
        }
    }

    @Test
    public void testConcurrency()
    {
        int count = 10;
        for (int i = 0; i < count; i++)
        {
            l.add("s" + i);
        }

        ListItr itr = (ListItr) l.listIterator();

        itr.next();
        itr.next();
        itr.next();
        itr.remove();

        assertTrue(l.size() == 9);
        assertTrue(l.get(2).equalsIgnoreCase("s3"));

        itr.next();
        itr.next();

        itr.add("bla");

        assertTrue(l.size() == 10);
        assertTrue(l.get(4).equalsIgnoreCase("bla"));

        itr.next();
        itr.next();

        itr.set("bla");

        assertTrue(l.size() == 10);
        assertTrue(l.get(6).equalsIgnoreCase("bla"));

    }
}
