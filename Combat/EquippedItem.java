package TBC.Combat;

public class EquippedItem 
{
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
	
	public String GetDisplayString()
	{
		return "";
	}
}
