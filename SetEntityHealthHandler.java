package TBC;

import TBC.Messages.StringMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ChunkLoader;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class SetEntityHealthHandler implements IMessageHandler<StringMessage, IMessage>
{
	@Override
	public IMessage onMessage(StringMessage message, MessageContext ctx) 
	{
		EntityPlayerMP entityPlayer = ctx.getServerHandler().playerEntity;
		String s = message.Data;
		int asHealthValue = new Integer(s);
		int mpValue = 0;
		entityPlayer.setHealth(asHealthValue);
		return null;
	}
}
