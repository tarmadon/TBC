package TBC;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import TBC.Messages.NBTTagCompoundMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class SyncPlayerDataHandlerClient implements IMessageHandler<NBTTagCompoundMessage, NBTTagCompoundMessage>
{
	@Override
	public NBTTagCompoundMessage onMessage(NBTTagCompoundMessage message, MessageContext ctx) 
	{
		if(message.tag != null)
		{
			EntityPlayer playerEntity = Minecraft.getMinecraft().thePlayer;
			NBTTagCompound c = message.tag;
			NBTTagCompound tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			SyncPlayerDataHandler.MergeTagCompound(c, tag);
			playerEntity.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
			MainMod.playerDataInit = true;
		}
		
		return null;
	}
}
