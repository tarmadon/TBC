package TBC.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import net.minecraft.nbt.CompressedStreamTools;

import org.apache.logging.log4j.Level;

import TBC.TagCompoundExt;
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
	
	public void ReadMessage(Object message) 
	{
		if(message instanceof CombatStartedMessage)
		{
			CombatStartedMessage m = (CombatStartedMessage)message;
			for(CombatEntity ally : m.Allies)
			{
				ally.tag = TagCompoundExt.AsTag(ally.tagAsData);
			}
			
			for(CombatEntity enemy : m.Enemies)
			{
				enemy.tag = TagCompoundExt.AsTag(enemy.tagAsData);
			}
			
			CombatId = m.CombatId;
			Allies = m.Allies;
			Enemies = m.Enemies;
		}
	}
}
