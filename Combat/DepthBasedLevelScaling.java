package TBC.Combat;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class DepthBasedLevelScaling implements ILevelScale
{
	private int startsAt;
	private int increment;

	public DepthBasedLevelScaling(int startsAt, int increment)
	{
		this.startsAt = startsAt;
		this.increment = increment;
	}

	public int GetCurrentLevel(EntityLivingBase entity)
	{
		if(entity.posY < 64)
		{
			return (int)((this.startsAt - entity.posY) / this.increment);
		}

		return 0;
	}
}
