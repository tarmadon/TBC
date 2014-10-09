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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.SidedProxy;
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
import TBC.Messages.CombatCommandMessage;
import TBC.Messages.CombatEndedMessage;
import TBC.Messages.CombatSyncDataMessage;
import TBC.Messages.NBTTagCompoundMessage;
import TBC.Messages.StringMessage;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BattleScreenClient extends GuiScreen
{
	private boolean combatEnding = false;
	private CombatSyncDataMessage dataBeingProcessed = null;
	private CombatEndedMessage combatEnded = null;
	
	private Queue<CombatSyncDataMessage> messagesLeftToShow = new ArrayBlockingQueue<CombatSyncDataMessage>(100);
	private Integer nextPlayerControl = null;
	
	private ArrayList<CombatEntity> enemies = new ArrayList<CombatEntity>();
	private ArrayList<CombatEntity> allies = new ArrayList<CombatEntity>();

	private CombatEngine combatEngine;
	private CombatEntity entityForCurrentTurn;
	private ICombatAbility abilityToUse;
	private BattleScreenDrawer display;
	private boolean skipMessage = false;
	private long combatId;
	
	private TurnState nextState = null;

	private TurnState turnState;
	private ItemStack[] henchmenItems;
	private EntityPlayer player;
	private World world;
	
	public BattleScreenClient(long combatId, ArrayList<CombatEntity> allies, ArrayList<CombatEntity> enemies)
	{
		this.player = Minecraft.getMinecraft().thePlayer;
		this.world = Minecraft.getMinecraft().theWorld;
		this.combatId = combatId;
		this.turnState = new TurnState(this.mc);
		this.display = new BattleScreenDrawer(this);
		this.allies = allies;
		this.enemies = enemies;
		this.combatEngine = new CombatEngine(allies, enemies, false, 0);
		//this.world.removeEntity(this.player);
	}

	public void SyncCombatData(CombatSyncDataMessage message)
	{
		this.messagesLeftToShow.add(message);
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
		return false;
	}

	protected void keyTyped(char par1, int par2)
	{
	};

	public void SetPlayerControl(Integer activeEntity)
	{
		this.nextPlayerControl = activeEntity;
	}
	
	public void ChooseAbilityCommand()
	{
		this.display.DisplayAbilityButtons(this.entityForCurrentTurn, this.combatEngine.GetChoosableAbilitiesForEntity(this.entityForCurrentTurn));
	}

	public void ChooseItemCommand()
	{
		this.display.DisplayItemButtons(this.mc, Minecraft.getMinecraft().thePlayer);
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
		this.abilityToUse = null;
		this.TargetCombatEntity(new ArrayList<CombatEntity>());
	}

	public void TargetCombatEntity(CombatEntity target)
	{
		ArrayList<CombatEntity> targets = new ArrayList<CombatEntity>();
		targets.add(target);
		TargetCombatEntity(targets);
	}

	public void TargetCombatEntity(ArrayList<CombatEntity> targets)
	{
		ArrayList<Integer> targetIds = new ArrayList<Integer>();
		for(int i = 0; i < targets.size(); i++)
		{
			targetIds.add(targets.get(i).id);
		}
		
		CombatCommandMessage m = new CombatCommandMessage();
		m.BattleId = this.combatId;
		m.AbilityToUse = this.abilityToUse;
		m.User = this.entityForCurrentTurn.id;
		m.Targets = targetIds;
		
		MainMod.playerCommandHandler.sendToServer(m);
		this.buttonList.clear();
	}

	private void EndAttack()
	{
		this.combatEngine.allies = this.dataBeingProcessed.Allies;
		this.combatEngine.enemies = this.dataBeingProcessed.Enemies;
		this.allies = this.dataBeingProcessed.Allies;
		this.enemies = this.dataBeingProcessed.Enemies;
		this.dataBeingProcessed = null;
		this.entityForCurrentTurn = null;
		this.abilityToUse = null;
		this.turnState.SetState(TurnState.Waiting, null, null, (ArrayList<CombatEntity>)null);
	}

	public void SetEndOfCombat(CombatEndedMessage message)
	{
		this.combatEnded = message;
	}
	
	private void StartEndingCombat()
	{
		boolean wonBattle = this.combatEnded.Won;
		ArrayList<String> messageQueue = new ArrayList<String>();
		if(this.combatEnded.XPGained == null && wonBattle)
		{
			messageQueue.add("Successfully escaped!");
		}
		else if(wonBattle)
		{
			messageQueue.add("You are victorious!");
			messageQueue.add(String.format("Gained %s XP, %s AP", this.combatEnded.XPGained, this.combatEnded.APGained));
		}
		else
		{
			messageQueue.add("You have been defeated.");
		}

		this.turnState.SetState(TurnState.DisplayingMessage, null, null, (ArrayList<CombatEntity>)null);
		this.turnState.messages = messageQueue;
		this.turnState.nextState = TurnState.EndOfCombat;
	}

	private void StartNextTurn(Integer entityId)
	{
		CombatEntity found = null;
		for(int i = 0; i < this.allies.size(); i++)
		{
			if(this.allies.get(i).id == entityId)
			{
				found = this.allies.get(i);
			}
		}
		
		if(found == null)
		{
			return;
		}
		
		this.entityForCurrentTurn = found;
		this.turnState.SetState(TurnState.PlayerControl, found, null, (ArrayList<CombatEntity>)null);
		this.display.DisplayCommandButtons();
	}

	public void EndCombat()
	{
		//this.world.spawnEntityInWorld(this.player);
		this.mc.thePlayer.closeScreen();
		this.mc.displayGuiScreen((GuiScreen)null);
	}

	public void drawScreen(int par1, int par2, float par3)
	{
		if(!combatEnding && this.dataBeingProcessed == null)
		{
			if(!this.messagesLeftToShow.isEmpty())
			{
				CombatSyncDataMessage next = this.messagesLeftToShow.remove();
				if(next != null)
				{
					this.dataBeingProcessed = next;
					this.turnState = new TurnState(Minecraft.getMinecraft());
					
					this.turnState.SetState(TurnState.DisplayingAttack, FindEntityWithId(this.dataBeingProcessed.User.item1), this.dataBeingProcessed.AbilityUsed, FindEntitiesWithIds(this.dataBeingProcessed.Targets));
					if(this.dataBeingProcessed.Messages.size() > 0)
					{
						this.turnState.phase = TurnState.DisplayingMessage;
						this.turnState.messages = this.dataBeingProcessed.Messages;
						this.turnState.nextState = TurnState.DisplayingAttack;
					}
				}
			}
			else if(this.combatEnded != null)
			{
				StartEndingCombat();
				combatEnding = true;
			}
			else if(this.nextPlayerControl != null)
			{
				StartNextTurn(this.nextPlayerControl);
				this.nextPlayerControl = null;
			}
		}
		
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
        		this.turnState.phase = this.turnState.nextState;
        		this.turnState.nextState = 0;
        	}
        }

        this.skipMessage = false;
        this.display.drawForeground(this.turnState, this.enemies, this.allies);
        if(this.turnState.phase == TurnState.DisplayingAttack && (this.turnState.ability == null || this.turnState.GetElapsedTime() > this.turnState.ability.GetAnimationTime()))
        {
        	this.EndAttack();
        }

        if(this.turnState.phase == TurnState.DisplayingEndOfTurn && this.turnState.GetElapsedTime() > 400)
        {
        	this.EndAttack();
        }
        
        if(this.turnState.phase == TurnState.EndOfCombat)
        {
        	EndCombat();
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
            			this.mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            		}

                    super.actionPerformed(guibutton);
                }
            }
        }
    }
    
    private ArrayList<CombatEntity> FindEntitiesWithIds(ArrayList<Pair<Integer, Integer>> idsToFind)
    {
    	ArrayList<CombatEntity> entities = new ArrayList<CombatEntity>();
    	for(int i = 0; i < idsToFind.size(); i++)
    	{
    		CombatEntity found = FindEntityWithId(idsToFind.get(i).item1);
    		if(found != null)
    		{
    			found.lastDamageTaken = idsToFind.get(i).item2;
    			entities.add(found);
    		}
    	}
    	
    	return entities;
    }
    
    private CombatEntity FindEntityWithId(int idToFind)
    {
    	for(int i = 0; i < this.allies.size(); i++)
    	{
    		if(this.allies.get(i).id == idToFind)
    		{
    			return this.allies.get(i);
    		}
    	}
    	
    	for(int i = 0; i < this.enemies.size(); i++)
    	{
    		if(this.enemies.get(i).id == idToFind)
    		{
    			return this.enemies.get(i);
    		}
    	}
    	
    	return null;
    }
}
