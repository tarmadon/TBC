package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.TurnState;

public class StandardAbility implements ICombatAbility
{
	private int cost;
	private int targetType;
	private String name;
	private IOneTimeEffect[] effects;
	private Boolean usableOutOfCombat;
	private Boolean isSpell;

	public StandardAbility(IOneTimeEffect[] effects, String name, int targetType, int cost, Boolean usableOutOfCombat, Boolean isSpell)
	{
		this.cost = cost;
		this.targetType = targetType;
		this.name = name;
		this.effects = effects;
		this.usableOutOfCombat = usableOutOfCombat;
		this.isSpell = isSpell;
	}

	public int GetMpCost()
	{
		return this.cost;
	}

	public String GetAbilityName()
	{
		return this.name;
	}

	public int GetAbilityTarget()
	{
		return this.targetType;
	}

	public IOneTimeEffect[] GetEffects(CombatEngine engine, CombatEntity user, ArrayList<CombatEntity> targets, ArrayList<String> messages)
	{
		if(!this.GetAbilityName().isEmpty() && messages != null)
		{
			messages.add(this.GetAbilityName());
		}

		return this.effects;
	}

	public Boolean IsUsableOutOfCombat()
	{
		return this.usableOutOfCombat;
	}

	public Boolean IsSpell()
	{
		return this.isSpell;
	}

	public void DrawUser(
			BattleScreenDrawer display,
			HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup,
			TurnState state,
			CombatEntity entity,
			boolean isAlly,
			boolean isTarget,
			int startXPos,
			int startYPos,
			int startRotation)
	{
		if(isAlly)
		{
			display.DrawCombatEntity(entity, startXPos - 20, startYPos, startRotation, state.GetElapsedTime(), isTarget);
		}
		else
		{
			display.DrawCombatEntity(entity, startXPos + 20, startYPos, startRotation, state.GetElapsedTime(), isTarget);
		}
	}

	public void DrawTarget(
			BattleScreenDrawer display,
			TurnState state,
			CombatEntity entity,
			boolean isAlly,
			int startXPos,
			int startYPos,
			int startRotation)
	{
		display.DrawCombatEntity(entity, startXPos, startYPos, startRotation, state.GetElapsedTime(), true);
	}

	public int GetAnimationTime()
	{
		return 600;
	}
}