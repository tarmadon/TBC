package TBC.Combat;

import java.io.Serializable;
import java.util.ArrayList;

import TBC.Combat.Abilities.ICombatAbility;

public class BaseItem implements Serializable
{
	private ArrayList<String> descriptions;
	private ArrayList<String> proficiencies;
	
	public BaseItem()
	{
	}
	
	public BaseItem(ArrayList<String> descriptions, ArrayList<String> proficiencies)
	{
		this.descriptions = descriptions;
		this.proficiencies = proficiencies;
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
