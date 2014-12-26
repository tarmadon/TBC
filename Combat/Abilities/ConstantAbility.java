package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.TurnState;

public class ConstantAbility implements IAbility
{
	private String abilityName;
	private List effects;
	private ArrayList<String> description;
	
	public ConstantAbility(String abilityName, List effects, ArrayList<String> description)
	{
		this.abilityName = abilityName;
		this.effects = effects;
		this.description = description;
	}

	public String GetAbilityName()
	{
		return this.abilityName;
	}

	public ArrayList<String> GetDescription() 
	{
		return this.description;
	}
	
	public List GetConstantEffects()
	{
		return this.effects;
	}
}
