package TBC.Messages;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

import net.minecraft.nbt.NBTTagCompound;

public class ItemDataMessage extends NBTTagCompoundMessage implements Serializable
{
	public Integer ItemDurability;
	public Integer Slot;
	
	@Override
	public void fromBytes(ByteBuf buf) 
	{
		super.fromBytes(buf);
		this.ItemDurability = this.tag.getInteger("TempItemDur");
		this.Slot = this.tag.getInteger("TempSlot");
		this.tag.removeTag("TempItemDur");
		this.tag.removeTag("TempSlot");
	}
	
	@Override
	public void toBytes(ByteBuf buf) 
	{
		this.tag.setInteger("TempItemDur", this.ItemDurability);
		this.tag.setInteger("TempSlot", this.Slot);
		super.toBytes(buf);
	}
}
