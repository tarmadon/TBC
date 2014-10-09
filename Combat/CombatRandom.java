package TBC.Combat;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class CombatRandom
{
	private static Random random = new Random();

	public static Random GetRandom()
	{
		return random;
	}
}
