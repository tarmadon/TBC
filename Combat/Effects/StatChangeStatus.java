package TBC.Combat.Effects;

import TBC.Combat.CombatEntity;
import TBC.Combat.IStatusChange;

public class StatChangeStatus implements IStatusChange, IEffectFactory
{
	public static final int AttackChange = 1;
	public static final int DefenseChange = 2;
	public static final int MagicChange = 3;
	public static final int MagicDefenseChange = 4;
	public static final int SpeedChange = 5;
	public static final int HpChange = 6;
	public static final int MpChange = 7;

	public int changeType;
	protected int flatStrength;
	protected float strengthMultiplier;
	protected int numTurnsToLive;
	protected int numTicksToLive;
	protected String abilityCategory;
	protected CombatEntity user;

	public StatChangeStatus(String abilityCategory, int changeType, int flatStrength, float strengthModifier, int numTurnsToLive, int numTicksToLive)
	{
		this.changeType = changeType;
		this.flatStrength = flatStrength;
		this.strengthMultiplier = strengthModifier;
		this.numTurnsToLive = numTurnsToLive;
		this.numTicksToLive = numTicksToLive;
		this.abilityCategory = abilityCategory;
	}

	public StatChangeStatus(String abilityCategory, int changeType, int flatStrength, float strengthModifier, int numTurnsToLive, int numTicksToLive, CombatEntity user)
	{
		this.changeType = changeType;
		this.flatStrength = flatStrength;
		this.strengthMultiplier = strengthModifier;
		this.numTurnsToLive = numTurnsToLive;
		this.numTicksToLive = numTicksToLive;
		this.abilityCategory = abilityCategory;
		this.user = user;
	}

	public Boolean ApplyToEntity(CombatEntity entity)
	{
		return false;
	}

	public Boolean IsExpiredOnNextTurn()
	{
		if(numTurnsToLive == -1)
		{
			return false;
		}

		numTurnsToLive--;
		if(numTurnsToLive < 1)
		{
			return true;
		}

		return false;
	}

	public int GetEffectiveStat(CombatEntity entity, int existingStat)
	{
		return flatStrength + Math.round(existingStat * strengthMultiplier);
	}

	public String GetEffectName()
	{
		return this.abilityCategory;
	}

	public Boolean IsExpiredOnNextTick()
	{
		if(numTicksToLive == -1)
		{
			return false;
		}

		numTicksToLive--;
		if(numTicksToLive < 1)
		{
			return true;
		}

		return false;
	}

	public Object CreateEffect(CombatEntity user, CombatEntity target)
	{
		return new StatChangeStatus(this.abilityCategory, this.changeType, this.flatStrength, this.strengthMultiplier, this.numTurnsToLive, this.numTicksToLive, user);
	}
}
