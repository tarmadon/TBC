package TBC.Messages;

import java.io.Serializable;

import TBC.CombatEntitySaveData;
import net.minecraft.nbt.NBTTagCompound;

public class CombatEndedMessage extends ObjectMessage implements Serializable
{
	public boolean Won;
	public boolean GainedLevel;
	public boolean GainedSkill;
	public Integer XPGained;
	public Integer APGained;
	
	@Override
	public void ReadMessage(Object message) 
	{
		if(message instanceof CombatEndedMessage)
		{
			CombatEndedMessage m = (CombatEndedMessage)message; 
			Won = m.Won;
			GainedLevel = m.GainedLevel;
			XPGained = m.XPGained;
			APGained = m.APGained;
		}
	}
}
