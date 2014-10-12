package TBC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import TBC.Messages.NBTTagCompoundMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ChunkLoader;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class SyncPlayerDataHandler implements IMessageHandler<NBTTagCompoundMessage, NBTTagCompoundMessage>
{
	@Override
	public NBTTagCompoundMessage onMessage(NBTTagCompoundMessage message, MessageContext ctx) 
	{
		EntityPlayerMP playerEntity = (EntityPlayerMP)ctx.getServerHandler().playerEntity;
		if(message.tag != null)
		{
			NBTTagCompound tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			MergeTagCompound(message.tag, tag);
			playerEntity.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
		}

		NBTTagCompoundMessage replyMessage = new NBTTagCompoundMessage();
		replyMessage.tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		return replyMessage;
	}
	
	public static void MergeTagCompound(NBTTagCompound newTag, NBTTagCompound existingTag)
	{
		for(Object keyAsObject : newTag.func_150296_c())
		{
			String key = (String)keyAsObject;
			if(key.startsWith("TBC"))
			{
				if(key.equals("TBCAbilities"))
				{
					existingTag.setString(key, newTag.getString(key));
				}
				else
				{
					existingTag.setInteger(key, newTag.getInteger(key));
				}
			}
		}
	}
}
