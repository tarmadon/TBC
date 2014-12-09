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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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

	public Pair<Boolean, Boolean> GainXP(World world, CombatEntity leveledPlayer, int gainedXp, int gainedAp, ArrayList<String> endOfCombatMessageQueue)
	{
		if(!(leveledPlayer.entityType == null))
		{
			return new Pair<Boolean, Boolean>(false, false);
		}

		boolean gainedLevel = false;
		boolean gainedSkill = false;
		EntityPlayer player = (EntityPlayer)world.getEntityByID(leveledPlayer.id);
		CombatEntitySaveData data = this.GetSavedPlayerData(player);
		SimpleEntry<Boolean, Integer> returnedXp = this.CheckGainedLevel(leveledPlayer, data.Level, data.CurrentXp + gainedXp, endOfCombatMessageQueue);
		SimpleEntry<Boolean, Integer> returnedAp = this.CheckGainedSkillLevel(leveledPlayer, data.ThiefClassLevel, data.CurrentAp + gainedAp, endOfCombatMessageQueue);
		data.CurrentXp = returnedXp.getValue();
		data.CurrentAp = returnedAp.getValue();
		if(returnedXp.getKey())
		{
			data.Level++;
			CombatEntityTemplate playerBaseStats = leveledPlayer.GetBaseStats();
			data.MaxHP = playerBaseStats.maxHp;
			data.MaxMP = playerBaseStats.maxMp;
			data.Attack = playerBaseStats.attack;
			data.Defense = playerBaseStats.defense;
			data.MAttack = playerBaseStats.mAttack;
			data.MDefense = playerBaseStats.mDefense;
			data.Speed = playerBaseStats.speed;
			gainedLevel = true;
		}

		if(returnedAp.getKey())
		{
			data.ThiefClassLevel++;
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

			data.Abilities = playerAbilities;
			gainedSkill = true;
		}

		SavePlayerData(player, data);
		return new Pair<Boolean, Boolean>(gainedLevel, gainedSkill);
	}

	public CombatEntitySaveData GetXpDataForPlayer(EntityPlayer entityPlayer)
	{
		return this.GetSavedPlayerData(entityPlayer);
	}

	public void SaveXpDataForPlayer(EntityPlayer entityPlayer, CombatEntitySaveData saveData)
	{
		this.SavePlayerData(entityPlayer, saveData);
	}

	public CombatEntityTemplate GetPlayerEntityFromSavedData(EntityPlayer entityPlayer, String name)
	{
		CombatEntitySaveData data = this.GetSavedPlayerData(entityPlayer);
		CombatEntityTemplate player = new CombatEntityTemplate();
		player.maxHp = data.MaxHP;
		player.maxMp = data.MaxMP;
		player.attack = data.Attack;
		player.defense = data.Defense;
		player.mAttack = data.MAttack;
		player.mDefense = data.MDefense;
		player.speed = data.Speed;
		player.name = name;

		ArrayList<Pair<Integer, ICombatAbility>> abilities = new ArrayList<Pair<Integer,ICombatAbility>>();
		for(String s : data.Abilities)
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

	private void SavePlayerData(EntityPlayer player, CombatEntitySaveData data)
	{
		NBTTagCompound persistedTag = PlayerSaveData.GetPlayerTag(player);
		data.saveNBTData(persistedTag);
		PlayerSaveData.SetPlayerTag(player, persistedTag);
	}

	private CombatEntitySaveData GetSavedPlayerData(EntityPlayer player)
	{
		CombatEntitySaveData data = new CombatEntitySaveData();
		data.loadNBTData(PlayerSaveData.GetPlayerTag(player));
		return data;
	}
}
