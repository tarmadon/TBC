package TBC.CombatScreen;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import org.lwjgl.opengl.GL11;

import TBC.Pair;
import TBC.TBCMod;
import TBC.Triplet;
import TBC.Combat.CombatEntity;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.DelayedAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.INonStackingEffect;

public class BattleScreenDrawer
{
	private Hashtable<String, Integer> xOffsetForStatus = new Hashtable<String, Integer>();

	private int cutoffLineHeight;
	private int cutoffLineYPos;

	private BattleScreenClient canvas;
	private List buttonsToSwap;
	private List commandButtons = new ArrayList<GuiButton>();

	public BattleScreenDrawer(BattleScreenClient canvas)
	{
		xOffsetForStatus.put("Poison", 0);
		xOffsetForStatus.put("Paralyze", 9);
		xOffsetForStatus.put("Slow", 18);
		xOffsetForStatus.put("Stop", 27);
		xOffsetForStatus.put("Haste", 36);
		xOffsetForStatus.put("Silence", 45);
		xOffsetForStatus.put("Confusion", 54);
		xOffsetForStatus.put("Blind", 63);
		this.canvas = canvas;
	}

	public void initGui()
	{
		this.cutoffLineHeight = this.canvas.height /4;
		if(this.cutoffLineHeight < 70)
		{
			this.cutoffLineHeight = 70;
		}

        this.cutoffLineYPos = this.canvas.height - cutoffLineHeight;
		ArrayList<GenericScrollBoxCellData> commandActions = new ArrayList<GenericScrollBoxCellData>();
		commandActions.add(new GenericScrollBoxCellData("Attack", "", new AttackCommandFunction(this.canvas)));
		commandActions.add(new GenericScrollBoxCellData("Magic", "", new SelectAbilityFunction(this.canvas)));
		commandActions.add(new GenericScrollBoxCellData("Item", "", new SelectItemFunction(this.canvas)));
		commandActions.add(new GenericScrollBoxCellData("Run", "", new AttemptEscapeFunction(this.canvas)));
		List buttons = new ArrayList<GuiButton>();
		buttons.add(new GenericScrollBox(0, 210, cutoffLineYPos + 7, this.canvas.width - 210, (this.canvas.height - cutoffLineYPos) - 10, "commands", commandActions, new ArrayList<GenericScrollBoxCellData>(), 1));
		this.commandButtons = buttons;
	}

	public void drawBackground(
			TurnState turnState,
			ArrayList<CombatEntity> leftSideEntities,
			ArrayList<CombatEntity> rightSideEntities,
			String message)
	{
		if(this.buttonsToSwap != null)
		{
			this.canvas.ChangeButtons(this.buttonsToSwap);
			this.buttonsToSwap = null;
		}

        fillAreaWithTexture(Blocks.grass.getIcon(1, 0), "/terrain.png", 16, 0, 0, this.canvas.width, this.cutoffLineYPos, rightSideEntities);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.canvas.GetMc().renderEngine.bindTexture(TBCMod.battleScreenCharWindow);
        this.canvas.drawTexturedModalRect(0, cutoffLineYPos, 0, 0, this.canvas.width, cutoffLineHeight);

        if(message != null)
        {
        	this.drawMessage(message);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.canvas.GetMc().renderEngine.bindTexture(TBCMod.battleScreenDivider);
        this.canvas.drawTexturedModalRect(95, cutoffLineYPos + 3, 0, 0, 3, cutoffLineHeight - 3);
        this.canvas.drawTexturedModalRect(205, cutoffLineYPos + 3, 0, 0, 3, cutoffLineHeight - 3);
        this.drawStatBars(turnState, rightSideEntities);
	}

	public void drawForeground(
			TurnState turnState,
			ArrayList<CombatEntity> leftSideEntities,
			ArrayList<CombatEntity> rightSideEntities)
	{
		RenderHelper.enableGUIStandardItemLighting();
		HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup = new HashMap<CombatEntity, Pair<Integer,Integer>>();
		for(int i = 0; i < leftSideEntities.size(); i++)
		{
			positionLookup.put(leftSideEntities.get(i), GetEnemyPosition(i + 1, leftSideEntities.size()));
		}

		for(int i = 0; i < rightSideEntities.size(); i++)
		{
			positionLookup.put(rightSideEntities.get(i), GetAllyPosition(i, rightSideEntities.size()));
		}

        this.drawEnemies(turnState, leftSideEntities, positionLookup);
        this.drawAllies(turnState, rightSideEntities, positionLookup);
        RenderHelper.disableStandardItemLighting();
	}

	public void DisplayTargetButtons(ArrayList<CombatEntity> enemies)
	{
		ArrayList<GenericScrollBoxCellData> targets = new ArrayList<GenericScrollBoxCellData>();
		int enemyCount = enemies.size() + 1;
		for(int i = 0; i < enemies.size(); i++)
		{
			String buttonName = enemies.get(i).GetName();
			targets.add(new GenericScrollBoxCellData(buttonName, "", new TargetEnemyFunction(this.canvas, enemies.get(i))));
		}

		ArrayList<GenericScrollBoxCellData> cancelButton = new ArrayList<GenericScrollBoxCellData>();
		cancelButton.add(new GenericScrollBoxCellData("Cancel", "", new CancelAttackCommandFunction(this.canvas)));
		List buttons = new ArrayList<GuiButton>();
		buttons.add(new GenericScrollBox(0, 210, cutoffLineYPos + 7, this.canvas.width - 210, (this.canvas.height - cutoffLineYPos) - 10, "commands", targets, cancelButton, 1));
		this.buttonsToSwap = buttons;
	}

	public void DisplayAbilityButtons(CombatEntity entity, ArrayList<ICombatAbility> abilities)
	{
		ArrayList<GenericScrollBoxCellData> usableAbilities = new ArrayList<GenericScrollBoxCellData>();
		for(int i = 0; i < abilities.size(); i++)
		{
			ICombatAbility ability = abilities.get(i);
			String abilityName = ability.GetAbilityName();
			String abilityCost = Integer.toString(ability.GetMpCost());
			if(abilityName != null && abilityName != ""  && !(ability instanceof ConstantAbility))
			{
				usableAbilities.add(new GenericScrollBoxCellData(abilityName, abilityCost, new UseAbilityFunction(this.canvas, ability)));
			}
		}

		int abilityHeight = (this.canvas.height - cutoffLineYPos) - 10;

		ArrayList<GenericScrollBoxCellData> constantItems = new ArrayList<GenericScrollBoxCellData>();
		constantItems.add(new GenericScrollBoxCellData("Cancel", "", new CancelAttackCommandFunction(this.canvas)));
		List buttons = new ArrayList<GuiButton>();
		buttons.add(new GenericScrollBox(0, 210, cutoffLineYPos + 7, this.canvas.width - 210, abilityHeight, "abilities", usableAbilities, constantItems, 0));
		this.buttonsToSwap = buttons;
	}

	public void DisplayItemButtons(Minecraft mc, EntityPlayer player)
	{
		ArrayList<GenericScrollBoxCellData> usableAbilities = new ArrayList<GenericScrollBoxCellData>();
		ArrayList<Pair<ICombatAbility, Integer>> abilities = EquippedItemManager.Instance.GetUsableItemsForPlayer(mc, player);
		for(int i = 0; i < abilities.size(); i++)
		{
			ICombatAbility ability = abilities.get(i).item1;
			usableAbilities.add(new GenericScrollBoxCellData(ability.GetAbilityName(), Integer.toString(abilities.get(i).item2), new UseAbilityFunction(this.canvas, ability)));
		}

		int abilityHeight = (this.canvas.height - cutoffLineYPos) - 10;
		ArrayList<GenericScrollBoxCellData> constantItems = new ArrayList<GenericScrollBoxCellData>();
		constantItems.add(new GenericScrollBoxCellData("Cancel", "", new CancelAttackCommandFunction(this.canvas)));
		List buttons = new ArrayList<GuiButton>();
		buttons.add(new GenericScrollBox(0, 210, cutoffLineYPos + 7, this.canvas.width - 210, abilityHeight, "abilities", usableAbilities, constantItems, 0));
		this.buttonsToSwap = buttons;
	}

	public void DisplayCommandButtons()
	{
		this.buttonsToSwap = this.commandButtons;
	}

	private void fillAreaWithTexture(String textureFile, int textureXOffset, int textureYOffset, int textureSize, int areaXOffset, int areaYOffset, int areaWidth, int areaHeight)
	{
		int numberXTiles = (areaWidth / textureSize) + 1;
		int numberYTiles = (areaHeight / textureSize) + 1;

		for(int i = 0; i<numberXTiles; i++)
		{
			for(int j = 0; j<numberYTiles; j++)
			{
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				//this.canvas.GetMc().renderEngine.bindTexture(textureFile);
				this.canvas.drawTexturedModalRect(i * textureSize, j * textureSize, textureXOffset, textureYOffset, textureSize, textureSize);
			}
		}
	}

	private void fillAreaWithTexture(IIcon texture, String textureFile, int textureSize, int areaXOffset, int areaYOffset, int areaWidth, int areaHeight, ArrayList<CombatEntity> right)
	{
		int numberXTiles = (areaWidth / textureSize) + 1;
		int numberYTiles = (areaHeight / textureSize) + 1;

		int color = 0;
//		if(texture.getIconName() == "grass_top")
//		{
//			for(int i = 0; i<right.size(); i++)
//			{
//				if(right.get(i).innerEntity != null)
//				{
//					EntityLivingBase player = right.get(i).innerEntity;
//					color = Blocks.grass.colorMultiplier(player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
//					break;
//				}
//			}
//		}

		for(int i = 0; i<numberXTiles; i++)
		{
			for(int j = 0; j<numberYTiles; j++)
			{
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.canvas.GetMc().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				if(texture.getIconName() == "grass_top")
				{
					this.drawTexturedModelRectFromIconWithColor(i * textureSize, j * textureSize, texture, textureSize, textureSize, color);
				}
				else
				{
					this.canvas.drawTexturedModelRectFromIcon(i * textureSize, j * textureSize, texture, textureSize, textureSize);
				}
			}
		}
	}

    public void drawTexturedModelRectFromIconWithColor(int par1, int par2, IIcon par3Icon, int par4, int par5, int color)
    {
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_F(red, green, blue);
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + par5), 0, (double)par3Icon.getMinU(), (double)par3Icon.getMaxV());
        tessellator.addVertexWithUV((double)(par1 + par4), (double)(par2 + par5), 0, (double)par3Icon.getMaxU(), (double)par3Icon.getMaxV());
        tessellator.addVertexWithUV((double)(par1 + par4), (double)(par2 + 0), 0, (double)par3Icon.getMaxU(), (double)par3Icon.getMinV());
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), 0, (double)par3Icon.getMinU(), (double)par3Icon.getMinV());
        tessellator.draw();
    }

	private void drawMessage(String message)
	{
        if(message != null)
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.canvas.GetMc().renderEngine.bindTexture(TBCMod.vanillaGui);
            this.canvas.drawTexturedModalRect(this.canvas.width/2 - 100, 5, 0, 66, 200, 20);

            int stringWidth = this.canvas.GetMc().fontRenderer.getStringWidth(message);
            this.drawLabel(this.canvas.width/2 - stringWidth/2, 10, message);
        }
	}

	private void drawLabel(int xPos, int yPos, String text)
	{
		FontRenderer fontRenderer = this.canvas.GetMc().fontRenderer;
		fontRenderer.drawString(text, xPos, yPos, 2);
	}

	private void drawStatBars(TurnState turnState, ArrayList<CombatEntity> allies)
	{
		int allyBasePositionX = 5;
		int allyBasePositionY = this.cutoffLineYPos + 7;
		this.drawLabel(allyBasePositionX + 115, allyBasePositionY, "HP");
		this.drawLabel(allyBasePositionX + 165, allyBasePositionY, "MP");

		for(int i =0; i<allies.size(); i++)
		{
			if(allies.get(i) == turnState.activeEntity && turnState.phase == TurnState.PlayerControl)
			{
				drawStatBar(allyBasePositionX, allyBasePositionY + 10 + (10*i), 120, allies.get(i), Integer.MAX_VALUE);
			}
			else
			{
				drawStatBar(allyBasePositionX, allyBasePositionY + 10 + (10*i), 120, allies.get(i), 1);
			}
		}
	}

	private void drawStatBar(int xPos, int yPos, int statXPosOffset, CombatEntity entity, int nameColor)
	{
		FontRenderer fontRenderer = this.canvas.GetMc().fontRenderer;
		int stringWidth = fontRenderer.getStringWidth(entity.GetName());
		fontRenderer.drawString(entity.GetName(), xPos, yPos, nameColor);

		String hpString = entity.currentHp + "/" + entity.GetMaxHp();
		int hpStringWidth = fontRenderer.getStringWidth(hpString);
		fontRenderer.drawString(hpString, xPos + statXPosOffset - hpStringWidth/2, yPos, 1);

		String mpString = entity.currentMp + "/" + entity.GetMaxMp();
		int mpStringWidth = fontRenderer.getStringWidth(mpString);
		fontRenderer.drawString(mpString, xPos + statXPosOffset + 50 - mpStringWidth/2, yPos, 1);
	}

	private void drawAllies(TurnState turnState, ArrayList<CombatEntity> allies, HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup)
	{
		if(allies.size() == 0)
		{
			return;
		}

		int totalAllyCount = allies.size();
		for(int i = totalAllyCount - 1; i>=0; i--)
		{
			CombatEntity entityToDraw = allies.get(totalAllyCount - (i + 1));
			if(entityToDraw.currentHp < 1)
			{
				continue;
			}

			Pair<Integer, Integer> position = positionLookup.get(entityToDraw);
			int playerXPos = position.item1;
			if(!entityToDraw.IsFrontLine())
			{
				playerXPos += 15;
			}
			
			int playerYPos = position.item2;

			DrawCombatEntity(turnState, positionLookup, entityToDraw, true, playerXPos, playerYPos, -70);
		}
	}

	public void DrawCombatEntity(TurnState turnState, HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup, CombatEntity entity, boolean isAlly, int xPos, int yPos, int rotation)
	{
		if(turnState.phase == TurnState.DisplayingAttack && turnState.ability != null)
		{
			if(turnState.activeEntity == entity)
			{
				boolean isAlsoTarget = turnState.targetEntities.contains(entity);
				turnState.ability.DrawUser(this, positionLookup, turnState, entity, isAlly, isAlsoTarget, xPos, yPos, rotation);
				return;
			}
			else if(turnState.targetEntities.contains(entity))
			{
				turnState.ability.DrawTarget(this, turnState, entity, isAlly, xPos, yPos, rotation);
				return;
			}
		}

		DrawCombatEntity(entity, xPos, yPos, rotation, -1, false);
	}

	public void DrawCombatEntity(CombatEntity entity, int xPos, int playerYPos, int rotation, long damageTextTime, boolean showHitIndicator)
	{
		Entity found = Minecraft.getMinecraft().theWorld.getEntityByID(entity.id);
		int yPos = playerYPos;
		int modelYPos = yPos;
		if(found instanceof EntityPlayer)
		{
			yPos -= 20;
			modelYPos -= 25;
		}

		drawCombatModel(found, xPos, modelYPos, entity, rotation);
		drawStatusEffects(xPos, yPos, entity);
		if(showHitIndicator)
		{
			drawHitIndicator(xPos, yPos);
		}

		if(damageTextTime > 0)
		{
			drawDamageText(xPos, yPos, entity, damageTextTime);
		}
	}

	private void drawEnemies(TurnState turnState, ArrayList<CombatEntity> enemies, HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup)
	{
		int numEnemies = enemies.size();
		for(int i =0; i < numEnemies; i++)
		{
			CombatEntity enemy = enemies.get(i);
			if(enemy.currentHp < 1)
			{
				continue;
			}

			Pair<Integer, Integer> enemyPosition = positionLookup.get(enemy);
			int xPos = enemyPosition.item1;
			int yPos = enemyPosition.item2;
			DrawCombatEntity(turnState, positionLookup, enemy, false, xPos, yPos, 110);
		}
	}

	private void drawStatusEffects(int xPos, int yPos, CombatEntity enemy)
	{
		ArrayList<String> toDisplay = new ArrayList<String>();
		for(Object ongoing : enemy.ongoingEffects)
		{
			if(ongoing instanceof INonStackingEffect)
			{
				INonStackingEffect nonStacking = (INonStackingEffect)ongoing;
				if(xOffsetForStatus.containsKey(nonStacking.GetEffectName()))
				{
					toDisplay.add(nonStacking.GetEffectName());
				}
			}
		}

		if(toDisplay.isEmpty())
		{
			return;
		}

		int startX = xPos + (40 - (5 * toDisplay.size()));
		this.canvas.GetMc().entityRenderer.setupOverlayRendering();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.canvas.GetMc().renderEngine.bindTexture(TBCMod.statusEffects);
		for(int i = 0; i< toDisplay.size(); i++)
		{
			Integer textureXOffset = this.xOffsetForStatus.get(toDisplay.get(i));
	        this.canvas.drawTexturedModalRect(startX + 10 * i, yPos - 38, textureXOffset, 0, 9, 9);
		}
	}

	private void drawDamageText(int xPos, int yPos, CombatEntity entity, long elapsedTime)
	{
		if(entity.lastDamageTaken != null)
		{
			int dmgOffset = Math.round(elapsedTime / 40);
			this.canvas.GetMc().entityRenderer.setupOverlayRendering();
			if(entity.lastDamageTaken > 0)
			{
				this.drawLabel(xPos + 35,  yPos - 38 - dmgOffset, entity.lastDamageTaken + "");
			}
			else if(entity.lastDamageTaken < 0)
			{
				this.drawColorLabel(xPos + 35,  yPos - 38 - dmgOffset, -entity.lastDamageTaken + "");
			}
			else
			{
				this.drawLabel(xPos + 35,  yPos - 38 - dmgOffset, "Miss");
			}
		}
	}

	private void drawHitIndicator(int xPos, int yPos)
	{
		this.canvas.GetMc().entityRenderer.setupOverlayRendering();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.canvas.GetMc().renderEngine.bindTexture(TBCMod.combatDecals);
		this.canvas.drawTexturedModalRect(xPos + 23, yPos - 25 , 0, 0, 32, 32);
	}

	private void drawCombatModel(Entity worldEntity, int xPos, int yPos, CombatEntity entity, int rotation)
	{
		EntityLivingBase el = (EntityLivingBase)worldEntity;
		if(worldEntity == null || !(worldEntity instanceof EntityPlayer || worldEntity instanceof EntitySlime))
		{
			try
			{
				el = (EntityLivingBase)EntityList.createEntityByName(entity.entityType, Minecraft.getMinecraft().theWorld);
				//el = (EntityLivingBase)worldEntity.getClass().getConstructor(new Class[] {World.class}).newInstance(new Object[] {worldEntity.worldObj});
			}
			catch (Exception e)
			{
			}
		}

		if(el == null)
		{
			return;
		}
		
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		this.canvas.GetMc().entityRenderer.setupOverlayRendering();
        GL11.glTranslatef(xPos + 40, yPos, 0);
        GL11.glRotatef(150, 1F, 0, 0);
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

	private Pair<Integer, Integer> GetEnemyPosition(int enemyNumber, int totalEnemies)
	{
		int enemyBasePositionX = 40;
		int enemyBasePositionY = (enemyNumber * ((this.canvas.height - this.cutoffLineHeight) /(totalEnemies + 1))) + 20;
		return new Pair<Integer, Integer>(enemyBasePositionX, enemyBasePositionY);
	}

	private Pair<Integer, Integer> GetAllyPosition(int allyNumber, int totalAllies)
	{
		int allyBasePositionX = this.canvas.width - 100;
		int offsetPerAlly = (this.canvas.height - this.cutoffLineHeight) /(totalAllies + 1);
		int allyBasePositionY = -(int)((totalAllies - 1 - allyNumber) * offsetPerAlly) + (-10 + this.canvas.height - this.cutoffLineHeight);
		return new Pair<Integer, Integer>(allyBasePositionX, allyBasePositionY);
	}

	private void drawColorLabel(int xPos, int yPos, String text)
	{
		//8453920
		//16777215
		FontRenderer fontRenderer = this.canvas.GetMc().fontRenderer;
		fontRenderer.drawString(text, xPos, yPos, 8453920);
	}
}
