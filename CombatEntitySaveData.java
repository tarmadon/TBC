package TBC;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.IExtendedEntityProperties;

public class CombatEntitySaveData implements Serializable
{
	public int QuestProgress;

	public int CurrentMp;
	public int CurrentXp;
	public int CurrentAp;
	public int Level;
	public int KnightClassLevel;
	public int ThiefClassLevel;
	public int MageClassLevel;
	public int MaxHP;
	public int MaxMP;
	public int Attack;
	public int Defense;
	public int MAttack;
	public int MDefense;
	public int Speed;
	public List<String> Abilities;

	public void loadNBTData(NBTTagCompound nbttagcompound)
	{
		QuestProgress = getIntOrDefault(nbttagcompound, "questProgress", 1);

		CurrentMp = getIntOrDefault(nbttagcompound, "TBCCurrentMP", 0);
		CurrentXp = getIntOrDefault(nbttagcompound, "TBCXP", 0);
		CurrentAp = getIntOrDefault(nbttagcompound, "TBCAP", 0);
		Level = getIntOrDefault(nbttagcompound, "TBCLevel", 1);
		KnightClassLevel = getIntOrDefault(nbttagcompound, "TBCKnightLevel", 1);
		ThiefClassLevel = getIntOrDefault(nbttagcompound, "TBCThiefLevel", 1);
		MageClassLevel = getIntOrDefault(nbttagcompound, "TBCMageLevel", 1);
		MaxHP = getIntOrDefault(nbttagcompound, "TBCMaxHP", 50);
		MaxMP = getIntOrDefault(nbttagcompound, "TBCMaxMP", 1);
		Attack = getIntOrDefault(nbttagcompound, "TBCAttack", 3);
		Defense = getIntOrDefault(nbttagcompound, "TBCDefense", 3);
		MAttack = getIntOrDefault(nbttagcompound, "TBCMAttack", 3);
		MDefense = getIntOrDefault(nbttagcompound, "TBCMDefense", 3);
		Speed = getIntOrDefault(nbttagcompound, "TBCSpeed", 5);

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
	}

	public void saveNBTData(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setInteger("questProgress", QuestProgress);

		nbttagcompound.setInteger("TBCCurrentMP", CurrentMp);
		nbttagcompound.setInteger("TBCXP", CurrentXp);
		nbttagcompound.setInteger("TBCAP", CurrentAp);
		nbttagcompound.setInteger("TBCLevel", Level);
		nbttagcompound.setInteger("TBCKnightLevel", KnightClassLevel);
		nbttagcompound.setInteger("TBCThiefLevel", ThiefClassLevel);
		nbttagcompound.setInteger("TBCMageLevel", MageClassLevel);
		nbttagcompound.setInteger("TBCMaxHP", MaxHP);
		nbttagcompound.setInteger("TBCMaxMP", MaxMP);
		nbttagcompound.setInteger("TBCAttack", Attack);
		nbttagcompound.setInteger("TBCDefense", Defense);
		nbttagcompound.setInteger("TBCMAttack", MAttack);
		nbttagcompound.setInteger("TBCMDefense", MDefense);
		nbttagcompound.setInteger("TBCSpeed", Speed);

		StringBuilder builder = new StringBuilder();
		for(String s : this.Abilities)
		{
			builder.append(s);
			builder.append(',');
		}

		builder.deleteCharAt(builder.length() - 1);
		nbttagcompound.setString("TBCAbilities", builder.toString());
	}

	public void init(Entity entity, World world)
	{
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
}
