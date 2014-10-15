package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import TBC.Pair;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.CombatScreen.IGenericAction;

public class ShowPartyRowMenuFunction implements IGenericAction 
{
	private StatsGui gui;
	private ArrayList<Pair<StatMenuCharData, Boolean>> selectedPartyMembers;
	private StatMenuCharData toAdd;
	
	public ShowPartyRowMenuFunction(StatsGui gui, ArrayList<Pair<StatMenuCharData, Boolean>> selectedPartyMembers, StatMenuCharData toAdd)
	{
		this.gui = gui;
		this.selectedPartyMembers = selectedPartyMembers;
		this.toAdd = toAdd;
	}

	@Override
	public void Invoke() 
	{
		ArrayList<Triplet<String, String, IGenericAction>> constant = new ArrayList<Triplet<String,String,IGenericAction>>();
		ArrayList<Triplet<String, String, IGenericAction>> display = new ArrayList<Triplet<String,String,IGenericAction>>();
		display.add(new Triplet<String, String, IGenericAction>("Front Row", "", new ShowPartyMenuFunction(this.gui, this.selectedPartyMembers, this.toAdd, true)));
		display.add(new Triplet<String, String, IGenericAction>("Back Row", "", new ShowPartyMenuFunction(this.gui, this.selectedPartyMembers, this.toAdd, false)));
		this.gui.ChangeButtonForSubMenu("SelectRow", display, constant, 1);
	}
}
