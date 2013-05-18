package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;

import com.google.common.eventbus.Subscribe;

public class BungeePerms extends Plugin implements Listener
{
	static BungeePerms instance;
	public static BungeePerms getInstance()
	{
		return instance;
	}
	
	BungeeCord bc;
	List<Group> groups;
	List<Player> players;
	Config permsconf;
	String permspath;
	int permsversion;
	@Override
	public void onLoad()
	{
		//static
		instance=this;
		
		bc=BungeeCord.getInstance();
		//load commands
		loadcmds();
		this.groups=new ArrayList<Group>();
		this.players=new ArrayList<Player>();
		//load perms form file
		permspath="/permissions.yml";
		permsconf=new Config(this, permspath);
		loadperms();
	}
	@Override
	public void onEnable()
	{
		bc.getLogger().info("Activating BungeePerms ...");
		bc.getPluginManager().registerListener(this,this);
	}
	@Override
	public void onDisable() 
	{
		bc.getLogger().info("Deactivating BungeePerms ...");
	}
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
						if(hasOrConsole(sender,"bungeeperms.help",true))
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
						if(hasOrConsole(sender,"bungeeperms.reload",true))
						{
							loadperms();
							sender.sendMessage("Permissions reloaded");
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("users"))
				{
					if(args.length==1)
					{
						if(hasOrConsole(sender,"bungeeperms.users.list",true))
						{
							if(players.size()>0)
							{
								sender.sendMessage(Color.Text+"There are following players:");
								for(Player p:players)
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
						if(hasOrConsole(sender,"bungeeperms.users.list",true))
						{
							if(!args[1].equalsIgnoreCase("-c"))
							{
								return false;
							}
							if(players.size()>0)
							{
								sender.sendMessage(Color.Text+"There are "+Color.Value+players.size()+Color.Text+" players registered.");
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
							if(hasOrConsole(sender,"bungeeperms.user.perms.list",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								Player user=getUser(player);
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
							if(hasOrConsole(sender,"bungeeperms.user.groups",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								Player user=getUser(player);
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
					}
					else if(args.length==4)
					{
						if(args[2].equalsIgnoreCase("add"))
						{
							if(hasOrConsole(sender,"bungeeperms.user.perms.add",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								String perm=args[3].toLowerCase();
								Player user=getUser(player);
								if(user!=null)
								{
									if(user.getExtraperms().contains("-"+perm.toLowerCase()))
									{
										user.getExtraperms().remove("-"+perm.toLowerCase());
										//user.getExtraperms().add(perm);
										permsconf.setListString("users."+player+".permissions", user.getExtraperms());
										sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+player+Color.Text+".");
									}
									else if(!user.getExtraperms().contains(perm.toLowerCase()))
									{
										user.getExtraperms().add(perm.toLowerCase());
										permsconf.setListString("users."+player+".permissions", user.getExtraperms());
										sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to player "+Color.User+player+Color.Text+".");
									}
								}
								else
								{
									sender.sendMessage(Color.Error+"The player "+Color.User+player+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("remove"))
						{
							if(hasOrConsole(sender,"bungeeperms.user.perms.remove",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								String perm=args[3].toLowerCase();
								Player user=getUser(player);
								if(user!=null)
								{
									if(user.getExtraperms().contains(perm.toLowerCase()))
									{
										user.getExtraperms().remove(perm.toLowerCase());
										permsconf.setListString("users."+player+".permissions", user.getExtraperms());
										sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+player+Color.Text+".");
									}
									else if(!user.getExtraperms().contains("-"+perm.toLowerCase()))
									{
										user.getExtraperms().add("-"+perm.toLowerCase());
										permsconf.setListString("users."+player+".permissions", user.getExtraperms());
										sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from player "+Color.User+player+Color.Text+".");
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
							if(hasOrConsole(sender,"bungeeperms.user.perms.has",true))
							{
								String player=Statics.getFullPlayerName(bc,args[1]);
								Player user=getUser(player);
								if(user!=null)
								{
									boolean has=hasPerm(player, args[3].toLowerCase());
									sender.sendMessage(Color.Text+"Player "+Color.User+player+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
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
						if(hasOrConsole(sender,"bungeeperms.groups.list",true))
						{
							if(groups.size()>0)
							{
								sender.sendMessage(Color.Text+"There are following groups:");
								for(Group g:groups)
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
							if(hasOrConsole(sender,"bungeeperms.group.perms.list",true))
							{
								String groupname=args[1];
								Group group=getGroup(groupname);
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
							if(hasOrConsole(sender,"bungeeperms.group.info",true))
							{
								String groupname=args[1];
								Group group=getGroup(groupname);
								if(group!=null)
								{
									sender.sendMessage(Color.Text+"Info to group "+Color.Value+groupname+Color.Text+":");
									
									//inheritances
									String inheritances="";
									for(int i=0;i<group.getInheritances().size();i++)
									{
										inheritances+=Color.Value+group.getInheritances().get(i)+Color.Text+" ("+Color.Value+getGroup(group.getInheritances().get(i)).getPerms().size()+Color.Text+")"+(i+1<group.getInheritances().size()?", ":"");
									}
									if(inheritances.length()==0)
									{
										inheritances=Color.Text+"(None)";
									}
									sender.sendMessage(Color.Text+"Inheritances: "+inheritances);
									
									//group perms
									sender.sendMessage(Color.Text+"Group permissions: "+Color.Value+group.getPerms().size());
									
									//all group perms
									sender.sendMessage(Color.Text+"Effective permissions: "+Color.Value+group.getEffectivePerms().size());
									
									//display
									sender.sendMessage(Color.Text+"Dislay name: "+ChatColor.RESET+group.getDisplay());
									
									//prefix
									sender.sendMessage(Color.Text+"Prefix: "+ChatColor.RESET+group.getPrefix());
									
									//suffix
									sender.sendMessage(Color.Text+"Suffix: "+ChatColor.RESET+group.getSuffix());
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("create"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.create",true))
							{
								String groupname=args[1];
								if(getGroup(groupname)!=null)
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" already exists!");
									return true;
								}
								Group group=new Group(groupname, new ArrayList<String>(), new ArrayList<String>(), 1500, false, "", "", "");
								groups.add(group);
								sortGroups();
								saveGroup(group);
								sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" created.");
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("delete"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.delete",true))
							{
								String groupname=args[1];
								for(int i=0;i<groups.size();i++)
								{
									if(groups.get(i).getName().equalsIgnoreCase(groupname))
									{
										Group group=groups.get(i);
										groups.remove(i);
										validateUserGroups();
										deleteGroup(group);
										sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" deleted.");
										return true;
									}
								}
								sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
							}
							return true;
						}
					}
					else if(args.length==4)
					{
						if(args[2].equalsIgnoreCase("add"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.perms.add",true))
							{
								String groupname=args[1];
								String perm=args[3];
								Group group=getGroup(groupname);
								if(group!=null)
								{
									if(group.getPerms().contains("-"+perm.toLowerCase()))
									{
										group.getPerms().remove("-"+perm.toLowerCase());
										saveGroup(group);
										sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+groupname+Color.Text+".");
									}
									else if(!group.getPerms().contains(perm.toLowerCase()))
									{
										group.getPerms().add(perm.toLowerCase());
										saveGroup(group);
										sender.sendMessage(Color.Text+"Added permission "+Color.Value+perm+Color.Text+" to group "+Color.Value+groupname+Color.Text+".");
									}
								}
								else
								{
									sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("remove"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.perms.remove",true))
							{
								String groupname=args[1];
								String perm=args[3];
								Group group=getGroup(groupname);
								if(group!=null)
								{
									if(group.getPerms().contains(perm.toLowerCase()))
									{
										group.getPerms().remove(perm.toLowerCase());
										saveGroup(group);
										sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+groupname+Color.Text+".");
									}
									else if(!group.getPerms().contains("-"+perm.toLowerCase()))
									{
										group.getPerms().add("-"+perm.toLowerCase());
										saveGroup(group);
										sender.sendMessage(Color.Text+"Removed permission "+Color.Value+perm+Color.Text+" from group "+Color.Value+groupname+Color.Text+".");
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
							if(hasOrConsole(sender,"bungeeperms.group.perms.has",true))
							{
								String groupname=args[1];
								String perm=args[3];
								Group group=getGroup(groupname);
								if(group!=null)
								{
									boolean has=group.has(perm.toLowerCase());
									sender.sendMessage(Color.Text+"Group "+Color.Value+groupname+Color.Text+" has the permission "+Color.Value+args[3]+Color.Text+": "+(has?ChatColor.GREEN:ChatColor.RED)+String.valueOf(has).toUpperCase());
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
							if(hasOrConsole(sender,"bungeeperms.group.rank",true))
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
								for(int i=0;i<groups.size();i++)
								{
									if(groups.get(i).getName().equalsIgnoreCase(groupname))
									{
										Group group=groups.get(i);
										group.setRank(rank);
										groups.set(i,group);
										saveGroup(group);
										sortGroups();
										sender.sendMessage(Color.Text+"Group rank set for group "+Color.Value+groupname+Color.Text+".");
										return true;
									}
								}
								sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("default"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.default",true))
							{
								String groupname=args[1];
								boolean isdefault;
								System.out.println(args[3]);
								try
								{
									isdefault=parseTrueFalse(args[3]);
								}
								catch(Exception e)
								{
									sender.sendMessage(Color.Error+"A form of '"+Color.Value+"true"+Color.Error+"','"+Color.Value+"false"+Color.Error+"','"+Color.Value+"yes"+Color.Error+"' or '"+Color.Value+"no"+Color.Error+"' is required!");
									return true;
								}
								
								for(int i=0;i<groups.size();i++)
								{
									if(groups.get(i).getName().equalsIgnoreCase(groupname))
									{
										Group group=groups.get(i);
										group.setIsdefault(isdefault);
										groups.set(i,group);
										saveGroup(group);
										sender.sendMessage(Color.Text+"Marked group "+Color.Value+groupname+Color.Text+" as "+(isdefault?"":"non-")+"default.");
										return true;
									}
								}
								sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("display"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.display",true))
							{
								String groupname=args[1];
								String display=args[3];
								for(int i=0;i<groups.size();i++)
								{
									if(groups.get(i).getName().equalsIgnoreCase(groupname))
									{
										Group group=groups.get(i);
										group.setDisplay(display);
										groups.set(i,group);
										saveGroup(group);
										sender.sendMessage(Color.Text+"Set display name for group "+Color.Value+groupname+Color.Text+".");
										return true;
									}
								}
								sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("prefix"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.prefix",true))
							{
								String groupname=args[1];
								String prefix=args[3];
								for(int i=0;i<groups.size();i++)
								{
									if(groups.get(i).getName().equalsIgnoreCase(groupname))
									{
										Group group=groups.get(i);
										group.setPrefix(prefix);
										groups.set(i,group);
										saveGroup(group);
										sender.sendMessage(Color.Text+"Set prefix for group "+Color.Value+groupname+Color.Text+".");
										return true;
									}
								}
								sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("suffix"))
						{
							if(hasOrConsole(sender,"bungeeperms.group.suffix",true))
							{
								String groupname=args[1];
								String suffix=args[3];
								for(int i=0;i<groups.size();i++)
								{
									if(groups.get(i).getName().equalsIgnoreCase(groupname))
									{
										Group group=groups.get(i);
										group.setSuffix(suffix);
										groups.set(i,group);
										saveGroup(group);
										sender.sendMessage(Color.Text+"Set prefix for group "+Color.Value+groupname+Color.Text+".");
										return true;
									}
								}
								sender.sendMessage(Color.Error+"The group "+Color.Value+groupname+Color.Error+" does not exist!");
							}
							return true;
						}
					}
				}
				else if(args[0].equalsIgnoreCase("promote"))
				{
					if(args.length==2)
					{
						if(hasOrConsole(sender,"bungeeperms.promote",true))
						{
							//getting next group
							Player player=getUser(Statics.getFullPlayerName(bc,args[1]));
							if(player==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+args[1]+Color.Error+" does not exist!");
								return true;
							}
							Group playergroup=getMainGroup(player);
							if(playergroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+player.getName()+Color.Error+" doesn't have a group!");
								return true;
							}
							Group nextgroup=getNextGroup(playergroup);
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
										List<String> sgroups=new ArrayList<String>();
										for(Group g:player.getGroups())
										{
											sgroups.add(g.getName());
										}
										permsconf.setListString("users."+player.getName()+".groups",sgroups);
										sender.sendMessage(Color.User+player.getName()+Color.Text+" is now "+Color.Value+nextgroup.getName()+Color.Text+"!");
										return true;
									}
								}
							}
							else
							{
								Player issuer=getUser(sender.getName());
								if(issuer==null)
								{
									sender.sendMessage(Color.Error+"You do not exist!");
									return true;
								}
								Group issuergroup=getMainGroup(issuer);
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
											List<String> sgroups=new ArrayList<String>();
											for(Group g:player.getGroups())
											{
												sgroups.add(g.getName());
											}
											permsconf.setListString("users."+player.getName()+".groups",sgroups);
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
						if(hasOrConsole(sender,"bungeeperms.demote",true))
						{
							//getting next group
							Player player=getUser(Statics.getFullPlayerName(bc,args[1]));
							if(player==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+args[1]+Color.Error+" does not exist!");
								return true;
							}
							Group playergroup=getMainGroup(player);
							if(playergroup==null)
							{
								sender.sendMessage(Color.Error+"The player "+Color.User+player.getName()+Color.Error+" doesn't have a group!");
								return true;
							}
							Group previousgroup=getPreviousGroup(playergroup);
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
										List<String> sgroups=new ArrayList<String>();
										for(Group g:player.getGroups())
										{
											sgroups.add(g.getName());
										}
										permsconf.setListString("users."+player.getName()+".groups",sgroups);
										sender.sendMessage(Color.User+player.getName()+Color.Text+" is now "+Color.Value+previousgroup.getName()+Color.Text+"!");
										return true;
									}
								}
							}
							else
							{
								Player issuer=getUser(sender.getName());
								if(issuer==null)
								{
									sender.sendMessage(Color.Error+"You do not exist!");
									return true;
								}
								Group issuergroup=getMainGroup(issuer);
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
											List<String> sgroups=new ArrayList<String>();
											for(Group g:player.getGroups())
											{
												sgroups.add(g.getName());
											}
											permsconf.setListString("users."+player.getName()+".groups",sgroups);
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
	private void showHelp(CommandSender sender) 
	{
		sender.sendMessage(ChatColor.GOLD+"                  ------ BungeePerms - Help -----");
		sender.sendMessage(ChatColor.GRAY+"Aliases: "+ChatColor.GOLD+"/bp");
		sender.sendMessage(ChatColor.GOLD+"/bungeeperms"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Welcomes you to BungeePerms");
		if(hasPermOrConsole(sender,"bungeeperms.help")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms help"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows the help");}
		if(hasPermOrConsole(sender,"bungeeperms.reload")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms reload"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Reloads the permissions");}
		if(hasPermOrConsole(sender,"bungeeperms.users")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms users [-c]"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the users [or shows the amount of them]");}
		if(hasPermOrConsole(sender,"bungeeperms.user.perms.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> add <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Adds a permission to the given user");}
		if(hasPermOrConsole(sender,"bungeeperms.user.perms.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> remove <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Remove a permission from the given user");}
		if(hasPermOrConsole(sender,"bungeeperms.user.perms.has")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> has <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Checks if the given user has the given permission");}
		if(hasPermOrConsole(sender,"bungeeperms.user.perms.list")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> list"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the permissions of the given user");}
		if(hasPermOrConsole(sender,"bungeeperms.user.groups")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms user <username> groups"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the groups the given user ist in");}
		if(hasPermOrConsole(sender,"bungeeperms.groups")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms groups"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the groups");}
		if(hasPermOrConsole(sender,"bungeeperms.group.info")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> info"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Shows information about the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.create")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> create"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Create a group with the given name");}
		if(hasPermOrConsole(sender,"bungeeperms.group.delete")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> delete"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Create the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.rank")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> rank <new rank>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the rank for the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.default")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> default <true|false>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Determines whether the given group is a default group or not");}
		if(hasPermOrConsole(sender,"bungeeperms.group.display")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> display <displayname>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the display name for the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.prefix")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> prefix <prefix>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the prefix for the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.suffix")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> suffix <suffix>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Sets the suffix for the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.perms.add")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> add <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Adds a permission to the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.perms.remove")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> remove <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Remove a permission from the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.group.perms.has")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> has <permission>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Checks if the given group has the given permission");}
		if(hasPermOrConsole(sender,"bungeeperms.group.perms.list")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms group <groupname> list"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Lists the permissions of the given group");}
		if(hasPermOrConsole(sender,"bungeeperms.promote")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms promote <username>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Promotes the given user to the next rank");}
		if(hasPermOrConsole(sender,"bungeeperms.demote")){sender.sendMessage(ChatColor.GOLD+"/bungeeperms demote <username>"+ChatColor.WHITE+" - "+ChatColor.GRAY+"Demotes the given user to the previous rank");}
		sender.sendMessage(ChatColor.GOLD+"---------------------------------------------------");
	}
	public Group getMainGroup(Player player) 
	{
		if(player==null)
		{
			throw new NullPointerException("player is null");
		}
		Group ret=player.getGroups().get(0);
		for(int i=1;i<player.getGroups().size();i++)
		{
			if(player.getGroups().get(i).getRank()>ret.getRank())
			{
				ret=player.getGroups().get(i);
			}
		}
		return ret;
	}
	public Group getNextGroup(Group group)
	{
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).getRank()==group.getRank())
			{
				if(i+1<groups.size())
				{
					return groups.get(i+1);
				}
				else
				{
					return null;
				}
			}
		}
		throw new IllegalArgumentException("group does not exist (anymore)");
	}
	public Group getPreviousGroup(Group group)
	{
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).getRank()==group.getRank())
			{
				if(i>0)
				{
					return groups.get(i-1);
				}
				else
				{
					return null;
				}
			}
		}
		throw new IllegalArgumentException("group does not exist (anymore)");
	}
	@Subscribe
	public void onLogin(LoginEvent e)
	{
		bc.getLogger().info("[BungeePerms] Login by "+e.getConnection().getName());
		boolean found=false;
		for(int i=0;i<players.size();i++)
		{
			if(e.getConnection().getName().equalsIgnoreCase(players.get(i).getName()))
			{
				found=true;
				break;
			}
		}
		if(!found)
		{
			String playername=e.getConnection().getName();
			bc.getLogger().info("[BungeePerms] Adding default player perms to "+playername);
			List<Group> groups=getDefaultGroups();
			Player p=new Player(playername, groups, new ArrayList<String>());
			//save to cache
			players.add(p);
			//save to file
			List<String> gs=new ArrayList<String>();
			for(Group g:groups)
			{
				gs.add(g.getName());
			}
			permsconf.setListString("users."+playername+".groups", gs);
			permsconf.save();
		}
	}
	@Subscribe
	public void onPermissionCheck(PermissionCheckEvent e)
	{
		e.setHasPermission(hasPermOrConsole(e.getSender(),e.getPermission()));
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
	private void loadperms()
	{
		this.groups.clear();
		this.players.clear();
		
		//load from file
		permsconf.load();
		
		//version
		permsversion=permsconf.getInt("version", 1);
		
		//load groups
		List<String> groups=permsconf.getSubNodes("groups");
		for(String g:groups)
		{
			List<String> inheritances=permsconf.getListString("groups."+g+".inheritances", new ArrayList<String>());
			List<String> permissions=permsconf.getListString("groups."+g+".permissions", new ArrayList<String>());
			boolean isdefault=permsconf.getBoolean("groups."+g+".default",false);
			int rank=permsconf.getInt("groups."+g+".rank", 1000);
			String display=permsconf.getString("groups."+g+".display", "");
			String prefix=permsconf.getString("groups."+g+".prefix", "");
			String suffix=permsconf.getString("groups."+g+".suffix", "");
			Group group=new Group(g, inheritances, permissions, rank, isdefault, display, prefix, suffix);
			this.groups.add(group);
		}
		sortGroups();
		
		//load users
		List<String> users=permsconf.getSubNodes("users");
		for(String u:users)
		{
			List<String> sgroups=permsconf.getListString("users."+u+".groups", new ArrayList<String>());
			List<Group> lgroups=new ArrayList<Group>();
			for(String s:sgroups)
			{
				Group g=getGroup(s);
				if(g!=null)
				{
					lgroups.add(g);
				}
			}
			List<String> extrapermissions=permsconf.getListString("users."+u+".permissions", new ArrayList<String>());
			Player player=new Player(u, lgroups, extrapermissions);
			this.players.add(player);
		}
	}
	public Group getGroup(String groupname)
	{
		for(Group g:groups)
		{
			if(g.getName().equalsIgnoreCase(groupname))
			{
				return g;
			}
		}
		return null;
	}
	public Player getUser(String username)
	{
		for(Player p:players)
		{
			if(p.getName().equalsIgnoreCase(username))
			{
				return p;
			}
		}
		return null;
	}
	public List<Group> getDefaultGroups()
	{
		List<Group> ret=new ArrayList<Group>();
		for(Group g:groups)
		{
			if(g.isDefault())
			{
				ret.add(g);
			}
		}
		return ret;
	}
	public boolean hasPerm(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			return getUser(sender.getName()).hasPerm(permission);
		}
		return false;
	}
	public boolean hasPermOrConsole(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			return getUser(sender.getName()).hasPerm(permission);
		}
		else if(sender instanceof ConsoleCommandSender)
		{
			return true;
		}
		return false;
	}
	public boolean hasPerm(String sender, String permission)
	{
		if(!sender.equalsIgnoreCase("CONSOLE"))
		{
			Player p=getUser(sender);
			if(p==null)
			{
				return false;
			}
			return p.hasPerm(permission);
		}
		return false;
	}
	public boolean hasPermOrConsole(String sender, String permission)
	{
		if(sender.equalsIgnoreCase("CONSOLE"))
		{
			return true;
		}
		else
		{
			Player p=getUser(sender);
			if(p==null)
			{
				return false;
			}
			return p.hasPerm(permission);
		}
	}
	public boolean has(CommandSender sender, String perm, boolean msg)
	{
		if(sender instanceof Player)
		{
			boolean isperm=(hasPerm(sender, perm));
			if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
			return isperm;
		}
		else
		{
			sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);
			return false;
		}
	}
	public boolean hasOrConsole(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPerm(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}
	public List<Group> getGroups()
	{
		return Collections.unmodifiableList(groups);
	}
	public List<Player> getUsers()
	{
		return Collections.unmodifiableList(players);
	}

	private void sortGroups()
	{
		Collections.sort(this.groups, new Comparator<Group>()
				{
					@Override
					public int compare(Group arg0, Group arg1) 
					{
						return -Integer.compare(arg0.getRank(), arg1.getRank());
					}
				});
	}
	private void validateUserGroups() 
	{
		for(int i=0;i<players.size();i++)
		{
			Player p=players.get(i);
			List<Group> pgroups=p.getGroups();
			for(int j=0;j<pgroups.size();j++)
			{
				if(getGroup(pgroups.get(j).getName())==null)
				{
					pgroups.remove(j);
					j--;
				}
			}
			p.setGroups(pgroups);
			players.set(i, p);
		}
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
	private void saveGroup(Group g)
	{
		permsconf.setListString("groups."+g.getName()+".inheritances", g.getInheritances());
		permsconf.setListString("groups."+g.getName()+".permissions", g.getPerms());
		permsconf.setInt("groups."+g.getName()+".rank", g.getRank());
		permsconf.setBool("groups."+g.getName()+".default", g.isDefault());
		permsconf.setString("groups."+g.getName()+".display", g.getDisplay());
		permsconf.setString("groups."+g.getName()+".prefix", g.getPrefix());
		permsconf.setString("groups."+g.getName()+".suffix", g.getSuffix());
	}
	private void deleteGroup(Group group) 
	{
		permsconf.deleteNode("groups."+group.getName());
	}
}