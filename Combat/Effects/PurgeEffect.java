package TBC.Combat.Effects;

import java.util.ArrayList;
import java.util.List;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;

public class PurgeEffect implements IOneTimeEffect 
{
	private String effectName;
	
	public PurgeEffect(String effectName)
	{
		this.effectName = effectName;
	}
	
	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target) 
	{
		List itemsToRemove = new ArrayList();
		for(Object effect : target.ongoingEffects)
		{
			if(effect instanceof INonStackingEffect && ((INonStackingEffect)effect).GetEffectName() == effectName)
			{
				itemsToRemove.add(effect);
			}
		}
		
		for(Object toRemove : itemsToRemove)
		{
			target.ongoingEffects.remove(toRemove);
		}
	}
}
