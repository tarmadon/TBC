package TBC.CombatScreen;

import TBC.MainMod;
import TBC.Messages.CombatSyncDataMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class SyncCombatDataHandler implements IMessageHandler<CombatSyncDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(CombatSyncDataMessage message, MessageContext ctx) 
	{
		MainMod.ClientBattle.SyncCombatData(message);
		return null;
	}

}
