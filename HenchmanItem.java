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

    public IIcon getIconFromDamage(int par1)
    {
        return this.icon;
    }
}
