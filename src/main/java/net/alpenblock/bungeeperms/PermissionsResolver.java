package net.alpenblock.bungeeperms;

import java.util.ArrayList;
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
        if (useRegex)
        {
            return simplifyRegex(perms);
        }
        else
        {
            return simplifyNormal(perms);
        }
    }

    public static List<String> simplifyNormal(List<String> perms)
    {
//        List<String> ret=new ArrayList<>();
//        
//        for(String perm:perms)
//        {
//            boolean added=false;
//            for(int i=0;i<ret.size();i++)
//            {
//                if(ret.get(i).equalsIgnoreCase(perm))
//                {
//                    added=true;
//                    break;
//                }
//                else if(ret.get(i).equalsIgnoreCase("-"+perm))
//                {
//                    ret.set(i,perm);
//                    added=true;
//                    break;
//                }
//                else if(perm.equalsIgnoreCase("-"+ret.get(i)))
//                {
//                    ret.remove(i);
//                    added=true;
//                    break;
//                }
//            }
//            if(!added)
//            {
//                ret.add(perm);
//            }
//        }
//        
//        return ret;
        return perms;
    }

    public static List<String> simplifyRegex(List<String> perms)
    {
//        List<String> ret=new ArrayList<>();
//        
//        for(String perm:perms)
//        {
//            boolean added=false;
//            for(int i=0;i<ret.size();i++)
//            {
//                String tocheck=perm;
//                boolean negate=false;
//                if(tocheck.startsWith("-"))
//                {
//                    negate=true;
//                    tocheck=tocheck.substring(1);
//                }
//
//                tocheck=tocheck
//                    .replaceAll("\\.", "\\\\.")
//                    .replaceAll("\\*", "\\.\\*")
//                    .replaceAll("#", "\\.");
//
//                boolean matches=ret.get(i).matches(tocheck);
//
//                if(matches)
//                {
//                    if(negate)
//                    {
//                        ret.remove(i--);
//                    }
//                    else
//                    {
//                        ret.set(i, perm);
//                    }
//                    added=true;
//                }
//            }
//            if(!added)
//            {
//                ret.add(perm);
//            }
//        }
//        
//        return ret;
        return perms;
    }
}
