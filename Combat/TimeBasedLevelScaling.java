package TBC.Combat;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.EnumDifficulty;

public class TimeBasedLevelScaling implements ILevelScale
{
	private int increment;

	public TimeBasedLevelScaling(int increment)
	{
		this.increment = increment;
	}

	public int GetCurrentLevel(EntityLivingBase entity)
	{
		if(entity.worldObj.difficultySetting != EnumDifficulty.HARD)
		{
			return 0;
		}

		return (int)(entity.worldObj.getTotalWorldTime() / this.increment);
	}
}
