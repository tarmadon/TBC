package TBC.Combat;

import java.util.ArrayList;

import TBC.Combat.Effects.StatChangeStatus;

public class FlatBonusEquippedItem extends EquippedItem
{
	private int itemEffectType;
	private int itemEffectStrength;
	private String itemSlot;
	private Boolean doesRequireWorn;

	public FlatBonusEquippedItem(int effectType, int effectStrength, String slot, ArrayList<String> description, ArrayList<String> proficiencies)
	{
		super(description, proficiencies);
		this.itemEffectType = effectType;
		this.itemEffectStrength = effectStrength;
		this.itemSlot = slot;
	}

	public Boolean HasEffect(int effectType)
	{
		return this.itemEffectType == effectType;
	}

	public int GetModifiedValue(int baseValue)
	{
		return baseValue + this.itemEffectStrength;
	}

	public String GetSlot()
	{
		return this.itemSlot;
	}
}