package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.TurnState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;

public class RemoveItemAbility implements ICombatAbility
{
	public static final int MainInventory = 0;
	public static final int ArmorInventory = 1;
	private ICombatAbility itemAbility;
	private ItemStack item;
	private Pair<Integer, Integer> inventoryItemPosition;
	private int damage;
	private Minecraft mc;
	private EntityPlayer player;
	
	public RemoveItemAbility(Minecraft mc, EntityPlayer player, Pair<Integer, Integer> inventoryItemPosition, int damage, ItemStack item, ICombatAbility itemAbility)
	{
		this.mc = mc;
		this.itemAbility = itemAbility;
		this.item = item;
		this.damage = damage;
		this.inventoryItemPosition = inventoryItemPosition;
		this.player = player;
	}
	
	public int GetMpCost()
	{
		return 0;
	}

	public Boolean IsUsableOutOfCombat() 
	{
		return this.itemAbility.IsUsableOutOfCombat();
	}
	
	public String GetAbilityName() 
	{
		return item.getDisplayName();
	}

	public int GetAbilityTarget() 
	{
		return itemAbility.GetAbilityTarget();
	}

	public IOneTimeEffect[] GetEffects(CombatEngine engine, CombatEntity user, ArrayList<CombatEntity> targets, ArrayList<String> messages) 
	{
		this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCRemoveItem", (this.inventoryItemPosition.item1 + "," + this.inventoryItemPosition.item2 + "," + this.damage).getBytes()));
		if(this.inventoryItemPosition.item1 == MainInventory)
		{
			ItemStack stack = player.inventory.mainInventory[this.inventoryItemPosition.item2];
			if(this.damage == -1)
			{
				if(stack.stackSize > 1)
				{
					stack.stackSize = stack.stackSize - 1;
				}
				else
				{
					player.inventory.setInventorySlotContents(this.inventoryItemPosition.item2, (ItemStack)null);
				}
			}
			else
			{
				stack.damageItem(this.damage, this.player);
			}
		}
		else
		{
			ItemStack stack = player.inventory.armorInventory[this.inventoryItemPosition.item2];
			if(this.damage == -1)
			{
				if(stack.stackSize > 1)
				{
					stack.stackSize = stack.stackSize - 1;
				}
				else
				{
					player.inventory.armorInventory[this.inventoryItemPosition.item2] = null;
				}
			}
			else
			{
				player.inventory.armorInventory[this.inventoryItemPosition.item2].damageItem(this.damage, this.player);
			}
		}
		
		messages.add(this.GetAbilityName());
		return this.itemAbility.GetEffects(engine, user, targets, messages);
	}

	public Boolean IsSpell() 
	{
		return false;
	}

	public void DrawUser(BattleScreenDrawer display, HashMap<CombatEntity, Pair<Integer, Integer>> positionLookup, TurnState state,
			CombatEntity entity, boolean isAlly, boolean isTarget,
			int startXPos, int startYPos, int startRotation) 
	{
		this.itemAbility.DrawUser(display, positionLookup, state, entity, isAlly, isTarget, startXPos, startYPos, startRotation);
	}

	public void DrawTarget(BattleScreenDrawer display, TurnState state,
			CombatEntity entity, boolean isAlly, int startXPos, int startYPos,
			int startRotation) 
	{
		this.itemAbility.DrawTarget(display, state, entity, isAlly, startXPos, startYPos, startRotation);
	}

	public int GetAnimationTime() 
	{
		return this.itemAbility.GetAnimationTime();
	}
}
