package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.DamageAlgorithm;
import net.minecraft.entity.player.EntityPlayer;

public class ManaStealEffect implements IOneTimeEffect
{
	private CombatEntity user;
	private int flatPhysicalStrength;
	private int flatMagicalStrength;
	private float physMultiplier;
	private float magMultiplier;
	
	public ManaStealEffect(int flatPhysicalStrength, int flatMagicalStrength, float physMultiplier, float magMultiplier)
	{
		this.flatPhysicalStrength = flatPhysicalStrength;
		this.flatMagicalStrength = flatMagicalStrength;
		this.physMultiplier = physMultiplier;
		this.magMultiplier = physMultiplier;
	}
	
	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target) 
	{
		int physicalStrength = this.flatPhysicalStrength + Math.round(user.GetAttack() * this.physMultiplier);
		int magicalStrength = this.flatMagicalStrength + Math.round(user.GetMagic() * this.magMultiplier);
		int mDefense = target.GetMagicDefense();
		int defense = target.GetDefense();
		int damage = DamageAlgorithm.GetPhysicalDamage(physicalStrength, defense);
		damage += DamageAlgorithm.GetMagicalDamage(magicalStrength, mDefense);
		damage = Math.min(damage, target.currentMp);
		target.currentMp = target.currentMp - damage;
		this.user.currentMp = Math.min(this.user.GetMaxMp(), this.user.currentMp + damage);
	}
}