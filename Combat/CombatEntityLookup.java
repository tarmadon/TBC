package TBC.Combat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.EntityRegistry;

import TBC.Pair;
import TBC.CombatEntitySaveData;
import TBC.PlayerSaveData;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.DefaultAttackAbility;
import TBC.Combat.Abilities.IAbility;
import TBC.Combat.Abilities.ICombatAbility;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class CombatEntityLookup
{
	public static CombatEntityLookup Instance = new CombatEntityLookup();

	public Hashtable<String, CombatEntityTemplate> lookupByName = new Hashtable<String, CombatEntityTemplate>();

	public ILevelScale levelScaling;
	private File file;

	public void Initialize(File file, ILevelScale levelScaling)
	{
		this.file = file;
		this.levelScaling = levelScaling;
		BufferedReader buffer = null;
		try
		{
			String defaultEncoding = "UTF-8";
			InputStreamReader input = new InputStreamReader(new FileInputStream(file), defaultEncoding);
			buffer = new BufferedReader(input);
			String nextLine;

			// Get the headers out of the way.
			buffer.readLine();
			while((nextLine = buffer.readLine()) != null)
			{
				String[] split = nextLine.split(",");
				if(split.length < 10)
				{
					continue;
				}

				CombatEntityTemplate baseTemplate = new CombatEntityTemplate();
				baseTemplate.name = split[0].trim();
				baseTemplate.maxHp = Integer.parseInt(split[1].trim());
				baseTemplate.maxMp = Integer.parseInt(split[2].trim());
				baseTemplate.attack = Integer.parseInt(split[3].trim());
				baseTemplate.defense = Integer.parseInt(split[4].trim());
				baseTemplate.mAttack = Integer.parseInt(split[5].trim());
				baseTemplate.mDefense = Integer.parseInt(split[6].trim());
				baseTemplate.speed = Integer.parseInt(split[7].trim());
				baseTemplate.xpValue = Integer.parseInt(split[8].trim());
				baseTemplate.apValue = Integer.parseInt(split[9].trim());
				int abilityStartIndex = 10;

				if(split.length <= abilityStartIndex || split[abilityStartIndex].trim() == "")
				{
					baseTemplate.abilities = this.GetDefaultAttacks();
				}
				else
				{
					ArrayList<Pair> abilities = new ArrayList<Pair>();
					for(int i = abilityStartIndex; i < split.length; i++)
					{
						String[] abilityAndWeight = split[i].split("/");
						if(abilityAndWeight.length != 2)
						{
							continue;
						}

						int weight = Integer.parseInt(abilityAndWeight[0]);
						IAbility ability = AbilityLookup.Instance.GetAbilityWithName(abilityAndWeight[1]);
						if(ability != null)
						{
							abilities.add(new Pair<Integer, IAbility>(weight, ability));
						}
						else
						{
							FMLLog.log(Level.ERROR, "Could not find ability: " + abilityAndWeight[1] + " for entity: " + baseTemplate.name);
						}
					}

					Pair<Integer, IAbility>[] abilitiesArray = new Pair[abilities.size()];
					baseTemplate.abilities = abilities.toArray(abilitiesArray);
				}

				lookupByName.put(baseTemplate.name, baseTemplate);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(buffer != null)
			{
				try
				{
					buffer.close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	public CombatEntity GetCombatEntity(int entityId, String entityType, String templateName, NBTTagCompound tag)
	{
		if(this.lookupByName.containsKey(templateName))
		{
			return new CombatEntity(entityId, entityType, this.lookupByName.get(templateName), tag);
		}

		FMLLog.severe("Could not find entity for:  " + templateName);
		return null;
	}

	public CombatEntity GetCombatEntityForPlayer(EntityPlayer player)
	{
		String playerName = player.getDisplayName();
		String lookupName = "Player" + playerName;
		if(this.lookupByName.containsKey(lookupName))
		{
			CombatEntityTemplate template = this.lookupByName.get(lookupName);
			return new CombatEntity(player.getEntityId(), null, template, PlayerSaveData.GetPlayerTag(player));
		}

		CombatEntitySaveData data = LevelingEngine.Instance.GetPlayerSaveData(player);
		CombatEntityTemplate playerTemplate = CombatEntityTemplate.GetCombatEntityTemplateFromSaveData(playerName, data);
		
		lookupByName.put(lookupName, playerTemplate);
		return new CombatEntity(player.getEntityId(), null, playerTemplate, PlayerSaveData.GetPlayerTag(player));
	}

	public void ClearCombatEntitiesForPlayers()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for(String lookupKey : this.lookupByName.keySet())
		{
			keys.add(lookupKey);
		}
		
		for(String lookupKey : keys)
		{
			if(lookupKey.startsWith("Player"))
			{
				this.lookupByName.remove(lookupKey);
			}
		}
	}

	private Pair[] GetDefaultAttacks()
	{
		return new Pair[]
				{
					new Pair<Integer, ICombatAbility>(1, new DefaultAttackAbility())
				};
	}
}
