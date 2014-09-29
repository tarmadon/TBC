package TBC;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ContainerForStatsGui extends Container
{
	public class SlotArmor2 extends Slot
	{
		final int armorType;
		final Container parent;

		public SlotArmor2(Container par1ContainerPlayer, IInventory par2IInventory, int par3, int par4, int par5, int par6)
		{
			super(par2IInventory, par3, par4, par5);
			this.parent = par1ContainerPlayer;
			this.armorType = par6;
		}

		public int getSlotStackLimit()
		{
			return 1;
		}

		public boolean isItemValid(ItemStack par1ItemStack)
		{
			Item item = (par1ItemStack == null ? null : par1ItemStack.getItem());
			return item != null && item.isValidArmor(par1ItemStack, armorType, Minecraft.getMinecraft().thePlayer);
		}

		@SideOnly(Side.CLIENT)
		public IIcon getBackgroundIconIndex()
		{
			return ItemArmor.func_94602_b(this.armorType);
		}
	}

	public class DummySlot extends Slot
	{
		public DummySlot(IInventory par1iInventory)
		{
			super(par1iInventory, 0, -50, -50);
		}

		public ItemStack getStack()
		{
			return null;
		}

		public void putStack(ItemStack par1ItemStack)
		{
		}
	}

	public SlotChar CharSlot;
	private IInventory separateInv;

	public ContainerForStatsGui(InventoryPlayer par1InventoryPlayer)
	{
		this.separateInv = new InventoryBasic("TempInv", true, 1);
        int i;
        for(i = 0; i<4; i++)
        {
        	this.addSlotToContainer(new DummySlot(par1InventoryPlayer));
        }

        CharSlot = new SlotChar(separateInv, 0, 46, 5);
        this.addSlotToContainer(CharSlot);

        for (i = 0; i < 4; ++i)
        {
            this.addSlotToContainer(new SlotArmor2(this, par1InventoryPlayer, par1InventoryPlayer.getSizeInventory() - 1 - i, 176, 118 + i * 18, i));
        }

        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 118 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(par1InventoryPlayer, i, 8 + i * 18, 176));
        }
	}

	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		return null;
	}
}
