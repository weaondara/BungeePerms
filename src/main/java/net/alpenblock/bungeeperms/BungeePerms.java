package net.alpenblock.bungeeperms;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.alpenblock.bungeeperms.config.YamlConfiguration;
import net.alpenblock.bungeeperms.io.BackEndType;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * The Class BungeePerms.
 */
public class BungeePerms extends Plugin implements Listener
{
	private static BungeePerms instance;
	public static BungeePerms getInstance()
	{
		return instance;
	}
	
	private BungeeCord bc;
	private Config config;
	private Debug debug;
    
	private PermissionsManager pm;
	
	@Override
	public void onLoad()
	{
		//static
		instance=this;
		
		bc=BungeeCord.getInstance();
        
        //check for config file existance
        File f=new File(getDataFolder(),"/config.yml");
        if(!f.exists()|!f.isFile())
        {
            bc.getLogger().info("[BungeePerms] no config file found -> copy packed default config.yml to data folder ...");
            f.getParentFile().mkdirs();
            try 
			{
				//file öffnen
				ClassLoader cl=this.getClass().getClassLoader();
	            URL url = cl.getResource("config.yml");
	            if(url!=null)
	            {
		            URLConnection connection = url.openConnection();
		            connection.setUseCaches(false);
		            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(connection.getInputStream());
		            defConfig.save(f);
	            }
	        } 
			catch (Exception e) 
	        {
				e.printStackTrace();
	        }
            bc.getLogger().info("[BungeePerms] copied default config.yml to data folder");
        }
        
        config=new Config(this,"/config.yml");
        config.load();
        debug=new Debug(this,config,"BP");
        
		//load commands
		loadcmds();
        
		pm=new PermissionsManager(this,config,debug);
	}
	
	@Override
	public void onEnable()
	{
		bc.getLogger().info("Activating BungeePerms ...");
        pm.enable();
	}
	
	@Override
	public void onDisable() 
	{
		bc.getLogger().info("Deactivating BungeePerms ...");
        pm.disable();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		if(cmd.getName().equalsIgnoreCase("bungeeperms"))
		{
			if(args.length==0)
			{
				sender.sendMessage(ChatColor.GOLD+"Welcome to BungeePerms, a BungeeCord permissions plugin");
				return true;
			}
			else if(args.length>0)
			{
				if(args[0].equalsIgnoreCase("help"))
				{
                    if(!matchArgs(sender,args,1))
                    {
                        return true;
                    }
                    
                    if(pm.hasOrConsole(sender,"bungeeperms.help",true))
                    {
                        showHelp(sender);
                        return true;
                    }
                    return true;
				}
				else if(args[0].equalsIgnoreCase("reload"))
				{
                    if(pm.hasOrConsole(sender,"bungeeperms.reload",true))
                    {
                        pm.loadConfig();
                        pm.loadPerms();
                        pm.sendPMAll("reload;all");
                        sender.sendMessage("Permissions reloaded");
                    }
                    return true;
				}
				else if(args[0].equalsIgnoreCase("users"))
				{
					if(args.length==1)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.users.list",true))
						{
                            List<String> users=pm.getRegisteredUsers();
							if(users.size()>0)
							{
                                String out=Color.Text+"Following players are registered: ";
								for(int i=0;i<users.size();i++)
								{
									out+=Color.User+users.get(i)+Color.Text+(i+1<users.size()?", ":"");
								}
                                sender.sendMessage(out);
							}
							else
							{
								sender.sendMessage(Color.Text+"No players found!");
							}
						}
						return true;
					}
					else if(args.length==2)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.users.list",true))
						{
							if(!args[1].equalsIgnoreCase("-c"))
							{
								return false;
							}
							if(pm.getRegisteredUsers().size()>0)
							{
								sender.sendMessage(Color.Text+"There are "+Color.Value+pm.getRegisteredUsers().size()+Color.Text+" players registered.");
							}
							else
							{
								sender.sendMessage(Color.Text+"No players found!");
							}
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("user"))
				{
                    if(args.length<3)
                    {
                        Messages.sendTooLessArgsMessage(sender);
                        return true;
                    }
                    if(args[2].equalsIgnoreCase("list"))
                    {
                        if(pm.hasOrConsole(sender,"bungeeperms.user.perms.list",true))
                        {
                            if(args.length>5)
                            {
                                Messages.sendTooManyArgsMessage(sender);
                                return true;
                            }
                            
                            String player=Statics.getFullPlayerName(bc,args[1]);
                            String server=args.length>3?args[3]:null;
                            String world=args.length>4?args[4]:null;

                            User user=pm.getUser(player);
                            if(user!=null)
                            {
                                sender.sendMessage(Color.Text+"Permissions of the player "+Color.User+user.getName()+Color.Text+":");
                                List<BPPermission> perms=user.getPermsWithOrigin(server,world);
                                for(BPPermission perm:perms)
                                {
                                    sender.sendMessage(Color.Text+"- "+Color.Value+perm.getPermission()+Color.Text+
                                            " ("+
                                            Color.Value+(!perm.isGroup() && perm.getOrigin().equalsIgnoreCase(player)?"own":perm.getOrigin())+Color.Text+
                                            (perm.getServer()!=null?" | "+Color.Value+perm.getServer()+Color.Text:"")+
                                            (perm.getWorld()!=null?" | "+Color.Value+perm.getWorld()+Color.Text:"")+
                                            ")");
                                }
                            }
                            else
                            {
                                sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
                            }
                        }
                        return true;
                    }
                    else if(args[2].equalsIgnoreCase("groups"))
                    {
                        if(pm.hasOrConsole(sender,"bungeeperms.user.groups",true))
                        {
                            if(!matchArgs(sender,args,3))
                            {
                                return true;
                            }
                            
                            String player=Statics.getFullPlayerName(bc,args[1]);
                            User user=pm.getUser(player);
                            if(user!=null)
                            {
                                sender.sendMessage(Color.Text+"Groups of the player "+Color.User+user.getName()+Color.Text+":");
                                for(Group g:user.getGroups())
                                {
                                    sender.sendMessage(Color.Text+"- "+Color.Value+g.getName());
                                }
                            }
                            else
                            {
                                sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
                            }
                        }
                        return true;
                    }
                    else if(args[2].equalsIgnoreCase("info"))
                    {
                        if(pm.hasOrConsole(sender,"bungeeperms.user.info",true))
                        {
                            if(!matchArgs(sender,args,3))
                            {
                                return true;
                            }
                            
                            String player=Statics.getFullPlayerName(bc,args[1]);
                            User user=pm.getUser(player);
                            if(user==null)
                            {
                                sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
                                return true;
                            }
                            String groups="";
                            for(int i=0;i<user.getGroups().size();i++)
                            {
                                groups+=Color.Value+user.getGroups().get(i).getName()+Color.Text+" ("+Color.Value+user.getGroups().get(i).getPerms().size()+Color.Text+")"+(i+1<user.getGroups().size()?", ":"");
                            }
                            sender.sendMessage(Color.Text+"Groups of the player "+Color.User+user.getName()+Color.Text+": "+groups);

                            //all group perms
                            sender.sendMessage(Color.Text+"Effective permissions: "+Color.Value+user.getEffectivePerms().size());//TODO
                        }
                        return true;
                    }
					else if(args.length>=4)
					{
						if(Statics.ArgAlias(args[2], new String[]{"add","addperm","addpermission"}))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.user.perms.add",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								String perm=args[3].toLowerCase();
								String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								User user=pm.getUser(player);
								if(user!=null)
								{
									if(server==null)
									{
										if(user.getExtraperms().contains("-"+perm))
										{
                                            pm.removeUserPerm(user,"-"+perm);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+user.getName()+Color.Text+".");
										}
										else if(!user.getExtraperms().contains(perm))
										{
                                            pm.addUserPerm(user,perm);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+user.getName()+Color.Text+".");
										}
										else
										{
											sender.sendMessage(Color.Text+"The player "+Color.Value+user.getName()+Color.Text+" already has the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
                                        if(world==null)
                                        {
                                            List<String> perserverperms=user.getServerPerms().get(server);
                                            if(perserverperms==null)
                                            {
                                                perserverperms=new ArrayList<>();
                                            }
                                            if(perserverperms.contains("-"+perm))
                                            {
                                                pm.removeUserPerServerPerm(user,server,"-"+perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else if(!perserverperms.contains(perm))
                                            {
                                                pm.addUserPerServerPerm(user,server,perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage(Color.Text+"The player "+Color.Value+user.getName()+Color.Text+" alreday has the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                        }
                                        else
                                        {
                                            Map<String, List<String>> perserverperms=user.getServerWorldPerms().get(server);
                                            if(perserverperms==null)
                                            {
                                                perserverperms=new HashMap<>();
                                            }
                                            
                                            List<String> perserverworldperms=perserverperms.get(world);
                                            if(perserverworldperms==null)
                                            {
                                                perserverworldperms=new ArrayList<>();
                                            }
                                            
                                            
                                            if(perserverworldperms.contains("-"+perm))
                                            {
                                                pm.removeUserPerServerWorldPerm(user,server,world,"-"+perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else if(!perserverworldperms.contains(perm))
                                            {
                                                pm.addUserPerServerWorldPerm(user,server,world,perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage(Color.Text+"The player "+Color.Value+user.getName()+Color.Text+" alreday has the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                        }
									}
								}
								else
								{
									sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(Statics.ArgAlias(args[2], new String[]{"remove","removeperm","removepermission"}))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.user.perms.remove",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								String perm=args[3].toLowerCase();
								String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								User user=pm.getUser(player);
								if(user!=null)
								{
									if(server==null)
									{
										if(user.getExtraperms().contains(perm))
										{
                                            pm.removeUserPerm(user, perm);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+user.getName()+Color.Text+".");
										}
										else if(!user.getExtraperms().contains("-"+perm))
										{
                                            pm.addUserPerm(user, "-"+perm);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+user.getName()+Color.Text+".");
										}
										else
										{
											sender.sendMessage(Color.Text+"The player "+Color.Value+user.getName()+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
                                        if(world==null)
                                        {
                                            List<String> perserverperms=user.getServerPerms().get(server);
                                            if(perserverperms==null)
                                            {
                                                perserverperms=new ArrayList<>();
                                            }
                                            if(perserverperms.contains(perm))
                                            {
                                                pm.removeUserPerServerPerm(user, server, perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else if(!perserverperms.contains("-"+perm))
                                            {
                                                pm.removeUserPerServerPerm(user, server, "-"+perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage(Color.Text+"The player "+Color.Value+user.getName()+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                        }
                                        else
                                        {
                                            Map<String, List<String>> perserverperms=user.getServerWorldPerms().get(server);
                                            if(perserverperms==null)
                                            {
                                                perserverperms=new HashMap<>();
                                            }
                                            
                                            List<String> perserverworldperms=perserverperms.get(world);
                                            if(perserverworldperms==null)
                                            {
                                                perserverworldperms=new ArrayList<>();
                                            }
                                            
                                            if(perserverworldperms.contains(perm))
                                            {
                                                pm.removeUserPerServerWorldPerm(user, server,world, perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else if(!perserverworldperms.contains("-"+perm))
                                            {
                                                pm.removeUserPerServerWorldPerm(user, server,world, "-"+perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+user.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage(Color.Text+"The player "+Color.Value+user.getName()+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                        }
									}
								}
								else
								{
									sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("has"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.user.perms.has",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								User user=pm.getUser(player);
								String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								if(user!=null)
								{
									if(server==null)
									{
										boolean has=pm.hasPerm(player, args[3].toLowerCase());
										sender.sendMessage(Color.Text+"Player "+Color.User+user.getName()+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
									}
									else
									{
                                        if(world==null)
                                        {
                                            ServerInfo si=bc.config.getServers().get(server);
                                            if(si==null)
                                            {
                                                sender.sendMessage(Color.Error+"The server "+Color.Value+server+Color.Error+" does not exist!");
                                                return true;
                                            }
                                            boolean has=pm.hasPermOnServer(user.getName(), args[3].toLowerCase(),si);
                                            sender.sendMessage(Color.Text+"Player "+Color.User+user.getName()+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+" on server "+Color.Value+server+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
                                        }
                                        else
                                        {
                                            ServerInfo si=bc.config.getServers().get(server);
                                            if(si==null)
                                            {
                                                sender.sendMessage(Color.Error+"The server "+Color.Value+server+Color.Error+" does not exist!");
                                                return true;
                                            }
                                            boolean has=pm.hasPermOnServerInWorld(user.getName(), args[3].toLowerCase(),si,world);
                                            sender.sendMessage(Color.Text+"Player "+Color.User+user.getName()+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
                                        }
                                    }
								}
								else
								{
									sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("addgroup"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.user.group.add",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								String groupname=args[3];
								Group group=pm.getGroup(groupname);
								if(group==null)
								{
									sender.sendMessage(Color.Error+"The group "+Color.User+groupname+Color.Error+" does not exist!");
									return true;
								}
								User u=pm.getUser(player);
								if(u!=null)
								{
									List<Group> groups=u.getGroups();
									for(int i=0;i<groups.size();i++)
									{
										if(groups.get(i).getName().equalsIgnoreCase(group.getName()))
										{
											sender.sendMessage(Color.Error+"Player is already in group "+Color.Value+groupname+Color.Error+"!");
											return true;
										}
									}
                                    
                                    pm.addUserGroup(u, group);
									sender.sendMessage(Color.Text+"Added group "+Color.Value+groupname+Color.Text+" to player "+Color.User+u.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("removegroup"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.user.group.remove",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								String groupname=args[3];
								Group group=pm.getGroup(groupname);
								if(group==null)
								{
									sender.sendMessage(Color.Error+"The group "+Color.User+groupname+Color.Error+" does not exist!");
									return true;
								}
								User u=pm.getUser(player);
								if(u!=null)
								{
									List<Group> groups=u.getGroups();
									for(int j=0;j<groups.size();j++)
									{
										if(groups.get(j).getName().equalsIgnoreCase(group.getName()))
										{
                                            pm.removeUserGroup(u, group);
											sender.sendMessage(Color.Text+"Removed group "+Color.Value+groupname+Color.Text+" from player "+Color.User+u.getName()+Color.Text+".");
											return true;
										}
									}
									sender.sendMessage(Color.Error+"Player is not in group "+Color.Value+groupname+Color.Error+"!");
								}
								else
								{
									sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("setgroup"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.user.group.set",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								String groupname=args[3];
								Group group=pm.getGroup(groupname);
								if(group==null)
								{
									sender.sendMessage(Color.Error+"The group "+Color.User+groupname+Color.Error+" does not exist!");
									return true;
								}
								User u=pm.getUser(player);
								if(u!=null)
								{
                                    List<Group> laddergroups=pm.getLadderGroups(group.getLadder());
                                    for(Group g:laddergroups)
                                    {
                                        pm.removeUserGroup(u, g);
                                    }
                                    pm.addUserGroup(u, group);
									sender.sendMessage(Color.Text+"Set group "+Color.Value+groupname+Color.Text+" for player "+Color.User+u.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
								}
							}
							return true;
						}
					}
				}
				else if(args[0].equalsIgnoreCase("groups"))
				{
                    if(pm.hasOrConsole(sender,"bungeeperms.groups.list",true))
                    {
                        if(!matchArgs(sender,args,1))
                        {
                            return true;
                        }
                        
                        if(pm.getGroups().size()>0)
                        {
                            sender.sendMessage(Color.Text+"There are following groups:");
                            for(String l:pm.getLadders())
                            {
                                for(Group g:pm.getLadderGroups(l))
                                {
                                    sender.sendMessage(Color.Text+"- "+Color.Value+g.getName()+Color.Text+" ("+Color.Value+l+Color.Text+")");
                                }
                            }
                        }
                        else
                        {
                            sender.sendMessage(Color.Text+"No groups found!");
                        }
                    }
                    return true;
				}
				else if(args[0].equalsIgnoreCase("group"))
				{
                    if(args.length<3)
                    {
                        Messages.sendTooLessArgsMessage(sender);
                        return true;
                    }
                    if(args[2].equalsIgnoreCase("list"))
                    {
                        if(pm.hasOrConsole(sender,"bungeeperms.group.perms.list",true))
                        {
                            
                            if(args.length>5)
                            {
                                Messages.sendTooManyArgsMessage(sender);
                                return true;
                            }
                            
                            String groupname=args[1];
                            String server=args.length>3?args[3]:null;
                            String world=args.length>4?args[4]:null;
                            Group group=pm.getGroup(groupname);
                            if(group!=null)
                            {
                                sender.sendMessage(Color.Text+"Permission of the group "+Color.Value+group.getName()+Color.Text+":");
                                List<BPPermission> perms=group.getPermsWithOrigin(server, world);
                                for(BPPermission perm:perms)
                                {
                                    sender.sendMessage(Color.Text+"- "+Color.Value+perm.getPermission()+Color.Text+
                                            " ("+
                                            Color.Value+(perm.getOrigin().equalsIgnoreCase(groupname)?"own":perm.getOrigin())+Color.Text+
                                            (perm.getServer()!=null?" | "+Color.Value+perm.getServer()+Color.Text:"")+
                                            (perm.getWorld()!=null?" | "+Color.Value+perm.getWorld()+Color.Text:"")+
                                            ")");
                                }
                            }
                            else
                            {
                                sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
                            }
                        }
                        return true;
                    }
                    else if(args[2].equalsIgnoreCase("info"))
                    {
                        if(pm.hasOrConsole(sender,"bungeeperms.group.info",true))
                        {
                            if(!matchArgs(sender,args,3))
                            {
                                return true;
                            }
                            
                            String groupname=args[1];
                            Group group=pm.getGroup(groupname);
                            if(group!=null)
                            {
                                sender.sendMessage(Color.Text+"Info to group "+Color.Value+group.getName()+Color.Text+":");

                                //inheritances
                                String inheritances="";
                                for(int i=0;i<group.getInheritances().size();i++)
                                {
                                    inheritances+=Color.Value+group.getInheritances().get(i)+Color.Text+" ("+Color.Value+pm.getGroup(group.getInheritances().get(i)).getPerms().size()+Color.Text+")"+(i+1<group.getInheritances().size()?", ":"");
                                }
                                if(inheritances.length()==0)
                                {
                                    inheritances=Color.Text+"(none)";
                                }
                                sender.sendMessage(Color.Text+"Inheritances: "+inheritances);

                                //group perms
                                sender.sendMessage(Color.Text+"Group permissions: "+Color.Value+group.getPerms().size());

                                //group rank
                                sender.sendMessage(Color.Text+"Rank: "+Color.Value+group.getRank());

                                //group ladder
                                sender.sendMessage(Color.Text+"Ladder: "+Color.Value+group.getLadder());

                                //default
                                sender.sendMessage(Color.Text+"Default: "+Color.Value+(group.isDefault()?ChatColor.GREEN:ChatColor.RED)+String.valueOf(group.isDefault()).toUpperCase());

                                //all group perms
                                sender.sendMessage(Color.Text+"Effective permissions: "+Color.Value+group.getEffectivePerms().size());

                                //display
                                sender.sendMessage(Color.Text+"Dislay name: "+ChatColor.RESET+(group.getDisplay().length()>0?group.getDisplay():Color.Text+"(none)"));

                                //prefix
                                sender.sendMessage(Color.Text+"Prefix: "+ChatColor.RESET+(group.getPrefix().length()>0?group.getPrefix():Color.Text+"(none)"));

                                //suffix
                                sender.sendMessage(Color.Text+"Suffix: "+ChatColor.RESET+(group.getSuffix().length()>0?group.getSuffix():Color.Text+"(none)"));
                            }
                            else
                            {
                                sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
                            }//TODO
                        }
                        return true;
                    }
                    else if(args[2].equalsIgnoreCase("create"))
                    {
                        if(pm.hasOrConsole(sender,"bungeeperms.group.create",true))
                        {
                            if(!matchArgs(sender,args,3))
                            {
                                return true;
                            }
                            
                            String groupname=args[1];
                            if(pm.getGroup(groupname)!=null)
                            {
                                sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" already exists!");
                                return true;
                            }
                            Group group=new Group(groupname, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String,Server>(), 1500, "default", false, "", "", "");
                            pm.addGroup(group);
                            sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" created.");
                        }
                        return true;
                    }
                    else if(args[2].equalsIgnoreCase("delete"))
                    {
                        if(pm.hasOrConsole(sender,"bungeeperms.group.delete",true))
                        {
                            if(!matchArgs(sender,args,3))
                            {
                                return true;
                            }
                            
                            String groupname=args[1];
                            Group group=pm.getGroup(groupname);
                            if(group!=null)
                            {
                                pm.deleteGroup(group);
                                sender.sendMessage(Color.Text+"Group "+Color.Value+group.getName()+Color.Text+" deleted.");
                            }
                            else
                            {
                                sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
                            }
                        }
                        return true;
                    }
                    
					else if(args.length>=4)
					{
						if(Statics.ArgAlias(args[2], new String[]{"add","addperm","addpermission"}))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.perms.add",true))
							{
								String groupname=args[1];
								String perm=args[3].toLowerCase();
								String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									if(server==null)
									{
										if(group.getPerms().contains("-"+perm))
										{
                                            pm.removeGroupPerm(group,"-"+perm);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+group.getName()+Color.Text+".");
										}
										else if(!group.getPerms().contains(perm))
										{
                                            pm.addGroupPerm(group,perm);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+group.getName()+Color.Text+".");
										}
										else
										{
											sender.sendMessage(Color.Text+"The group "+Color.Value+group.getName()+Color.Text+" already has the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
                                        if(world==null)
                                        {
                                            Server srv=group.getServers().get(server);
                                            if(srv==null)
                                            {
                                                srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
                                            }
                                            List<String> perserverperms=srv.getPerms();
                                            if(perserverperms.contains("-"+perm))
                                            {
                                                pm.removeGroupPerServerPerm(group, server, "-"+perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else if(!perserverperms.contains(perm))
                                            {
                                                pm.addGroupPerServerPerm(group, server, perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage("The group "+Color.Value+group.getName()+Color.Text+" already has the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                        }
                                        else
                                        {
                                            Server srv=group.getServers().get(server);
                                            if(srv==null)
                                            {
                                                srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
                                            }
                                            
                                            World w=srv.getWorlds().get(world);
                                            if(w==null)
                                            {
                                                w=new World(world,new ArrayList<String>(),"","","");
                                            }
                                            
                                            List<String> perserverworldperms=w.getPerms();
                                            if(perserverworldperms.contains("-"+perm))
                                            {
                                                pm.removeGroupPerServerWorldPerm(group, server,world, "-"+perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else if(!perserverworldperms.contains(perm))
                                            {
                                                pm.addGroupPerServerWorldPerm(group, server,world, perm);
                                                sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage("The group "+Color.Value+group.getName()+Color.Text+" already has the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                        }
									}
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(Statics.ArgAlias(args[2], new String[]{"remove","removeperm","removepermission"}))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.perms.remove",true))
							{
								String groupname=args[1];
								String perm=args[3].toLowerCase();
								String server=args.length>4?args[4]:null;
								String world=args.length>5?args[5]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									if(server==null)
									{
										if(group.getPerms().contains(perm))
										{
                                            pm.removeGroupPerm(group, perm);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+group.getName()+Color.Text+".");
										}
										else if(!group.getPerms().contains("-"+perm))
										{
                                            pm.addGroupPerm(group, "-"+perm);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+group.getName()+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The group "+Color.Value+group.getName()+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
                                        if(world==null)
                                        {
                                            Server srv=group.getServers().get(server);
                                            if(srv==null)
                                            {
                                                srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
                                            }
                                            List<String> perserverperms=srv.getPerms();
                                            if(perserverperms.contains(perm))
                                            {
                                                pm.removeGroupPerServerPerm(group, server, perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else if(!perserverperms.contains("-"+perm))
                                            {
                                                pm.addGroupPerServerPerm(group, server, "-"+perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage("The group "+Color.Value+group.getName()+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
                                            }
                                        }
                                        else
                                        {
                                            Server srv=group.getServers().get(server);
                                            if(srv==null)
                                            {
                                                srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
                                            }
                                            
                                            World w=srv.getWorlds().get(world);
                                            if(w==null)
                                            {
                                                w=new World(world,new ArrayList<String>(),"","","");
                                            }
                                            
                                            List<String> perserverworldperms=w.getPerms();
                                            if(perserverworldperms.contains(perm))
                                            {
                                                pm.removeGroupPerServerWorldPerm(group, server,world, perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else if(!perserverworldperms.contains("-"+perm))
                                            {
                                                pm.addGroupPerServerWorldPerm(group, server,world, "-"+perm);
                                                sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+group.getName()+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                            else
                                            {
                                                sender.sendMessage("The group "+Color.Value+group.getName()+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+".");
                                            }
                                        }
									}
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("has"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.perms.has",true))
							{
								String groupname=args[1];
								String perm=args[3];
								String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									if(server==null)
									{
										boolean has=group.has(perm.toLowerCase());
										sender.sendMessage(Color.Text+"Group "+Color.Value+group.getName()+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
									}
									else
									{
                                        if(world==null)
                                        {
                                            ServerInfo si=bc.config.getServers().get(server);
                                            if(si==null)
                                            {
                                                sender.sendMessage(Color.Error+"The server "+Color.Value+server+Color.Error+" does not exist!");
                                                return true;
                                            }
                                            boolean has=group.hasOnServer(perm.toLowerCase(),si);
                                            sender.sendMessage(Color.Text+"Group "+Color.Value+group.getName()+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+" on server "+Color.Value+server+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
                                        }
                                        else
                                        {
                                            ServerInfo si=bc.config.getServers().get(server);
                                            if(si==null)
                                            {
                                                sender.sendMessage(Color.Error+"The server "+Color.Value+server+Color.Error+" does not exist!");
                                                return true;
                                            }
                                            
                                            boolean has=group.hasOnServerInWorld(perm.toLowerCase(),si,world);
                                            sender.sendMessage(Color.Text+"Group "+Color.Value+group.getName()+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+" on server "+Color.Value+server+Color.Text+" in world "+Color.Value+world+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
                                        }
									}
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						if(Statics.ArgAlias(args[2], new String[]{"addinherit","addinheritance"}))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.inheritances.add",true))
							{
								String groupname=args[1];
								String addgroup=args[3];
								Group toadd=pm.getGroup(addgroup);
								if(toadd==null)
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+addgroup+Color.Error+" does not exist!");
									return true;
								}
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									List<String> inheritances=group.getInheritances();
									
									//check for already existing inheritance
									for(String s:inheritances)
									{
										if(s.equalsIgnoreCase(toadd.getName()))
										{
											sender.sendMessage(Color.Error+"The group already inherits from "+Color.Value+addgroup+Color.Error+"!");
											return true;
										}
									}
                                    
                                    pm.addGroupInheritance(group, toadd);
                                    
									sender.sendMessage(Color.Text+"Added inheritance "+Color.Value+addgroup+Color.Text+" to group "+Color.Value+group.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(Statics.ArgAlias(args[2], new String[]{"removeinherit","removeinheritance"}))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.inheritances.remove",true))
							{
								String groupname=args[1];
								String removegroup=args[3];
								Group toremove=pm.getGroup(removegroup);
								if(toremove==null)
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+removegroup+Color.Error+" does not exist!");
									return true;
								}
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									List<String> inheritances=group.getInheritances();
									for(int i=0;i<inheritances.size();i++)
									{
										if(inheritances.get(i).equalsIgnoreCase(toremove.getName()))
										{
											pm.removeGroupInheritance(group, toremove);
                                            
											sender.sendMessage(Color.Text+"Removed inheritance "+Color.Value+removegroup+Color.Text+" from group "+Color.Value+group.getName()+Color.Text+".");
											return true;
										}
									}
									sender.sendMessage(Color.Error+"The group "+Color.Value+group.getName()+Color.Error+" does not inherit from group "+Color.Value+removegroup+Color.Error+"!");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("rank"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.rank",true))
							{
								String groupname=args[1];
								int rank;
								try
								{
									rank=Integer.parseInt(args[3]);
									if(rank<1)
									{
										throw new Exception();
									}
								}
								catch(Exception e)
								{
									sender.sendMessage(Color.Error+"A whole number greater than 0 is required!");
									return true;
								}
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
                                    pm.rankGroup(group,rank);
									sender.sendMessage(Color.Text+"Group rank set for group "+Color.Value+group.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
                        else if(args[2].equalsIgnoreCase("ladder"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.ladder",true))
							{
								String groupname=args[1];
								String ladder=args[3];
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
                                    pm.ladderGroup(group,ladder);
									sender.sendMessage(Color.Text+"Group ladder set for group "+Color.Value+group.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("default"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.default",true))
							{
								String groupname=args[1];
								boolean isdefault;
								try
								{
									isdefault=parseTrueFalse(args[3]);
								}
								catch(Exception e)
								{
									sender.sendMessage(Color.Error+"A form of '"+Color.Value+"true"+Color.Error+"','"+Color.Value+"false"+Color.Error+"','"+Color.Value+"yes"+Color.Error+"' or '"+Color.Value+"no"+Color.Error+"' is required!");
									return true;
								}
								
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
                                    pm.setGroupDefault(group, isdefault);
									sender.sendMessage(Color.Text+"Marked group "+Color.Value+group.getName()+Color.Text+" as "+(isdefault?"":"non-")+"default.");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("display"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.display",true))
							{
								String groupname=args[1];
								String display=args[3];
                                String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
                                    pm.setGroupDisplay(group, display,server,world);
									sender.sendMessage(Color.Text+"Set display name for group "+Color.Value+group.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("prefix"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.prefix",true))
							{
								String groupname=args[1];
								String prefix=args[3];
                                String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									pm.setGroupPrefix(group, prefix,server,world);
									sender.sendMessage(Color.Text+"Set display name for group "+Color.Value+group.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("suffix"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.suffix",true))
							{
								String groupname=args[1];
								String suffix=args[3];
                                String server=args.length>4?args[4]:null;
                                String world=args.length>5?args[5]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									pm.setGroupSuffix(group, suffix,server,world);
									sender.sendMessage(Color.Text+"Set display name for group "+Color.Value+group.getName()+Color.Text+".");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
					}
				}
				else if(args[0].equalsIgnoreCase("promote"))
				{
					if(args.length>=2)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.promote",true))
						{
							//getting next group
							User user=pm.getUser(Statics.getFullPlayerName(bc,args[1]));
							if(user==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+args[1]+Color.Error+" does not exist!");
								return true;
							}
							Group playergroup=null;
							Group nextgroup=null;
                            if(args.length==3)
                            {
                                String ladder=args[2];
                                playergroup = user.getGroupByLadder(ladder);
                                if(playergroup!=null)
                                {
                                    nextgroup=pm.getNextGroup(playergroup);
                                }
                                else
                                {
                                    List<Group> laddergroups=pm.getLadderGroups(ladder);
                                    if(!laddergroups.isEmpty())
                                    {
                                        nextgroup=laddergroups.get(0);
                                    }
                                }
                            }
                            else
                            {
                                playergroup=pm.getMainGroup(user);
                                if(playergroup==null)
                                {
                                    sender.sendMessage(Color.Error+"The player "+Color.User+user.getName()+Color.Error+" doesn't have a group!");
                                    return true;
                                }
                                nextgroup=pm.getNextGroup(playergroup);
                            }
							
							if(nextgroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+user.getName()+Color.Error+" can't be promoted!");
								return true;
							}
                            
                            //permision checks if sender is a player
                            if(sender instanceof ProxiedPlayer)
                            {
                                User issuer=pm.getUser(sender.getName());
								if(issuer==null)
								{
									sender.sendMessage(Color.Error+"You do not exist!");
									return true;
								}
								Group issuergroup=pm.getMainGroup(issuer);
								if(issuergroup==null)
								{
									sender.sendMessage(Color.Error+"You don't have a group!");
									return true;
								}
                                if(!(issuergroup.getRank()<nextgroup.getRank()))
								{
									sender.sendMessage(Color.Error+"You can't promote the player "+Color.User+user.getName()+Color.Error+"!");
									return true;
								}
                            }
                            
							//promote player
                            //remove old group if neccessary
                            if(playergroup!=null)
                            {
                                pm.removeUserGroup(user, playergroup);
                            }
                            pm.addUserGroup(user, nextgroup);
                            sender.sendMessage(Color.User+user.getName()+Color.Text+" is now "+Color.Value+nextgroup.getName()+Color.Text+"!");
                            return true;
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("demote"))
				{
					if(args.length>=2)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.demote",true))
						{
							//getting next group
							User user=pm.getUser(Statics.getFullPlayerName(bc,args[1]));
							if(user==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+args[1]+Color.Error+" does not exist!");
								return true;
							}
                            
                            Group playergroup=null;
							Group previousgroup=null;
                            if(args.length==3)
                            {
                                String ladder=args[2];
                                playergroup = user.getGroupByLadder(ladder);
                                if(playergroup!=null)
                                {
                                    previousgroup=pm.getPreviousGroup(playergroup);
                                }
                            }
                            else
                            {
                                playergroup=pm.getMainGroup(user);
                                if(playergroup==null)
                                {
                                    sender.sendMessage(Color.Error+"The player "+Color.User+user.getName()+Color.Error+" doesn't have a group!");
                                    return true;
                                }
                                previousgroup=pm.getPreviousGroup(playergroup);
                            }
                            
							if(previousgroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+user.getName()+Color.Error+" can't be demoted!");
								return true;
							}
                            
							//permision checks if sender is a player
							if(sender instanceof ProxiedPlayer)
							{
								User issuer=pm.getUser(sender.getName());
								if(issuer==null)
								{
									sender.sendMessage(Color.Error+"You do not exist!");
									return true;
								}
								Group issuergroup=pm.getMainGroup(issuer);
								if(issuergroup==null)
								{
									sender.sendMessage(Color.Error+"You don't have a group!");
									return true;
								}
								if(!(issuergroup.getRank()<playergroup.getRank()))
								{
									sender.sendMessage(Color.Error+"You can't demote the player "+Color.User+user.getName()+Color.Error+"!");
									return true;
								}
							}
                            
                            //demote
                            //remove old group if neccessary
                            if(playergroup!=null)
                            {
                                pm.removeUserGroup(user, playergroup);
                            }
                            pm.addUserGroup(user, previousgroup);
                            sender.sendMessage(Color.User+user.getName()+Color.Text+" is now "+Color.Value+previousgroup.getName()+Color.Text+"!");
                            return true;
						}
						return true;
					}
				}
                else if(args[0].equalsIgnoreCase("format"))
				{
                    if(pm.hasOrConsole(sender,"bungeeperms.format",true))
                    {
                        sender.sendMessage(Color.Text+"Formating permissions file/table ...");
                        pm.format();
                        sender.sendMessage(Color.Message+"Finished formating.");
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("cleanup"))
				{
                    if(pm.hasOrConsole(sender,"bungeeperms.cleanup",true))
                    {
                        sender.sendMessage(Color.Text+"Cleaning up permissions file/table ...");
                        int deleted=pm.cleanup();
                        sender.sendMessage(Color.Message+"Finished cleaning. Deleted "+Color.Value+deleted+" users"+Color.Message+".");
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("backend"))
				{
                    if(pm.hasOrConsole(sender,"bungeeperms.backend",true))
                    {
                        if(args.length==1)
                        {
                            sender.sendMessage(Color.Text+"Currently using "+Color.Value+pm.getBackEnd().getType().name()+Color.Text+" as backend");
                        }
                        else if(args.length==2)
                        {
                            String stype=args[1];
                            BackEndType type=null;
                            for(BackEndType bet:BackEndType.values())
                            {
                                if(stype.equalsIgnoreCase(bet.name()))
                                {
                                    type=bet;
                                    break;
                                }
                            }
                            if(type==null)
                            {
                                sender.sendMessage(Color.Error+"Invalid backend type! "+Color.Value+BackEndType.YAML.name()+Color.Error+" or "+Color.Value+BackEndType.MySQL.name()+Color.Error+" is required!");
                                return true;
                            }
                            
                            if(type==pm.getBackEnd().getType())
                            {
                                sender.sendMessage(Color.Error+"Invalid backend type! You can't migrate from "+Color.Value+pm.getBackEnd().getType().name()+Color.Error+" to "+Color.Value+type.name()+Color.Error+"!");
                                return true;
                            }
                            
                            sender.sendMessage(Color.Text+"Migrating permissions to "+Color.Value+type.name()+Color.Text+" ...");
                            pm.migrateBackEnd(type);
                            sender.sendMessage(Color.Message+"Finished migration.");
                        }
                    }
                    return true;
                }
			}
		}
		return false;
	}
	
	private void showHelp(CommandSender sender) 
	{
		sender.sendMessage(ChatColor.GOLD+"                  ------ BungeePerms - Help -----");
		sender.sendMessage(ChatColor.GRAY+"Aliases: "+ChatColor.GOLD+"/bp");
		sender.sendMessage(ChatColor.GOLD+"/bungeeperms"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Welcomes you to BungeePerms");
		if(pm.hasPermOrConsole(sender,"bungeeperms.help")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms help"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows the help");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.reload")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms reload"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Reloads the permissions");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.users")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms users [-c]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the users [or shows the amount of them]");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.info")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> info"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows information to the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.perms.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> addperm <permission> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Adds a permission to the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.perms.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> removeperm <permission> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Remove a permission from the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.perms.has")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> has <permission> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Checks if the given user has the given permission");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.perms.list")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> list"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the permissions of the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.group.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> addgroup <groupname>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Added the given group to the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.group.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> removegroup <groupname>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Removes the given group from the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.group.set")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> setgroup <groupname>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the given group as the main group for the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.groups")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> groups"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the groups the given user is in");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.groups")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms groups"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the groups");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.info")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> info"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows information about the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.create")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> create"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Create a group with the given name");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.delete")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> delete"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Create the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.inheritances.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> addinherit <group>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Adds a inheritance to the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.inheritances.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> removeinherit <group>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Remove a inheritance from the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.rank")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> rank <new rank>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the rank for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.ladder")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> ladder <new ladder>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the ladder for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.default")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> default <true|false>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Determines whether the given group is a default group or not");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.display")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> display <displayname> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the display name for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.prefix")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> prefix <prefix> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the prefix for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.suffix")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> suffix <suffix> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the suffix for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> addperm <permission> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Adds a permission to the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> removeperm <permission> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Remove a permission from the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.has")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> has <permission> [server [world]]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Checks if the given group has the given permission");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.list")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> list"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the permissions of the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.promote")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms promote <username> [ladder]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Promotes the given user to the next rank");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.demote")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms demote <username> [ladder]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Demotes the given user to the previous rank");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.format")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms format"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Reformates the permission.yml or mysql table - "+ChatColor.RED+" BE CAREFUL");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.cleanup")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms cleanup"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Cleans up the permission.yml or mysql table - "+ChatColor.RED+" !BE VERY CAREFUL! - removes a lot of players from the permissions.yml if configured");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.backend")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms backend [yaml|mysql|mysql2]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows the used permissions database (file or mysql table) [or migrates to the given database] - "+ChatColor.RED+" !BE CAREFUL! - BungeePerms needs a mysql account on your server and general table permissions");}
		sender.sendMessage(ChatColor.GOLD+"---------------------------------------------------");
	}
	
	private void loadcmds()
	{
		bc.getPluginManager().registerCommand(this,
				new Command("bungeeperms",null,"bp")
				{
					@Override 
					public void execute(CommandSender sender, String[] args) 
					{
						if(!onCommand(sender, this, "", args))
						{
							sender.sendMessage(Color.Error+"[BungeePerms] Command not found");
						}
					}
				});
	}
	
	private boolean parseTrueFalse(String truefalse) 
	{
		if(Statics.ArgAlias(truefalse, new String[]{"true","yes","t","y","+"}))
		{
			return true;
		}
		else if(Statics.ArgAlias(truefalse, new String[]{"false","no","f","n","-"}))
		{
			return false;
		}
		throw new IllegalArgumentException("truefalse does not represent a boolean value");
	}
	
	public PermissionsManager getPermissionsManager()
	{
		return pm;
	}
    
    public static boolean matchArgs(CommandSender sender, String[] args,int length)
    {
        if(args.length>length)
        {
            Messages.sendTooManyArgsMessage(sender);
            return false;
        }
        else if(args.length<length)
        {
            Messages.sendTooLessArgsMessage(sender);
            return false;
        }
        return true;
    }
}