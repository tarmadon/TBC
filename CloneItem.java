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
		private IIcon icon;

	public CloneItem(Item originalItem)
	{
		this.originalItem = originalItem;
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
}
