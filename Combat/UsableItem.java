package TBC.Combat;

import java.util.ArrayList;
import java.util.List;

import TBC.Combat.Abilities.ICombatAbility;

public class UsableItem 
{
	private List<ICombatAbility> ability;
	private int damageFromUse;
	private ArrayList<String> descriptions;
	private ArrayList<String> proficiencies;
	
	public UsableItem(ICombatAbility ability, int damageFromUse, ArrayList<String> descriptions, ArrayList<String> proficiencies)
	{
		ArrayList<ICombatAbility> abilityList = new ArrayList<ICombatAbility>();
		abilityList.add(ability);
		this.ability = abilityList;
		this.damageFromUse = damageFromUse;
		this.descriptions = descriptions;
		this.proficiencies = proficiencies;
	}
	
	public UsableItem(List<ICombatAbility> ability, int damageFromUse, ArrayList<String> descriptions, ArrayList<String> proficiencies)
	{
		this.ability = ability;
		this.damageFromUse = damageFromUse;
		this.descriptions = descriptions;
		this.proficiencies = proficiencies;
	}
	
	public List<ICombatAbility> GetUseAbility()
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
	
	public ArrayList<String> RequiredProficiencies()
	{
		return proficiencies;
	}
}
