package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.IGenericAction;

public class SelectAbilitiesMenuFunction implements IGenericAction
{
	private StatsGui gui;

	public SelectAbilitiesMenuFunction(StatsGui gui)
	{
		this.gui = gui;
	}

	public void Invoke()
	{
		ArrayList<Triplet<String, String, IGenericAction>> buttons = new ArrayList<Triplet<String,String,IGenericAction>>();
		for(int i = 0; i < this.gui.partyMembers.size(); i++)
		{
			StatMenuCharData partyMember = this.gui.partyMembers.get(i);
			buttons.add(new Triplet<String, String, IGenericAction>(partyMember.CombatEntity.name, "", new ShowAbilitiesForCharMenuFunction(this.gui, partyMember)));
		}
		
		ArrayList<Triplet<String, String, IGenericAction>> constantButtons = new ArrayList<Triplet<String,String,IGenericAction>>();
		constantButtons.add(new Triplet<String, String, IGenericAction>("Back", "", new SelectMainMenuFunction(this.gui)));
		
		this.gui.ChangeButtonForSubMenu("SelectMemberForAbilities", buttons, constantButtons, 1);
	}
}
