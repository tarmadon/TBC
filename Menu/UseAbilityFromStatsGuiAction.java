package TBC.Menu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.PlayerSaveData;
import TBC.Triplet;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.IGenericAction;
import TBC.Messages.ItemDataMessage;
import TBC.Messages.NBTTagCompoundMessage;
import TBC.Messages.StringMessage;

public class UseAbilityFromStatsGuiAction implements IGenericAction
{
	private StatsGui statsGui;
	private StatMenuCharData user;
	private ArrayList<StatMenuCharData> targets;
	private ICombatAbility ability;
	private IGenericAction returnAction;
	
	public UseAbilityFromStatsGuiAction(StatsGui statsGui, StatMenuCharData user, ICombatAbility ability, ArrayList<StatMenuCharData> targets, IGenericAction returnAction)
	{
		this.statsGui = statsGui;
		this.user = user;
		this.ability = ability;
		this.targets = targets;
		this.returnAction = returnAction;
	}

	public void Invoke()
	{
		ArrayList<CombatEntity> allies = new ArrayList<CombatEntity>();
		for(int i = 0; i < this.statsGui.partyMembers.size(); i++)
		{
			allies.add(this.statsGui.partyMembers.get(i).CombatEntity);
		}

		ArrayList<CombatEntity> targetCombatEntities = new ArrayList<CombatEntity>();
		for(int i = 0; i < this.targets.size(); i++)
		{
			targetCombatEntities.add(targets.get(i).CombatEntity);
		}
		
		CombatEngine engine = new CombatEngine(allies, new ArrayList<CombatEntity>(), true, 0);
		engine.Attack(user.CombatEntity, targetCombatEntities, ability, new ArrayList<String>());
		
		user.CombatEntity.ApplyDamage();
		for(CombatEntity target : targetCombatEntities)
		{
			target.ApplyDamage();
		}

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		DamageSource source = DamageSource.causePlayerDamage(player);
		source.damageType = "bypass";

		SyncCombatEntityHealth(mc, user);
		for(int i = 0; i < targets.size(); i++)
		{
			StatMenuCharData target = targets.get(i);
			if(target.CombatEntity.id != user.CombatEntity.id)
			{
				SyncCombatEntityHealth(mc, target);
			}
		}

		this.returnAction.Invoke();
	}

	private void SyncCombatEntityHealth(Minecraft mc, StatMenuCharData entity)
	{
		if(entity.Player != null)
		{
			EntityPlayer player = mc.thePlayer;
			float maxHealth = player.getMaxHealth();
			float currentHpPercentage = (float)entity.CombatEntity.currentHp / entity.CombatEntity.GetMaxHp();
			int healthToSet = Math.round((currentHpPercentage * maxHealth) + .499999F);
			StringMessage setHealth = new StringMessage();
			setHealth.Data = "" + healthToSet;
			MainMod.setHealthHandler.sendToServer(setHealth);

			NBTTagCompound tag = PlayerSaveData.GetPlayerTag(mc.thePlayer);
			tag.setInteger("TBCPlayerMP", entity.CombatEntity.currentMp);
			PlayerSaveData.SetPlayerTag(player, tag);
			SyncTagToServer(player);
		}
		else
		{
			float currentHpPercentage = (float)entity.CombatEntity.currentHp / entity.CombatEntity.GetMaxHp();
			int healthToSet = Math.round((currentHpPercentage * 100) + .499999F);
			int index = entity.CombatEntity.id;
			ItemStack h = entity.Item;
			if(index == 9)
			{
				index = -1;
			}

			ItemDataMessage itemDurMessage = new ItemDataMessage();
			CombatEntitySaveData d = HenchmanItem.GetCombatEntitySaveData(entity.Item);
			d.CurrentMp = entity.CombatEntity.currentMp;
			HenchmanItem.SetCombatEntitySaveData(d, entity.Item);
			itemDurMessage.Slot = index;
			itemDurMessage.ItemDurability = 100 - healthToSet;
			itemDurMessage.tag = entity.Item.getTagCompound();
			MainMod.setItemDataHandler.sendToServer(itemDurMessage);
		}
	}

	private void SyncTagToServer(EntityPlayer playerEntity)
	{
		NBTTagCompoundMessage message = new NBTTagCompoundMessage();
		message.tag = PlayerSaveData.GetPlayerTag(playerEntity);
		MainMod.syncPlayerDataHandler.sendToServer(message);
	}
}
