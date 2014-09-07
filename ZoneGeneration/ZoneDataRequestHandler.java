package TBC.ZoneGeneration;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.ChunkCoordIntPair;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class ZoneDataRequestHandler implements IPacketHandler 
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		if(FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER)
		{
			return;
		}
		
		if(player instanceof EntityPlayerMP)
		{
			String data = new String(packet.data);
			String[] params = data.split(",");
			if(params.length != 2)
			{
				FMLLog.log(Level.WARNING, "Invalid request for zone data.");
				return;
			}
			
			Integer xCoord = new Integer(params[0]);
			Integer zCoord = new Integer(params[1]);
			HashMap<Integer, ZoneChunkData> zoneData = ZoneHandler.ServerInstance.GetRegionDataForAllBiomes(new ChunkCoordIntPair(xCoord, zCoord));
			if(zoneData != null)
			{
				ZoneResponseData responseData = new ZoneResponseData();
				responseData.ChunkXPos = xCoord;
				responseData.ChunkZPos = zCoord;
				responseData.ZoneData = zoneData;
				
				ByteArrayOutputStream byteArrayStream = null;
				ObjectOutputStream outputStream = null;
				try 
				{
					byteArrayStream = new ByteArrayOutputStream();
					outputStream = new ObjectOutputStream(byteArrayStream);
					outputStream.writeObject(responseData);
					EntityPlayerMP playerEntity = (EntityPlayerMP)player;
					playerEntity.playerNetServerHandler.sendPacketToPlayer(new Packet250CustomPayload("TBCResZData", byteArrayStream.toByteArray()));
				} catch (IOException e) {
					FMLLog.log(Level.SEVERE, e.toString());
				}
				finally
				{
					try 
					{
						outputStream.close();
						byteArrayStream.close();
					} catch (IOException e) {}
				}
			}
		}
	}
}
