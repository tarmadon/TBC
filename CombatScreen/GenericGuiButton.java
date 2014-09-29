package TBC.CombatScreen;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

public class GenericGuiButton extends GuiButton {
	private IGenericAction onClick;

	public GenericGuiButton(int par1, int par2, int par3, int par4, int par5, String par6Str, IGenericAction onClick)
	{
		super(par1, par2, par3, par4, par5, par6Str);
		this.enabled = true;
		this.onClick = onClick;
	}

	public void onClick()
	{
		this.onClick.Invoke();
	}
}
