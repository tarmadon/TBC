package TBC;

import java.util.List;

import TBC.ContainerForStatsGui.SlotArmor2;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

public class HenchmanItem extends Item
{
	public String henchmanType;
	public String henchmanName;
	public IIcon icon;

	public HenchmanItem(String henchmanType, String henchmanName)
	{
		this.henchmanType = henchmanType;
		this.henchmanName = henchmanName;
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setMaxDamage(100);
		this.setMaxStackSize(1);
	}

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.icon = par1IconRegister.registerIcon("skull_skeleton");
    }

    @Override
    public IIcon getIconFromDamage(int par1)
    {
        return this.icon;
    }
    
    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) 
    {
    	if(item.hasTagCompound())
    	{
    		item.getTagCompound().setBoolean("TBCinParty", false);
    	}
    	
    	return super.onDroppedByPlayer(item, player);
    }
    
    public static void SetInParty(ItemStack item, boolean inParty)
    {
    	if(!item.hasTagCompound())
    	{
    		item.setTagCompound(new NBTTagCompound());
    	}
    	
    	item.getTagCompound().setBoolean("TBCinParty", inParty);
    }
    
    public static boolean IsInParty(ItemStack item)
    {
    	if(item.getTagCompound().hasKey("TBCinParty"))
    	{
    		return item.getTagCompound().getBoolean("TBCinParty");
    	}
    	
    	return false;
    }
    
    public static void SetItem(int slot, ItemStack item)
    {
    	if(!item.hasTagCompound())
    	{
    		item.setTagCompound(new NBTTagCompound());
    	}
    	
    	NBTTagCompound itemTag = new NBTTagCompound();
    	itemTag = item.writeToNBT(itemTag);
    	item.getTagCompound().setTag("TBCslot" + slot, itemTag);
    }
    
    public static ItemStack[] GetItems(ItemStack item)
    {
    	ItemStack[] equipped = new ItemStack[4];
    	for(int i = 0; i < 4; i++)
    	{
    		if(item.getTagCompound().hasKey("TBCslot" + i))
    		{
    			equipped[i] = ItemStack.loadItemStackFromNBT(item.getTagCompound().getCompoundTag("TBCslot" + i));
    		}
    	}
    	
    	return equipped;
    }
}
