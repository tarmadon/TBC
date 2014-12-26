package TBC.Combat;

import java.util.ArrayList;

public class EquippedItem extends BaseItem
{
	public EquippedItem(ArrayList<String> descriptions,	ArrayList<String> proficiencies) 
	{
		super(descriptions, proficiencies);
	}

	public Boolean HasEffect(int effectType)
	{
		return false;
	}

	public int GetModifiedValue(int effectType, int baseValue)
	{
		return baseValue;
	}

	public String GetSlot()
	{
		return "";
	}
}
