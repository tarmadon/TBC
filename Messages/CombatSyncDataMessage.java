package TBC.Messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import TBC.Pair;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;

public class CombatSyncDataMessage extends ObjectMessage implements Serializable
{
	public Pair<Integer, Integer> User;
	public ICombatAbility AbilityUsed;
	public ArrayList<String> Messages;
	public ArrayList<Pair<Integer, Integer>> Targets;
	public ArrayList<CombatEntity> Allies;
	public ArrayList<CombatEntity> Enemies;
	
	@Override
	public void ReadMessage(Object message) 
	{
		if(message instanceof CombatSyncDataMessage)
		{
			CombatSyncDataMessage m = (CombatSyncDataMessage)message; 
			User = m.User;
			AbilityUsed = m.AbilityUsed;
			Messages = m.Messages;
			Targets = m.Targets;
			Allies = m.Allies;
			Enemies = m.Enemies;
		}
	}
}
