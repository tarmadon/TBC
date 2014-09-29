package TBC;

import TBC.Combat.Abilities.RemoveItemAbility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class RemoveItemHandler implements IMessageHandler<StringMessage, IMessage>
{
	public IMessage onMessage(StringMessage message, MessageContext ctx) 
	{
		String s = message.Data;
		String[] s2 = s.split(",");
		Pair<Integer, Integer> inventoryItemPosition = new Pair(new Integer(s2[0]), new Integer(s2[1]));
		int damage = new Integer(s2[2]);
		
		EntityPlayer player = (EntityPlayer)ctx.getServerHandler().playerEntity;

		if(inventoryItemPosition.item1 == RemoveItemAbility.MainInventory)
		{
			ItemStack stack = player.inventory.mainInventory[inventoryItemPosition.item2];
			if(damage == -1)
			{
				if(stack.stackSize > 1)
				{
					stack.stackSize = stack.stackSize - 1;
				}
				else
				{
					player.inventory.setInventorySlotContents(inventoryItemPosition.item2, (ItemStack)null);
				}
			}
			else
			{
				stack.damageItem(damage, player);
			}
		}
		else
		{
			ItemStack stack = player.inventory.armorInventory[inventoryItemPosition.item2];
			if(damage == -1)
			{
				if(stack.stackSize > 1)
				{
					stack.stackSize = stack.stackSize - 1;
				}
				else
				{
					player.inventory.armorInventory[inventoryItemPosition.item2] = null;
				}
			}
			else
			{
				player.inventory.armorInventory[inventoryItemPosition.item2].damageItem(damage, player);
			}
		}
		
		return null;
	}
}
