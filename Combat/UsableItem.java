package TBC.Combat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import TBC.Combat.Abilities.ICombatAbility;

public class UsableItem extends BaseItem implements Serializable
{
	private List<ICombatAbility> ability;
	private int damageFromUse;
	
	public UsableItem()
	{
	}
	
	public UsableItem(ICombatAbility ability, int damageFromUse, ArrayList<String> descriptions, ArrayList<String> proficiencies)
	{
		super(descriptions, proficiencies);
		ArrayList<ICombatAbility> abilityList = new ArrayList<ICombatAbility>();
		abilityList.add(ability);
		this.ability = abilityList;
		this.damageFromUse = damageFromUse;
	}
	
	public UsableItem(List<ICombatAbility> ability, int damageFromUse, ArrayList<String> descriptions, ArrayList<String> proficiencies)
	{
		super(descriptions, proficiencies);
		this.ability = ability;
		this.damageFromUse = damageFromUse;
	}
	
	public List<ICombatAbility> GetUseAbility()
	{
		return this.ability;
	}
	
	public int GetDamageFromUse()
	{
		return this.damageFromUse;
	}
}
