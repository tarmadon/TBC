package TBC.Combat;

import TBC.Combat.Effects.IExpiringEffect;
import TBC.Combat.Effects.INonStackingEffect;

public interface IStatusChange extends IExpiringEffect, INonStackingEffect
{
	Boolean ApplyToEntity(CombatEntity target);
}