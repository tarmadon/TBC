package TBC;

import TBC.Messages.StringMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class OpenGuiHandler implements IMessageHandler<StringMessage, IMessage>
{
	@Override
	public IMessage onMessage(StringMessage message, MessageContext ctx) 
	{
		EntityPlayer playerEntity = Minecraft.getMinecraft().thePlayer;
		playerEntity.openGui(TBCMod.instance, 0, playerEntity.worldObj, playerEntity.serverPosX, playerEntity.serverPosY, playerEntity.serverPosZ);
		return null;
	}
}
