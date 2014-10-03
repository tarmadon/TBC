package TBC.Combat;

import TBC.ZoneGeneration.ZoneHandler;
import TBC.ZoneGeneration.ZoneChunkData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class AreaBasedLevelScaling implements ILevelScale
{
	public int GetCurrentLevel(EntityLivingBase entity)
	{
		int areaLevel = 0;
		if(ZoneHandler.Instance.GetRegionDataForAllBiomes(new ChunkCoordIntPair((int)entity.posX >> 4, (int)entity.posZ >> 4)) != null)
		{
			World world = entity.worldObj;
			Chunk c = entity.worldObj.getChunkFromBlockCoords((int)entity.posX, (int)entity.posZ);
			BiomeGenBase biome = c.getBiomeGenForWorldCoords((int)entity.posX & 15, (int)entity.posZ & 15, world.getWorldChunkManager());
			ZoneChunkData data = ZoneHandler.Instance.GetRegionData(c.getChunkCoordIntPair(), biome);
			if(data != null)
			{
				areaLevel = data.AreaLevel;
			}
		}

		return areaLevel;
	}
}
