package TBC.Menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.Pair;
import TBC.PlayerSaveData;
import TBC.Quintuplet;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.EquippedItem;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.JobLookup;
import TBC.Combat.LevelingEngine;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;

public class ShowEquipmentForCharMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private StatMenuCharData user;
	private Integer equipmentSlot;
	
	public ShowEquipmentForCharMenuFunction(StatsGui gui, StatMenuCharData player, Integer equipmentSlot)
	{
		this.gui = gui;
		this.user = player;
		this.equipmentSlot = equipmentSlot;
	}
	
	@Override
	public void Invoke() 
	{
		ItemStack[] currentlyEquipped;
		CombatEntitySaveData data;
		if(this.user.Player != null)
		{
			currentlyEquipped = EquippedItemManager.Instance.GetEquippedItems(PlayerSaveData.GetPlayerTag(this.user.Player));
			data = LevelingEngine.Instance.GetPlayerSaveData(this.user.Player);
		}
		else
		{
			currentlyEquipped = HenchmanItem.GetItems(this.user.Item);
			data = HenchmanItem.GetCombatEntitySaveData(this.user.Item);
		}
		
		ArrayList<GenericScrollBoxCellData> leftSide = new ArrayList<GenericScrollBoxCellData>();
		HandleSlot(this.equipmentSlot, 0, leftSide, currentlyEquipped[0]);
		HandleSlot(this.equipmentSlot, 1, leftSide, currentlyEquipped[1]);
		HandleSlot(this.equipmentSlot, 2, leftSide, currentlyEquipped[2]);
		HandleSlot(this.equipmentSlot, 3, leftSide, currentlyEquipped[3]);
		HandleSlot(this.equipmentSlot, 4, leftSide, currentlyEquipped[4]);
		
		ArrayList<GenericScrollBoxCellData> rightSide = new ArrayList<GenericScrollBoxCellData>();
		if(equipmentSlot != null)
		{
			ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>> equippable = EquippedItemManager.Instance.GetEquippableItemsForPlayer(gui.mc, gui.player);
			List<String> proficiencies = JobLookup.Instance.GetProficiencies(data.CurrentJob, data.GetJobLevelMin1(data.CurrentJob), true);
			if(!data.SecondaryJob.isEmpty())
			{
				proficiencies.addAll(JobLookup.Instance.GetProficiencies(data.SecondaryJob, data.GetJobLevelMin1(data.SecondaryJob), false));
			}
			
			for(Quintuplet<Integer, Integer, Item, EquippedItem> equip : equippable)
			{
				if(equip.item4.GetSlot().equals(GetSlotForIndex(equipmentSlot)))
				{
					ArrayList<String> reqs = equip.item4.RequiredProficiencies();
					boolean foundAllProf = true;
					for(String req : reqs)
					{
						boolean foundProf = false;
						for(String prof : proficiencies)
						{
							if(req.equals(prof))
							{
								foundProf = true;
								break;
							}
						}
						
						foundAllProf = foundProf;
						if(!foundAllProf)
						{
							break;
						}
					}
					
					if(!foundAllProf)
					{
						continue;
					}
					
					rightSide.add(new GenericScrollBoxCellData(new ItemStack(equip.item3).getDisplayName(), "", new ChangeEquipmentMenuFunction(gui, user, equip, equipmentSlot), equip.item4.DescriptionStrings()));
				}
			}
			
			if(currentlyEquipped[equipmentSlot] != null)
			{
				rightSide.add(new GenericScrollBoxCellData("Unequip", "", new ChangeEquipmentMenuFunction(gui, user, null, equipmentSlot)));
			}
		}
		
		ArrayList<GenericScrollBoxCellData> constantButtons = new ArrayList<GenericScrollBoxCellData>();
		constantButtons.add(new GenericScrollBoxCellData("Back", "", new SelectEquipmentMenuFunction(this.gui)));
		
		this.gui.ChangeButtonForSubMenu("Equipment", leftSide, rightSide, constantButtons, 0);
	}

	private void HandleSlot(Integer chosenEquipmentSlot, int slotToHandle, ArrayList<GenericScrollBoxCellData> equipSlotButtons, ItemStack currentItem)
	{
		String slotName = GetSlotForIndex(slotToHandle);
		List<String> hoverText = new ArrayList<String>();
		if(currentItem != null)
		{
			slotName = currentItem.getDisplayName();
			hoverText = EquippedItemManager.Instance.GetEquippedItem(currentItem).DescriptionStrings();
		}
		
		if(chosenEquipmentSlot != null && chosenEquipmentSlot == slotToHandle)
		{
			equipSlotButtons.add(new GenericScrollBoxCellData(slotName, "", null, hoverText));
		}
		else
		{
			equipSlotButtons.add(new GenericScrollBoxCellData(slotName, "", new ShowEquipmentForCharMenuFunction(this.gui, this.user, slotToHandle), hoverText));
		}
	}
	
	private String GetSlotForIndex(int index)
	{
		if(index == 0)
		{
			return EquippedItemManager.MainHandItemSlot;
		}
		else if(index == 1)
		{
			return EquippedItemManager.OffHandItemSlot;
		}
		else if(index == 2)
		{
			return EquippedItemManager.ArmorItemSlot;
		}
		else if(index == 3)
		{
			return EquippedItemManager.AccItemSlot;
		}
		else if(index == 4)
		{
			return EquippedItemManager.AccItemSlot;
		}
		
		return EquippedItemManager.MainHandItemSlot;
	}
}
