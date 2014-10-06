package TBC.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import TBC.Combat.CombatEntity;
import TBC.ZoneGeneration.ZoneResponseData;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class CombatStartedMessage extends ObjectMessage implements Serializable
{
	public long CombatId;
	public ArrayList<CombatEntity> Allies;
	public ArrayList<CombatEntity> Enemies;

	public void ReadMessage(Object message) 
	{
		if(message instanceof CombatStartedMessage)
		{
			CombatStartedMessage m = (CombatStartedMessage)message; 
			CombatId = m.CombatId;
			Allies = m.Allies;
			Enemies = m.Enemies;
		}
	}
}
