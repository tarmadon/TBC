package TBC;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import TBC.Combat.CombatEntityTemplate;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.IAbility;
import TBC.Combat.Abilities.ICombatAbility;

public class CombatEntitySaveData implements Serializable
{
	public int QuestProgress;

	public int IsInParty;
	public int IsFrontRow;
	public int CurrentMp;
	public int CurrentXp;
	public int CurrentAp;
	public int Level;
	public int BonusMaxHP;
	public int BonusMaxMP;
	public int BonusAttack;
	public int BonusDefense;
	public int BonusMAttack;
	public int BonusMDefense;
	public int BonusSpeed;
	public List<String> Abilities;
	public List<Pair<String, Integer>> JobLevels;
	public String CurrentJob;
	public String SecondaryJob;
	
	public CombatEntitySaveData()
	{	
	}
	
	public CombatEntitySaveData(CombatEntityTemplate data)
	{
		this.BonusMaxHP = data.maxHp;
		this.BonusMaxMP = data.maxMp;
		this.BonusAttack = data.attack;
		this.BonusDefense = data.defense;
		this.BonusMAttack = data.mAttack;
		this.BonusMDefense = data.mDefense;
		this.BonusSpeed = data.speed;

		this.Abilities = new ArrayList<String>();
		for(Pair<Integer, IAbility> s : data.abilities)
		{
			this.Abilities.add(AbilityLookup.Instance.GetLookupNameForAbility(s.item2));
		}
		
		this.JobLevels = new ArrayList<Pair<String, Integer>>();
		this.CurrentJob = "";
		this.SecondaryJob = "";
	}
	
	public void loadNBTData(NBTTagCompound nbttagcompound)
	{
		QuestProgress = getIntOrDefault(nbttagcompound, "questProgress", 1);
		IsInParty = getIntOrDefault(nbttagcompound, "TBCIsInParty", 0);
		IsFrontRow = getIntOrDefault(nbttagcompound, "TBCIsFrontRow", 0);
		CurrentMp = getIntOrDefault(nbttagcompound, "TBCCurrentMP", 0);
		CurrentXp = getIntOrDefault(nbttagcompound, "TBCXP", 0);
		CurrentAp = getIntOrDefault(nbttagcompound, "TBCAP", 0);
		Level = getIntOrDefault(nbttagcompound, "TBCLevel", 1);
		BonusMaxHP = getIntOrDefault(nbttagcompound, "TBCMaxHP", 0);
		BonusMaxMP = getIntOrDefault(nbttagcompound, "TBCMaxMP", 0);
		BonusAttack = getIntOrDefault(nbttagcompound, "TBCAttack", 0);
		BonusDefense = getIntOrDefault(nbttagcompound, "TBCDefense", 0);
		BonusMAttack = getIntOrDefault(nbttagcompound, "TBCMAttack", 0);
		BonusMDefense = getIntOrDefault(nbttagcompound, "TBCMDefense", 0);
		BonusSpeed = getIntOrDefault(nbttagcompound, "TBCSpeed", 0);
		CurrentJob = getStringOrDefault(nbttagcompound, "TBCCurrentJob", "Adventurer");
		SecondaryJob = getStringOrDefault(nbttagcompound, "TBCSecondaryJob", "");
		
		if(nbttagcompound.hasKey("TBCAbilities"))
		{
			String abilities = nbttagcompound.getString("TBCAbilities");
			String[] splitAbilities = abilities.split(",");
			this.Abilities = Arrays.asList(splitAbilities);
		}
		else
		{
			this.Abilities = new ArrayList<String>();
			this.Abilities.add("Default");
		}
		
		this.JobLevels = new ArrayList<Pair<String, Integer>>();
		if(nbttagcompound.hasKey("TBCClassLevels"))
		{
			NBTTagCompound classLevels = nbttagcompound.getCompoundTag("TBCClassLevels");
			for(Object keyAsObject : classLevels.func_150296_c())
			{
				String jobName = (String)keyAsObject;
				Integer jobLevel = classLevels.getInteger(jobName);
				this.JobLevels.add(new Pair<String, Integer>(jobName, jobLevel));
			}
		}
		else
		{
			this.JobLevels.add(new Pair<String, Integer>("Adventurer", 1));
		}
	}

	public void saveNBTData(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setInteger("questProgress", QuestProgress);
		nbttagcompound.setString("TBCCurrentJob", CurrentJob);
		nbttagcompound.setString("TBCSecondaryJob", SecondaryJob);

		nbttagcompound.setInteger("TBCIsInParty", IsInParty);
		nbttagcompound.setInteger("TBCIsFrontRow", IsFrontRow);
		nbttagcompound.setInteger("TBCCurrentMP", CurrentMp);
		nbttagcompound.setInteger("TBCXP", CurrentXp);
		nbttagcompound.setInteger("TBCAP", CurrentAp);
		nbttagcompound.setInteger("TBCLevel", Level);
		nbttagcompound.setInteger("TBCMaxHP", BonusMaxHP);
		nbttagcompound.setInteger("TBCMaxMP", BonusMaxMP);
		nbttagcompound.setInteger("TBCAttack", BonusAttack);
		nbttagcompound.setInteger("TBCDefense", BonusDefense);
		nbttagcompound.setInteger("TBCMAttack", BonusMAttack);
		nbttagcompound.setInteger("TBCMDefense", BonusMDefense);
		nbttagcompound.setInteger("TBCSpeed", BonusSpeed);

		StringBuilder builder = new StringBuilder();
		for(String s : this.Abilities)
		{
			builder.append(s);
			builder.append(',');
		}

		builder.deleteCharAt(builder.length() - 1);
		nbttagcompound.setString("TBCAbilities", builder.toString());
		
		NBTTagCompound classLevels = new NBTTagCompound();
		for(Pair<String, Integer> job : this.JobLevels)
		{
			classLevels.setInteger(job.item1, job.item2);
		}
		
		nbttagcompound.setTag("TBCClassLevels", classLevels);
	}

	public void init(Entity entity, World world)
	{
	}

	public int GetJobLevel(String jobName)
	{
		for(Pair<String, Integer> jobLevel : this.JobLevels)
		{
			if(jobLevel.item1.equals(jobName))
			{
				return jobLevel.item2;
			}
		}
		
		return 0;
	}
	
	public int GetJobLevelMin1(String jobName)
	{
		for(Pair<String, Integer> jobLevel : this.JobLevels)
		{
			if(jobLevel.item1.equals(jobName))
			{
				return jobLevel.item2;
			}
		}
		
		return 1;
	}
	
	private int getIntOrDefault(NBTTagCompound tag, String key, int defaultValue)
	{
		if(tag.hasKey(key))
		{
			return tag.getInteger(key);
		}
		else
		{
			return defaultValue;
		}
	}
	
	private String getStringOrDefault(NBTTagCompound tag, String key, String defaultValue)
	{
		if(tag.hasKey(key))
		{
			return tag.getString(key);
		}
		else
		{
			return defaultValue;
		}
	}
}
