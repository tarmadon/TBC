package TBC;

import TBC.Messages.ItemDataMessage;
import TBC.Messages.StringMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class SetItemDataHandler implements IMessageHandler<ItemDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(ItemDataMessage message, MessageContext ctx) 
	{
		int asSlot = message.Slot;
		int asDamage = message.ItemDurability;
		EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
		ItemStack stack;
		if(asSlot == -1)
		{
			stack = ((Slot)entityPlayer.openContainer.inventorySlots.get(4)).getStack();
		}
		else
		{
			stack = entityPlayer.inventory.mainInventory[asSlot];
		}

		stack.setItemDamage(asDamage);
		if(!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		TagCompoundExt.MergeTagCompound(message.tag, stack.getTagCompound());
		return null;
	}
}
