package TBC.Messages;

import java.io.Serializable;

import TBC.Combat.CombatEntity;

public class CombatPlayerControlMessage extends ObjectMessage implements Serializable
{
	public Integer Active;
	
	@Override
	public void ReadMessage(Object message) 
	{
		if(message instanceof CombatPlayerControlMessage)
		{
			CombatPlayerControlMessage m = (CombatPlayerControlMessage)message; 
			Active = m.Active;
		}
	}
}
