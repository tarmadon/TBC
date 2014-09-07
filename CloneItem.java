package TBC;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class CloneItem extends Item
{
	private Item originalItem;
	private String description;
	private Icon icon;
	
	public CloneItem(int par1, Item originalItem, String description) 
	{
		super(par1);
		this.originalItem = originalItem;
		this.description = description;
		this.setCreativeTab(originalItem.getCreativeTab());
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) 
	{
	}
	
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1) 
	{
		return this.originalItem.getIconFromDamage(par1);
	}
	
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) 
    {
    	super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
    	if(this.description != null)
    	{
    		par3List.add(this.description);
    	}
    }
}
