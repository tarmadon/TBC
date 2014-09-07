package TBC.EnemyLabels;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class EntityDataRequestHandler implements IPacketHandler 
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		if(player instanceof EntityPlayerMP)
		{
			String data = new String(packet.data);
			String[] params = data.split(",");
			Integer entityId = new Integer(params[0]);
			String requestedParam = params[1];
			
			Entity requestedEntity = ((EntityPlayerMP) player).worldObj.getEntityByID(entityId);
			if(requestedEntity != null)
			{
				String value = requestedEntity.getEntityData().getString(requestedParam);
				EntityPlayerMP playerEntity = (EntityPlayerMP)player;
				playerEntity.playerNetServerHandler.sendPacketToPlayer(new Packet250CustomPayload("TBCResEData", (entityId + "," + requestedParam + "," + value).getBytes()));
			}
		}
	}
}
