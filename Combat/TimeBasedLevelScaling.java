package TBC.Combat;

import net.minecraft.entity.EntityLiving;

public class TimeBasedLevelScaling implements ILevelScale 
{
	private int increment;
	
	public TimeBasedLevelScaling(int increment)
	{
		this.increment = increment;
	}
	
	public int GetCurrentLevel(EntityLiving entity) 
	{
		if(entity.worldObj.difficultySetting != 3)
		{
			return 0;
		}
		
		return (int)(entity.worldObj.getTotalWorldTime() / this.increment);
	}
}
