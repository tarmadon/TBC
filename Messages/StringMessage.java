package TBC.Messages;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class StringMessage implements IMessage
{
	public String Data;
	
	public StringMessage()
	{
	}
	
	public StringMessage(String data)
	{
		Data = data;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		Data = new String(bytes).trim();
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		if(Data != null)
		{
			buf.writeBytes(Data.getBytes());
		}
	}
}
