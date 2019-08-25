package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.alpenblock.bungeeperms.platform.Sender;

public class PermissionsResolver
{

    public final List<PermissionsPreProcessor> preprocessors = new ArrayList<>();
    public final List<PermissionsPostProcessor> postprocessors = new ArrayList<>();

    public void registerProcessor(PermissionsPreProcessor processor)
    {
        preprocessors.add(processor);
    }

    public void unregisterProcessor(PermissionsPreProcessor processor)
    {
        preprocessors.remove(processor);
    }

    public void registerProcessor(PermissionsPostProcessor processor)
    {
        postprocessors.add(processor);
    }

    public void unregisterProcessor(PermissionsPostProcessor processor)
    {
        postprocessors.remove(processor);
    }

    public List<BPPermission> preprocess(List<BPPermission> perms, Sender s)
    {
        for (PermissionsPreProcessor p : preprocessors)
        {
            perms = p.process(perms, s);
        }

        return perms;
    }

    public Boolean postprocess(String perm, Boolean result, Sender s)
    {
        for (PermissionsPostProcessor p : postprocessors)
        {
            result = p.process(perm, result, s);
        }

        return result;
    }

    @Getter
    @Setter
    private boolean useRegex = false;
    @Getter
    @Setter
    private ResolvingMode resolvingMode = ResolvingMode.SEQUENTIAL;

    public Boolean hasPerm(List<BPPermission> perms, String perm)
    {
        if (useRegex)
        {
            switch (resolvingMode)
            {
                case SEQUENTIAL:
                    return hasRegexSequential(perms, perm);
                case BESTMATCH:
                    return hasRegexBestMatch(perms, perm);
                default:
                    throw new IllegalStateException();
            }
        }
        else
        {
            switch (resolvingMode)
            {
                case SEQUENTIAL:
                    return hasNormalSequential(perms, perm);
                case BESTMATCH:
                    return hasNormalBestMatch(perms, perm);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public static Boolean hasNormalSequential(List<BPPermission> perms, String perm)
    {
        perm = Statics.toLower(perm);

        Boolean has = null;

        List<String> lperm = Statics.toList(perm, ".");

        for (BPPermission p : perms)
        {
            if (p.getPermission().equalsIgnoreCase(perm))
            {
                has = true;
            }
            else if (p.getPermission().equalsIgnoreCase("-" + perm))
            {
                has = false;
            }
            else if (p.getPermission().endsWith("*"))
            {
                List<String> lp = Statics.toList(p.getPermission(), ".");
                int index = 0;
                try
                {
                    while (index < lp.size() && index < lperm.size())
                    {
                        if (lp.get(index).equalsIgnoreCase(lperm.get(index))
                            || (index == 0 && lp.get(index).equalsIgnoreCase("-" + lperm.get(index))))
                        {
                            index++;
                        }
                        else
                        {
                            break;
                        }
                    }
                    if (lp.get(index).equalsIgnoreCase("*") || (index == 0 && lp.get(0).equalsIgnoreCase("-*")))
                    {
                        has = !lp.get(0).startsWith("-");
                    }
                }
                catch (Exception e)
                {
                    BungeePerms.getInstance().getDebug().log(e);
                }
            }
        }

        return has;
    }

    public static Boolean hasNormalBestMatch(List<BPPermission> perms, String perm)
    {
        perm = Statics.toLower(perm);

//        perms = sortNormalBest(perms);
        for (int i = perms.size() - 1; i >= 0; i--)
        {
            BPPermission p = perms.get(i);
            if (p.getPermission().equalsIgnoreCase("-" + perm))
                return false;
            else if (p.getPermission().equalsIgnoreCase(perm))
                return true;
        }

        List<String> lperm = Statics.toList(perm, ".");
        for (int j = lperm.size() - 1; j >= 0; j--)
        {
            String permpart = Statics.arrayToString(lperm.subList(0, j).toArray(new String[j]), 0, j, ".") + (j == 0 ? "*" : ".*");
            for (int i = perms.size() - 1; i >= 0; i--)
            {
                BPPermission p = perms.get(i);
                if (p.getPermission().equalsIgnoreCase("-" + permpart))
                    return false;
                else if (p.getPermission().equalsIgnoreCase(permpart))
                    return true;
            }
        }

        return null;
    }

    public static Boolean hasRegexSequential(List<BPPermission> perms, String perm)
    {
        perm = Statics.toLower(perm);

        Boolean has = null;

        for (BPPermission p : perms)
        {
            String tocheck = p.getPermission();
            if (tocheck.startsWith("-"))
                tocheck = tocheck.substring(1);

            tocheck = tocheck
                    .replaceAll("\\.", "\\\\.")
                    .replaceAll("\\*", "[^\\\\.]\\*")
                    .replaceAll("#", "\\.");

            if (perm.matches(tocheck))
                has = !p.getPermission().startsWith("-");
        }

        return has;
    }

    public static Boolean hasRegexBestMatch(List<BPPermission> perms, String perm)
    {
        perm = Statics.toLower(perm);

        perms = sortRegexBest(perms);

        for (int i = perms.size() - 1; i >= 0; i--)
        {
            BPPermission p = perms.get(i);
            String tocheck = p.getPermission();
            if (tocheck.startsWith("-"))
                tocheck = tocheck.substring(1);

            tocheck = tocheck
                    .replaceAll("\\.", "\\\\.")
                    .replaceAll("\\*", "[^\\\\.]\\*")
                    .replaceAll("#", "[^\\\\.]");

            if (perm.matches(tocheck))
                return !p.getPermission().startsWith("-");
        }

        return null;
    }

    public static List<BPPermission> sortNormalBest(List<BPPermission> perms)
    {
        perms = new ArrayList(perms);
        perms.sort(new Comparator<BPPermission>()
        {
            @Override
            public int compare(BPPermission o1, BPPermission o2)
            {
                if (o1.getPermission().startsWith("-") && !o2.getPermission().startsWith("-"))
                    return -1;
                else if (!o1.getPermission().startsWith("-") && o2.getPermission().startsWith("-"))
                    return 1;
                else
                    return 0;
            }
        });
        return perms;
    }

    public static List<BPPermission> sortRegexBest(List<BPPermission> perms)
    {
        boolean oldisGroup = false;
        boolean oldtimed = false;
        String oldorigin = null;
        String oldserver = null;
        String oldworld = null;

        List<List<BPPermission>> gnormal = new ArrayList();
        List<List<BPPermission>> gplaceholder = new ArrayList();
        List<List<BPPermission>> gstar = new ArrayList();
        List<List<BPPermission>> gregex = new ArrayList();

        int i = 0;
        while (i < perms.size())
        {
            List<BPPermission> normal = new ArrayList();
            List<BPPermission> placeholder = new ArrayList();
            List<BPPermission> star = new ArrayList();
            List<BPPermission> regex = new ArrayList();

            for (; i < perms.size(); i++)
            {
                BPPermission p = perms.get(i);

                boolean brk = p.isGroup() != oldisGroup
                              || oldtimed != (p.getTimedStart() != null)
                              || ((oldorigin == null) != (p.getOrigin() == null)) || (oldorigin != null && !oldorigin.equals(p.getOrigin()))
                              || ((oldserver == null) != (p.getServer() == null)) || (oldserver != null && !oldserver.equals(p.getServer()))
                              || ((oldworld == null) != (p.getWorld() == null)) || (oldworld != null && !oldworld.equals(p.getWorld()));

                oldisGroup = p.isGroup();
                oldtimed = p.getTimedStart() != null;
                oldorigin = p.getOrigin();
                oldserver = p.getServer();
                oldworld = p.getWorld();

                if (brk)
                    break;

                if (p.getPermission().matches("-?[a-z0-9\\.\\#]*\\*?"))
                {
                    if (p.getPermission().contains("#"))
                        placeholder.add(p);
                    else if (p.getPermission().endsWith("*"))
                        star.add(p);
                    else
                        normal.add(p);
                }
                else
                    regex.add(p);
            }

            gnormal.add(normal);
            gplaceholder.add(placeholder);
            gstar.add(star);
            gregex.add(regex);
        }

        perms = new ArrayList();
        for (int j = 0; j < gnormal.size(); j++)
        {
            perms.addAll(gnormal.get(j));
            perms.addAll(gstar.get(j));
            perms.addAll(gplaceholder.get(j));
            perms.addAll(gregex.get(j));
        }

//        perms = new ArrayList();
//        perms.addAll(normal);
//        perms.addAll(star);
//        perms.addAll(placeholder);
//        perms.addAll(regex);
        return perms;
    }

    public static enum ResolvingMode
    {
        SEQUENTIAL,
        BESTMATCH;
    }
}
