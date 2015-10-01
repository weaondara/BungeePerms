package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import net.alpenblock.bungeeperms.platform.Sender;

public class PermissionsResolver
{
    public final static Pattern PATTERN_BASIC_PERMISSION = Pattern.compile("(?:\\p{Alnum}|\\.)+");

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

    public List<String> preprocess(List<String> perms, Sender s)
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

    public Boolean has(List<String> perms, String perm)
    {
        if (useRegex)
        {
            return hasRegex(perms, perm);
        }
        else
        {
            return hasNormal(perms, perm);
        }
    }

    public static Boolean hasNormal(List<String> perms, String perm)
    {
        Boolean has = null;

        List<String> lperm = Statics.toList(perm, ".");

        for (String p : perms)
        {
            if (p.equalsIgnoreCase(perm))
            {
                has = true;
            }
            else if (p.equalsIgnoreCase("-" + perm))
            {
                has = false;
            }
            else if (p.endsWith("*"))
            {
                List<String> lp = Statics.toList(p, ".");
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
                    e.printStackTrace();
                }
            }
        }

        return has;
    }

    public static Boolean hasRegex(List<String> perms, String perm)
    {
        Boolean has = null;

        for (String p : perms)
        {
            String tocheck = p;
            boolean negate = false;
            if (p.startsWith("-"))
            {
                negate = true;
                tocheck = p.substring(1);
            }

            tocheck = tocheck
                    .replaceAll("\\.", "\\\\.")
                    .replaceAll("\\*", "\\.\\*")
                    .replaceAll("#", "\\.");

            boolean matches = perm.matches(tocheck);

            if (matches)
            {
                has = !negate;
            }
        }

        return has;
    }

    public List<String> simplify(List<String> perms)
    {
        if(isUseRegex())
        {
            // Don't simplify regex perms
            // Simplifying them is possible, but requires the program to test
            // whether one regex is a subset of another. Such a test is possible
            // but not provided by the java runtime library. Despite such a test
            // would be difficult to implement and require exponential time to complete
            return simplifyRegex(perms);
        }
        else {
            return simplifyNormal(perms);
        }
    }

    private List<String> simplifyNormal(List<String> perms) {
        List<String> ret=new ArrayList<>();

        for(String perm:perms)
        {
            String tocheck=perm;
            boolean negative = false;
            if(tocheck.startsWith("-"))
            {
                negative = true;
                tocheck=tocheck.substring(1);
            }

            // remove nodes which will be hidden by the one we're adding
            boolean removedPermissions = false;
            for(int i=0;i<ret.size();i++)
            {
                String existingPermission = ret.get(i);
                if(existingPermission.startsWith("-"))
                {
                    existingPermission = existingPermission.substring(1);
                }
                Boolean matches=has(Collections.singletonList(tocheck), existingPermission);

                if(matches != null)
                {
                    removedPermissions = true;
                    ret.remove(i--);
                }
            }


            // check whether previous permission nodes already cover the permission where processing right now
            if(!removedPermissions)
            {
                Boolean check = hasNormal(ret, tocheck);
                if (check != null && check != negative)
                {
                    continue;
                }
            }

            ret.add(perm);
        }

        return ret;
    }

    private List<String> simplifyRegex(List<String> perms) {
        List<String> ret=new ArrayList<>();

        for(String perm:perms)
        {
            String tocheck=perm;
            boolean negative = false;
            if(tocheck.startsWith("-"))
            {
                negative = true;
                tocheck=tocheck.substring(1);
            }

            // remove nodes which will be hidden by the one we're adding
            boolean removedPermissions = false;
            for(int i=0;i<ret.size();i++)
            {
                String existingPermission = ret.get(i);
                if(existingPermission.startsWith("-"))
                {
                    existingPermission = existingPermission.substring(1);
                }
                if(PATTERN_BASIC_PERMISSION.matcher(existingPermission).matches())
                {
                    Boolean matches = hasRegex(Collections.singletonList(tocheck), existingPermission);

                    if (matches != null)
                    {
                        removedPermissions = true;
                        ret.remove(i--);
                    }
                }
            }


            // check whether previous permission nodes already cover the permission where processing right now
            if(!removedPermissions && PATTERN_BASIC_PERMISSION.matcher(tocheck).matches())
            {
                Boolean check = hasRegex(ret, tocheck);
                if (check != null && check != negative)
                {
                    continue;
                }
            }

            ret.add(perm);
        }

        return ret;
    }
}
