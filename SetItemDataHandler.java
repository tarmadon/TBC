package TBC;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class SetItemDataHandler implements IMessageHandler<StringMessage, IMessage>
{
	@Override
	public IMessage onMessage(StringMessage message, MessageContext ctx) 
	{
		String s = message.Data;
		String[] s2 = s.split(",");
		int asSlot = new Integer(s2[0]);
		int asDamage = new Integer(s2[1]);
		int asMp = new Integer(s2[2]);
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
		NBTTagCompound existingTag = stack.getTagCompound();
		if(existingTag == null)
		{
			existingTag = new NBTTagCompound();
		}

		existingTag.setInteger("HenchMP", asMp);
		stack.setTagCompound(existingTag);
		return null;
	}
}
