package TBC;

import java.util.Random;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BattleEntity extends Entity
{
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
			FMLLog.log(Level.ERROR, "Battle clicked: " + this.combatId);
		}
		
		return true;
    }
	
	@Override
	public boolean shouldRenderInPass(int pass) 
	{
		return false;
	}

	private int numUpdates = 0; 
	private double velocityRatio = 0.05;
	
	@Override
	public void onUpdate() 
	{
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
