package TBC.Combat;

import java.util.ArrayList;

public class EquippedItem
{
	private ArrayList<String> descriptions;
	
	public EquippedItem(ArrayList<String> descriptions)
	{
		this.descriptions = descriptions;
	}
	
	public Boolean HasEffect(int effectType)
	{
		return false;
	}

	public int GetModifiedValue(int baseValue)
	{
		return baseValue;
	}

	public String GetSlot()
	{
		return "";
	}

	public int GetPriority()
	{
		return 0;
	}

	public Boolean DoesRequireWorn()
	{
		return false;
	}

	public ArrayList<String> DescriptionStrings()
	{
		return descriptions;
	}
}
