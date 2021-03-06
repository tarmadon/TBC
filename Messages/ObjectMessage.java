package TBC.Messages;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.logging.log4j.Level;

import TBC.ZoneGeneration.ZoneResponseData;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public abstract class ObjectMessage implements IMessage
{
	public abstract void ReadMessage(Object message);
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		ByteArrayInputStream byteArrayStream = null;
		ObjectInputStream inputStream = null;
		ZoneResponseData response = null;
		try
		{
			byteArrayStream = new ByteArrayInputStream(bytes);
			inputStream = new ObjectInputStream(byteArrayStream);
			Object obj = inputStream.readObject();
			ReadMessage(obj); 
		}
		catch (IOException e) 
		{
			FMLLog.log(Level.ERROR, e.toString());
		}
		catch (ClassNotFoundException e) 
		{
			FMLLog.log(Level.ERROR, e.toString());
		}
		catch (Exception e)
		{
			FMLLog.log(Level.ERROR, e.toString());
		}
		finally
		{
			try
			{
				if(inputStream != null)
				{
					inputStream.close();
				}
				
				if(byteArrayStream != null)
				{
					byteArrayStream.close();
				}
			} catch (IOException e) {}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) 
	{
		ByteArrayOutputStream byteArrayStream = null;
		ObjectOutputStream outputStream = null;
		try
		{
			byteArrayStream = new ByteArrayOutputStream();
			outputStream = new ObjectOutputStream(byteArrayStream);
			outputStream.writeObject(this);
		} catch (IOException e) {
			FMLLog.log(Level.ERROR, e.toString());
		}
		catch(Exception e){}
		finally
		{
			try
			{
				outputStream.close();
				byteArrayStream.close();
			} catch (IOException e) {}
		}
		
		buf.writeBytes(byteArrayStream.toByteArray());
	}
}
