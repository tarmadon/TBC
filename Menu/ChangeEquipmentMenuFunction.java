package TBC.Menu;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.Pair;
import TBC.PlayerSaveData;
import TBC.Quintuplet;
import TBC.SetItemDataHandler;
import TBC.Combat.EquippedItem;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.Abilities.RemoveItemAbility;
import TBC.CombatScreen.IGenericAction;
import TBC.Messages.ItemDataMessage;
import TBC.Messages.NBTTagCompoundMessage;
import TBC.Messages.StringMessage;

public class ChangeEquipmentMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private StatMenuCharData user;
	private Quintuplet<Integer, Integer, Item, EquippedItem> item;
	
	public ChangeEquipmentMenuFunction(StatsGui gui, StatMenuCharData user, Quintuplet<Integer, Integer, Item, EquippedItem> item)
	{
		this.gui = gui;
		this.user = user;
		this.item = item;
	}

	public void Invoke() 
	{
		ItemStack inventoryItem;
		StringMessage syncToServer = new StringMessage();
		syncToServer.Data = this.item.item1 + "," + this.item.item2 + "," + -1;
		MainMod.removeItemHandler.sendToServer(syncToServer);
		
		if(item.item1 == RemoveItemAbility.ArmorInventory)
		{
			inventoryItem = this.gui.player.inventory.armorInventory[item.item2];
			this.gui.player.inventory.armorInventory[item.item2] = null;
		}
		else
		{
			inventoryItem = this.gui.player.inventory.mainInventory[item.item2];
			this.gui.player.inventory.mainInventory[item.item2] = null;
		}
		
		ItemStack[] existingEquipped;
		if(this.user.Player != null)
		{
			existingEquipped = EquippedItemManager.Instance.GetEquippedItems(PlayerSaveData.GetPlayerTag(this.user.Player));
		}
		else
		{
			existingEquipped = HenchmanItem.GetItems(this.user.Item);
		}
		
		int index = GetIndexForSlot(item.item4.GetSlot());
		if(existingEquipped[index] != null)
		{
			this.gui.player.inventory.addItemStackToInventory(existingEquipped[index]);
			NBTTagCompound tag = new NBTTagCompound();
			existingEquipped[index].writeToNBT(tag);
			MainMod.addItemHandler.sendToServer(new NBTTagCompoundMessage(tag));
		}
		
		if(this.user.Player != null)
		{
			EquippedItemManager.SetItem(index, inventoryItem, PlayerSaveData.GetPlayerTag(this.user.Player));
			MainMod.syncPlayerDataHandler.sendToServer(new NBTTagCompoundMessage(PlayerSaveData.GetPlayerTag(this.user.Player)));
		}
		else
		{
			HenchmanItem.SetItem(index, inventoryItem, this.user.Item);
			ItemDataMessage m = new ItemDataMessage();
			m.tag = HenchmanItem.GetTag(this.user.Item);
			m.ItemDurability = this.user.Item.getItemDamage();
			m.Slot = this.user.CombatEntity.id;
			MainMod.setItemDataHandler.sendToServer(m);
		}
		
		new ShowEquipmentForCharMenuFunction(this.gui, this.user, item.item4.GetSlot()).Invoke();
	}
	
	private int GetIndexForSlot(String slot)
	{
		if(slot.equals(EquippedItemManager.MainHandItemSlot))
		{
			return 0;
		}
		else if(slot.equals(EquippedItemManager.HeadItemSlot))
		{
			return 1;
		}
		else if(slot.equals(EquippedItemManager.TorsoItemSlot))
		{
			return 2;
		}
		else if(slot.equals(EquippedItemManager.LegsItemSlot))
		{
			return 3;
		}
		else if(slot.equals(EquippedItemManager.FootItemSlot))
		{
			return 4;
		}
		else
		{
			return 0;
		}
	}
}
