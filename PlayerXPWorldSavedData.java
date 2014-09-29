package TBC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerXPWorldSavedData
{
	public int QuestProgress;

	public int PlayerMp;
	public int PlayerXp;
	public int PlayerAp;
	public int PlayerLevel;
	public int BowSkillLevel;
	public int SwordSkillLevel;
	public int AxeSkillLevel;
	public int PlayerMaxHP;
	public int PlayerMaxMP;
	public int PlayerAttack;
	public int PlayerDefense;
	public int PlayerMAttack;
	public int PlayerMDefense;
	public int PlayerSpeed;
	public List<String> PlayerAbilities;

	public void loadNBTData(NBTTagCompound nbttagcompound)
	{
		QuestProgress = getIntOrDefault(nbttagcompound, "questProgress", 1);

		PlayerMp = getIntOrDefault(nbttagcompound, "TBCPlayerMP", 0);
		PlayerXp = getIntOrDefault(nbttagcompound, "playerXP", 0);
		PlayerAp = getIntOrDefault(nbttagcompound, "playerAP", 0);
		PlayerLevel = getIntOrDefault(nbttagcompound, "playerLevel", 1);
		BowSkillLevel = getIntOrDefault(nbttagcompound, "bowSkillLevel", 1);
		SwordSkillLevel = getIntOrDefault(nbttagcompound, "swordSkillLevel", 1);
		AxeSkillLevel = getIntOrDefault(nbttagcompound, "axeSkillLevel", 1);
		PlayerMaxHP = getIntOrDefault(nbttagcompound, "playerMaxHP", 50);
		PlayerMaxMP = getIntOrDefault(nbttagcompound, "playerMaxMP", 1);
		PlayerAttack = getIntOrDefault(nbttagcompound, "playerAttack", 3);
		PlayerDefense = getIntOrDefault(nbttagcompound, "playerDefense", 3);
		PlayerMAttack = getIntOrDefault(nbttagcompound, "playerMAttack", 3);
		PlayerMDefense = getIntOrDefault(nbttagcompound, "playerMDefense", 3);
		PlayerSpeed = getIntOrDefault(nbttagcompound, "playerSpeed", 5);

		if(nbttagcompound.hasKey("playerAbilities"))
		{
			String abilities = nbttagcompound.getString("playerAbilities");
			String[] splitAbilities = abilities.split(",");
			this.PlayerAbilities = Arrays.asList(splitAbilities);
		}
		else
		{
			this.PlayerAbilities = new ArrayList<String>();
			this.PlayerAbilities.add("Default");
		}
	}

	public void saveNBTData(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setInteger("questProgress", QuestProgress);

		nbttagcompound.setInteger("TBCPlayerMP", PlayerMp);
		nbttagcompound.setInteger("playerXP", PlayerXp);
		nbttagcompound.setInteger("playerAP", PlayerAp);
		nbttagcompound.setInteger("playerLevel", PlayerLevel);
		nbttagcompound.setInteger("bowSkillLevel", BowSkillLevel);
		nbttagcompound.setInteger("swordSkillLevel", SwordSkillLevel);
		nbttagcompound.setInteger("axeSkillLevel", AxeSkillLevel);
		nbttagcompound.setInteger("playerMaxHP", PlayerMaxHP);
		nbttagcompound.setInteger("playerMaxMP", PlayerMaxMP);
		nbttagcompound.setInteger("playerAttack", PlayerAttack);
		nbttagcompound.setInteger("playerDefense", PlayerDefense);
		nbttagcompound.setInteger("playerMAttack", PlayerMAttack);
		nbttagcompound.setInteger("playerMDefense", PlayerMDefense);
		nbttagcompound.setInteger("playerSpeed", PlayerSpeed);

		StringBuilder builder = new StringBuilder();
		for(String s : this.PlayerAbilities)
		{
			builder.append(s);
			builder.append(',');
		}

		builder.deleteCharAt(builder.length() - 1);
		nbttagcompound.setString("playerAbilities", builder.toString());
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
