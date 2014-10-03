package TBC.ZoneGeneration;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.apache.logging.log4j.Level;

import com.ibm.icu.impl.ZoneMeta;
import TBC.StringMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class ZoneDataRequestHandler implements IMessageHandler<StringMessage, ZoneDataMessage>
{
	@Override
	public ZoneDataMessage onMessage(StringMessage message, MessageContext ctx) 
	{
		String data = message.Data;
		String[] params = data.split(",");
		if(params.length != 2)
		{
			FMLLog.log(Level.WARN, "Invalid request for zone data.");
			return null;
		}

		Integer xCoord = new Integer(params[0]);
		Integer zCoord = new Integer(params[1]);
		HashMap<Integer, ZoneChunkData> zoneData = ZoneHandler.Instance.GetRegionDataForAllBiomes(new ChunkCoordIntPair(xCoord, zCoord));
		if(zoneData != null)
		{
			ZoneResponseData responseData = new ZoneResponseData();
			responseData.ChunkXPos = xCoord;
			responseData.ChunkZPos = zCoord;
			responseData.ZoneData = zoneData;

			ZoneDataMessage response = new ZoneDataMessage();
			response.Data = responseData;
			return response;
		}
		
		return null;
	}
}
