package TBC.Messages;

import java.io.Serializable;
import java.util.ArrayList;

import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;

public class CombatCommandMessage extends ObjectMessage implements Serializable
{
	public Long BattleId;
	public ICombatAbility AbilityToUse;
	public Integer User;
	public ArrayList<Integer> Targets;
	
	@Override
	public void ReadMessage(Object message) 
	{
		if(message instanceof CombatCommandMessage)
		{
			CombatCommandMessage m = (CombatCommandMessage)message;
			BattleId = m.BattleId;
			AbilityToUse = m.AbilityToUse;
			User = m.User;
			Targets = m.Targets;
		}
	} 
}
