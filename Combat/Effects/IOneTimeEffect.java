package TBC.Combat.Effects;

import java.util.ArrayList;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;

public interface IOneTimeEffect
{
	void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target);
}