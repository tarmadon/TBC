package TBC;

import TBC.Combat.Abilities.RemoveItemAbility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class RemoveItemHandler implements IPacketHandler
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player basePlayer) 
	{
		String s = new String(packet.data);
		String[] s2 = s.split(",");
		Pair<Integer, Integer> inventoryItemPosition = new Pair(new Integer(s2[0]), new Integer(s2[1]));
		int damage = new Integer(s2[2]);
		EntityPlayer player = (EntityPlayer)basePlayer;
		
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
	}
}
