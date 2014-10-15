package TBC.Menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.Pair;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.CombatScreen.IGenericAction;
import TBC.Messages.NBTTagCompoundMessage;

public class ShowPartyMenuFunction implements IGenericAction 
{
	private StatsGui gui;
	private ArrayList<Pair<StatMenuCharData, Boolean>> selectedPartyMembers;
	private StatMenuCharData toAdd;
	private boolean frontRow;
	
	public ShowPartyMenuFunction(StatsGui gui, ArrayList<Pair<StatMenuCharData, Boolean>> selectedPartyMembers, StatMenuCharData toAdd, boolean frontRow)
	{
		this.gui = gui;
		this.selectedPartyMembers = selectedPartyMembers;
		this.toAdd = toAdd;
		this.frontRow = frontRow;
	}
	
	@Override
	public void Invoke() 
	{
		if(toAdd != null)
		{
			selectedPartyMembers.add(new Pair<StatMenuCharData, Boolean>(toAdd, this.frontRow));
			if(selectedPartyMembers.size() == 4)
			{
				new ApplyPartyChangesMenuFunction(this.gui, this.selectedPartyMembers).Invoke();
				return;
			}
		}
		
		EntityPlayer player = this.gui.player;
		ArrayList<StatMenuCharData> potentialPartyMembers = new ArrayList<StatMenuCharData>();
		CombatEntity c = CombatEntityLookup.Instance.GetCombatEntityForPlayer(player);
		potentialPartyMembers.add(new StatMenuCharData(player, null, c, false));
				
		ItemStack[] items = player.inventory.mainInventory;
		for(int i = 0; i < items.length; i++)
		{
			if(items[i] != null)
			{
				ItemStack stack = (ItemStack)items[i];
				if(stack.getItem() instanceof HenchmanItem)
				{
					HenchmanItem hench = (HenchmanItem)stack.getItem();
					CombatEntity cHench = CombatEntityLookup.Instance.GetCombatEntity(i, hench.henchmanType, hench.henchmanName);
					potentialPartyMembers.add(new StatMenuCharData(null, stack, cHench, false));
				}
			}
		}

		ArrayList<Triplet<String, String, IGenericAction>> display = new ArrayList<Triplet<String,String,IGenericAction>>();
		for(int i = 0; i < potentialPartyMembers.size(); i++)
		{
			StatMenuCharData potential = potentialPartyMembers.get(i);
			boolean alreadyAdded = false;
			for(int j = 0; j < this.selectedPartyMembers.size(); j++)
			{
				if(potential.Player == this.selectedPartyMembers.get(j).item1.Player && potential.Item == this.selectedPartyMembers.get(j).item1.Item)
				{
					alreadyAdded = true;
					break;
				}
			}
			
			if(!alreadyAdded)
			{
				display.add(new Triplet<String, String, IGenericAction>(potential.CombatEntity.GetName(), "", new ShowPartyRowMenuFunction(this.gui, this.selectedPartyMembers, potential)));
			}
		}
		
		ArrayList<Triplet<String, String, IGenericAction>> constant = new ArrayList<Triplet<String,String,IGenericAction>>();
		if(this.selectedPartyMembers.size() > 0)
		{
			constant.add(new Triplet<String, String, IGenericAction>("Done", "", new ApplyPartyChangesMenuFunction(this.gui, this.selectedPartyMembers)));
		}
		
		constant.add(new Triplet<String, String, IGenericAction>("Cancel", "", new SelectMainMenuFunction(this.gui)));
		this.gui.ChangeButtonForSubMenu("ShowParty", display, constant, 0);
	}
}
