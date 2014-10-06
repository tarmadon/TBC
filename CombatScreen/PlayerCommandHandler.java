package TBC.CombatScreen;

import org.apache.logging.log4j.Level;

import TBC.MainMod;
import TBC.Messages.CombatCommandMessage;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PlayerCommandHandler implements IMessageHandler<CombatCommandMessage, IMessage>
{
	@Override
	public IMessage onMessage(CombatCommandMessage message, MessageContext ctx) 
	{
		Battle b = null;
		for(int i = 0; i<10; i++)
		{
			b = MainMod.ServerBattles.get(message.BattleId);
			if(b != null)
			{
				break;
			}
			
			try 
			{
				Thread.sleep(1000);
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		if(b == null)
		{
			FMLLog.log(Level.ERROR, "Could not find battle to handle player command");
		}
		
		b.HandlePlayerCommand(message);
		return null;
	}
}
