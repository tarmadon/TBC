package TBC.Combat;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import TBC.CombatEntitySaveData;
import TBC.Pair;
import TBC.Combat.Abilities.AbilityLookup;
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

	public Pair<Integer, ICombatAbility>[] abilities;
	
	public CombatEntityTemplate()
	{
	}
	
	public CombatEntityTemplate(String name, CombatEntitySaveData data)
	{
		this.maxHp = data.MaxHP;
		this.maxMp = data.MaxMP;
		this.attack = data.Attack;
		this.defense = data.Defense;
		this.mAttack = data.MAttack;
		this.mDefense = data.MDefense;
		this.speed = data.Speed;
		this.name = name;

		ArrayList<Pair<Integer, ICombatAbility>> abilities = new ArrayList<Pair<Integer,ICombatAbility>>();
		for(String s : data.Abilities)
		{
			ICombatAbility ability = AbilityLookup.Instance.GetAbilityWithName(s);
			if(ability != null)
			{
				abilities.add(new Pair<Integer, ICombatAbility>(1, ability));
			}
		}

		Pair<Integer, ICombatAbility>[] asArray = new Pair[abilities.size()];
		abilities.toArray(asArray);
		this.abilities = asArray;
	}
}
