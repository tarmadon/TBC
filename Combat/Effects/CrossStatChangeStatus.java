package TBC.Combat.Effects;

import TBC.Combat.CombatEntity;

public class CrossStatChangeStatus extends StatChangeStatus 
{
	private int sourceStat;
	
	public CrossStatChangeStatus(
			String abilityCategory, 
			int changeType,
			int sourceStat,
			int flatStrength, 
			float strengthModifier, 
			int numTurnsToLive,
			int numTicksToLive) 
	{
		super(abilityCategory, changeType, flatStrength, strengthModifier, numTurnsToLive, numTicksToLive);
		this.sourceStat = sourceStat;
	}
	
	@Override
	public int GetEffectiveStat(CombatEntity entity, int existingStat) 
	{
		int source = 0;
		if(sourceStat == StatChangeStatus.AttackChange)
		{
			source = entity.GetAttack();
		}
		else if(sourceStat == StatChangeStatus.DefenseChange)
		{
			source = entity.GetDefense();
		}
		else if(sourceStat == StatChangeStatus.MagicChange)
		{
			source = entity.GetMagic();
		}
		else if(sourceStat == StatChangeStatus.MagicDefenseChange)
		{
			source = entity.GetMagicDefense();
		}
		else if(sourceStat == StatChangeStatus.SpeedChange)
		{
			source = entity.GetSpeed();
		}
		else if(sourceStat == StatChangeStatus.HpChange)
		{
			source = entity.GetMaxHp();
		}
		else if(sourceStat == StatChangeStatus.MpChange)
		{
			source = entity.GetMaxMp();
		}
		
		return existingStat + this.flatStrength + Math.round(this.strengthMultiplier * source);
	}
	
	@Override
	public Object CreateEffect(CombatEntity user, CombatEntity target) 
	{
		return new CrossStatChangeStatus(abilityCategory, sourceStat, sourceStat, sourceStat, strengthMultiplier, numTurnsToLive, numTicksToLive);
	}
}
