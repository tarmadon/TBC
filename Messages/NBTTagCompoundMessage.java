package TBC.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class NBTTagCompoundMessage implements IMessage
{
	public NBTTagCompound tag = null; 
	
	public NBTTagCompoundMessage()
	{
	}
	
	public NBTTagCompoundMessage(NBTTagCompound data)
	{
		tag = data;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		
		try {
			tag = CompressedStreamTools.read(new DataInputStream(new ByteArrayInputStream(data)));
		} catch (IOException e) {}
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			CompressedStreamTools.write(tag, new DataOutputStream(outputStream));
		} catch (IOException e) {}
		
		buf.writeBytes(outputStream.toByteArray());
	}

}
