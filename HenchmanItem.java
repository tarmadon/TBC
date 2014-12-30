package TBC;

import java.util.List;

import TBC.Combat.CombatEntityLookup;
import TBC.Combat.CombatEntityTemplate;
import TBC.Combat.EquippedItemManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
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
    	if(item.hasTagCompound() && item.getTagCompound().hasKey("TBCIsInParty"))
    	{
    		CombatEntitySaveData d = HenchmanItem.GetCombatEntitySaveData(item);
    		d.IsInParty = 0;
    		HenchmanItem.SetCombatEntitySaveData(d, item);
    	}
    	
    	return super.onDroppedByPlayer(item, player);
    }
    
    public static void SetItem(int slot, ItemStack item, ItemStack hench)
    {
    	if(!hench.hasTagCompound())
    	{
    		hench.setTagCompound(new NBTTagCompound());
    	}
    	
    	EquippedItemManager.Instance.SetItem(slot, item, hench.getTagCompound());
    }
    
    public static ItemStack[] GetItems(ItemStack item)
    {
    	if(item.hasTagCompound())
    	{
    		return EquippedItemManager.Instance.GetEquippedItems(item.getTagCompound());
    	}
    	
    	return new ItemStack[5];
    }
    
    public static NBTTagCompound GetTag(ItemStack item)
    {
    	return item.getTagCompound();
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) 
    {
    	if(stack.getItem() instanceof HenchmanItem)
    	{
    		CombatEntitySaveData d = HenchmanItem.GetCombatEntitySaveData(stack);
    		if(!d.Name.isEmpty())
    		{
    			return d.Name.trim() + " Link";
    		}
    	}
    	
    	return super.getItemStackDisplayName(stack);
    }
    
    public static void SetCombatEntitySaveData(CombatEntitySaveData data, ItemStack hench)
    {
    	if(!hench.hasTagCompound())
    	{
    		hench.setTagCompound(new NBTTagCompound());
    	}
    	
    	data.saveNBTData(hench.getTagCompound());
    }
    
    public static CombatEntitySaveData GetCombatEntitySaveData(ItemStack hench)
    {
    	CombatEntitySaveData data = new CombatEntitySaveData();
    	if(!hench.hasTagCompound() || !hench.getTagCompound().hasKey("TBCIsInParty"))
    	{
        	HenchmanItem item = (HenchmanItem)hench.getItem();
        	CombatEntitySaveData d;
        	if(item.henchmanName == "Adventurer")
        	{
        		d = new CombatEntitySaveData();
        		d.loadNBTData(new NBTTagCompound());
        		d.CurrentJob = "Adventurer";
        		d.JobLevels.add(new Pair<String, Integer>("Adventurer", 1));
            	d.Level = 1;
        	}
        	else
        	{
        		CombatEntityTemplate t = CombatEntityLookup.Instance.lookupByName.get(item.henchmanName);
            	d = new CombatEntitySaveData(t);
        		d.CurrentJob = "Monster";
        		d.JobLevels.add(new Pair<String, Integer>("Monster", 1));
        		d.Level = 1;
        	}
        	
        	HenchmanItem.SetCombatEntitySaveData(d, hench);
    	}
    	
    	data.loadNBTData(hench.getTagCompound());
    	return data;
    }
}
