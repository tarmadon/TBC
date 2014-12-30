package TBC.Menu;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.PlayerSaveData;
import TBC.Quintuplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.EquippedItem;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.JobLookup;
import TBC.Combat.LevelingEngine;
import TBC.CombatScreen.IGenericAction;
import TBC.Messages.ItemDataMessage;
import TBC.Messages.NBTTagCompoundMessage;

public class ChangeJobMenuFunction implements IGenericAction 
{
	private StatsGui gui;
	private StatMenuCharData user;
	private String jobName;
	private boolean isPrimary;
	
	public ChangeJobMenuFunction(StatsGui gui, StatMenuCharData user, String jobName, boolean isPrimary)
	{
		this.gui = gui;
		this.user = user;
		this.jobName = jobName;
		this.isPrimary = isPrimary;
	}

	public void Invoke() 
	{
		CombatEntitySaveData data;
		ItemStack[] currentlyEquipped;
		if(user.Player != null)
		{
			data = LevelingEngine.Instance.GetPlayerSaveData(user.Player);
			currentlyEquipped = EquippedItemManager.Instance.GetEquippedItems(PlayerSaveData.GetPlayerTag(this.user.Player));
		}
		else
		{
			data = HenchmanItem.GetCombatEntitySaveData(user.Item);
			currentlyEquipped = HenchmanItem.GetItems(this.user.Item);
		}
		
		if(isPrimary)
		{
			data.CurrentJob = jobName;
		}
		else
		{
			data.SecondaryJob = jobName;
		}

		if(data.SecondaryJob.equals(data.CurrentJob))
		{
			data.SecondaryJob = "";
		}
		
		data.CurrentAp = 0;
		List<String> proficiencies = JobLookup.Instance.GetProficiencies(data.CurrentJob, data.GetJobLevelMin1(data.CurrentJob), true);
		if(!data.SecondaryJob.isEmpty())
		{
			proficiencies.addAll(JobLookup.Instance.GetProficiencies(data.SecondaryJob, data.GetJobLevelMin1(data.SecondaryJob), false));
		}
		
		for(int i = 0; i < currentlyEquipped.length; i++)
		{
			if(currentlyEquipped[i] == null)
			{
				continue;
			}
			
			EquippedItem item = EquippedItemManager.Instance.GetEquippedItem(currentlyEquipped[i], EquippedItemManager.Instance.GetSlotForIndex(i));
			boolean foundAllReq = true;
			for(String req : item.RequiredProficiencies())
			{
				boolean foundReq = false;
				for(String prof : proficiencies)
				{
					if(req.equals(prof))
					{
						foundReq = true;
						break;
					}
				}
				
				if(!foundReq)
				{
					foundAllReq = false;
					break;
				}
			}
			
			if(!foundAllReq)
			{
				this.gui.player.inventory.addItemStackToInventory(currentlyEquipped[i]);
				NBTTagCompound tag = new NBTTagCompound();
				currentlyEquipped[i].writeToNBT(tag);
				MainMod.addItemHandler.sendToServer(new NBTTagCompoundMessage(tag));
				if(this.user.Player != null)
				{
					EquippedItemManager.SetItem(i, null, PlayerSaveData.GetPlayerTag(this.user.Player));
				}
				else
				{
					HenchmanItem.SetItem(i, null, this.user.Item);
				}
			}
		}
		
		if(user.Player != null)
		{
			LevelingEngine.Instance.SaveXpDataForPlayer(user.Player, data);
			MainMod.syncPlayerDataHandler.sendToServer(new NBTTagCompoundMessage(PlayerSaveData.GetPlayerTag(this.user.Player)));
			this.user.CombatEntity = CombatEntity.GetCombatEntity(this.user.Player).item2;
		}
		else
		{
			HenchmanItem.SetCombatEntitySaveData(data, user.Item);
			ItemDataMessage m = new ItemDataMessage();
			m.tag = HenchmanItem.GetTag(this.user.Item);
			m.ItemDurability = this.user.Item.getItemDamage();
			m.Slot = this.user.CombatEntity.id;
			MainMod.setItemDataHandler.sendToServer(m);
			this.user.CombatEntity = CombatEntity.GetCombatEntity(this.user.CombatEntity.id, user.Item).item2;
		}
		
		new ShowJobsForCharMenuFunction(gui, user, "").Invoke();
	}
}
