package TBC.CombatScreen;

import TBC.MainMod;
import TBC.Messages.CombatEndedMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class EndCombatHandler implements IMessageHandler<CombatEndedMessage, IMessage>
{
	@Override
	public IMessage onMessage(CombatEndedMessage message, MessageContext ctx) 
	{
		MainMod.ClientBattle.SetEndOfCombat(message);
		return null;
	}
}
