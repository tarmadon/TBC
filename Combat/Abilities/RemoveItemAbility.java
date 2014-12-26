package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import TBC.MainMod;
import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.UsableItem;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.TurnState;
import TBC.Messages.StringMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class RemoveItemAbility implements ICombatAbility
{
	public static final int MainInventory = 0;
	public static final int ArmorInventory = 1;
	private ICombatAbility itemAbility;
	private UsableItem item;
	private Pair<Integer, Integer> inventoryItemPosition;
	private int damage;
	private int playerId;

	public RemoveItemAbility(int playerId, Pair<Integer, Integer> inventoryItemPosition, int damage, ICombatAbility itemAbility, UsableItem item)
	{
		this.itemAbility = itemAbility;
		this.damage = damage;
		this.inventoryItemPosition = inventoryItemPosition;
		this.playerId = playerId;
		this.item = item;
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
		EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().theWorld.getEntityByID(this.playerId);
		if(this.inventoryItemPosition.item1 == RemoveItemAbility.ArmorInventory)
		{
			return player.inventory.armorInventory[this.inventoryItemPosition.item2].getDisplayName();
		}
		else
		{
			return player.inventory.mainInventory[this.inventoryItemPosition.item2].getDisplayName();
		}
	}

	public ArrayList<String> GetDescription() 
	{
		return this.item.DescriptionStrings();
	}
	
	public int GetAbilityTarget()
	{
		return itemAbility.GetAbilityTarget();
	}

	public IOneTimeEffect[] GetEffects(CombatEngine engine, CombatEntity user, ArrayList<CombatEntity> targets, ArrayList<String> messages)
	{
		EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().theWorld.getEntityByID(this.playerId);
		StringMessage syncToServer = new StringMessage();
		syncToServer.Data = this.inventoryItemPosition.item1 + "," + this.inventoryItemPosition.item2 + "," + this.damage;
		MainMod.removeItemHandler.sendToServer(syncToServer);
		messages.add(this.GetAbilityName());
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
				stack.damageItem(this.damage, player);
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
				player.inventory.armorInventory[this.inventoryItemPosition.item2].damageItem(this.damage, player);
			}
		}

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

	@Override
	public Boolean IsUsableInCombat() 
	{
		return true;
	}
}
