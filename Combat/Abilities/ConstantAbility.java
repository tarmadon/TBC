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

public class ConstantAbility implements ICombatAbility
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

	public Boolean IsUsableOutOfCombat()
	{
		return false;
	}

	public String GetAbilityName()
	{
		return this.abilityName;
	}

	public int GetAbilityTarget()
	{
		return AbilityTargetType.Self;
	}

	public ArrayList<String> GetDescription() 
	{
		return this.description;
	}
	
	public int GetMpCost()
	{
		return 100000;
	}

	public IOneTimeEffect[] GetEffects(CombatEngine engine, CombatEntity user, ArrayList<CombatEntity> targets, ArrayList<String> messages)
	{
		return null;
	}

	public List GetConstantEffects()
	{
		return this.effects;
	}

	public Boolean IsSpell()
	{
		return false;
	}

	public void DrawUser(BattleScreenDrawer display, HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup, TurnState state,
			CombatEntity entity, boolean isAlly, boolean isTarget,
			int startXPos, int startYPos, int startRotation)
	{
	}

	public void DrawTarget(BattleScreenDrawer display, TurnState state,
			CombatEntity entity, boolean isAlly, int startXPos, int startYPos,
			int startRotation)
	{
	}

	public int GetAnimationTime()
	{
		return 0;
	}
}
