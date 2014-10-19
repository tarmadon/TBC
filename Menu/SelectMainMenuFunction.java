package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import TBC.Pair;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;

public class SelectMainMenuFunction implements IGenericAction 
{
	private StatsGui gui;
	
	public SelectMainMenuFunction(StatsGui gui)
	{
		this.gui = gui;
	}
	
	@Override
	public void Invoke() 
	{
		ArrayList<GenericScrollBoxCellData> buttons = new ArrayList<GenericScrollBoxCellData>();
		buttons.add(new GenericScrollBoxCellData("Job", "", null));
		buttons.add(new GenericScrollBoxCellData("Items", "", new ShowItemsMenuFunction(this.gui, this.gui.player, this.gui.partyMembers.get(0))));
		buttons.add(new GenericScrollBoxCellData("Abilities", "", new SelectAbilitiesMenuFunction(this.gui)));
		buttons.add(new GenericScrollBoxCellData("Equip", "", null));
		buttons.add(new GenericScrollBoxCellData("Status", "", null));
		buttons.add(new GenericScrollBoxCellData("Party", "", new ShowPartyMenuFunction(this.gui, new ArrayList<Pair<StatMenuCharData,Boolean>>(), null, false)));
		
		this.gui.ChangeButtonForMainMenu("MainMenu", buttons, new ArrayList<GenericScrollBoxCellData>(), 1);
	}
}
