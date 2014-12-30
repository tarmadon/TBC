package TBC.Menu;

import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.PlayerSaveData;
import TBC.Combat.CombatEntity;
import TBC.Combat.LevelingEngine;
import TBC.CombatScreen.IGenericAction;
import TBC.CombatScreen.IGenericStringAction;
import TBC.Messages.ItemDataMessage;
import TBC.Messages.NBTTagCompoundMessage;

public class ChangeCharNameMenuFunction implements IGenericAction, IGenericStringAction
{
	private StatsGui gui;
	private StatMenuCharData user;
	
	public ChangeCharNameMenuFunction(StatsGui gui, StatMenuCharData player)
	{
		this.gui = gui;
		this.user = player;
	}

	@Override
	public void Invoke() 
	{
		new ShowStatsForCharMenuFunction(gui, user, false).Invoke();
	}

	@Override
	public void Invoke(String stringParam) 
	{
		if(user.Player != null)
		{
			CombatEntitySaveData d = LevelingEngine.Instance.GetPlayerSaveData(user.Player);
			d.Name = stringParam;
			LevelingEngine.Instance.SaveXpDataForPlayer(user.Player, d);
			MainMod.syncPlayerDataHandler.sendToServer(new NBTTagCompoundMessage(PlayerSaveData.GetPlayerTag(user.Player)));
			user.CombatEntity = CombatEntity.GetCombatEntity(user.Player).item2;
		}
		else
		{
			CombatEntitySaveData d = HenchmanItem.GetCombatEntitySaveData(user.Item);
			d.Name = stringParam;
			HenchmanItem.SetCombatEntitySaveData(d, user.Item);
			ItemDataMessage m = new ItemDataMessage();
			m.tag = HenchmanItem.GetTag(user.Item);
			m.ItemDurability = user.Item.getItemDamage();
			m.Slot = user.CombatEntity.id;
			MainMod.setItemDataHandler.sendToServer(m);
			user.CombatEntity = CombatEntity.GetCombatEntity(user.CombatEntity.id, user.Item).item2;
		}
		
		new ShowStatsForCharMenuFunction(gui, user, false).Invoke();
	}
}
