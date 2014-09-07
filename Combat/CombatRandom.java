package TBC.Combat;

import java.util.Random;

import net.minecraft.client.Minecraft;

public class CombatRandom 
{
	private static Random random;
	
	public static Random GetRandom()
	{
		if(Minecraft.getMinecraft().thePlayer != null)
		{
			random = Minecraft.getMinecraft().thePlayer.getRNG();
		}
		else
		{
			random = new Random();
		}
		
		return random;
	}
}
