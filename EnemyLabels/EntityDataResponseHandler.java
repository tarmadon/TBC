package TBC.EnemyLabels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import TBC.Messages.StringMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class EntityDataResponseHandler implements IMessageHandler<StringMessage, IMessage>
{
	@Override
	public IMessage onMessage(StringMessage message, MessageContext ctx) 
	{
		String data = new String(message.Data);
		String[] params = data.split(",");
		Integer entityId = new Integer(params[0]);
		if(params.length == 3)
		{
			Entity e = Minecraft.getMinecraft().theWorld.getEntityByID(entityId);
			if(e != null)
			{
				e.getEntityData().setString(params[1], params[2]);
			}
		}
		
		return null;
	}
}
