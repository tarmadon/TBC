package TBC.Combat.Abilities;

import java.util.ArrayList;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.Combat.TriggeredEffects.DelayedEffect;

public class DelayedAbility extends SacrificeAbility 
{
	private ICombatAbility delayedAbility;
	private boolean hasInitialEffect = false;
	
	public DelayedAbility(ICombatAbility delayedAbility, String name, int targetType, int cost, Boolean usableOutOfCombat, Boolean isSpell) 
	{
		super(new IOneTimeEffect[0], new IOneTimeEffect[0], name, targetType, cost, usableOutOfCombat, isSpell);
		hasInitialEffect = false;
		this.delayedAbility = delayedAbility;
	}
	
	public DelayedAbility(IOneTimeEffect[] initialSelfEffects, IOneTimeEffect[] initialEffects, ICombatAbility delayedAbility, String name, int targetType, int cost, Boolean usableOutOfCombat, Boolean isSpell) 
	{
		super(initialSelfEffects, initialEffects, name, targetType, cost, usableOutOfCombat, isSpell);
		if(initialEffects.length != 0 || initialSelfEffects.length != 0)
		{
			hasInitialEffect = true;
		}
		
		this.delayedAbility = delayedAbility;
	}
	
	public IOneTimeEffect[] GetEffects(CombatEngine engine, CombatEntity user, ArrayList<CombatEntity> targets, ArrayList<String> messages) 
	{
		if(!this.GetAbilityName().isEmpty())
		{
			messages.add(String.format("Started {0}", this.GetAbilityName()));
		}
		
		user.ongoingEffects.add(new DelayedEffect(2, -1, this.delayedAbility, targets));
		return super.GetEffects(engine, user, targets, null);
	}
	
	public Boolean HasInitialEffect()
	{
		return this.hasInitialEffect;
	}
}
