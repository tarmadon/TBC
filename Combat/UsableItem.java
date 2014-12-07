package TBC.Combat;

import java.util.ArrayList;

import TBC.Combat.Abilities.ICombatAbility;

public class UsableItem 
{
	private ICombatAbility ability;
	private int damageFromUse;
	private ArrayList<String> descriptions;
	
	public UsableItem(ICombatAbility ability, int damageFromUse, ArrayList<String> descriptions)
	{
		this.ability = ability;
		this.damageFromUse = damageFromUse;
		this.descriptions = descriptions;
	}
	
	public ICombatAbility GetUseAbility()
	{
		return this.ability;
	}
	
	public int GetDamageFromUse()
	{
		return this.damageFromUse;
	}
	
	public ArrayList<String> DescriptionStrings()
	{
		return this.descriptions;
	}
}
