package TBC.Combat;

import java.util.ArrayList;

import TBC.Combat.Abilities.ConstantAbility;

public class ConstantItem extends BaseItem
{
	private ArrayList<ConstantAbility> abilities;
	
	public ConstantItem(ArrayList<String> descriptions, ArrayList<String> proficiencies, ArrayList<ConstantAbility> abilities) 
	{
		super(descriptions, proficiencies);
		this.abilities = abilities;
	}

	public ArrayList<ConstantAbility> GetConstantAbilities()
	{
		return this.abilities;
	}
}
