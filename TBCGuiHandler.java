package TBC;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import TBC.CombatScreen.BattleScreenClient;
import TBC.CombatScreen.StartCombatHandler;
import TBC.Menu.StatsGui;
import TBC.Messages.StringMessage;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.internal.FMLMessage;
import cpw.mods.fml.relauncher.Side;

public class TBCGuiHandler implements IGuiHandler
{
	public Object getServerGuiElement(int ID, EntityPlayer playerBase, World world, int x, int y, int z)
	{
		if(ID == 0)
		{
			MainMod.openGuiHandler.sendTo(new StringMessage(), (EntityPlayerMP)playerBase);
//			EntityPlayerMP entityPlayerMP = (EntityPlayerMP)playerBase;
//			entityPlayerMP.getNextWindowId();
//            entityPlayerMP.closeContainer();
//            int windowId = entityPlayerMP.currentWindowId;
//            ModContainer mc = FMLCommonHandler.instance().findContainerFor(TBCMod.instance);
//            OpenGUIMessageCopy openGui = new OpenGUIMessageCopy(windowId, mc.getModId(), ID, x, y, z);
//            EmbeddedChannel embeddedChannel = NetworkRegistry.INSTANCE.getChannel("FML", Side.SERVER);
//            embeddedChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.PLAYER);
//            embeddedChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entityPlayerMP);
//            embeddedChannel.writeOutbound(openGui);
			
//			NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(FMLCommonHandler.instance().findContainerFor(TBCMod.instance));
//			EntityPlayerMP player = (EntityPlayerMP)playerBase;
//        	Packet250CustomPayload pkt = new Packet250CustomPayload();
//        	pkt.channel = "FML";
//        	byte[] packetData = new OpenGuiPacket().generatePacket(0, nmh.getNetworkId(), 0, x, y, z);
//        	pkt.data = Bytes.concat(new byte[] { UnsignedBytes.checkedCast(4) }, packetData );
//        	pkt.length = pkt.data.length;
//        	player.playerNetServerHandler.sendPacketToPlayer(pkt);
		}
		else if(ID == 1)
		{
        	return null;
		}

		return null;
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID == 0)
		{
			BattleScreenClient screen = new BattleScreenClient(StartCombatHandler.CombatId, StartCombatHandler.Allies, StartCombatHandler.Enemies);
			MainMod.ClientBattle = screen;
			return screen;
		}
		else
		{
			StatsGui gui = new StatsGui(player);
        	return gui;
		}
	}
}