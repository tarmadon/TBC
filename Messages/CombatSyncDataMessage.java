package TBC.Messages;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import TBC.Pair;
import TBC.TagCompoundExt;
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
	public void toBytes(ByteBuf buf) 
	{
		for(CombatEntity ally : Allies)
		{
			ally.tagAsData = TagCompoundExt.AsByteArray(ally.tag);
		}
		
		for(CombatEntity enemy : Enemies)
		{
			enemy.tagAsData = TagCompoundExt.AsByteArray(enemy.tag);
		}
		
		super.toBytes(buf);
	}
	
	@Override
	public void ReadMessage(Object message) 
	{
		if(message instanceof CombatSyncDataMessage)
		{
			CombatSyncDataMessage m = (CombatSyncDataMessage)message;
			for(CombatEntity ally : m.Allies)
			{
				ally.tag = TagCompoundExt.AsTag(ally.tagAsData);
			}
			
			for(CombatEntity enemy : m.Enemies)
			{
				enemy.tag = TagCompoundExt.AsTag(enemy.tagAsData);
			}
			
			User = m.User;
			AbilityUsed = m.AbilityUsed;
			Messages = m.Messages;
			Targets = m.Targets;
			Allies = m.Allies;
			Enemies = m.Enemies;
		}
	}
}
