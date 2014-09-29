package TBC;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

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
import TBC.CombatScreen.IGenericAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class StatsGui extends GuiInventory
{
	protected int yInventoryOffset = 34;

	private float ySize_lo;

	private float xSize_lo;

	private int stackSize;

	private ArrayList<Object> tempRemoved;

	private Item oldCharItem;
	private Boolean hasSetCharItem;

	private ContainerForStatsGui asStatContainer;
	public int mode = 0;
	private ArrayList<GenericGuiButton> sharedButtons;

	public StatsGui(EntityPlayer par1EntityPlayer, ContainerForStatsGui container)
	{
		super(par1EntityPlayer);
		this.inventorySlots = container;
		this.asStatContainer = container;
		xSize = 206;
		ySize = 199;
	}

	public void initGui()
	{
		super.initGui();
		GenericGuiButton showStatusButton = new GenericGuiButton(300, this.guiLeft + 115, this.guiTop + 2, 20, 20, "S", new ChangeGuiModeFunction(this, null, null, 0));
		GenericGuiButton showAbilitiesButton = new GenericGuiButton(301, this.guiLeft + 135, this.guiTop + 2, 20, 20, "A", new ChangeGuiModeFunction(this, null, null, 1));
		GenericGuiButton showItemsButton = new GenericGuiButton(302, this.guiLeft + 155, this.guiTop + 2, 20, 20, "I", new ChangeGuiModeFunction(this, null, null, 2));
		this.sharedButtons = new ArrayList<GenericGuiButton>();
		this.sharedButtons.add(showStatusButton);
		this.sharedButtons.add(showAbilitiesButton);
		this.sharedButtons.add(showItemsButton);
		this.buttonList.clear();
		this.buttonList.addAll(this.sharedButtons);
		oldCharItem = null;
		hasSetCharItem = false;
	}

	public void drawScreen(int par1, int par2, float par3)
	{
        this.xSize_lo = (float)par1;
        this.ySize_lo = (float)par2;
        super.drawScreen(par1, par2, par3);
        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            guibutton.drawButton(this.mc, par1, par2);
        }
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

        super.mouseClicked(par1, par2, par3);
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
    	CombatEntity player = null;
    	PlayerXPWorldSavedData xpData = null;
    	if(this.asStatContainer.CharSlot.getStack() != null)
    	{
    		ItemStack itemStack = this.asStatContainer.CharSlot.getStack();
    		Item item = itemStack.getItem();
    		if(item instanceof HenchmanItem)
    		{
				HenchmanItem h = (HenchmanItem)item;
				EntityLiving renderEntity = (EntityLiving) EntityList.createEntityByName(h.henchmanType, mc.theWorld);
				renderEntity.getEntityData().setInteger("henchmanIndex", this.mc.thePlayer.inventory.getHotbarSize());
				CombatEntity henchmanEntity = CombatEntityLookup.Instance.GetCombatEntity(renderEntity, h.henchmanName);
				henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getDamage(itemStack)/(float)h.getMaxDamage())));
				NBTTagCompound itemData = itemStack.getTagCompound();
				if(itemData != null && itemData.hasKey("HenchMP"))
				{
					henchmanEntity.currentMp = itemData.getInteger("HenchMP");
				}

    			xpData = new PlayerXPWorldSavedData();
    			xpData.PlayerLevel = 1;
    			player = henchmanEntity;
    		}
    	}

    	if(player == null)
    	{
    		player = CombatEntity.GetCombatEntity(Minecraft.getMinecraft().thePlayer, 1);
    		xpData = LevelingEngine.Instance.GetXpDataForPlayer((EntityPlayer)player.innerEntity);
    	}

    	this.fontRendererObj.drawString(xpData.PlayerLevel + "", 102, 9, 2);

    	// Check to see if the user has changed, if it has return to stats page
    	ItemStack chosen = this.asStatContainer.CharSlot.getStack();
    	if ((chosen == null && oldCharItem != null) || (chosen != null && (chosen.getItem() != oldCharItem)))
    	{
    		this.ChangeMode(player, null, 0);
    		if(chosen == null)
    		{
    			oldCharItem = null;
    		}
    		else
    		{
    			oldCharItem = chosen.getItem();
    		}
    	}

    	if(this.mode == 0)
    	{
    		drawStatus(player, xpData);
    	}
    	else if(this.mode == 1)
    	{
    		drawAbilities(player);
    	}
    	else if(this.mode == 2)
    	{
    		drawItems(player);
    	}
    	else if(this.mode == 3)
    	{
    		this.fontRendererObj.drawString("On Who?", 8, 25, 2);
    	}
    }

    private void drawItems(CombatEntity user)
    {
    	this.fontRendererObj.drawString("Items:", 8, 25, 2);
    	if(!hasSetCharItem)
    	{
    		this.ChangeMode(user, null, 2);
    		this.hasSetCharItem = true;
    	}
    }

    private void drawAbilities(CombatEntity player)
    {
    	this.fontRendererObj.drawString("Abilities:", 8, 25, 2);
    	ItemStack chosen = this.asStatContainer.CharSlot.getStack();
    	if(!hasSetCharItem)
    	{
    		this.ChangeMode(player, null, 1);
    		this.hasSetCharItem = true;
    	}
    }

    public void ChangeMode(CombatEntity player, ICombatAbility abilityToUse, int newMode)
    {
    	hasSetCharItem = false;
    	this.buttonList.clear();
		this.buttonList.addAll(this.sharedButtons);
		if(newMode == 1 && player != null)
    	{
        	SetupAbilitiesTab(player);
    	}
		else if(newMode == 2 && player != null)
		{
			SetupItemsTab(player);
		}
		else if(newMode == 3)
		{
			SetupTargetSelection(player, abilityToUse);
		}

		this.mode = newMode;
    }

	private void SetupTargetSelection(CombatEntity player, ICombatAbility abilityToUse)
	{
		EntityPlayer actualPlayer = Minecraft.getMinecraft().thePlayer;
		ArrayList<Triplet<String, String, IGenericAction>> displayItems = new ArrayList<Triplet<String,String,IGenericAction>>();
		ItemStack[] henchmenItems = new ItemStack[actualPlayer.inventory.getHotbarSize() + 1];
    	if(this.asStatContainer.CharSlot.getStack() != null && this.asStatContainer.CharSlot.getStack().getItem() instanceof HenchmanItem)
    	{
    		henchmenItems[actualPlayer.inventory.getHotbarSize()] = this.asStatContainer.CharSlot.getStack();
    	}

		ArrayList<CombatEntity> otherPartyMembers = GetOtherPartyMembers(actualPlayer, henchmenItems);
		if(abilityToUse.GetAbilityTarget() == AbilityTargetType.AllAllies)
		{
			otherPartyMembers.add(player);
			displayItems.add(new Triplet("Current Party", "", new UseAbilityFromStatsGuiAction(this, this.mode, player, abilityToUse, otherPartyMembers, henchmenItems)));
		}
		else if(abilityToUse.GetAbilityTarget() == AbilityTargetType.Self)
		{
			ArrayList<CombatEntity> playerTarget = new ArrayList<CombatEntity>();
			playerTarget.add(player);
			displayItems.add(new Triplet(player.GetName(), "", new UseAbilityFromStatsGuiAction(this, this.mode, player, abilityToUse, playerTarget, henchmenItems)));
		}
		else if(abilityToUse.GetAbilityTarget() == AbilityTargetType.OneAlly)
		{
			ArrayList<CombatEntity> playerTarget = new ArrayList<CombatEntity>();
			playerTarget.add(player);
			displayItems.add(new Triplet(player.GetName(), "", new UseAbilityFromStatsGuiAction(this, this.mode, player, abilityToUse, playerTarget, henchmenItems)));
			for(int i = 0; i< otherPartyMembers.size(); i++)
			{
				ArrayList<CombatEntity> henchTarget = new ArrayList<CombatEntity>();
				henchTarget.add(otherPartyMembers.get(i));
				displayItems.add(new Triplet(otherPartyMembers.get(i).GetName(), "", new UseAbilityFromStatsGuiAction(this, this.mode, player, abilityToUse, henchTarget, henchmenItems)));
			}
		}

		ArrayList<Triplet<String, String, IGenericAction>> cancelButton = new ArrayList<Triplet<String,String,IGenericAction>>();
		cancelButton.add(new Triplet<String, String, IGenericAction>("Cancel", "", new ChangeGuiModeFunction(this, null, null, this.mode)));
		GenericScrollBox scrollBox = new GenericScrollBox(307, this.guiLeft + 10, this.guiTop + 37, 175, 63, "Target", displayItems, cancelButton, 0);
		this.buttonList.add(scrollBox);
	}

	private void SetupItemsTab(CombatEntity player)
	{
		ArrayList<Triplet<String, String, IGenericAction>> displayItems = new ArrayList<Triplet<String,String,IGenericAction>>();
		ArrayList<Quintuplet<Item, EquippedItem, ICombatAbility, Integer>> items = EquippedItemManager.Instance.GetAllKnownItemsForPlayer(Minecraft.getMinecraft(), Minecraft.getMinecraft().thePlayer);
		for(int i = 0; i < items.size(); i++)
		{
			IGenericAction action = null;
			ICombatAbility itemAbility = items.get(i).item3;
			String itemName = "";
			if(itemAbility != null && itemAbility.IsUsableOutOfCombat())
			{
				itemName = itemAbility.GetAbilityName();
				action = new ChangeGuiModeFunction(this, player, itemAbility, 3);
			}
			else
			{
				itemName = items.get(i).item1.getItemStackDisplayName(new ItemStack(items.get(i).item1));
			}

			displayItems.add(new Triplet(itemName, items.get(i).item4 + "", action));
		}

		if(displayItems.size() == 0)
		{
			displayItems.add(new Triplet("You have no items.", "", null));
		}

		GenericScrollBox scrollBox = new GenericScrollBox(307, this.guiLeft + 10, this.guiTop + 37, 175, 63, "Items", displayItems, new ArrayList<Triplet<String,String,IGenericAction>>(), 0);
		this.buttonList.add(scrollBox);
	}

	private void SetupAbilitiesTab(CombatEntity player)
	{
		ArrayList<Triplet<String, String, IGenericAction>> displayAbilities = new ArrayList<Triplet<String,String,IGenericAction>>();
		Pair<Integer, ICombatAbility>[] abilities = player.GetAbilities();
		for(int i = 0; i < abilities.length; i++)
		{
			IGenericAction action = null;
			ICombatAbility ability = abilities[i].item2;
			if(ability.IsUsableOutOfCombat() && player.currentMp >= ability.GetMpCost())
			{
				action = new ChangeGuiModeFunction(this, player, ability, 3);
			}

			if(!ability.GetAbilityName().isEmpty())
			{
				String mpDisplay = ability.GetMpCost() + "";
				if(ability instanceof ConstantAbility)
				{
					mpDisplay = "";
				}

				displayAbilities.add(new Triplet(ability.GetAbilityName(), mpDisplay, action));
			}
		}

		if(displayAbilities.size() == 0)
		{
			displayAbilities.add(new Triplet("You have no abilities.", "", null));
		}

		GenericScrollBox scrollBox = new GenericScrollBox(304, this.guiLeft + 10, this.guiTop + 37, 175, 63, "Abilities", displayAbilities, new ArrayList<Triplet<String,String,IGenericAction>>(), 0);
		this.buttonList.add(scrollBox);
	}

    private ArrayList<CombatEntity> GetOtherPartyMembers(EntityPlayer actualPlayer, ItemStack[] henchmenItems)
    {
    	ArrayList<CombatEntity> partyMembers = new ArrayList<CombatEntity>();
    	for(int i = 0; i< actualPlayer.inventory.getHotbarSize(); i++)
		{
			if(actualPlayer.inventory.mainInventory[i] != null && actualPlayer.inventory.mainInventory[i].getItem() instanceof HenchmanItem)
			{
				HenchmanItem h = (HenchmanItem)actualPlayer.inventory.mainInventory[i].getItem();
				EntityLiving renderEntity = (EntityLiving) EntityList.createEntityByName(h.henchmanType, actualPlayer.worldObj);
				renderEntity.getEntityData().setInteger("henchmanIndex", i);
				CombatEntity henchmanEntity = CombatEntityLookup.Instance.GetCombatEntity(renderEntity, h.henchmanName);
				henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getDamage(actualPlayer.inventory.mainInventory[i])/(float)h.getMaxDamage())));
				if(henchmanEntity.currentHp < 1)
				{
					continue;
				}

				NBTTagCompound itemData = actualPlayer.inventory.mainInventory[i].getTagCompound();
				if(itemData != null && itemData.hasKey("HenchMP"))
				{
					henchmanEntity.currentMp = itemData.getInteger("HenchMP");
				}

				henchmenItems[i] = actualPlayer.inventory.mainInventory[i];
				partyMembers.add(henchmanEntity);
			}
		}

    	if(this.asStatContainer.CharSlot.getStack() != null && this.asStatContainer.CharSlot.getStack().getItem() instanceof HenchmanItem)
    	{
    		partyMembers.add(CombatEntityLookup.Instance.GetCombatEntityForPlayer(actualPlayer));
    	}

    	return partyMembers;
    }

	private void drawStatus(CombatEntity player, PlayerXPWorldSavedData xpData)
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
    	this.fontRendererObj.drawString(xpData.PlayerXp + " / " + LevelingEngine.GetXpRequiredForLevel(xpData.PlayerLevel), rightValueXPos, firstLineYPos, 2);

    	this.fontRendererObj.drawString("AP:", rightLabelXPos, secondLineYPos, 2);
    	this.fontRendererObj.drawString(xpData.PlayerAp + "", rightValueXPos, secondLineYPos, 2);

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
}
