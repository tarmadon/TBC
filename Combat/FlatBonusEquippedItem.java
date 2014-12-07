package TBC.Combat;

import java.util.ArrayList;

import TBC.Combat.Effects.StatChangeStatus;

public class FlatBonusEquippedItem extends EquippedItem
{
	private int itemEffectType;
	private int itemEffectStrength;
	private String itemSlot;
	private Boolean doesRequireWorn;

	public FlatBonusEquippedItem(int effectType, int effectStrength, String slot, ArrayList<String> description)
	{
		this(effectType, effectStrength, slot, false, description);
	}

	public FlatBonusEquippedItem(int effectType, int effectStrength, String slot, Boolean doesRequireWorn, ArrayList<String> description)
	{
		super(description);
		this.itemEffectType = effectType;
		this.itemEffectStrength = effectStrength;
		this.itemSlot = slot;
		this.doesRequireWorn = doesRequireWorn;
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

	public Boolean DoesRequireWorn()
	{
		return this.doesRequireWorn;
	}

	public String GetDisplayString()
	{
		String effectName = "";
		if(itemEffectType == StatChangeStatus.AttackChange)
		{
			effectName = "Attack";
		}
		else if(itemEffectType == StatChangeStatus.DefenseChange)
		{
			effectName = "Defense";
		}
		else if(itemEffectType == StatChangeStatus.HpChange)
		{
			effectName = "Hp";
		}
		else if(itemEffectType == StatChangeStatus.MagicChange)
		{
			effectName = "Magic";
		}
		else if(itemEffectType == StatChangeStatus.MpChange)
		{
			effectName = "Mp";
		}
		else if(itemEffectType == StatChangeStatus.SpeedChange)
		{
			effectName = "Speed";
		}

		if(itemEffectStrength > 0)
		{
			return effectName + " +" + this.itemEffectStrength;
		}
		else
		{
			return effectName + " " + this.itemEffectStrength;
		}
	}
}