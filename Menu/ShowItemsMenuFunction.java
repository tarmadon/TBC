package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import TBC.Quintuplet;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.EquippedItem;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;

public class ShowItemsMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private EntityPlayer player;
	private StatMenuCharData user;
	
	public ShowItemsMenuFunction(StatsGui gui, EntityPlayer player, StatMenuCharData user)
	{
		this.gui = gui;
		this.player = player;
		this.user = user;
	}
	
	@Override
	public void Invoke() 
	{
		ArrayList<GenericScrollBoxCellData> displayItems = new ArrayList<GenericScrollBoxCellData>();
		ArrayList<Quintuplet<Item, EquippedItem, ICombatAbility, Integer>> items = EquippedItemManager.Instance.GetAllKnownItemsForPlayer(Minecraft.getMinecraft(), Minecraft.getMinecraft().thePlayer);
		for(int i = 0; i < items.size(); i++)
		{
			IGenericAction action = null;
			ICombatAbility itemAbility = items.get(i).item3;
			String itemName = "";
			ArrayList<String> hoverText = new ArrayList<String>();
			if(itemAbility != null)
			{
				ArrayList<String> descriptions = itemAbility.GetDescription();
				if(descriptions.size() > 0)
				{
					hoverText = itemAbility.GetDescription();
				}
			}
			
			if(itemAbility != null && itemAbility.IsUsableOutOfCombat())
			{
				itemName = itemAbility.GetAbilityName();
				action = new ChooseTargetForAbilityMenuFunction(this.gui, this.user, itemAbility, new ShowItemsMenuFunction(this.gui, this.player, this.user));
			}
			else
			{
				itemName = items.get(i).item1.getItemStackDisplayName(new ItemStack(items.get(i).item1));
				if(items.get(i).item2 != null)
				{
					hoverText = items.get(i).item2.DescriptionStrings();
				}
			}
			

			displayItems.add(new GenericScrollBoxCellData(itemName, items.get(i).item4 + "", action, hoverText));
		}

		if(displayItems.size() == 0)
		{
			displayItems.add(new GenericScrollBoxCellData("You have no items.", "", null));
		}

		ArrayList<GenericScrollBoxCellData> constantButtons = new ArrayList<GenericScrollBoxCellData>();
		constantButtons.add(new GenericScrollBoxCellData("Back", "", new SelectMainMenuFunction(this.gui)));
		this.gui.ChangeButtonForSubMenu("Items", displayItems, constantButtons, 0);
	}
}
