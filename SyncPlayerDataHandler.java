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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
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
			NBTTagCompound tag = PlayerSaveData.GetPlayerTag(playerEntity);
			TagCompoundExt.MergeTagCompound(message.tag, tag);
			PlayerSaveData.SetPlayerTag(playerEntity, tag);
		}

		NBTTagCompoundMessage replyMessage = new NBTTagCompoundMessage();
		replyMessage.tag = PlayerSaveData.GetPlayerTag(playerEntity);
		return replyMessage;
	}
}
