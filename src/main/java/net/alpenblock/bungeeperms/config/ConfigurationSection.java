package net.alpenblock.bungeeperms.config;

import java.util.Map;
import java.util.Set;
import java.util.List;

public interface ConfigurationSection 
{
	public Set<String> getKeys(boolean deep);
	public Map<String, Object> getValues(boolean deep);
	public boolean contains(String path);
	public boolean isSet(String path);
	public String getCurrentPath();
	public String getName();
	public Configuration getRoot();
	public ConfigurationSection getParent();
	public Object get(String path);
	public Object get(String path, Object def);
	public void set(String path, Object value);
	public ConfigurationSection createSection(String path);
	public ConfigurationSection createSection(String path, Map<?, ?> map);
	public String getString(String path);
	public String getString(String path, String def);
	public boolean isString(String path);
	public int getInt(String path);
	public int getInt(String path, int def);
	public boolean isInt(String path);
	public boolean getBoolean(String path);
	public boolean getBoolean(String path, boolean def);
	public boolean isBoolean(String path);
	public double getDouble(String path);
	public double getDouble(String path, double def);
	public boolean isDouble(String path);
	public long getLong(String path);
	public long getLong(String path, long def);
	public boolean isLong(String path);
	public List<?> getList(String path);
	public List<?> getList(String path, List<?> def);
	public boolean isList(String path);
	public List<String> getStringList(String path);
	public List<Integer> getIntegerList(String path);
	public List<Boolean> getBooleanList(String path);
	public List<Double> getDoubleList(String path);
	public List<Float> getFloatList(String path);
	public List<Long> getLongList(String path);
	public List<Byte> getByteList(String path);
	public List<Character> getCharacterList(String path);
	public List<Short> getShortList(String path);
	public List<Map<?, ?>> getMapList(String path);
	public ConfigurationSection getConfigurationSection(String path);
	public boolean isConfigurationSection(String path);
	public ConfigurationSection getDefaultSection();
	public void addDefault(String path, Object value);
}
