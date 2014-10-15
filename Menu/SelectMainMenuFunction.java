package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import TBC.Pair;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
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
		ArrayList<Triplet<String, String, IGenericAction>> buttons = new ArrayList<Triplet<String,String,IGenericAction>>();
		buttons.add(new Triplet<String, String, IGenericAction>("Job", "", null));
		buttons.add(new Triplet<String, String, IGenericAction>("Items", "", new ShowItemsMenuFunction(this.gui, this.gui.player, this.gui.partyMembers.get(0))));
		buttons.add(new Triplet<String, String, IGenericAction>("Abilities", "", new SelectAbilitiesMenuFunction(this.gui)));
		buttons.add(new Triplet<String, String, IGenericAction>("Equip", "", null));
		buttons.add(new Triplet<String, String, IGenericAction>("Status", "", null));
		buttons.add(new Triplet<String, String, IGenericAction>("Party", "", new ShowPartyMenuFunction(this.gui, new ArrayList<Pair<StatMenuCharData,Boolean>>(), null, false)));
		
		this.gui.ChangeButtonForMainMenu("MainMenu", buttons, new ArrayList<Triplet<String, String, IGenericAction>>(), 1);
	}
}
