package TBC.CombatScreen;

import org.apache.logging.log4j.Level;

import TBC.MainMod;
import TBC.Messages.CombatPlayerControlMessage;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PlayerControlHandler implements IMessageHandler<CombatPlayerControlMessage, IMessage>
{
	@Override
	public IMessage onMessage(CombatPlayerControlMessage message, MessageContext ctx) 
	{
		for(int i = 0; i<10; i++)
		{
			if(MainMod.ClientBattle != null)
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
		
		if(MainMod.ClientBattle == null)
		{
			FMLLog.log(Level.ERROR, "Could not find battle to set player control");
		}
		
		MainMod.ClientBattle.SetPlayerControl(message.Active);
		return null;
	}
}
