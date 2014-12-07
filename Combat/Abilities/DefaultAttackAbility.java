package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.Random;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.Combat.Effects.PhysicalDamageEffect;
import TBC.Combat.TriggeredEffects.BaseTriggeredEffect;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DefaultAttackAbility extends StandardAbility
{
	private static IOneTimeEffect[] effect = new IOneTimeEffect[] { new PhysicalDamageEffect(0) };

	public DefaultAttackAbility()
	{
		super(effect, "", AbilityTargetType.OneEnemy, 0, false, false, new ArrayList<String>());
	}
}