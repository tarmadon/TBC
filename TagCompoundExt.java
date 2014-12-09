package TBC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;

public class TagCompoundExt 
{
	public static byte[] AsByteArray(NBTTagCompound tag)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			CompressedStreamTools.write(tag, new DataOutputStream(outputStream));
		} catch (IOException e) {}
		
		return outputStream.toByteArray();
	}
	
	public static NBTTagCompound AsTag(byte[] data)
	{
		try {
			return CompressedStreamTools.read(new DataInputStream(new ByteArrayInputStream(data)));
		} catch (IOException e) {}
		return null;
	}
	
	public static void MergeTagCompound(NBTTagCompound newTag, NBTTagCompound existingTag)
	{
		for(Object keyAsObject : newTag.func_150296_c())
		{
			String key = (String)keyAsObject;
			if(key.startsWith("TBC"))
			{
				NBTBase tag = newTag.getTag(key);
				if(tag instanceof NBTTagString)
				{
					existingTag.setString(key, newTag.getString(key));
				}
				else if(tag instanceof NBTTagInt)
				{
					existingTag.setInteger(key, newTag.getInteger(key));
				}
				else if(tag instanceof NBTTagCompound)
				{
					existingTag.setTag(key, newTag.getTag(key));
				}
			}
		}
	}
}
