package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;

// TODO: Auto-generated Javadoc
/**
 * The Class BungeePerms.
 */
public class BungeePerms extends Plugin implements Listener
{
	
	/** The instance. */
	static BungeePerms instance;
	
	/**
	 * Gets the single instance of BungeePerms.
	 *
	 * @return single instance of BungeePerms
	 */
	public static BungeePerms getInstance()
	{
		return instance;
	}
	
	/** The bc. */
	BungeeCord bc;
	
	/** The pm. */
	PermissionsManager pm;
	
	/* (non-Javadoc)
	 * @see net.md_5.bungee.api.plugin.Plugin#onLoad()
	 */
	@Override
	public void onLoad()
	{
		//static
		instance=this;
		
		bc=BungeeCord.getInstance();
		//load commands
		loadcmds();
		pm=new PermissionsManager(this);
	}
	
	/* (non-Javadoc)
	 * @see net.md_5.bungee.api.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable()
	{
		bc.getLogger().info("Activating BungeePerms ...");
	}
	
	/* (non-Javadoc)
	 * @see net.md_5.bungee.api.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() 
	{
		bc.getLogger().info("Deactivating BungeePerms ...");
	}
	
	/**
	 * On command.
	 *
	 * @param sender the sender
	 * @param cmd the cmd
	 * @param label the label
	 * @param args the args
	 * @return true, if successful
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		if(cmd.getName().equalsIgnoreCase("bungeeperms"))
		{
			if(args.length==0)
			{
				sender.sendMessage(ChatColor.GOLD+"Welcome to BungeePerms, a BungeeCord permissions plugin!");
				return true;
			}
			else if(args.length>0)
			{
				if(args[0].equalsIgnoreCase("help"))
				{
					if(args.length==1)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.help",true))
						{
							showHelp(sender);
							return true;
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("reload"))
				{
					if(args.length==1)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.reload",true))
						{
							pm.loadPerms();
							sender.sendMessage("Permissions reloaded");
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("users"))
				{
					if(args.length==1)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.users.list",true))
						{
							if(pm.getUsers().size()>0)
							{
								sender.sendMessage(Color.Text+"There are following players:");
								for(Player p:pm.getUsers())
								{
									sender.sendMessage(Color.Text+"- "+Color.User+p.getName());
								}
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
							if(pm.getUsers().size()>0)
							{
								sender.sendMessage(Color.Text+"There are "+Color.Value+pm.getUsers().size()+Color.Text+" players registered.");
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
					if(args.length==3)
					{
						if(args[2].equalsIgnoreCase("list"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.user.perms.list",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								Player user=pm.getUser(player);
								if(user!=null)
								{
									sender.sendMessage(Color.Text+"Permissions of the player "+Color.User+player+Color.Text+":");
									List<String> perms=user.getEffectivePerms();
									for(String perm:perms)
									{
										sender.sendMessage(Color.Text+"- "+Color.Value+perm);
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
								String player=Statics.getFullPlayerName(bc,args[1]);
								Player user=pm.getUser(player);
								if(user!=null)
								{
									sender.sendMessage(Color.Text+"Groups of the player "+Color.User+player+Color.Text+":");
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
								String player=Statics.getFullPlayerName(bc,args[1]);
								Player user=pm.getUser(player);
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
								sender.sendMessage(Color.Text+"Groups of the player "+Color.User+player+Color.Text+": "+groups);
								
								//all group perms
								sender.sendMessage(Color.Text+"Effective permissions: "+Color.Value+user.getEffectivePerms().size());//TODO
							}
							return true;
						}
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
								Player user=pm.getUser(player);
								if(user!=null)
								{
									if(server==null)
									{
										if(user.getExtraperms().contains("-"+perm.toLowerCase()))
										{
											user.getExtraperms().remove("-"+perm.toLowerCase());
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+player+Color.Text+".");
										}
										else if(!user.getExtraperms().contains(perm.toLowerCase()))
										{
											user.getExtraperms().add(perm.toLowerCase());
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+player+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The player "+Color.Value+player+Color.Text+" already has the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
										List<String> perserverperms=user.getServerPerms().get(server);
										if(perserverperms==null)
										{
											perserverperms=new ArrayList<>();
										}
										if(perserverperms.contains("-"+perm.toLowerCase()))
										{
											perserverperms.remove("-"+perm.toLowerCase());
											user.getServerPerms().put(server, perserverperms);
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+player+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else if(!perserverperms.contains(perm.toLowerCase()))
										{
											perserverperms.add(perm.toLowerCase());
											user.getServerPerms().put(server, perserverperms);
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+player+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The player "+Color.Value+player+Color.Text+" alreday has the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
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
								Player user=pm.getUser(player);
								if(user!=null)
								{
									if(server==null)
									{
										if(user.getExtraperms().contains(perm.toLowerCase()))
										{
											user.getExtraperms().remove(perm.toLowerCase());
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+player+Color.Text+".");
										}
										else if(!user.getExtraperms().contains("-"+perm.toLowerCase()))
										{
											user.getExtraperms().add("-"+perm.toLowerCase());
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+player+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The player "+Color.Value+player+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
										List<String> perserverperms=user.getServerPerms().get(server);
										if(perserverperms==null)
										{
											perserverperms=new ArrayList<>();
										}
										if(perserverperms.contains(perm.toLowerCase()))
										{
											perserverperms.remove(perm.toLowerCase());
											user.getServerPerms().put(server, perserverperms);
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+player+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else if(!perserverperms.contains("-"+perm.toLowerCase()))
										{
											perserverperms.add("-"+perm.toLowerCase());
											user.getServerPerms().put(server, perserverperms);
											pm.updateUser(user);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+player+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The player "+Color.Value+player+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
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
								Player user=pm.getUser(player);
								String server=args.length>4?args[4]:null;
								if(user!=null)
								{
									if(server==null)
									{
										boolean has=pm.hasPerm(player, args[3].toLowerCase());
										sender.sendMessage(Color.Text+"Player "+Color.User+player+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
									}
									else
									{
										ServerInfo si=bc.config.getServers().get(server);
										if(si==null)
										{
											sender.sendMessage(Color.Error+"The server "+Color.Value+server+Color.Error+" does not exist!");
											return true;
										}
										boolean has=pm.hasPermOnServer(player, args[3].toLowerCase(),si);
										sender.sendMessage(Color.Text+"Player "+Color.User+player+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+" on server "+Color.Value+server+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
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
								Player p=pm.getUser(player);
								if(p!=null)
								{
									List<Group> groups=p.getGroups();
									for(int i=0;i<groups.size();i++)
									{
										if(groups.get(i).getName().equalsIgnoreCase(group.getName()))
										{
											sender.sendMessage(Color.Error+"Player is already in group "+Color.Value+groupname+Color.Error+"!");
											return true;
										}
									}
									groups.add(group);
									p.setGroups(groups);
									pm.updateUser(p);
									sender.sendMessage(Color.Text+"Added group "+Color.Value+groupname+Color.Text+" to player "+Color.User+player+Color.Text+".");
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
								Player p=pm.getUser(player);
								if(p!=null)
								{
									List<Group> groups=p.getGroups();
									for(int j=0;j<groups.size();j++)
									{
										if(groups.get(j).getName().equalsIgnoreCase(group.getName()))
										{
											groups.remove(j);
											p.setGroups(groups);
											pm.updateUser(p);
											sender.sendMessage(Color.Text+"Removed group "+Color.Value+groupname+Color.Text+" from player "+Color.User+player+Color.Text+".");
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
								Player p=pm.getUser(player);
								if(p!=null)
								{
									List<Group> groups=p.getGroups();
									Group main=pm.getMainGroup(p);
									if(main==null)
									{
										groups.add(group);
									}
									else
									{
										for(int i=0;i<groups.size();i++)
										{
											if(groups.get(i).getName().equalsIgnoreCase(main.getName()))
											{
												groups.set(i, group);
												break;
											}
										}
									}
									p.setGroups(groups);
									pm.updateUser(p);
									sender.sendMessage(Color.Text+"Set group "+Color.Value+groupname+Color.Text+" for player "+Color.User+player+Color.Text+".");
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
					if(args.length==1)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.groups.list",true))
						{
							if(pm.getGroups().size()>0)
							{
								sender.sendMessage(Color.Text+"There are following groups:");
								for(Group g:pm.getGroups())
								{
									sender.sendMessage(Color.Text+"- "+Color.Value+g.getName());
								}
							}
							else
							{
								sender.sendMessage(Color.Text+"No groups found!");
							}
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("group"))
				{
					if(args.length==3)
					{
						if(args[2].equalsIgnoreCase("list"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.perms.list",true))
							{
								String groupname=args[1];
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									sender.sendMessage(Color.Text+"Permission of the group "+Color.Value+groupname+Color.Text+":");
									List<String> perms=group.getEffectivePerms();
									for(String perm:perms)
									{
										sender.sendMessage(Color.Text+"- "+Color.Value+perm);
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
								String groupname=args[1];
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									sender.sendMessage(Color.Text+"Info to group "+Color.Value+groupname+Color.Text+":");
									
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
								String groupname=args[1];
								if(pm.getGroup(groupname)!=null)
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" already exists!");
									return true;
								}
								Group group=new Group(groupname, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String,List<String>>(), 1500, false, "", "", "");
								pm.addGroup(group);
								sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" created.");
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("delete"))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.delete",true))
							{
								String groupname=args[1];
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									pm.deleteGroup(group);
									sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" deleted.");
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
					}
					else if(args.length>=4)
					{
						if(Statics.ArgAlias(args[2], new String[]{"add","addperm","addpermission"}))
						{
							if(pm.hasOrConsole(sender,"bungeeperms.group.perms.add",true))
							{
								String groupname=args[1];
								String perm=args[3];
								String server=args.length>4?args[4]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									if(server==null)
									{
										if(group.getPerms().contains("-"+perm.toLowerCase()))
										{
											group.getPerms().remove("-"+perm.toLowerCase());
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+groupname+Color.Text+".");
										}
										else if(!group.getPerms().contains(perm.toLowerCase()))
										{
											group.getPerms().add(perm.toLowerCase());
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+groupname+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The group "+Color.Value+groupname+Color.Text+" already has the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
										List<String> perserverperms=group.getServerPerms().get(server);
										if(perserverperms==null)
										{
											perserverperms=new ArrayList<>();
										}
										if(perserverperms.contains("-"+perm.toLowerCase()))
										{
											perserverperms.remove("-"+perm.toLowerCase());
											group.getServerPerms().put(server, perserverperms);
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+groupname+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else if(!perserverperms.contains(perm.toLowerCase()))
										{
											perserverperms.add(perm.toLowerCase());
											group.getServerPerms().put(server, perserverperms);
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+groupname+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The group "+Color.Value+groupname+Color.Text+" already has the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
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
								String perm=args[3];
								String server=args.length>4?args[4]:null;
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									if(server==null)
									{
										if(group.getPerms().contains(perm.toLowerCase()))
										{
											group.getPerms().remove(perm.toLowerCase());
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+groupname+Color.Text+".");
										}
										else if(!group.getPerms().contains("-"+perm.toLowerCase()))
										{
											group.getPerms().add("-"+perm.toLowerCase());
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+groupname+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The group "+Color.Value+groupname+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+".");
										}
									}
									else
									{
										List<String> perserverperms=group.getServerPerms().get(server);
										if(perserverperms==null)
										{
											perserverperms=new ArrayList<>();
										}
										if(perserverperms.contains(perm.toLowerCase()))
										{
											perserverperms.remove(perm.toLowerCase());
											group.getServerPerms().put(server, perserverperms);
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+groupname+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else if(!perserverperms.contains("-"+perm.toLowerCase()))
										{
											perserverperms.add("-"+perm.toLowerCase());
											group.getServerPerms().put(server, perserverperms);
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+groupname+Color.Text+" on server "+Color.Value+server+Color.Text+".");
										}
										else
										{
											sender.sendMessage("The group "+Color.Value+groupname+Color.Text+" never had the permission "+Color.Value+perm+Color.Text+" on server "+Color.Value+server+Color.Text+".");
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
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									if(server==null)
									{
										boolean has=group.has(perm.toLowerCase());
										sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
									}
									else
									{
										ServerInfo si=bc.config.getServers().get(server);
										if(si==null)
										{
											sender.sendMessage(Color.Error+"The server "+Color.Value+server+Color.Error+" does not exist!");
											return true;
										}
										boolean has=group.hasOnServer(perm.toLowerCase(),si);
										sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+" on server "+Color.Value+server+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());

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
									inheritances.add(toadd.getName());
									
									//saving
									group.setInheritances(inheritances);
									pm.updateGroup(group);
									sender.sendMessage(Color.Text+"Added inheritance "+Color.Value+addgroup+Color.Text+" to group "+Color.Value+groupname+Color.Text+".");
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
											inheritances.remove(i);
											group.setInheritances(inheritances);
											pm.updateGroup(group);
											sender.sendMessage(Color.Text+"Removed inheritance "+Color.Value+removegroup+Color.Text+" from group "+Color.Value+groupname+Color.Text+".");
											return true;
										}
									}
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not inherit from group "+Color.Value+removegroup+Color.Error+"!");
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
								int rank=0;
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
									group.setRank(rank);
									pm.updateGroup(group);
									sender.sendMessage(Color.Text+"Group rank set for group "+Color.Value+groupname+Color.Text+".");
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
									group.setIsdefault(isdefault);
									pm.updateGroup(group);
									sender.sendMessage(Color.Text+"Marked group "+Color.Value+groupname+Color.Text+" as "+(isdefault?"":"non-")+"default.");
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
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									group.setDisplay(display);
									pm.updateGroup(group);
									sender.sendMessage(Color.Text+"Set display name for group "+Color.Value+groupname+Color.Text+".");
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
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									group.setPrefix(prefix);
									pm.updateGroup(group);
									sender.sendMessage(Color.Text+"Set display name for group "+Color.Value+groupname+Color.Text+".");
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
								Group group=pm.getGroup(groupname);
								if(group!=null)
								{
									group.setSuffix(suffix);
									pm.updateGroup(group);
									sender.sendMessage(Color.Text+"Set display name for group "+Color.Value+groupname+Color.Text+".");
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
					if(args.length==2)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.promote",true))
						{
							//getting next group
							Player player=pm.getUser(Statics.getFullPlayerName(bc,args[1]));
							if(player==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+args[1]+Color.Error+" does not exist!");
								return true;
							}
							Group playergroup=pm.getMainGroup(player);
							if(playergroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+player.getName()+Color.Error+" doesn't have a group!");
								return true;
							}
							Group nextgroup=pm.getNextGroup(playergroup);
							if(nextgroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+player.getName()+Color.Error+" can't get promoted!");
								return true;
							}
							//promote player
							if(sender instanceof ConsoleCommandSender)
							{
								for(int i=0;i<player.getGroups().size();i++)
								{
									if(player.getGroups().get(i).getRank()==playergroup.getRank())
									{
										player.getGroups().remove(i);
										player.getGroups().add(nextgroup);
										pm.updateUser(player);
										sender.sendMessage(Color.User+player.getName()+Color.Text+" is now "+Color.Value+nextgroup.getName()+Color.Text+"!");
										return true;
									}
								}
							}
							else
							{
								Player issuer=pm.getUser(sender.getName());
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
								System.out.println(issuergroup.getRank());
								System.out.println(nextgroup.getRank());
								if(issuergroup.getRank()<nextgroup.getRank())
								{
									for(int i=0;i<player.getGroups().size();i++)
									{
										if(player.getGroups().get(i).getRank()==playergroup.getRank())
										{
											player.getGroups().remove(i);
											player.getGroups().add(nextgroup);
											pm.updateUser(player);
											sender.sendMessage(Color.User+player.getName()+Color.Text+" is now "+Color.Value+nextgroup.getName()+Color.Text+"!");
											return true;
										}
									}
									sender.sendMessage(Color.Error+"Error during promotion of player "+Color.User+player.getName()+Color.Error+"!");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"You can't promote the player "+Color.User+player.getName()+Color.Error+"!");
									return true;
								}
							}
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("demote"))
				{
					if(args.length==2)
					{
						if(pm.hasOrConsole(sender,"bungeeperms.demote",true))
						{
							//getting next group
							Player player=pm.getUser(Statics.getFullPlayerName(bc,args[1]));
							if(player==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+args[1]+Color.Error+" does not exist!");
								return true;
							}
							Group playergroup=pm.getMainGroup(player);
							if(playergroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+player.getName()+Color.Error+" doesn't have a group!");
								return true;
							}
							Group previousgroup=pm.getPreviousGroup(playergroup);
							if(previousgroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+player.getName()+Color.Error+" can't get demoted!");
								return true;
							}
							//demote player
							if(sender instanceof ConsoleCommandSender)
							{
								for(int i=0;i<player.getGroups().size();i++)
								{
									if(player.getGroups().get(i).getRank()==playergroup.getRank())
									{
										player.getGroups().remove(i);
										player.getGroups().add(previousgroup);
										pm.updateUser(player);
										sender.sendMessage(Color.User+player.getName()+Color.Text+" is now "+Color.Value+previousgroup.getName()+Color.Text+"!");
										return true;
									}
								}
							}
							else
							{
								Player issuer=pm.getUser(sender.getName());
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
								if(issuergroup.getRank()<playergroup.getRank())
								{
									for(int i=0;i<player.getGroups().size();i++)
									{
										if(player.getGroups().get(i).getRank()==playergroup.getRank())
										{
											player.getGroups().remove(i);
											player.getGroups().add(previousgroup);
											pm.updateUser(player);
											sender.sendMessage(Color.User+player.getName()+Color.Text+" is now "+Color.Value+previousgroup.getName()+Color.Text+"!");
											return true;
										}
									}
									sender.sendMessage(Color.Error+"Error during demotion of player "+Color.User+player.getName()+Color.Error+"!");
									return true;
								}
								else
								{
									sender.sendMessage(Color.Error+"You can't demote the player "+Color.User+player.getName()+Color.Error+"!");
									return true;
								}
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Show help.
	 *
	 * @param sender the sender
	 */
	private void showHelp(CommandSender sender) 
	{
		sender.sendMessage(ChatColor.GOLD+"                  ------ BungeePerms - Help -----");
		sender.sendMessage(ChatColor.GRAY+"Aliases: "+ChatColor.GOLD+"/bp");
		sender.sendMessage(ChatColor.GOLD+"/bungeeperms"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Welcomes you to BungeePerms");
		if(pm.hasPermOrConsole(sender,"bungeeperms.help")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms help"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows the help");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.reload")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms reload"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Reloads the permissions");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.users")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms users [-c]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the users [or shows the amount of them]");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.info")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> info"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows information to the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.perms.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> addperm <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Adds a permission to the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.perms.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> removeperm <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Remove a permission from the given user");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.user.perms.has")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> has <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Checks if the given user has the given permission");}
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
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.default")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> default <true|false>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Determines whether the given group is a default group or not");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.display")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> display <displayname>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the display name for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.prefix")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> prefix <prefix>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the prefix for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.suffix")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> suffix <suffix>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the suffix for the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> addperm <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Adds a permission to the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> removeperm <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Remove a permission from the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.has")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> has <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Checks if the given group has the given permission");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.group.perms.list")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> list"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the permissions of the given group");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.promote")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms promote <username>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Promotes the given user to the next rank");}
		if(pm.hasPermOrConsole(sender,"bungeeperms.demote")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms demote <username>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Demotes the given user to the previous rank");}
		sender.sendMessage(ChatColor.GOLD+"---------------------------------------------------");
	}
	
	/**
	 * Loadcmds.
	 */
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
	
	/**
	 * Parses the true false.
	 *
	 * @param truefalse the truefalse
	 * @return true, if successful
	 */
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
	
	/**
	 * Gets the permissions manager.
	 *
	 * @return the permissions manager
	 */
	public PermissionsManager getPermissionsManager()
	{
		return pm;
	}
}