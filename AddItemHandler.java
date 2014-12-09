package TBC;

import TBC.Combat.Abilities.RemoveItemAbility;
import TBC.Messages.NBTTagCompoundMessage;
import TBC.Messages.StringMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class AddItemHandler implements IMessageHandler<NBTTagCompoundMessage, IMessage>
{
	public IMessage onMessage(NBTTagCompoundMessage message, MessageContext ctx) 
	{
		ItemStack itemToAdd = ItemStack.loadItemStackFromNBT(message.tag);
		itemToAdd.stackSize = 1;
		ctx.getServerHandler().playerEntity.inventory.addItemStackToInventory(itemToAdd);
		return null;
	}
}
