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
import TBC.CombatScreen.GenericGuiButton;
import TBC.CombatScreen.GenericScrollBox;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class StatsGui extends GuiInventory
{
	private class Mode
	{
		public static final int MAIN = 0;
		public static final int SUBSCREEN = 1;
	}
	
	public ArrayList<StatMenuCharData> partyMembers = new ArrayList<StatMenuCharData>();
	public GenericScrollBox masterButton;
	private float ySize_lo;
	private float xSize_lo;
	public int mode;
	public EntityPlayer player;
	private Pair<Integer, Integer> prevPosition = null;
	private long positionStartTime = 0;
	
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
        
        if(this.masterButton.mousePressed(mc, par1, par2))
        {
        	GenericScrollBoxCellData d = this.masterButton.GetCellUnderMouse(par1, par2);
        	if(d != null && !d.HoverText.isEmpty())
        	{
        		ArrayList<String> toShow = new ArrayList<String>();
        		toShow.add(d.HoverText);
        		this.drawHoveringText(toShow, par1, par2, fontRendererObj);
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

        //super.mouseClicked(par1, par2, par3);
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
    			this.fontRendererObj.drawString(partyMember.CombatEntity.GetName(), labelXPos, yOffset + firstLineYPos + (i * gapBetweenChars), 2);
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
    				this.drawCombatModel(partyMember.Player, xPos, this.guiTop + playerYPosOffset + (i * gapBetweenChars), -60);
    			}
    			else
    			{
    				int partyYPosOffset = 50;
    				this.drawCombatModel(player.worldObj, ((HenchmanItem)partyMember.Item.getItem()).henchmanType, xPos, this.guiTop + (i * gapBetweenChars) + partyYPosOffset, -60);
    			}
    		}
    	}
    	else if(this.mode == Mode.SUBSCREEN)
    	{
    	}
    }

    public void ChangeButton(GenericScrollBox button)
    {
    	masterButton = button;
    	this.buttonList.clear();
		this.buttonList.add(button);
    }
    
    public void ChangeButtonForSubMenu(String name, ArrayList<GenericScrollBoxCellData> items, ArrayList<GenericScrollBoxCellData> constantItems, Integer numColumns)
    {
    	this.mode = Mode.SUBSCREEN;
    	GenericScrollBox scrollBox = new GenericScrollBox(307, this.guiLeft + 10, this.guiTop + 10, 236, 180, name, items, constantItems, numColumns);
    	masterButton = scrollBox;
    	this.buttonList.clear();
		this.buttonList.add(scrollBox);
    }

    public void ChangeButtonForMainMenu(String name, ArrayList<GenericScrollBoxCellData> items, ArrayList<GenericScrollBoxCellData> constantItems, Integer numColumns)
    {
    	this.mode = Mode.MAIN;
    	int xOffset = 200;
    	GenericScrollBox scrollBox = new GenericScrollBox(307, this.guiLeft + xOffset, this.guiTop + 10, 246-xOffset, 180, name, items, constantItems, numColumns);
    	masterButton = scrollBox;
    	this.buttonList.clear();
		this.buttonList.add(scrollBox);
    }

	private void drawStatus(CombatEntity player, CombatEntitySaveData xpData)
	{
		int leftLabelXPos = 8;
    	int leftValueXPos = 45;
    	int rightLabelXPos = 102;
    	int rightValueXPos = 139;

    	int firstLineYPos = 25;
    	int secondLineYPos = 37;
    	int thirdLineYPos = 56;
    	int fourthLineYPos = 68;
    	int fifthLineYPos = 80;
    	int sixthLineYPos = 92;
    	int seventhLineYPos = 104;

    	this.fontRendererObj.drawString("HP:", leftLabelXPos, firstLineYPos, 2);
    	this.fontRendererObj.drawString(player.currentHp + " / " + player.GetMaxHp(), leftValueXPos, firstLineYPos, 2);

    	this.fontRendererObj.drawString("MP:", leftLabelXPos, secondLineYPos, 2);
    	this.fontRendererObj.drawString(player.currentMp + " / " + player.GetMaxMp(), leftValueXPos, secondLineYPos, 2);

    	this.fontRendererObj.drawString("XP:", rightLabelXPos, firstLineYPos, 2);
    	this.fontRendererObj.drawString(xpData.CurrentXp + " / " + LevelingEngine.GetXpRequiredForLevel(xpData.Level), rightValueXPos, firstLineYPos, 2);

    	this.fontRendererObj.drawString("AP:", rightLabelXPos, secondLineYPos, 2);
    	this.fontRendererObj.drawString(xpData.CurrentAp + "", rightValueXPos, secondLineYPos, 2);

    	this.fontRendererObj.drawString("Att:", leftLabelXPos, thirdLineYPos, 2);
    	this.fontRendererObj.drawString(player.GetAttack() + "", leftValueXPos, thirdLineYPos, 2);

    	this.fontRendererObj.drawString("Def:", leftLabelXPos, fourthLineYPos, 2);
    	this.fontRendererObj.drawString(player.GetDefense() + "", leftValueXPos, fourthLineYPos, 2);

    	this.fontRendererObj.drawString("MAtt:", leftLabelXPos, fifthLineYPos, 2);
    	this.fontRendererObj.drawString(player.GetMagic() + "", leftValueXPos, fifthLineYPos, 2);

    	this.fontRendererObj.drawString("MDef:", leftLabelXPos, sixthLineYPos, 2);
    	this.fontRendererObj.drawString(player.GetMagicDefense() + "", leftValueXPos, sixthLineYPos, 2);

    	this.fontRendererObj.drawString("Spd:", leftLabelXPos, seventhLineYPos, 2);
    	this.fontRendererObj.drawString(player.GetSpeed() + "", leftValueXPos, seventhLineYPos, 2);

    	this.fontRendererObj.drawString("Str:", rightLabelXPos, thirdLineYPos, 2);
    	this.fontRendererObj.drawString(0 + "", rightValueXPos, thirdLineYPos, 2);

    	this.fontRendererObj.drawString("Dex:", rightLabelXPos, fourthLineYPos, 2);
    	this.fontRendererObj.drawString(0 + "", rightValueXPos, fourthLineYPos, 2);

    	this.fontRendererObj.drawString("Con:", rightLabelXPos, fifthLineYPos, 2);
    	this.fontRendererObj.drawString(0 + "", rightValueXPos, fifthLineYPos, 2);

    	this.fontRendererObj.drawString("Int:", rightLabelXPos, sixthLineYPos, 2);
    	this.fontRendererObj.drawString(0 + "", rightValueXPos, sixthLineYPos, 2);

    	this.fontRendererObj.drawString("Will:", rightLabelXPos, seventhLineYPos, 2);
    	this.fontRendererObj.drawString(0 + "", rightValueXPos, seventhLineYPos, 2);
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
		while(staging.size() != 0)
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
	
	private void drawCombatModel(World world, String entityType, int xPos, int yPos, int rotation)
	{
		EntityLivingBase el = (EntityLivingBase)EntityList.createEntityByName(entityType, world);
		if(el == null)
		{
			return;
		}
		
		drawCombatModel(el, xPos, yPos, rotation);
	}
	
	private void drawCombatModel(EntityLivingBase el, int xPos, int yPos, int rotation)
	{
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        GL11.glTranslatef(xPos + 40, yPos, 0);
        GL11.glRotatef(170, 1F, 0, 0);
        GL11.glRotatef(el.prevRenderYawOffset + rotation, 0, 1F, 0);
        GL11.glScaled(15, 15, 15);
        el.prevSwingProgress = 0;
        el.swingProgress = 0;
        el.limbSwing = 0;
        el.prevLimbSwingAmount = 0;
        el.limbSwingAmount = 0;
        el.prevRotationYawHead = el.prevRenderYawOffset;
        el.rotationYawHead = el.prevRenderYawOffset;
        el.rotationPitch = 0;
        el.prevRotationPitch = 0;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Render ren = (Render) RenderManager.instance.getEntityClassRenderObject(el.getClass());
        ren.doRender(el, 0, 0, 0, 0F, 0F);
        GL11.glPopAttrib();
	}
}
