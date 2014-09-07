package TBC.EnemyLabels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class EntityDataResponseHandler implements IPacketHandler 
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		String data = new String(packet.data);
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
	}
}
