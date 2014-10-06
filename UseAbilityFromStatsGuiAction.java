package TBC;

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

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.IGenericAction;
import TBC.Messages.NBTTagCompoundMessage;
import TBC.Messages.StringMessage;

public class UseAbilityFromStatsGuiAction implements IGenericAction
{
	private StatsGui statsGui;
	private CombatEntity user;
	private ArrayList<CombatEntity> targets;
	private ICombatAbility ability;
	private int returnMode;
	private ItemStack[] henchmanEntities;

	public UseAbilityFromStatsGuiAction(StatsGui statsGui, int returnMode, CombatEntity user, ICombatAbility ability, ArrayList<CombatEntity> targets, ItemStack[] henchmanEntities)
	{
		this.statsGui = statsGui;
		this.returnMode = returnMode;
		this.user = user;
		this.ability = ability;
		this.targets = targets;
		this.henchmanEntities = henchmanEntities;
	}

	public void Invoke()
	{
		ArrayList<CombatEntity> allies = new ArrayList<CombatEntity>();
		allies.add(this.user);

		// If the user is also a target, make sure there is only one CombatEntity being used (no duplicates).
		// Otherwise, there will be problems syncing with the server.
		for(int i = 0; i< targets.size(); i++)
		{
			if(targets.get(i).id == this.user.id)
			{
				targets.set(i, this.user);
			}
		}

		CombatEngine engine = new CombatEngine(allies, new ArrayList<CombatEntity>(), true, 0);
		engine.Attack(user, targets, ability, new ArrayList<String>());
		user.ApplyDamage();
		for(CombatEntity target : targets)
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
			CombatEntity target = targets.get(i);
			if(target.id != user.id)
			{
				SyncCombatEntityHealth(mc, target);
			}
		}

		this.statsGui.ChangeMode(this.user, null, this.returnMode);
	}

	private void SyncCombatEntityHealth(Minecraft mc, CombatEntity entity)
	{
		if(entity.entityType == null)
		{
			EntityClientPlayerMP player = mc.thePlayer;
			float maxHealth = player.getMaxHealth();
			float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
			int healthToSet = Math.round((currentHpPercentage * maxHealth) + .499999F);
			StringMessage setHealth = new StringMessage();
			setHealth.Data = "" + healthToSet;
			MainMod.setHealthHandler.sendToServer(setHealth);

			NBTTagCompound tag = mc.thePlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			tag.setInteger("TBCPlayerMP", entity.currentMp);
			player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
			SyncTagToServer(player);
		}
		else
		{
			float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
			int healthToSet = Math.round((currentHpPercentage * 100) + .499999F);
			int index = entity.id;
			ItemStack h = this.henchmanEntities[index];
			if(index == 9)
			{
				index = -1;
			}

			StringMessage itemDurMessage = new StringMessage();
			itemDurMessage.Data = index + "," + (100 - healthToSet) + "," + entity.currentMp;
			MainMod.setItemDataHandler.sendToServer(itemDurMessage);
		}
	}

	private void SyncTagToServer(EntityPlayer playerEntity)
	{
		NBTTagCompoundMessage message = new NBTTagCompoundMessage();
		message.tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		MainMod.syncPlayerDataHandler.sendToServer(message);
	}
}
