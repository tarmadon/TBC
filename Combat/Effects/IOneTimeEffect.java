package TBC.Combat.Effects;

import java.io.Serializable;
import java.util.ArrayList;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;

public interface IOneTimeEffect extends Serializable
{
	void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target);
}