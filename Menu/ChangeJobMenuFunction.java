package TBC.Menu;

import net.minecraft.item.Item;
import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.PlayerSaveData;
import TBC.Quintuplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.EquippedItem;
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
		if(user.Player != null)
		{
			data = LevelingEngine.Instance.GetPlayerSaveData(user.Player);
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
			LevelingEngine.Instance.SaveXpDataForPlayer(user.Player, data);
			MainMod.syncPlayerDataHandler.sendToServer(new NBTTagCompoundMessage(PlayerSaveData.GetPlayerTag(this.user.Player)));
			this.user.CombatEntity = CombatEntity.GetCombatEntity(this.user.Player).item2;
		}
		else
		{
			data = HenchmanItem.GetCombatEntitySaveData(user.Item);
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
