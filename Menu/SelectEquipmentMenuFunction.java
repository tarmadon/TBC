package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;

public class SelectEquipmentMenuFunction implements IGenericAction
{
	private StatsGui gui;

	public SelectEquipmentMenuFunction(StatsGui gui)
	{
		this.gui = gui;
	}

	public void Invoke()
	{
		ArrayList<GenericScrollBoxCellData> buttons = new ArrayList<GenericScrollBoxCellData>();
		for(int i = 0; i < this.gui.partyMembers.size(); i++)
		{
			StatMenuCharData partyMember = this.gui.partyMembers.get(i);
			buttons.add(new GenericScrollBoxCellData(partyMember.CombatEntity.name, "", new ShowEquipmentForCharMenuFunction(this.gui, partyMember, null)));
		}
		
		ArrayList<GenericScrollBoxCellData> constantButtons = new ArrayList<GenericScrollBoxCellData>();
		constantButtons.add(new GenericScrollBoxCellData("Back", "", new SelectMainMenuFunction(this.gui)));
		
		this.gui.ChangeButtonForSubMenu("SelectMemberForEquipment", buttons, constantButtons, 1);
	}
}
