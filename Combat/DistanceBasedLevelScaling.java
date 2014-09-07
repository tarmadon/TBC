package TBC.Combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class DistanceBasedLevelScaling implements ILevelScale 
{
	private int increment;
	
	public DistanceBasedLevelScaling(int increment)
	{
		this.increment = increment;
	}
	
	public int GetCurrentLevel(EntityLiving entity) 
	{
		return this.GetCurrentLevel((int)entity.posX, (int)entity.posZ);
	}
	
	public int GetCurrentLevel(int xCoord, int zCoord) 
	{
		//ChunkCoordinates spawnPoint = Minecraft.getMinecraft().theWorld.getSpawnPoint();
		ChunkCoordinates origin = new ChunkCoordinates(0, 0, 0);
		float sqrDistance = origin.getDistanceSquared(xCoord, origin.posY, zCoord);
		double distance = Math.sqrt(sqrDistance);
		return (int)(distance / this.increment);
	}
}
