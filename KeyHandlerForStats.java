package TBC;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class KeyHandlerForStats extends KeyHandler
{
	public boolean wasKeyPressed = false;
	private Object parentMod;
	
	public KeyHandlerForStats(Object parentMod) 
	{
		super(new KeyBinding[]{new KeyBinding("Open Character Screen", Keyboard.KEY_TAB)},
			  new boolean[]{true});
		this.parentMod = parentMod;
	}

	public String getLabel() 
	{
		return "Open Character Screen";
	}

	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT);
	}

	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) 
	{
		wasKeyPressed = false;
	}

	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) 
	{
		if(wasKeyPressed)
		{
			return;
		}
		
		wasKeyPressed = true;
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.thePlayer != null)
		{
			if(mc.currentScreen instanceof StatsGui)
			{
				mc.thePlayer.closeScreen();
				mc.displayGuiScreen((GuiScreen)null);
				return;
			}
			
			EntityPlayer player = mc.thePlayer;
			player.openGui(this.parentMod, 1, player.worldObj, player.serverPosX, player.serverPosY, player.serverPosZ);
		}
	}
}