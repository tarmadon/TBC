package TBC;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet100OpenWindow;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import TBC.CombatScreen.BattleScreen;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.network.OpenGuiPacket;

public class TBCGuiHandler implements IGuiHandler
{
	public Object getServerGuiElement(int ID, EntityPlayer playerBase, World world, int x, int y, int z) 
	{
		if(ID == 0)
		{
			NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(FMLCommonHandler.instance().findContainerFor(TBCMod.instance));
			EntityPlayerMP player = (EntityPlayerMP)playerBase;
        	Packet250CustomPayload pkt = new Packet250CustomPayload();
        	pkt.channel = "FML";
        	byte[] packetData = new OpenGuiPacket().generatePacket(0, nmh.getNetworkId(), 0, x, y, z);
        	pkt.data = Bytes.concat(new byte[] { UnsignedBytes.checkedCast(4) }, packetData );
        	pkt.length = pkt.data.length;
        	player.playerNetServerHandler.sendPacketToPlayer(pkt);
		}
		else if(ID == 1)
		{
        	return new ContainerForStatsGui(playerBase.inventory);
		}
		
		return null;
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		if(ID == 0)
		{
			if(MainMod.setEnemies != null)
			{
				BattleScreen screen = new BattleScreen(player, MainMod.setEnemies, MainMod.isPlayerAttacker);
				return screen;
			}
			
			return new BattleScreen(player, MainMod.enemy, MainMod.isPlayerAttacker);
		}
		else
		{
			StatsGui gui = new StatsGui(player, new ContainerForStatsGui(player.inventory));
//			NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(FMLCommonHandler.instance().findContainerFor(MasterMod.instance));
//        	Packet250CustomPayload pkt = new Packet250CustomPayload();
//        	pkt.channel = "FML";
//        	byte[] packetData = new OpenGuiPacket().generatePacket(gui.inventorySlots.windowId, nmh.getNetworkId(), 1, x, y, z);
//        	pkt.data = Bytes.concat(new byte[] { UnsignedBytes.checkedCast(4) }, packetData );
//        	pkt.length = pkt.data.length;
//        	
//        	Minecraft.getMinecraft().getNetHandler().addToSendQueue(new Packet100OpenWindow(gui.inventorySlots.windowId, 1, gui.inventorySlots.getInvName(), par1IInventory.getSizeInventory(), par1IInventory.isInvNameLocalized()));
        	return gui;
		}
	}
}