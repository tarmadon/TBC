package TBC.ZoneGeneration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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

public class ZoneDataResponseHandler implements IPacketHandler 
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		if(FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT)
		{
			return;
		}
		
		ByteArrayInputStream byteArrayStream = null;
		ObjectInputStream inputStream = null;
		ZoneResponseData response = null;
		try 
		{
			byteArrayStream = new ByteArrayInputStream(packet.data);
			inputStream = new ObjectInputStream(byteArrayStream);
			Object obj = inputStream.readObject();
			if(obj instanceof ZoneResponseData)
			{
				response = (ZoneResponseData)obj;
			}
		} 
		catch (IOException e) {} 
		catch (ClassNotFoundException e) {}
		finally
		{
			try 
			{
				inputStream.close();
				byteArrayStream.close();
			} catch (IOException e) {}
		}
		
		if(response != null)
		{
			ZoneHandler.ClientInstance.SetRegionDataForAllBiomes(new ChunkCoordIntPair(response.ChunkXPos,  response.ChunkZPos), response.ZoneData);
		}
	}
}
