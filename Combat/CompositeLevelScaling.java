package TBC.Combat;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class CompositeLevelScaling implements ILevelScale
{
	private ILevelScale[] children;
	private Boolean useAverage;

	public CompositeLevelScaling(ILevelScale[] children, Boolean useAverage)
	{
		this.children = children;
		this.useAverage = useAverage;
	}

	public int GetCurrentLevel(EntityLivingBase entity)
	{
		int levelScaleTotal = 0;
		for(int i = 0; i<children.length; i++)
		{
			levelScaleTotal += children[i].GetCurrentLevel(entity);
		}

		if(useAverage)
		{
			levelScaleTotal = levelScaleTotal / this.children.length;
		}

		return levelScaleTotal;
	}
}
