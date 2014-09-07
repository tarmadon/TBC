package TBC;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class SetItemDataHandler implements IPacketHandler 
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		String s = new String(packet.data);
		String[] s2 = s.split(",");
		int asSlot = new Integer(s2[0]);
		int asDamage = new Integer(s2[1]);
		int asMp = new Integer(s2[2]);
		EntityPlayer entityPlayer = (EntityPlayer)player;
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
	}
}
