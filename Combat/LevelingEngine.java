package TBC.Combat;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import cpw.mods.fml.common.Loader;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import TBC.Pair;
import TBC.PlayerXPWorldSavedData;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.ICombatAbility;

public class LevelingEngine
{
	public static LevelingEngine Instance = new LevelingEngine();

	public static int GetXpRequiredForLevel(int level)
	{
		return (int)(Math.pow(2, level - 1) * 10);
	}

	public SimpleEntry<Boolean, Integer> CheckGainedLevel(CombatEntity player, int currentLevel, int newXp, ArrayList<String> messages)
	{
		int xpRequired = GetXpRequiredForLevel(currentLevel);
		if(newXp >= xpRequired)
		{
			player.ApplyLevelUp(
					currentLevel,
					1,
					1,
					1,
					1,
					1,
					1);
			return new SimpleEntry<Boolean, Integer>(true, newXp - xpRequired);
		}
		else
		{
			return new SimpleEntry<Boolean, Integer>(false, newXp);
		}
	}

	public SimpleEntry<Boolean, Integer> CheckGainedSkillLevel(CombatEntity player, int currentLevel, int newAp, ArrayList<String> messages)
	{
		if(newAp >= (currentLevel * 10))
		{
			if(currentLevel == 1)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Barrage"));
			}
			else if(currentLevel == 2)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Guardbreak"));
			}
			else if(currentLevel == 3)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Heal"));
			}
			else if(currentLevel == 4)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Smite"));
			}
			else if(currentLevel == 5)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Fireball"));
			}
			else if(currentLevel == 6)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Blind"));
			}
			else if(currentLevel == 7)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Blade Frenzy"));
			}
			else if(currentLevel == 8)
			{
				player.ApplySkillLevelUp(AbilityLookup.Instance.GetAbilityWithName("Regen10"));
			}

			return new SimpleEntry<Boolean, Integer>(true, newAp - (currentLevel * 10));
		}
		else
		{
			return new SimpleEntry<Boolean, Integer>(false, newAp);
		}
	}

	public void GainXP(CombatEntity leveledPlayer, int gainedXp, int gainedAp, ArrayList<String> endOfCombatMessageQueue)
	{
		if(!(leveledPlayer.innerEntity instanceof EntityPlayer))
		{
			return;
		}

		PlayerXPWorldSavedData data = this.GetSavedPlayerData((EntityPlayer)leveledPlayer.innerEntity);
		SimpleEntry<Boolean, Integer> returnedXp = this.CheckGainedLevel(leveledPlayer, data.PlayerLevel, data.PlayerXp + gainedXp, endOfCombatMessageQueue);
		SimpleEntry<Boolean, Integer> returnedAp = this.CheckGainedSkillLevel(leveledPlayer, data.SwordSkillLevel, data.PlayerAp + gainedAp, endOfCombatMessageQueue);
		data.PlayerXp = returnedXp.getValue();
		data.PlayerAp = returnedAp.getValue();
		if(returnedXp.getKey())
		{
			data.PlayerLevel++;
			CombatEntityTemplate playerBaseStats = leveledPlayer.GetBaseStats();
			data.PlayerMaxHP = playerBaseStats.maxHp;
			data.PlayerMaxMP = playerBaseStats.maxMp;
			data.PlayerAttack = playerBaseStats.attack;
			data.PlayerDefense = playerBaseStats.defense;
			data.PlayerMAttack = playerBaseStats.mAttack;
			data.PlayerMDefense = playerBaseStats.mDefense;
			data.PlayerSpeed = playerBaseStats.speed;
			endOfCombatMessageQueue.add(String.format("Level up! %s is now level: %s", leveledPlayer.name, data.PlayerLevel));
		}

		if(returnedAp.getKey())
		{
			data.SwordSkillLevel++;
			ArrayList<String> playerAbilities = new ArrayList<String>();
			for(Pair<Integer,ICombatAbility> a : leveledPlayer.GetAbilities())
			{
				String abilityName = a.item2.GetAbilityName();
				if(abilityName == "")
				{
					abilityName = "Default";
				}

				playerAbilities.add(abilityName);
			}

			data.PlayerAbilities = playerAbilities;
			endOfCombatMessageQueue.add(String.format("Skill Level up! %s is now skill level: %s", leveledPlayer.name, data.SwordSkillLevel));
		}

		SavePlayerData((EntityPlayer)leveledPlayer.innerEntity, data);
	}

	public PlayerXPWorldSavedData GetXpDataForPlayer(EntityPlayer entityPlayer)
	{
		return this.GetSavedPlayerData(entityPlayer);
	}

	public void SaveXpDataForPlayer(EntityPlayer entityPlayer, PlayerXPWorldSavedData saveData)
	{
		this.SavePlayerData(entityPlayer, saveData);
	}

	public CombatEntityTemplate GetPlayerEntityFromSavedData(EntityPlayer entityPlayer, String name)
	{
		PlayerXPWorldSavedData data = this.GetSavedPlayerData(entityPlayer);
		CombatEntityTemplate player = new CombatEntityTemplate();
		player.maxHp = data.PlayerMaxHP;
		player.maxMp = data.PlayerMaxMP;
		player.attack = data.PlayerAttack;
		player.defense = data.PlayerDefense;
		player.mAttack = data.PlayerMAttack;
		player.mDefense = data.PlayerMDefense;
		player.speed = data.PlayerSpeed;
		player.name = name;

		ArrayList<Pair<Integer, ICombatAbility>> abilities = new ArrayList<Pair<Integer,ICombatAbility>>();
		for(String s : data.PlayerAbilities)
		{
			ICombatAbility ability = AbilityLookup.Instance.GetAbilityWithName(s);
			if(ability != null)
			{
				abilities.add(new Pair<Integer, ICombatAbility>(1, ability));
			}
		}

		Pair<Integer, ICombatAbility>[] asArray = new Pair[abilities.size()];
		abilities.toArray(asArray);
		player.abilities = asArray;
		return player;
	}

	private void SavePlayerData(EntityPlayer player, PlayerXPWorldSavedData data)
	{
		NBTTagCompound persistedTag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		data.saveNBTData(persistedTag);
		player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistedTag);
	}

	private PlayerXPWorldSavedData GetSavedPlayerData(EntityPlayer player)
	{
		PlayerXPWorldSavedData data = new PlayerXPWorldSavedData();
		data.loadNBTData(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG));
		return data;
	}
}
