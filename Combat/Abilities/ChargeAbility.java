package TBC.Combat.Abilities;

import java.util.HashMap;

import TBC.Pair;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.TurnState;

public class ChargeAbility extends StandardAbility
{
	public ChargeAbility(IOneTimeEffect[] effects, String name, int targetType,
			int cost, Boolean usableOutOfCombat, Boolean isSpell)
	{
		super(effects, name, targetType, cost, usableOutOfCombat, isSpell);
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
		long time = state.GetElapsedTime();
		int targetXPos = 0;
		int targetYPos = 0;
		if(state.targetEntities != null && state.targetEntities.size() > 0)
		{
			CombatEntity target = state.targetEntities.get(0);
			Pair<Integer, Integer> pos = positionLookup.get(target);
			targetXPos = pos.item1;
			targetYPos = pos.item2 - 18;
		}

		int xPos = startXPos;
		int yPos = startYPos;
		if(time < 500)
		{
			xPos = Math.round(startXPos + ((time * (targetXPos - startXPos))/500));
			yPos = Math.round(startYPos + ((time * (targetYPos - startYPos))/500));
		}

		display.DrawCombatEntity(entity, xPos, yPos, startRotation, time - 500, isTarget);
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
		long time = state.GetElapsedTime();
		boolean showHitIndicator = false;
		if(time > 500)
		{
			showHitIndicator = true;
		}

		display.DrawCombatEntity(entity, startXPos, startYPos, startRotation, time - 700, showHitIndicator);
	}

	public int GetAnimationTime()
	{
		return 1500;
	}
}
