package TBC.EnemyLabels;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import TBC.StringMessage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class EntityDataRequestHandler implements IMessageHandler<StringMessage, StringMessage>
{
	@Override
	public StringMessage onMessage(StringMessage message, MessageContext ctx) 
	{
		String data = new String(message.Data);
		String[] params = data.split(",");
		Integer entityId = new Integer(params[0]);
		String requestedParam = params[1];

		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		Entity requestedEntity = player.worldObj.getEntityByID(entityId);
		if(requestedEntity != null)
		{
			String value = requestedEntity.getEntityData().getString(requestedParam);
			EntityPlayerMP playerEntity = player;
			EnemyLabelMod.syncEnemyDataHandler.sendTo(new StringMessage(entityId + "," + requestedParam + "," + value), player);
		}
		
		return null;
	}
}
