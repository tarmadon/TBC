package TBC;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerSaveData 
{
	public static NBTTagCompound GetPlayerTag(EntityPlayer entityPlayer)
	{
		return entityPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
	}
	
	public static void SetPlayerTag(EntityPlayer entityPlayer, NBTTagCompound tag)
	{
		entityPlayer.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
	}
}
