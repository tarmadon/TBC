package TBC.CombatScreen;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import TBC.MainMod;
import TBC.TBCMod;
import TBC.Combat.CombatEntity;
import TBC.Messages.CombatStartedMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class StartCombatHandler implements IMessageHandler<CombatStartedMessage, IMessage>
{
	public static long CombatId;
	public static ArrayList<CombatEntity> Allies;
	public static ArrayList<CombatEntity> Enemies;
	
	@Override
	public IMessage onMessage(CombatStartedMessage message, MessageContext ctx) 
	{
		CombatId = message.CombatId;
		Allies = message.Allies;
		Enemies = message.Enemies;
		
		EntityPlayer playerEntity = Minecraft.getMinecraft().thePlayer;
		playerEntity.openGui(TBCMod.instance, 0, playerEntity.worldObj, playerEntity.serverPosX, playerEntity.serverPosY, playerEntity.serverPosZ);
		return null;
	}
}
