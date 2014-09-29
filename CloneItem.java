package TBC;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class CloneItem extends Item
{
	private Item originalItem;
	private String description;
	private IIcon icon;

	public CloneItem(Item originalItem, String description)
	{
		this.originalItem = originalItem;
		this.description = description;
		this.setCreativeTab(originalItem.getCreativeTab());
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
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
