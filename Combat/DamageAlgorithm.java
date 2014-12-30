package TBC.Combat;

public class DamageAlgorithm
{
	public static int GetPhysicalDamage(int attack, int defense)
	{
		return GetRatioDamage(attack, defense);
	}

	public static int GetMagicalDamage(int attack, int defense)
	{
		return GetRatioDamage(attack, defense);
		//return GetFlatDamage(attack, defense);
	}

	public static int GetFlatDamage(int attack, int defense)
	{
		double baseDmg = attack - defense;
		int damage = (int)(Math.round(baseDmg * (.9F + (CombatRandom.GetRandom().nextInt(101) / 500F))));
		if(damage < 1)
		{
			damage = 1;
		}

		return damage;
	}

	public static int GetRatioDamage(int attack, int defense)
	{
		double baseDmg = attack * (50 / (double)(50 + defense));
		int damage = (int)(Math.round(baseDmg * (.9F + (CombatRandom.GetRandom().nextInt(101) / 500F))));
		if(damage < 1)
		{
			damage = 1;
		}

		return damage;
	}

	public static int GetExponentialDamage(int attack, int defense)
	{
		double dmgModifier = 1.0D;
		if(attack > defense)
		{
			dmgModifier = Math.log10((attack + 1) / ((float)(defense + 1))) + 1;
		}
		else
		{
			dmgModifier = attack / ((float)defense);
		}

		double baseDmg = attack * dmgModifier;
		int damage = (int)(Math.round(baseDmg * (.9F + (CombatRandom.GetRandom().nextInt(101) / 500F))));
		if(damage < 1)
		{
			damage = 1;
		}

		return damage;
	}
}
