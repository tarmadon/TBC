package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.TurnState;
import net.minecraft.entity.EntityLiving;

public interface ICombatAbility
{
	Boolean IsSpell();
	Boolean IsUsableOutOfCombat();
	String GetAbilityName();
	int GetAbilityTarget();
	int GetMpCost();
	IOneTimeEffect[] GetEffects(CombatEngine engine, CombatEntity user, ArrayList<CombatEntity> targets, ArrayList<String> messages);

	void DrawUser(BattleScreenDrawer display, HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup, TurnState state, CombatEntity entity, boolean isAlly, boolean isTarget, int startXPos, int startYPos, int startRotation);
	void DrawTarget(BattleScreenDrawer display, TurnState state, CombatEntity entity, boolean isAlly, int startXPos, int startYPos, int startRotation);
	int GetAnimationTime();
}