package TBC.CombatScreen;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class DisplayEntityPlayer extends AbstractClientPlayer
{
	public DisplayEntityPlayer(World p_i45324_1_, GameProfile p_i45324_2_) 
	{
		super(p_i45324_1_, p_i45324_2_);
	}

	@Override
	public void addChatMessage(IChatComponent p_145747_1_) 
	{
		return;
	}

	@Override
	public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_) 
	{
		return false;
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() 
	{
		return null;
	}
}
