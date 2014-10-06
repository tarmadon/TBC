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

import TBC.Messages.ZoneDataMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class ZoneDataResponseHandler implements IMessageHandler<ZoneDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(ZoneDataMessage message, MessageContext ctx) 
	{
		if(message.Data != null)
		{
			ZoneHandler.Instance.SetRegionDataForAllBiomes(new ChunkCoordIntPair(message.Data.ChunkXPos,  message.Data.ChunkZPos), message.Data.ZoneData);
		}
		
		return null;
	}
}
