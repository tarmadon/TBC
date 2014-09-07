package TBC;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet40EntityMetadata;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ChunkLoader;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class SetEntityHealthHandler implements IPacketHandler 
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		EntityPlayerMP entityPlayer = (EntityPlayerMP)player;
		String s = new String(packet.data);
		int asHealthValue = new Integer(s);
		int mpValue = 0;
		entityPlayer.setEntityHealth(asHealthValue);
	}
}
