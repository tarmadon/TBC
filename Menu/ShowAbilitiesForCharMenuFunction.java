package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import TBC.Pair;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.IGenericAction;

public class ShowAbilitiesForCharMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private StatMenuCharData user;
	
	public ShowAbilitiesForCharMenuFunction(StatsGui gui, StatMenuCharData player)
	{
		this.gui = gui;
		this.user = player;
	}
	
	@Override
	public void Invoke() 
	{
		ArrayList<Triplet<String, String, IGenericAction>> displayAbilities = new ArrayList<Triplet<String,String,IGenericAction>>();
		Pair<Integer, ICombatAbility>[] abilities = user.CombatEntity.GetAbilities();
		for(int i = 0; i < abilities.length; i++)
		{
			IGenericAction action = null;
			ICombatAbility ability = abilities[i].item2;
			if(ability.IsUsableOutOfCombat() && user.CombatEntity.currentMp >= ability.GetMpCost())
			{
				// Targeting.
				action = new ChooseTargetForAbilityMenuFunction(this.gui, this.user, ability, new ShowAbilitiesForCharMenuFunction(this.gui, this.user));
			}

			if(!ability.GetAbilityName().isEmpty())
			{
				String mpDisplay = ability.GetMpCost() + "";
				if(ability instanceof ConstantAbility)
				{
					mpDisplay = "";
				}

				displayAbilities.add(new Triplet(ability.GetAbilityName(), mpDisplay, action));
			}
		}

		if(displayAbilities.size() == 0)
		{
			displayAbilities.add(new Triplet("You have no abilities.", "", null));
		}

		ArrayList<Triplet<String, String, IGenericAction>> constantButtons = new ArrayList<Triplet<String,String,IGenericAction>>();
		constantButtons.add(new Triplet<String, String, IGenericAction>("Back", "", new SelectAbilitiesMenuFunction(this.gui)));
		
		this.gui.ChangeButtonForSubMenu("Abilities", displayAbilities, constantButtons, 0);
	}

}
