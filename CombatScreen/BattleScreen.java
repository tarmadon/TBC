package TBC.CombatScreen;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.Combat.CombatEntityTemplate;
import TBC.Combat.CombatRandom;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.LevelingEngine;
import TBC.Combat.Abilities.AbilityTargetType;
import TBC.Combat.Abilities.DefaultAttackAbility;
import TBC.Combat.Abilities.DelayedAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BattleScreen extends GuiScreen 
{
	private ArrayList<GenericGuiButton> buttonsToSwap;
	
	private ArrayList<CombatEntity> enemies = new ArrayList<CombatEntity>();
	private ArrayList<CombatEntity> allies = new ArrayList<CombatEntity>();
	private CombatEngine combatEngine;
	private LevelingEngine levelingEngine;
	private EntityPlayer player;
	
	private CombatEntity entityForCurrentTurn;
	private ICombatAbility abilityToUse;
	private BattleScreenDrawer display;

	private boolean skipMessage = false;
	private TurnState nextState = null;
	
	private TurnState turnState;
	private ItemStack[] henchmenItems;
	
	public BattleScreen(EntityPlayer player, EntityLiving enemyEntity, boolean isAttacker)
	{
		henchmenItems = new ItemStack[player.inventory.getHotbarSize()];
		this.turnState = new TurnState(this.mc);
		this.display = new BattleScreenDrawer(this);
		this.player = player;
		allies.add(CombatEntity.GetCombatEntity(player, 1));
		
		int foundHenchmen = 0;
		for(int i = 0; i< player.inventory.getHotbarSize(); i++)
		{
			if(player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof HenchmanItem)
			{
				HenchmanItem h = (HenchmanItem)player.inventory.mainInventory[i].getItem();
				EntityLiving renderEntity = (EntityLiving) EntityList.createEntityByName(h.henchmanType, player.worldObj);
				renderEntity.setPosition(player.posX, player.posY, player.posZ);
				renderEntity.getEntityData().setInteger("henchmanIndex", i);
				CombatEntity henchmanEntity = CombatEntityLookup.Instance.GetCombatEntity(renderEntity, h.henchmanName);
				henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getItemDamageFromStack(player.inventory.mainInventory[i])/(float)h.getMaxDamage())));
				if(henchmanEntity.currentHp < 1)
				{
					continue;
				}
				
				NBTTagCompound itemData = player.inventory.mainInventory[i].getTagCompound();
				if(itemData != null && itemData.hasKey("HenchMP"))
				{
					henchmanEntity.currentMp = itemData.getInteger("HenchMP");
				}
				
				henchmenItems[i] = player.inventory.mainInventory[i];
				allies.add(henchmanEntity);
				foundHenchmen++;
				if(foundHenchmen >= 3)
				{
					break;
				}
			}
		}
		
		Vec3 position = enemyEntity.getPosition(1.0F);
		double enemyX = position.xCoord;
		double enemyY = position.yCoord;
		double enemyZ = position.zCoord;
		
		int encounterRadius = 10;
		AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(enemyX - encounterRadius, enemyY - encounterRadius, enemyZ - encounterRadius, enemyX + encounterRadius, enemyY + encounterRadius, enemyZ + encounterRadius); 
		List additionalEnemies = enemyEntity.worldObj.getEntitiesWithinAABBExcludingEntity(enemyEntity, boundingBox);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put(EntityList.getEntityString(enemyEntity), 1);
		enemies.add(CombatEntity.GetCombatEntity(enemyEntity, 1));
		
		if(additionalEnemies != null && additionalEnemies.size() > 0)
		{
			for(int i = 0; i<additionalEnemies.size(); i++)
			{
				Object additionalEnemy = additionalEnemies.get(i);
				if(additionalEnemy instanceof EntityPlayer)
				{
					continue;
				}
			
				if(additionalEnemy instanceof EntityLiving)
				{
					EntityLiving additionalLivingEnemy = (EntityLiving)additionalEnemy;
					if(additionalLivingEnemy.canEntityBeSeen(player))
					{
						Integer existingOfThisType = map.get(additionalLivingEnemy.getEntityName());
						int numOfThisType = 1;
						if(existingOfThisType != null)
						{
							numOfThisType += existingOfThisType;
						}
						
						enemies.add(CombatEntity.GetCombatEntity(additionalLivingEnemy, numOfThisType));
						map.put(EntityList.getEntityString(additionalLivingEnemy), numOfThisType);
						
						if(enemies.size() >= 5)
						{
							break;
						}
					}
				}
			}
		}
		
		this.combatEngine = new CombatEngine(this.allies, this.enemies, isAttacker);
		this.levelingEngine = new LevelingEngine();
		this.StartNextTurn();
	}

	public BattleScreen(EntityPlayer player, ArrayList<Pair<EntityLiving, String>> setEnemies, boolean isAttacker)
	{
		henchmenItems = new ItemStack[player.inventory.getHotbarSize()];
		this.turnState = new TurnState(this.mc);
		this.display = new BattleScreenDrawer(this);
		this.player = player;
		allies.add(CombatEntity.GetCombatEntity(player, 1));
		
		int foundHenchmen = 0;
		for(int i = 0; i< player.inventory.getHotbarSize(); i++)
		{
			if(player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof HenchmanItem)
			{
				HenchmanItem h = (HenchmanItem)player.inventory.mainInventory[i].getItem();
				EntityLiving renderEntity = (EntityLiving) EntityList.createEntityByName(h.henchmanType, player.worldObj);
				renderEntity.setPosition(player.posX, player.posY, player.posZ);
				renderEntity.getEntityData().setInteger("henchmanIndex", i);
				CombatEntity henchmanEntity = CombatEntityLookup.Instance.GetCombatEntity(renderEntity, h.henchmanName);
				henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getItemDamageFromStack(player.inventory.mainInventory[i])/(float)h.getMaxDamage())));
				if(henchmanEntity.currentHp < 1)
				{
					continue;
				}
				
				NBTTagCompound itemData = player.inventory.mainInventory[i].getTagCompound();
				if(itemData != null && itemData.hasKey("HenchMP"))
				{
					henchmanEntity.currentMp = itemData.getInteger("HenchMP");
				}
				
				henchmenItems[i] = player.inventory.mainInventory[i];
				allies.add(henchmanEntity);
				foundHenchmen++;
				if(foundHenchmen >= 3)
				{
					break;
				}
			}
		}
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0; i< setEnemies.size(); i++)
		{
			Integer existingOfThisType = map.get(setEnemies.get(i).item2);
			int numOfThisType = 1;
			if(existingOfThisType != null)
			{
				numOfThisType += existingOfThisType;
			}
			
			enemies.add(CombatEntity.GetCombatEntity(setEnemies.get(i).item1, setEnemies.get(i).item2, 1));
			map.put(setEnemies.get(i).item2, numOfThisType);
		}
		
		this.combatEngine = new CombatEngine(this.allies, this.enemies, isAttacker);
		this.levelingEngine = new LevelingEngine();
		this.StartNextTurn();
	}
	
	public void initGui() 
	{
		this.display.initGui();
	}
	
	public void ChangeButtons(List newButtons)
	{
		this.buttonList.clear();
		this.buttonList.addAll(newButtons);
	}
	
	public Minecraft GetMc()
	{
		return this.mc;
	}
	
	public boolean doesGuiPauseGame() 
	{
		return true;
	}

	protected void keyTyped(char par1, int par2) 
	{
	};
	
	public void ChooseAbilityCommand()
	{
		this.display.DisplayAbilityButtons(this.entityForCurrentTurn, this.combatEngine.GetChoosableAbilitiesForEntity(this.entityForCurrentTurn));
	}
	
	public void ChooseItemCommand()
	{
		this.display.DisplayItemButtons(this.mc, this.player);
	}
	
	public void DefaultAttackCommand()
	{
		if(this.entityForCurrentTurn != null)
		{
			this.UseAbilityCommand(this.entityForCurrentTurn.GetAbilities()[0].item2);
		}
	}
	
	public void CancelAttackCommand()
	{
		this.display.DisplayCommandButtons();
		this.abilityToUse = null;
	}
	
	public void UseAbilityCommand(ICombatAbility ability)
	{
		this.abilityToUse = ability;
		ArrayList<ArrayList<CombatEntity>> targets = this.combatEngine.GetValidTargets(this.entityForCurrentTurn, ability.GetAbilityTarget());
		if(targets.size() == 1)
		{			
			this.TargetCombatEntity(targets.get(0));
		}
		else 
		{
			ArrayList<CombatEntity> masterList = new ArrayList<CombatEntity>();
			for(ArrayList<CombatEntity> target : targets)
			{
				masterList.add(target.get(0));
			}
			
			this.display.DisplayTargetButtons(masterList);
		}
	}
	
	public void AttemptEscape()
	{
		if(this.combatEngine.CanEscape())
		{
			ArrayList<String> messages = new ArrayList<String>();
			messages.add("Successfully escaped!");
			TurnState next = new TurnState(mc);
			next.SetState(TurnState.EndOfCombat, null, null, (CombatEntity)null);
			this.turnState.SetDisplayMessageState(messages, next);
		}
		else
		{
			ArrayList<String> messages = new ArrayList<String>();
			messages.add("Failed to escape!");
			TurnState next = new TurnState(mc);
			next.SetState(TurnState.DisplayingEndOfTurn, null, null, (CombatEntity)null);
			this.turnState.SetDisplayMessageState(messages, next);
		}
	}

	public void TargetCombatEntity(CombatEntity target)
	{
		ArrayList<CombatEntity> targets = new ArrayList<CombatEntity>();
		targets.add(target);
		TargetCombatEntity(targets);
	}
	
	public void TargetCombatEntity(ArrayList<CombatEntity> targets)
	{
		ArrayList<CombatEntity> targetsToDisplay = targets;
		if(this.abilityToUse instanceof DelayedAbility)
		{
			if(!((DelayedAbility)this.abilityToUse).HasInitialEffect())
			{
				targetsToDisplay = new ArrayList<CombatEntity>();
			}
		}
		
		ArrayList<String> messageQueue = new ArrayList<String>();
		this.combatEngine.Attack(this.entityForCurrentTurn, targets, this.abilityToUse, messageQueue);
		this.buttonList.clear();
		TurnState next = new TurnState(mc);
		next.SetState(TurnState.DisplayingAttack, this.turnState.activeEntity, this.abilityToUse, targetsToDisplay);
		this.turnState.SetDisplayMessageState(messageQueue, next);
		
		for(int i = 0; i<targetsToDisplay.size(); i++){
			String soundName = "damage.hit";
			try 
			{
				Method getHurtSoundMethod = targets.get(i).innerEntity.getClass().getDeclaredMethod("getHurtSound");
				getHurtSoundMethod.setAccessible(true);
				soundName = (String)getHurtSoundMethod.invoke(targets.get(i).innerEntity);
			} 
			catch (Exception e)	{}
			this.mc.sndManager.playSoundFX(soundName, .99F, 1.0F);
		}
	}
	
	private Boolean EndAttack()
	{
		int activeEnemies = 0;
		for(CombatEntity enemy : this.enemies)
		{
			enemy.ApplyDamage();
			if(enemy.currentHp > 0)
			{
				activeEnemies++;
			}
		}
		
		int activeAllies = 0;
		for(CombatEntity ally : this.allies)
		{
			ally.ApplyDamage();
			if(ally.currentHp > 0)
			{
				activeAllies++;
			}
		}
		
		boolean endCombatCalled = false;
		CombatEntity currentEntity = this.entityForCurrentTurn;
		this.ClearAttack();
		if(!(this.turnState.phase == TurnState.DisplayingEndOfTurn))
		{
			Boolean needsDisplay = this.combatEngine.EndTurn(currentEntity);
			if(activeEnemies == 0 || activeAllies == 0)
			{
				SetEndOfCombat();
				endCombatCalled = true;
				return false;
			}
			else if(needsDisplay)
			{
				this.entityForCurrentTurn = currentEntity;
				return true;
			}
		}

		if(!endCombatCalled && (activeEnemies == 0 || activeAllies == 0))
		{
			SetEndOfCombat();
		}
		else  // Start next turn
		{
			this.StartNextTurn();
		}
		
		return false;
	}
	
	private void SetEndOfCombat()
	{
		boolean wonBattle = false;
		for(CombatEntity entity : this.allies)
		{
			if(entity.currentHp > 0)
			{
				wonBattle = true;
			}
		}
		
		ArrayList<String> messageQueue = new ArrayList<String>();
		if(wonBattle)
		{
			SimpleEntry<Integer, Integer> rewards = this.combatEngine.GetXpAndApReward(this.enemies);
			messageQueue.add("You are victorious!");
			messageQueue.add(String.format("Gained %s XP, %s AP", rewards.getKey(), rewards.getValue()));
			for(CombatEntity e : this.allies)
			{
				LevelingEngine.Instance.GainXP(e, rewards.getKey(), rewards.getValue(), messageQueue);
			}
		}
		else
		{
			messageQueue.add("You have been defeated.");
		}
		
		TurnState next = new TurnState(this.mc);
		next.SetState(TurnState.EndOfCombat, null, null, (CombatEntity)null);
		this.turnState.SetDisplayMessageState(messageQueue, next);
	}
	
	private void StartNextTurn()
	{
		CombatEntity next = this.combatEngine.GetNextTurn();
		this.entityForCurrentTurn = next;
		
		if(this.allies.contains(next)) 	// If next turn is for player
		{
			Pair<ICombatAbility, ArrayList<CombatEntity>> queued = this.combatEngine.GetQueuedAbility(next);
			if(queued != null)
			{
				ArrayList<String> messageQueue = new ArrayList<String>();
				this.combatEngine.Attack(next, queued.item2, queued.item1, messageQueue);
				TurnState nextTurn = new TurnState(this.mc);
				nextTurn.SetState(TurnState.DisplayingAttack, next, queued.item1, queued.item2);
				this.turnState.SetDisplayMessageState(messageQueue, nextTurn);
			}
			else
			{
				this.turnState.SetState(TurnState.PlayerControl, next, null, (ArrayList<CombatEntity>)null);
				this.display.DisplayCommandButtons();
			}
		}
		else  // If next turn is for enemies
		{
			ArrayList<String> messageQueue = new ArrayList<String>();
			Pair<ICombatAbility, ArrayList<CombatEntity>> attack = this.combatEngine.Attack(next, messageQueue);
			TurnState nextTurn = new TurnState(this.mc);
			nextTurn.SetState(TurnState.DisplayingAttack, next, attack.item1, attack.item2);
			this.turnState.SetDisplayMessageState(messageQueue, nextTurn);
		}
	}
	
	private void ClearAttack()
	{
		this.entityForCurrentTurn = null;
		this.abilityToUse = null;
	}
	
	private void EndCombat()
	{
		DamageSource source = DamageSource.causePlayerDamage(this.player);
		source.damageType = "bypass";
		boolean anyAlliesAlive = false;
		for(CombatEntity entity : this.allies)
		{
			if(entity.currentHp > 0)
			{
				anyAlliesAlive = true;
			}
		}
		
		for(int i = 0; i<this.allies.size(); i++)
		{
			CombatEntity entity = this.allies.get(i);
			if(entity.currentHp < 1)
			{
				EntityLiving deadEntity = entity.innerEntity;
				if(deadEntity instanceof EntityPlayer)
				{
					if(!anyAlliesAlive)
					{
						SyncTagToServer((EntityPlayer)deadEntity);
						this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCSetHealth", "0".getBytes()));
					}
					else
					{
						this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCSetHealth", "1".getBytes()));
						NBTTagCompound tag = deadEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
						tag.setInteger("TBCPlayerMP", entity.currentMp);
						deadEntity.getEntityData().setCompoundTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
						SyncTagToServer((EntityPlayer)deadEntity);
					}
				}
				else if(deadEntity.getEntityData().hasKey("henchmanIndex"))
				{
					int index = deadEntity.getEntityData().getInteger("henchmanIndex");
					ItemStack h = this.henchmenItems[index];
					int currentMp = entity.currentMp;
					this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCSetDur", (index + ",101," + currentMp).getBytes()));
				}
			}
			else if(entity.innerEntity != null && entity.innerEntity instanceof EntityPlayer)
			{
				int maxHealth = entity.innerEntity.getMaxHealth();
				float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
				int healthToSet = Math.round((currentHpPercentage * maxHealth) + .499999F);
				this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCSetHealth", ("" + healthToSet).getBytes()));
				
				NBTTagCompound tag = entity.innerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
				tag.setInteger("TBCPlayerMP", entity.currentMp);
				entity.innerEntity.getEntityData().setCompoundTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
				SyncTagToServer((EntityPlayer)entity.innerEntity);
			}
			else if(entity.innerEntity.getEntityData().hasKey("henchmanIndex"))
			{
				float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
				int healthToSet = Math.round((currentHpPercentage * 100) + .499999F);
				int index = entity.innerEntity.getEntityData().getInteger("henchmanIndex");
				ItemStack h = this.henchmenItems[index];
				this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCSetDur", (index + "," + (100 - healthToSet) + "," + entity.currentMp).getBytes()));
			}
		}
		
		for(int i = 0; i<this.enemies.size(); i++)
		{
			CombatEntity entity = this.enemies.get(i);
			if(entity.currentHp < 1)
			{
				EntityLiving deadEntity = entity.innerEntity;
				deadEntity.attackEntityFrom(source, 1000);
				deadEntity.setLastAttackingEntity(this.player);
			}
		}
		
		this.mc.thePlayer.closeScreen();
		this.mc.displayGuiScreen((GuiScreen)null);
		MainMod.enemy = null;
		MainMod.setEnemies = null;
	}

	public void drawScreen(int par1, int par2, float par3) 
	{
		String message = null;
		if(turnState.messages != null && turnState.messages.size() > 0)
		{
			message = turnState.messages.get(0);
		}
		
        this.display.drawBackground(this.turnState, this.enemies, this.allies, message);
        super.drawScreen(par1, par2, par3);
        
        if(this.turnState.phase == TurnState.DisplayingMessage)
        {
        	if(this.turnState.GetElapsedTime() > 1000 || (skipMessage && this.turnState.GetElapsedTime() > 250))
	        {
	        	if(turnState.messages != null && turnState.messages.size() > 0)
	        	{
	        		turnState.messages.remove(0);
	        	}
	        
	        	this.turnState.phaseStartTime = this.mc.getSystemTime();
	        }
        	
        	if(turnState.messages == null || turnState.messages.size() == 0)
        	{
        		this.turnState.SetState(this.turnState.nextState.phase, this.turnState.nextState.activeEntity, this.turnState.nextState.ability, this.turnState.nextState.targetEntities);
        	}
        }
        
        this.skipMessage = false;
        this.display.drawForeground(this.turnState, this.enemies, this.allies);
        if(this.turnState.phase == TurnState.DisplayingAttack && this.turnState.GetElapsedTime() > this.turnState.ability.GetAnimationTime())
        {
        	Boolean displayEndOfTurn = this.EndAttack();
        	if(displayEndOfTurn)
        	{
        		this.turnState.SetState(TurnState.DisplayingEndOfTurn, turnState.activeEntity, null, turnState.targetEntities);
        	}
        }
        
        if(this.turnState.phase == TurnState.DisplayingEndOfTurn && this.turnState.GetElapsedTime() > 400)
        {
        	this.EndAttack();
        }
        
        if(this.turnState.phase == TurnState.EndOfCombat)
        {
    		this.EndCombat();
    		MainMod.lastAttackTime = this.mc.getSystemTime();
        }
    }

    protected void mouseClicked(int par1, int par2, int par3)
    {
        if (par3 == 0)
        {
        	this.skipMessage = true;
            for (int l = 0; l < this.buttonList.size(); ++l)
            {
                GuiButton guibutton = (GuiButton)this.buttonList.get(l);
                if (guibutton.mousePressed(this.mc, par1, par2))
                {
                	boolean playSound = true;
            		if(guibutton instanceof GenericGuiButton)
            		{
            			((GenericGuiButton)guibutton).onClick();
            		}
            		
            		if(guibutton instanceof GenericScrollBox)
            		{
            			playSound = ((GenericScrollBox)guibutton).onClick(par1, par2);
            		}
                	
            		if(playSound)
            		{
            			this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
            		}
            		
                    super.actionPerformed(guibutton);
                }
            }
        }
    }
	
	private void SyncTagToServer(EntityPlayer playerEntity)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			NBTTagCompound.writeNamedTag(playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG), new DataOutputStream(outputStream));
		} catch (IOException e) {}
			
		Minecraft.getMinecraft().getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCPlayerData", outputStream.toByteArray()));
	}
}
