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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.AbstractMap.SimpleEntry;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import TBC.Pair;
import TBC.Combat.Abilities.DefaultAttackAbility;
import TBC.Combat.Abilities.ICombatAbility;

public class CombatEntitySpawnLookup
{
	public static CombatEntitySpawnLookup Instance = new CombatEntitySpawnLookup();

	public class TemplateWithLevel
	{
		public TemplateWithLevel(Integer level, String templateName)
		{
			this.level = level;
			this.templateName = templateName;
		}

		public String templateName;
		public int level;
	}

	public Hashtable<String, ArrayList<TemplateWithLevel>> lookup = new Hashtable<String, ArrayList<TemplateWithLevel>>();
	private ILevelScale levelScaling;
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
				if(split.length < 7)
				{
					continue;
				}

				CombatEntityTemplate baseTemplate = new CombatEntityTemplate();
				String name = split[0].trim();
				String displayName = split[1].trim();
				String dropItem = split[2].trim();
				String recipeItem = split[3].trim();
				String biome = split[4].trim();
				Integer level = Integer.parseInt(split[5].trim());
				Integer weight = Integer.parseInt(split[6].trim());

				ArrayList<Pair<String, Integer>> additionalDrops = new ArrayList<Pair<String,Integer>>();
				if(split.length >= 8)
				{
					for(int i = 7; i < split.length; i++)
					{
						String value = split[i];
						String[] parts = value.split("/");
						Pair<String, Integer> drop = new Pair<String, Integer>(parts[1], Integer.parseInt(parts[0]));
						additionalDrops.add(drop);
					}
				}

				if(!lookup.containsKey(name))
				{
					lookup.put(name, new ArrayList<TemplateWithLevel>());
				}

				lookup.get(name).add(new TemplateWithLevel(level, displayName));
				ItemReplacementLookup.Instance.AddItemData(name, displayName, dropItem, recipeItem, additionalDrops);
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

	public CombatEntity GetCombatEntity(int entityId, EntityLivingBase innerEntity)
	{
		String entityName = EntityList.getEntityString(innerEntity);
		if(entityName == null)
		{
			return null;
		}
		
		if(innerEntity.getEntityData().getString("TBCEntityName") != "")
		{
			return CombatEntityLookup.Instance.GetCombatEntity(entityId, entityName, innerEntity.getEntityData().getString("TBCEntityName"));
		}

		if(entityName != null && this.lookup.containsKey(entityName))
		{
			ArrayList<TemplateWithLevel> leveledCreatures = this.lookup.get(entityName);
			int currentLevel = this.levelScaling.GetCurrentLevel(innerEntity);
			if(currentLevel == 0)
			{
				currentLevel = 1;
			}

			ArrayList<TemplateWithLevel> nearMatches = new ArrayList<TemplateWithLevel>();
			for(TemplateWithLevel t : leveledCreatures)
			{
				if(t.level <= currentLevel && t.level >= currentLevel - 5)
				{
					nearMatches.add(t);
				}
			}

			if(nearMatches.size() == 0)
			{
				for(TemplateWithLevel t : leveledCreatures)
				{
					if(nearMatches.size() == 0 && t.level <= currentLevel)
					{
						nearMatches.add(t);
					}
					else if(nearMatches.get(0).level == t.level)
					{
						nearMatches.add(t);
					}
					else if(nearMatches.get(0).level < t.level)
					{
						nearMatches.clear();
						nearMatches.add(t);
					}
				}
			}

			if(nearMatches.size() == 0)
			{
				nearMatches.add(leveledCreatures.get(0));
			}

			int totalWeight = nearMatches.size();
			int value = CombatRandom.GetRandom().nextInt(totalWeight);
			CombatEntity found = CombatEntityLookup.Instance.GetCombatEntity(entityId, entityName, nearMatches.get(value).templateName);
			if(found != null)
			{
				return found;
			}

			String templateName = nearMatches.get(value).templateName;
			CombatEntityTemplate template = this.BuildDefaultEntityTemplate(innerEntity);
			template.name = templateName;
			CombatEntityLookup.Instance.lookupByName.put(templateName, template);
			return new CombatEntity(entityId, entityName, template, new NBTTagCompound());
		}

		CombatEntityTemplate template = this.BuildDefaultEntityTemplate(innerEntity);
		CombatEntityLookup.Instance.lookupByName.put(entityName, template);
		return new CombatEntity(entityId, entityName, template, new NBTTagCompound());
	}

	public void LogUnknownEntities()
	{
		for(Object key : EntityList.classToStringMapping.keySet())
		{
			Class asClass = (Class)key;
			if(asClass != null)
			{
				if(EntityLiving.class.isAssignableFrom(asClass) && !asClass.isInterface() && !Modifier.isAbstract(asClass.getModifiers()))
				{
					String entityName = (String)EntityList.classToStringMapping.get(asClass);
					if(!lookup.containsKey(entityName))
					{
						PrintWriter writer = null;
						try
						{
							writer = new PrintWriter(new FileWriter(this.file, true));
							StringBuilder sb = new StringBuilder();
							sb.append(entityName + ",");
							sb.append(entityName + ",,,All,1,100,,");
							writer.println(sb);
						} catch (IOException e) { FMLLog.severe("Could not write unknown creature: %s", entityName); }
						finally
						{
							writer.close();
						}
					}
				}
			}
		}
	}

	private CombatEntityTemplate BuildDefaultEntityTemplate(EntityLivingBase innerEntity)
	{
		String entityName = EntityList.getEntityString(innerEntity);
		float maxHp = innerEntity.getMaxHealth();
		int speed = Math.round(10 * innerEntity.getAIMoveSpeed());
		if (innerEntity instanceof EntityMob)
		{
			// Technically not correctly, should be attack strength vs. player not vs. self
			double attack = ((EntityMob) innerEntity).getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
			CombatEntityTemplate mobTemplate = new CombatEntityTemplate();
			mobTemplate.maxHp = (int) maxHp;
			mobTemplate.maxMp = 0;
			mobTemplate.attack = (int) attack * 5;
			mobTemplate.defense = (int) attack * 5;
			mobTemplate.mAttack = (int) attack * 5;
			mobTemplate.mDefense = (int) attack * 5;
			mobTemplate.speed = speed;
			mobTemplate.name = entityName;
			mobTemplate.abilities = GetDefaultAttacks();
			return mobTemplate;
		}

		if(innerEntity instanceof EntityAnimal)
		{
			int effectiveAttack = (int)maxHp / 10;
			EntityAnimal asAnimal = (EntityAnimal)innerEntity;
			CombatEntityTemplate animalTemplate = new CombatEntityTemplate();
			animalTemplate.maxHp = (int)maxHp;
			animalTemplate.maxMp = 0;
			animalTemplate.attack = effectiveAttack;
			animalTemplate.defense = effectiveAttack;
			animalTemplate.mAttack = effectiveAttack;
			animalTemplate.mDefense = effectiveAttack;
			animalTemplate.speed = speed;
			animalTemplate.name = entityName;
			animalTemplate.abilities = GetDefaultAttacks();
		}

		return GetDefaultEntity("Default" + entityName);
	}

	private CombatEntityTemplate GetDefaultEntity(String name)
	{
		CombatEntityTemplate player = new CombatEntityTemplate();
		player.maxHp = 50;
		player.maxMp = 1;
		player.attack = 3;
		player.defense = 3;
		player.mAttack = 3;
		player.mDefense = 3;
		player.speed = 5;
		player.name = name;
		player.abilities = new Pair[]
			{
				new Pair<Integer, ICombatAbility>(1, new DefaultAttackAbility())
			};

		return player;
	}

	private Pair[] GetDefaultAttacks()
	{
		return new Pair[]
				{
					new Pair<Integer, ICombatAbility>(1, new DefaultAttackAbility())
				};
	}
}
