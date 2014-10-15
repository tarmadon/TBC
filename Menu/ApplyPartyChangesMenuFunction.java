package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.Pair;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.CombatScreen.IGenericAction;
import TBC.Messages.ItemDataMessage;
import TBC.Messages.NBTTagCompoundMessage;

public class ApplyPartyChangesMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private ArrayList<Pair<StatMenuCharData, Boolean>> selectedPartyMembers;
	
	public ApplyPartyChangesMenuFunction(StatsGui gui, ArrayList<Pair<StatMenuCharData, Boolean>> selectedPartyMembers)
	{
		this.gui = gui;
		this.selectedPartyMembers = selectedPartyMembers;
	}

	@Override
	public void Invoke() 
	{
		for(int i = 0; i < this.gui.partyMembers.size(); i++)
		{
			StatMenuCharData selected = this.gui.partyMembers.get(i);
			SyncCharData(0, selected, false);
		}
		
		for(int i = 0; i < selectedPartyMembers.size(); i++)
		{
			Pair<StatMenuCharData, Boolean> selected = selectedPartyMembers.get(i);
			SyncCharData(i + 1, selected.item1, selected.item2);
		}
		
		this.gui.RefreshCurrentPartyMembers();
		new SelectMainMenuFunction(this.gui).Invoke();
	}
	
	private void SyncCharData(int index, StatMenuCharData selected, Boolean frontRow)
	{
		selected.FrontRow = frontRow;
		if(selected.Player != null)
		{
			EntityPlayer player = selected.Player;
			CombatEntitySaveData playerData = new CombatEntitySaveData();
			playerData.loadNBTData(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG));
			playerData.IsInParty = index;
			playerData.IsFrontRow = frontRow ? 1 : 0;
			
			playerData.saveNBTData(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG));
			NBTTagCompoundMessage m = new NBTTagCompoundMessage();
			m.tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			MainMod.syncPlayerDataHandler.sendToServer(m);
		}
		else
		{
			ItemStack stack = selected.Item;
			CombatEntitySaveData saveData = HenchmanItem.GetCombatEntitySaveData(stack);
			saveData.IsInParty = index;
			saveData.IsFrontRow = frontRow ? 1 : 0;
			HenchmanItem.SetCombatEntitySaveData(saveData, stack);
			
			ItemDataMessage itemData = new ItemDataMessage();
			itemData.Slot = selected.CombatEntity.id;
			itemData.ItemDurability = selected.Item.getItemDamage();
			itemData.tag = stack.getTagCompound();
			MainMod.setItemDataHandler.sendToServer(itemData);
		}
	}
}
