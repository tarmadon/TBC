package TBC.Combat;

import java.awt.Window.Type;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import TBC.Pair;
import TBC.PlayerSaveData;
import TBC.Quintuplet;
import TBC.Triplet;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Abilities.RemoveItemAbility;
import TBC.Combat.Effects.StatChangeStatus;

import scala.Console;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class EquippedItemManager
{
	public static final String MainHandItemSlot = "MainHand";
	public static final String TorsoItemSlot = "Torso";
	public static final String FootItemSlot = "Foot";
	public static final String HeadItemSlot = "Head";
	public static final String LegsItemSlot = "Legs";

	public static EquippedItemManager Instance = new EquippedItemManager();
	public Hashtable<String, EquippedItem> lookup = new Hashtable<String, EquippedItem>();
	public Hashtable<String, UsableItem> usableLookup = new Hashtable<String, UsableItem>();

	private File file;

	public void Initialize(File file)
	{
		this.file = file;
		BufferedReader buffer = null;
		try
		{
			String defaultEncoding = "UTF-8";
			InputStreamReader input = new InputStreamReader(new FileInputStream(file), defaultEncoding);
			buffer = new BufferedReader(input);
			String nextLine;

			// Get the headers out of the way.
			buffer.readLine();
			while((nextLine = buffer.readLine()) != null)
			{
				if(nextLine.startsWith("##"))
				{
					break;
				}

				String[] split = nextLine.split(",");
				if(split.length < 5)
				{
					continue;
				}

				ArrayList<String> descriptions = new ArrayList<String>();
				if(split.length >= 9)
				{
					String descriptionRawString = split[8].trim();
					String[] descriptionSplit = descriptionRawString.split(";");
					for(String description : descriptionSplit)
					{
						descriptions.add(description);
					}
				}
				
				if(split[1].trim().toLowerCase().contains("flat"))
				{
					int effectType = Integer.parseInt(split[4].trim());
					int effectStrength = Integer.parseInt(split[5].trim());
					String slot = split[2].trim();
					boolean requiresWorn = Boolean.parseBoolean(split[3].trim());
					
					
					lookup.put(split[0].trim(), new FlatBonusEquippedItem(effectType, effectStrength, slot, requiresWorn, descriptions));
				}

				if(split[1].trim().toLowerCase().contains("use"))
				{
					String abilityName = split[6].trim();
					ICombatAbility ability = AbilityLookup.Instance.GetAbilityWithName(abilityName);
					if(ability != null)
					{
						usableLookup.put(split[0].trim(), new UsableItem(ability, Integer.parseInt(split[7].trim()), descriptions));
					}
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(buffer != null)
			{
				try
				{
					buffer.close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	public ArrayList<Pair<ICombatAbility, Integer>> GetUsableItemsForPlayer(Minecraft mc, EntityPlayer player)
	{
		ArrayList<Pair<ICombatAbility, Integer>> usableItems = new ArrayList<Pair<ICombatAbility, Integer>>();
		InventoryPlayer inventory = player.inventory;
		for(int i = 0; i<inventory.armorInventory.length; i++)
		{
			ItemStack s = inventory.armorInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(usableLookup.containsKey(effectiveItemName))
				{
					UsableItem ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.ArmorInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility(), ability);
					usableItems.add(new Pair(itemAbility, 1));
				}
			}
		}

		for(int i = 0; i<inventory.mainInventory.length; i++)
		{
			ItemStack s = inventory.mainInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(usableLookup.containsKey(effectiveItemName))
				{
					UsableItem ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.MainInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility(), ability);
					usableItems.add(new Pair(itemAbility, s.stackSize));
				}
			}
		}

		return usableItems;
	}

	public ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>> GetEquippableItemsForPlayer(Minecraft mc, EntityPlayer player)
	{
		ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>> usableItems = new ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>>();
		InventoryPlayer inventory = player.inventory;
		for(int i = 0; i<inventory.armorInventory.length; i++)
		{
			ItemStack s = inventory.armorInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(lookup.containsKey(effectiveItemName))
				{
					EquippedItem item = lookup.get(effectiveItemName);
					usableItems.add(new Quintuplet(RemoveItemAbility.ArmorInventory, i, s.getItem(), item));
				}
			}
		}

		for(int i = 0; i<inventory.mainInventory.length; i++)
		{
			ItemStack s = inventory.mainInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(lookup.containsKey(effectiveItemName))
				{
					EquippedItem item = lookup.get(effectiveItemName);
					usableItems.add(new Quintuplet(RemoveItemAbility.MainInventory, i, s.getItem(), item));
				}
			}
		}

		return usableItems;
	}
	
	public EquippedItem GetEquippedItem(ItemStack itemStack)
	{
		return lookup.get(itemStack.getItem().getUnlocalizedName().replaceFirst("item.", ""));
	}
	
	public ArrayList<Quintuplet<Item, EquippedItem, ICombatAbility, Integer>> GetAllKnownItemsForPlayer(Minecraft mc, EntityPlayer player)
	{
		ArrayList<Quintuplet<Item, EquippedItem, ICombatAbility, Integer>> usableItems = new ArrayList<Quintuplet<Item, EquippedItem, ICombatAbility, Integer>>();
		InventoryPlayer inventory = player.inventory;
		for(int i = 0; i<inventory.armorInventory.length; i++)
		{
			ItemStack s = inventory.armorInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(usableLookup.containsKey(effectiveItemName))
				{
					UsableItem ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.ArmorInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility(), ability);
					usableItems.add(new Quintuplet(s.getItem(), null, itemAbility, 1));
				}
				else if(lookup.containsKey(effectiveItemName))
				{
					EquippedItem item = lookup.get(effectiveItemName);
					usableItems.add(new Quintuplet(s.getItem(), item, null, 1));
				}
			}
		}

		for(int i = 0; i<inventory.mainInventory.length; i++)
		{
			ItemStack s = inventory.mainInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(usableLookup.containsKey(effectiveItemName))
				{
					UsableItem ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.MainInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility(), ability);
					usableItems.add(new Quintuplet(s.getItem(), null, itemAbility, s.stackSize));
				}
				else if(lookup.containsKey(effectiveItemName))
				{
					EquippedItem item = lookup.get(effectiveItemName);
					usableItems.add(new Quintuplet(s.getItem(), item, null, s.stackSize));
				}
			}
		}

		return usableItems;
	}

	public ArrayList<ICombatAbility> GetAbilitiesFromEquippedItems(NBTTagCompound tag)
	{
		ArrayList<ICombatAbility> equippedItems = new ArrayList<ICombatAbility>();
		ItemStack[] items = this.GetEquippedItems(tag);
		for(ItemStack s : items)
		{
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(usableLookup.containsKey(effectiveItemName))
				{
					UsableItem ability = usableLookup.get(effectiveItemName);
					equippedItems.add(ability.GetUseAbility());
				}
			}
		}

		return equippedItems;
	}

	public int GetEffectiveStat(CombatEntity entity, int statType, int currentStat)
	{
		int returnStat = currentStat;
		if(entity.tag != null)
		{
			ItemStack[] items = this.GetEquippedItems(entity.tag);
			for(ItemStack item : items)
			{
				if(item != null)
				{
					String itemName = item.getItem().getUnlocalizedName();
					returnStat = ApplyFoundItem(itemName, statType, returnStat);
				}
			}
		}

		return returnStat;
	}

	private int ApplyFoundItem(String itemName, int statType, int currentStat)
	{
		String effectiveItemName = itemName.replaceFirst("item.", "");
		if(lookup.containsKey(effectiveItemName))
		{
			EquippedItem foundItem = lookup.get(effectiveItemName);
			if(foundItem.HasEffect(statType))
			{
				return foundItem.GetModifiedValue(currentStat);
			}
		}

		return currentStat;
	}
	
	public ItemStack[] GetEquippedItems(NBTTagCompound tag)
	{	
		ItemStack[] equipped = new ItemStack[5];
		for(int i = 0; i < 5; i++)
		{
			if(tag.hasKey("TBCslot" + i))
			{
				equipped[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("TBCslot" + i));
			}
		}
		
		return equipped;
	}
	
    public static void SetItem(int slot, ItemStack item, NBTTagCompound tag)
    {
    	NBTTagCompound itemTag = new NBTTagCompound();
    	itemTag = item.writeToNBT(itemTag);
    	tag.setTag("TBCslot" + slot, itemTag);
    }
}
