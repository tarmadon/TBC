package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import TBC.HenchmanItem;
import TBC.Pair;
import TBC.PlayerSaveData;
import TBC.Quintuplet;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.EquippedItem;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;

public class ShowEquipmentForCharMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private StatMenuCharData user;
	private String equipmentSlot;
	
	public ShowEquipmentForCharMenuFunction(StatsGui gui, StatMenuCharData player, String equipmentSlot)
	{
		this.gui = gui;
		this.user = player;
		this.equipmentSlot = equipmentSlot;
	}
	
	@Override
	public void Invoke() 
	{
		ItemStack[] currentlyEquipped;
		if(this.user.Player != null)
		{
			currentlyEquipped = EquippedItemManager.Instance.GetEquippedItems(PlayerSaveData.GetPlayerTag(this.user.Player));
		}
		else
		{
			currentlyEquipped = HenchmanItem.GetItems(this.user.Item);
		}
		
		ArrayList<GenericScrollBoxCellData> leftSide = new ArrayList<GenericScrollBoxCellData>();
		HandleSlot(this.equipmentSlot, EquippedItemManager.MainHandItemSlot, leftSide, currentlyEquipped[0]);
		HandleSlot(this.equipmentSlot, EquippedItemManager.HeadItemSlot, leftSide, currentlyEquipped[1]);
		HandleSlot(this.equipmentSlot, EquippedItemManager.TorsoItemSlot, leftSide, currentlyEquipped[2]);
		HandleSlot(this.equipmentSlot, EquippedItemManager.LegsItemSlot, leftSide, currentlyEquipped[3]);
		HandleSlot(this.equipmentSlot, EquippedItemManager.FootItemSlot, leftSide, currentlyEquipped[4]);
		
		ArrayList<GenericScrollBoxCellData> rightSide = new ArrayList<GenericScrollBoxCellData>();
		if(equipmentSlot != null)
		{
			ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>> equippable = EquippedItemManager.Instance.GetEquippableItemsForPlayer(gui.mc, gui.player);
			for(Quintuplet<Integer, Integer, Item, EquippedItem> equip : equippable)
			{
				if(equip.item4.GetSlot().equals(equipmentSlot))
				{
					rightSide.add(new GenericScrollBoxCellData(new ItemStack(equip.item3).getDisplayName(), "", new ChangeEquipmentMenuFunction(gui, user, equip), equip.item4.DescriptionStrings().get(0)));
				}
			}
		}
		
		ArrayList<GenericScrollBoxCellData> constantButtons = new ArrayList<GenericScrollBoxCellData>();
		constantButtons.add(new GenericScrollBoxCellData("Back", "", new SelectAbilitiesMenuFunction(this.gui)));
		
		this.gui.ChangeButtonForSubMenu("Equipment", leftSide, rightSide, constantButtons, 0);
	}

	private void HandleSlot(String chosenEquipmentSlot, String slotToHandle, ArrayList<GenericScrollBoxCellData> equipSlotButtons, ItemStack currentItem)
	{
		String slotName = slotToHandle;
		String hoverText = "";
		if(currentItem != null)
		{
			slotName = currentItem.getDisplayName();
			hoverText = EquippedItemManager.Instance.GetEquippedItem(currentItem).DescriptionStrings().get(0);
		}
		
		if(chosenEquipmentSlot == slotToHandle)
		{
			equipSlotButtons.add(new GenericScrollBoxCellData(slotName, "", null, hoverText));
		}
		else
		{
			equipSlotButtons.add(new GenericScrollBoxCellData(slotName, "", new ShowEquipmentForCharMenuFunction(this.gui, this.user, slotToHandle), hoverText));
		}
	}
}
