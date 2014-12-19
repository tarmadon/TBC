package TBC;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class BattleEntity extends Entity
{
	private final double velocityRatio = 0;
	private int numUpdates = 0; 
	private long combatId;
	
	public BattleEntity(World world) 
	{
		super(world);
		this.setSize(1.0F, 1.0F);
	}
	
	public BattleEntity(World world, long combatId) 
	{
		this(world);
		this.combatId = combatId;
	}
	
	@Override
	public boolean canBeCollidedWith() 
	{
		// This controls whether the game recognizes this as in front of you (so you can activate it).  Do not turn off.
		return true;
	};
	
	@Override
	protected void entityInit() 
	{
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_) 
	{
		this.combatId = p_70037_1_.getLong("combatId");
		if(!this.worldObj.isRemote && !MainMod.ServerBattles.containsKey(this.combatId))
		{
			this.kill();
		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_) 
	{
		p_70014_1_.setLong("combatId", this.combatId);
	}
	
	@Override
    public boolean interactFirst(EntityPlayer p_130002_1_)
    {
		if(!this.worldObj.isRemote)
		{
			if(MainMod.ServerBattles.containsKey(this.combatId))
			{
				MainMod.ServerBattles.get(this.combatId).AddPlayerToCombat((EntityPlayerMP)p_130002_1_);
			}
		}
		
		return true;
    }
	
	@Override
	public boolean shouldRenderInPass(int pass) 
	{
		return false;
	}

	@Override
	public void onUpdate() 
	{
		if(!this.worldObj.isRemote && !MainMod.ServerBattles.containsKey(this.combatId))
		{
			this.kill();
		}
		
		super.onUpdate();
		if(numUpdates == 4)
		{
			if(this.worldObj.isRemote)
			{
				this.worldObj.spawnParticle("largeexplode", this.posX + .5, this.posY + .5, this.posZ + .5, this.rand.nextGaussian() * velocityRatio, this.rand.nextGaussian() * velocityRatio, this.rand.nextGaussian() * velocityRatio);
			}
			
			numUpdates = 0;
		}
		else
		{
			numUpdates++;
		}
	}
}
