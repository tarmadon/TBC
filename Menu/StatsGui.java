package TBC.Menu;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.Pair;
import TBC.Quintuplet;
import TBC.TBCMod;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.Combat.EquippedItem;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.LevelingEngine;
import TBC.Combat.Abilities.AbilityTargetType;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.StatChangeStatus;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.GenericGuiButton;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class StatsGui extends GuiInventory
{
	private class Mode
	{
		public static final int MAIN = 0;
		public static final int SUBSCREEN = 1;
		public static final int CUSTOM = 2;
	}
	
	public ArrayList<StatMenuCharData> partyMembers = new ArrayList<StatMenuCharData>();
	private float ySize_lo;
	private float xSize_lo;
	public int mode;
	public EntityPlayer player;
	private Pair<Integer, Integer> prevPosition = null;
	private long positionStartTime = 0;
	private ICustomMenuRender customRenderer;
	
	public StatsGui(EntityPlayer par1EntityPlayer)
	{
		super(par1EntityPlayer);
		mode = Mode.MAIN;
		player = par1EntityPlayer;
		xSize = 256;
		ySize = 199;
		RefreshCurrentPartyMembers();
	}

	public void initGui()
	{
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
		new SelectMainMenuFunction(this).Invoke();
	}

	public void drawScreen(int par1, int par2, float par3)
	{
        this.xSize_lo = (float)par1;
        this.ySize_lo = (float)par2;
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(par3, par1, par2);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            guibutton.drawButton(this.mc, par1, par2);
        }
        
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)this.guiLeft, (float)this.guiTop, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        short short1 = 240;
        short short2 = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)short1 / 1.0F, (float)short2 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        this.drawGuiContainerForegroundLayer(par1, par2);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
        
        for(Object b : this.buttonList)
        {
        	if(b instanceof GenericScrollBox)
        	{
        		GenericScrollBox scrollBox = (GenericScrollBox)b;
            	GenericScrollBoxCellData d = scrollBox.GetCellUnderMouse(par1, par2);
            	if(d != null && !d.HoverText.isEmpty())
            	{
            		ArrayList<String> toShow = new ArrayList<String>();
            		toShow.addAll(d.HoverText);
            		this.drawHoveringText(toShow, par1, par2, fontRendererObj);
            	}
        	}
        }
        
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
	}

	protected void mouseClicked(int par1, int par2, int par3)
	{
        if (par3 == 0)
        {
            for (int l = 0; l < this.buttonList.size(); ++l)
            {
                GuiButton guibutton = (GuiButton)this.buttonList.get(l);
                if (guibutton.mousePressed(this.mc, par1, par2))
                {
                    if(guibutton instanceof GenericGuiButton)
                    {
                    	GenericGuiButton generic = (GenericGuiButton)guibutton;
                    	generic.onClick();
                    }
                    else if(guibutton instanceof GenericScrollBox)
                    {
                    	GenericScrollBox generic = (GenericScrollBox)guibutton;
                    	generic.onClick(par1, par2);
                    }
                }
            }
        }
	}

	protected void keyTyped(char key, int keycode) 
	{
		Object o = this.buttonList.get(0);
		if(o instanceof GenericScrollBox)
		{
			((GenericScrollBox)o).keyTyped(key, keycode);
		}
		
		if(keycode == 1)
		{
			this.mc.thePlayer.closeScreen();
		}
	}
	
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(TBCMod.statsGuiBackground);
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        int playerXPos = k + 172;
        int playerYPos = l + 110;
	}
	
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
    	if(this.mode == Mode.MAIN)
    	{
    		Minecraft.getMinecraft().renderEngine.bindTexture(TBCMod.battleScreenDivider);
            this.drawTexturedModalRect(192, 5, 0, 0, 3, 190);
            
    		int yOffset = 20;
    		int labelXPos = 60;
    		int firstLineYPos = 0;
    		int secondLineYPos = 10;
    		int thirdLineYPos = 20;
    		int gapBetweenChars = 40;
    		
    		for(int i = 0; i < this.partyMembers.size(); i++)
    		{
    			StatMenuCharData partyMember = this.partyMembers.get(i);
    			CombatEntitySaveData d = null;
    			if(partyMember.Player != null)
    			{
    				d = LevelingEngine.Instance.GetPlayerSaveData(partyMember.Player);
    			}
    			else
    			{
    				d = HenchmanItem.GetCombatEntitySaveData(partyMember.Item);
    			}
    			
    			this.fontRendererObj.drawString(partyMember.CombatEntity.GetName() + " Lvl: " + d.Level, labelXPos, yOffset + firstLineYPos + (i * gapBetweenChars), 2);
    			this.fontRendererObj.drawString("HP: " + partyMember.CombatEntity.currentHp + "/" + partyMember.CombatEntity.GetMaxHp(), labelXPos, yOffset + secondLineYPos + (i * gapBetweenChars), 2);
    			this.fontRendererObj.drawString("MP: " + partyMember.CombatEntity.currentMp + "/" + partyMember.CombatEntity.GetMaxMp(), labelXPos, yOffset + thirdLineYPos + (i * gapBetweenChars), 2);
    		}
    		
    		for(int i = 0; i < this.partyMembers.size(); i++)
    		{
    			int xPos = this.guiLeft;
    			StatMenuCharData partyMember = this.partyMembers.get(i);
    			if(partyMember.FrontRow)
    			{
    				xPos -= 10;
    			}
    			
    			if(partyMember.Player != null)
    			{
    				int playerYPosOffset = 25;
    				BattleScreenDrawer.drawCombatModel(player.worldObj, xPos, this.guiTop + playerYPosOffset + (i * gapBetweenChars), partyMember.CombatEntity, -60, 20);
    			}
    			else
    			{
    				String henchmanType = ((HenchmanItem)partyMember.Item.getItem()).henchmanType;
    				int partyYPosOffset = 50;
    				if(henchmanType == "player")
    				{
    					partyYPosOffset = 25;
    				}
    				
    				BattleScreenDrawer.drawCombatModel(player.worldObj, xPos, this.guiTop + (i * gapBetweenChars) + partyYPosOffset, partyMember.CombatEntity, -60, 20);
    			}
    		}
    	}
    	else if(this.mode == Mode.SUBSCREEN)
    	{
    	}
    	else if(this.mode == Mode.CUSTOM)
    	{
    		this.customRenderer.Render(this, this.fontRendererObj);
    	}
    }
    
    public void ChangeButtonForCustomRenderer(ICustomMenuRender render, String name, ArrayList<GenericScrollBoxCellData> items, ArrayList<GenericScrollBoxCellData> constantItems, Integer numColumns)
    {
    	this.ChangeButtonForSubMenu(name, items, constantItems, numColumns);
    	this.mode = Mode.CUSTOM;
    	this.customRenderer = render;
    }
    
    public void ChangeButtonForSubMenu(String name, ArrayList<GenericScrollBoxCellData> leftSideItems, ArrayList<GenericScrollBoxCellData> rightSideItems, ArrayList<GenericScrollBoxCellData> constantItems, Integer numColumns)
    {
    	this.customRenderer = null;
    	this.mode = Mode.SUBSCREEN;
    	GenericScrollBox scrollBoxLeft = new GenericScrollBox(307, this.guiLeft + 10, this.guiTop + 10, 80, 180, name, leftSideItems, new ArrayList<GenericScrollBoxCellData>(), 1);
    	GenericScrollBox scrollBoxRight = new GenericScrollBox(308, this.guiLeft + 90, this.guiTop + 10, 156, 180, name, rightSideItems, constantItems, numColumns);
    	this.buttonList.clear();
		this.buttonList.add(scrollBoxLeft);
		this.buttonList.add(scrollBoxRight);
    }
    
    public void ChangeButtonForSubMenu(String name, ArrayList<GenericScrollBoxCellData> items, ArrayList<GenericScrollBoxCellData> constantItems, Integer numColumns)
    {
    	this.customRenderer = null;
    	this.mode = Mode.SUBSCREEN;
    	GenericScrollBox scrollBox = new GenericScrollBox(307, this.guiLeft + 10, this.guiTop + 10, 236, 180, name, items, constantItems, numColumns);
    	this.buttonList.clear();
		this.buttonList.add(scrollBox);
    }

    public void ChangeButtonForMainMenu(String name, ArrayList<GenericScrollBoxCellData> items, ArrayList<GenericScrollBoxCellData> constantItems, Integer numColumns)
    {
    	this.customRenderer = null;
    	this.mode = Mode.MAIN;
    	int xOffset = 200;
    	GenericScrollBox scrollBox = new GenericScrollBox(307, this.guiLeft + xOffset, this.guiTop + 10, 246-xOffset, 180, name, items, constantItems, numColumns);
    	this.buttonList.clear();
		this.buttonList.add(scrollBox);
    }

	public void RefreshCurrentPartyMembers()
	{
		ArrayList<Pair<Integer, StatMenuCharData>> staging = new ArrayList<Pair<Integer,StatMenuCharData>>();
		ItemStack[] items = player.inventory.mainInventory;
		for(int i = 0; i < items.length; i++)
		{
			if(items[i] != null)
			{
				ItemStack stack = items[i];
				if(stack.getItem() instanceof HenchmanItem)
				{
					if(stack.getItem() instanceof HenchmanItem)
					{
						Pair<Integer, CombatEntity> c = CombatEntity.GetCombatEntity(i, stack);
						if(c != null && c.item1 > 0)
						{
							staging.add(new Pair(c.item1, new StatMenuCharData(null, stack, c.item2, c.item2.IsFrontLine())));
						}
					}
				}
			}
		}
		
		Pair<Integer, CombatEntity> c = CombatEntity.GetCombatEntity(player);
		if(staging.size() == 0 || c.item1 > 0)
		{
			staging.add(new Pair(c.item1 < 1 ? 1 : c.item1, new StatMenuCharData(player, null, c.item2, c.item2.IsFrontLine())));
		}
		
		this.partyMembers.clear();
		while(staging.size() != 0 && this.partyMembers.size() < 4)
		{
			int lowestPos = -1;
			int lowest = 1000;
			for(int i = 0; i < staging.size(); i++)
			{
				int position = staging.get(i).item1;
				if(position < lowest)
				{
					lowest = position;
					lowestPos = i;
				}
			}
			
			this.partyMembers.add(staging.get(lowestPos).item2);
			staging.remove(lowestPos);
		}
	}
}
