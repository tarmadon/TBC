package TBC.Combat;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import TBC.Pair;
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
}
