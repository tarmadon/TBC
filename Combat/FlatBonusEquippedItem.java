package TBC.Combat;

import java.util.ArrayList;

import TBC.Pair;
import TBC.Combat.Effects.StatChangeStatus;

public class FlatBonusEquippedItem extends EquippedItem
{
	private ArrayList<Pair<Integer, Integer>> modifiers;
	private String itemSlot;
	private Boolean doesRequireWorn;

	public FlatBonusEquippedItem(ArrayList<Pair<Integer, Integer>> modifiers, String slot, ArrayList<String> description, ArrayList<String> proficiencies)
	{
		super(description, proficiencies);
		this.modifiers = modifiers;
		this.itemSlot = slot;
	}

	public Boolean HasEffect(int effectType)
	{
		boolean foundModifier = false;
		for(Pair<Integer, Integer> modifier : modifiers)
		{
			if(modifier.item1 == effectType)
			{
				foundModifier = true;
			}
		}
		
		return foundModifier;
	}

	public int GetModifiedValue(int effectType, int baseValue)
	{
		for(Pair<Integer, Integer> modifier : modifiers)
		{
			if(modifier.item1 == effectType)
			{
				return baseValue + modifier.item2;
			}
		}
		
		return baseValue;
	}

	public String GetSlot()
	{
		return this.itemSlot;
	}
}