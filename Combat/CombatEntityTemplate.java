package TBC.Combat;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import TBC.CombatEntitySaveData;
import TBC.Pair;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.IAbility;
import TBC.Combat.Abilities.ICombatAbility;

public class CombatEntityTemplate implements Serializable
{
	public String name;

	public int maxHp;
	public int maxMp;
	public int attack;
	public int defense;
	public int mAttack;
	public int mDefense;
	public int speed;
	public int xpValue;
	public int apValue;

	public Pair<Integer, IAbility>[] abilities;
	
	public CombatEntityTemplate()
	{
	}
	
	public static CombatEntityTemplate GetCombatEntityTemplateFromSaveData(String name, CombatEntitySaveData data)
	{
		CombatEntityTemplate template = JobLookup.Instance.RecalculateStats(data.CurrentJob, data.Level);
		template.maxHp += data.BonusMaxHP;
		template.maxMp += data.BonusMaxMP;
		template.attack += data.BonusAttack;
		template.defense += data.BonusDefense;
		template.mAttack += data.BonusMAttack;
		template.mDefense += data.BonusMDefense;
		template.speed += data.BonusSpeed;
		template.name = name;

		ArrayList<Pair<Integer, IAbility>> abilities = new ArrayList<Pair<Integer,IAbility>>();
		for(String s : data.Abilities)
		{
			IAbility ability = AbilityLookup.Instance.GetAbilityWithName(s);
			if(ability != null)
			{
				abilities.add(new Pair<Integer, IAbility>(1, ability));
			}
		}

		int primaryJobLevel = data.GetJobLevelMin1(data.CurrentJob);
		List<IAbility> jobAbilities = JobLookup.Instance.GetJobAbilities(data.CurrentJob, primaryJobLevel, true, false);
		for(IAbility ability : jobAbilities)
		{
			abilities.add(new Pair<Integer, IAbility>(1, ability));
		}
		
		if(!data.SecondaryJob.isEmpty())
		{
			int secondaryJobLevel = data.GetJobLevelMin1(data.SecondaryJob);
			List<IAbility> secondaryJobAbilities = JobLookup.Instance.GetJobAbilities(data.SecondaryJob, secondaryJobLevel, false, false);
			for(IAbility ability : secondaryJobAbilities)
			{
				abilities.add(new Pair<Integer, IAbility>(1, ability));
			}
		}
		
		Pair<Integer, IAbility>[] asArray = new Pair[abilities.size()];
		abilities.toArray(asArray);
		template.abilities = asArray;
		return template;
	}
}
