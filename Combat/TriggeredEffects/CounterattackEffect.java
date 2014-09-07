package TBC.Combat.TriggeredEffects;

import java.util.ArrayList;
import java.util.Arrays;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatRandom;
import TBC.Combat.DamageType;
import TBC.Combat.Abilities.AbilityTargetType;
import TBC.Combat.Abilities.DefaultAttackAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Abilities.StandardAbility;
import TBC.Combat.Effects.DelayTurnEffect;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.Combat.Effects.PhysicalDamageEffect;

public class CounterattackEffect extends BaseTriggeredEffect 
{
	private int chanceOutOf100;
	private ICombatAbility counterAttack;
	
	public CounterattackEffect(int chanceOutOf100, ICombatAbility counterAttack)
	{
		this.chanceOutOf100 = chanceOutOf100;
		this.counterAttack = counterAttack;
	}
	
	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker) 
	{
		if(!effectFromAttacker && ((source.GetDamageType() & DamageType.Uncounterable) != DamageType.Uncounterable))
		{
			if(CombatRandom.GetRandom().nextFloat() < this.chanceOutOf100/100F)
			{
				if(defender.ongoingEffects == null)
				{
					defender.ongoingEffects = new ArrayList();
				}
				
				new DelayTurnEffect(-100).ApplyToEntity(engine, defender, defender);
				int target = counterAttack.GetAbilityTarget();
				if(target == AbilityTargetType.OneEnemy)
				{
					defender.ongoingEffects.add(new DelayedEffect(1, 0, this.counterAttack, new ArrayList<CombatEntity>(Arrays.asList(attacker))));
				}
				else if(target == AbilityTargetType.OneAlly || target == AbilityTargetType.OneDeadAlly)
				{
					defender.ongoingEffects.add(new DelayedEffect(1, 0, this.counterAttack, new ArrayList<CombatEntity>(Arrays.asList(defender))));
				}
				else if(target == AbilityTargetType.AllAllies)
				{
					if(engine.allies.contains(defender))
					{
						defender.ongoingEffects.add(new DelayedEffect(1, 0, this.counterAttack, new ArrayList<CombatEntity>(engine.allies)));
					}
					else
					{
						defender.ongoingEffects.add(new DelayedEffect(1, 0, this.counterAttack, new ArrayList<CombatEntity>(engine.enemies)));
					}
				}
				else if(target == AbilityTargetType.AllEnemies)
				{
					if(engine.allies.contains(defender))
					{
						defender.ongoingEffects.add(new DelayedEffect(1, 0, this.counterAttack, new ArrayList<CombatEntity>(engine.enemies)));
					}
					else
					{
						defender.ongoingEffects.add(new DelayedEffect(1, 0, this.counterAttack, new ArrayList<CombatEntity>(engine.allies)));
					}
				}
			}
		}
		
		return super.OnDamage(engine, attacker, defender, source, damage, effectFromAttacker);
	}
}
