package TBC;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.DamageSource;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.IGenericAction;

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
			if(targets.get(i).innerEntity == this.user.innerEntity)
			{
				targets.set(i, this.user);
			}
		}
		
		CombatEngine engine = new CombatEngine(allies, new ArrayList<CombatEntity>(), true);
		engine.Attack(user, targets, ability, null);
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
			if(target.innerEntity != user.innerEntity)
			{
				SyncCombatEntityHealth(mc, target);
			}
		}
		
		this.statsGui.ChangeMode(this.user, null, this.returnMode);
	}

	private void SyncCombatEntityHealth(Minecraft mc, CombatEntity entity) 
	{
		if(entity.innerEntity != null && entity.innerEntity instanceof EntityPlayer)
		{
			int maxHealth = entity.innerEntity.getMaxHealth();
			float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
			int healthToSet = Math.round((currentHpPercentage * maxHealth) + .499999F);
			mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCSetHealth", ("" + healthToSet).getBytes()));
			
			NBTTagCompound tag = entity.innerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			tag.setInteger("TBCPlayerMP", entity.currentMp);
			entity.innerEntity.getEntityData().setCompoundTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
			SyncTagToServer((EntityPlayer)entity.innerEntity);
		}
		else if(entity.innerEntity.getEntityData().hasKey("henchmanIndex"))
		{
			float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
			int healthToSet = Math.round((currentHpPercentage * 100) + .499999F);
			int index = entity.innerEntity.getEntityData().getInteger("henchmanIndex");
			ItemStack h = this.henchmanEntities[index];
			if(index == 9)
			{
				index = -1;
			}
			
			mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCSetDur", (index + "," + (100 - healthToSet) + "," + entity.currentMp).getBytes()));
		}
	}
	
	private void SyncTagToServer(EntityPlayer playerEntity)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			NBTTagCompound.writeNamedTag(playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG), new DataOutputStream(outputStream));
		} catch (IOException e) {}
			
		Minecraft.getMinecraft().getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCPlayerData", outputStream.toByteArray()));
	}
}
