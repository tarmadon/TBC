package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;

public class FlatManaEffect implements IOneTimeEffect
{
	private int flatStrength;

	public FlatManaEffect(int flatStrength)
	{
		this.flatStrength = flatStrength;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target)
	{
		target.currentMp = target.currentMp + this.flatStrength;
		if(target.currentMp > target.GetMaxMp())
		{
			target.currentMp = target.GetMaxMp();
		}
	}
}
