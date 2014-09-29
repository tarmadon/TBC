package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.DamageAlgorithm;
import TBC.Combat.DamageType;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;
import net.minecraft.entity.player.EntityPlayer;

public class MagicDamageEffect implements IOneTimeEffect, IDamageEffect
{
	private int flatMagicStrength;
	private float magicMultiplier;
	private int additionalDamageTypes;

	public MagicDamageEffect(int flatMagicStrength, float magicMultiplier)
	{
		this(flatMagicStrength, magicMultiplier, 0);
	}

	public MagicDamageEffect(int flatMagicStrength, float magicMultiplier, int additionalDamageTypes)
	{
		this.flatMagicStrength = flatMagicStrength;
		this.magicMultiplier = magicMultiplier;
		this.additionalDamageTypes = additionalDamageTypes;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target)
	{
		int effectiveStrength = this.flatMagicStrength + Math.round(user.GetMagic() * magicMultiplier);
		int defense = target.GetMagicDefense();
		int damage = DamageAlgorithm.GetMagicalDamage(effectiveStrength, defense);
		engine.DoDamage(user, target, this, damage);
	}

	public int GetDamageType()
	{
		return DamageType.Magical | this.additionalDamageTypes;
	}
}