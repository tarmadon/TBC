package TBC.Combat;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import cpw.mods.fml.common.Loader;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import TBC.HenchmanItem;
import TBC.Pair;
import TBC.CombatEntitySaveData;
import TBC.PlayerSaveData;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.ICombatAbility;

public class LevelingEngine
{
	public static LevelingEngine Instance = new LevelingEngine();

	public static int GetXpRequiredForLevel(int level)
	{
		return (int)(Math.pow(2, level - 1) * 10);
	}

	public static int GetApRequiredForLevel(int level)
	{
		return 10 * level;
	}
	
	public Boolean CheckGainedLevel(CombatEntitySaveData saveData, int newXp, ArrayList<String> messages)
	{
		String jobName = saveData.CurrentJob;
		if(jobName.isEmpty())
		{
			return false;
		}
		
		int currentLevel = saveData.Level;
		int xpRequired = GetXpRequiredForLevel(currentLevel);
		if(newXp >= xpRequired)
		{
			Pair<String, Integer> foundJobEntry = null;
			for(Pair<String, Integer> jobLevel : saveData.JobLevels)
			{
				if(jobLevel.item1.equals(saveData.CurrentJob))
				{
					foundJobEntry = jobLevel;
				}
			}
			
			saveData.Level += 1;
			saveData.CurrentXp = newXp - xpRequired;
			return true;
		}
		else
		{
			saveData.CurrentXp = newXp;
			return false;
		}
	}

	public Boolean CheckGainedSkillLevel(CombatEntitySaveData saveData, int newAp, ArrayList<String> messages)
	{
		String jobName = saveData.CurrentJob;
		if(jobName.isEmpty())
		{
			return false;
		}
		
		int currentSkillLevel = 1;
		Pair<String, Integer> foundJobEntry = null;
		for(Pair<String, Integer> jobLevel : saveData.JobLevels)
		{
			if(jobLevel.item1.equals(jobName))
			{
				foundJobEntry = jobLevel;
				currentSkillLevel = foundJobEntry.item2;
			}
		}

		int apRequired = GetApRequiredForLevel(currentSkillLevel);
		if(newAp >= apRequired)
		{
			if(foundJobEntry != null)
			{
				saveData.JobLevels.remove(foundJobEntry);
			}
			
			saveData.JobLevels.add(new Pair<String, Integer>(jobName, currentSkillLevel + 1));
			saveData.CurrentAp = newAp - apRequired;
			return true;
		}
		else
		{
			if(foundJobEntry == null)
			{
				saveData.JobLevels.add(new Pair<String, Integer>(jobName, 1));
			}
			
			saveData.CurrentAp = newAp;
			return false;
		}
	}

	public Pair<Boolean, Boolean> GainXP(CombatEntity leveledPlayer, EntityPlayer player, int gainedXp, int gainedAp, ArrayList<String> endOfCombatMessageQueue)
	{
		CombatEntitySaveData save = this.GetPlayerSaveData(player);
		Pair<Boolean, Boolean> levelStatus = this.GainXP(save, gainedXp, gainedAp, endOfCombatMessageQueue);
		this.SavePlayerData(player, save);
		return levelStatus;
	}
	
	public Pair<Boolean, Boolean> GainXP(CombatEntity leveledPlayer, ItemStack stack, int gainedXp, int gainedAp, ArrayList<String> endOfCombatMessageQueue)
	{
		CombatEntitySaveData save = HenchmanItem.GetCombatEntitySaveData(stack);
		Pair<Boolean, Boolean> levelStatus = this.GainXP(save, gainedXp, gainedAp, endOfCombatMessageQueue);
		HenchmanItem.SetCombatEntitySaveData(save, stack);
		return levelStatus;
	}
	
	public Pair<Boolean, Boolean> GainXP(CombatEntitySaveData data, int gainedXp, int gainedAp, ArrayList<String> endOfCombatMessageQueue)
	{
		boolean gainedLevel = this.CheckGainedLevel(data, data.CurrentXp + gainedXp, endOfCombatMessageQueue);
		boolean gainedSkill = this.CheckGainedSkillLevel(data, data.CurrentAp + gainedAp, endOfCombatMessageQueue);
		return new Pair<Boolean, Boolean>(gainedLevel, gainedSkill);
	}

	public CombatEntitySaveData GetPlayerSaveData(EntityPlayer entityPlayer)
	{
		CombatEntitySaveData data = new CombatEntitySaveData();
		data.loadNBTData(PlayerSaveData.GetPlayerTag(entityPlayer));
		return data;
	}

	public void SaveXpDataForPlayer(EntityPlayer entityPlayer, CombatEntitySaveData saveData)
	{
		this.SavePlayerData(entityPlayer, saveData);
	}

	private void SavePlayerData(EntityPlayer player, CombatEntitySaveData data)
	{
		NBTTagCompound persistedTag = PlayerSaveData.GetPlayerTag(player);
		data.saveNBTData(persistedTag);
		PlayerSaveData.SetPlayerTag(player, persistedTag);
	}
}
