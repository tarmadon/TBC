package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import TBC.HenchmanItem;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.AbilityTargetType;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.IGenericAction;

public class ChooseTargetForAbilityMenuFunction implements IGenericAction 
{
	private StatsGui gui;
	private StatMenuCharData user;
	private ICombatAbility ability;
	private IGenericAction cancelAction;
	
	public ChooseTargetForAbilityMenuFunction(StatsGui gui, StatMenuCharData user, ICombatAbility ability, IGenericAction cancelAction)
	{
		this.gui = gui;
		this.user = user;
		this.ability = ability;
		this.cancelAction = cancelAction;
	}
	
	@Override
	public void Invoke() 
	{
		EntityPlayer actualPlayer = Minecraft.getMinecraft().thePlayer;
		ArrayList<Triplet<String, String, IGenericAction>> displayItems = new ArrayList<Triplet<String,String,IGenericAction>>();
		ItemStack[] henchmenItems = new ItemStack[actualPlayer.inventory.getHotbarSize() + 1];

		ArrayList<StatMenuCharData> allPartyMembers = this.gui.partyMembers;
		if(ability.GetAbilityTarget() == AbilityTargetType.AllAllies)
		{
			displayItems.add(new Triplet("Current Party", "", new UseAbilityFromStatsGuiAction(this.gui, this.user, this.ability, allPartyMembers, this.cancelAction)));
		}
		else if(this.ability.GetAbilityTarget() == AbilityTargetType.Self)
		{
			ArrayList<StatMenuCharData> userTarget = new ArrayList<StatMenuCharData>();
			userTarget.add(this.user);
			displayItems.add(new Triplet(this.user.CombatEntity.GetName(), "HP: " + this.user.CombatEntity.currentHp + "/" + this.user.CombatEntity.GetMaxHp(), new UseAbilityFromStatsGuiAction(this.gui, this.user, this.ability, userTarget, this.cancelAction)));
		}
		else if(this.ability.GetAbilityTarget() == AbilityTargetType.OneAlly)
		{
			for(int i = 0; i< allPartyMembers.size(); i++)
			{
				StatMenuCharData target = allPartyMembers.get(i);
				ArrayList<StatMenuCharData> oneAllyTarget = new ArrayList<StatMenuCharData>();
				oneAllyTarget.add(target);
				displayItems.add(new Triplet(target.CombatEntity.GetName(), "HP: " + target.CombatEntity.currentHp + "/" + target.CombatEntity.GetMaxHp(), new UseAbilityFromStatsGuiAction(this.gui, this.user, this.ability, oneAllyTarget, this.cancelAction)));
			}
		}

		ArrayList<Triplet<String, String, IGenericAction>> cancelButton = new ArrayList<Triplet<String,String,IGenericAction>>();
		cancelButton.add(new Triplet<String, String, IGenericAction>("Cancel", "", this.cancelAction));
		this.gui.ChangeButtonForSubMenu("UseAbility", displayItems, cancelButton, 1);
	}
}
