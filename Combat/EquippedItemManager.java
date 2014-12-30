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
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.IAbility;
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
	public static final String OffHandItemSlot = "OffHand";
	public static final String ArmorItemSlot = "Armor";
	public static final String AccItemSlot = "Acc";

	public static EquippedItemManager Instance = new EquippedItemManager();
	public Hashtable<String, Hashtable<String, EquippedItem>> lookup = new Hashtable<String, Hashtable<String, EquippedItem>>();
	public Hashtable<String, UsableItem> usableLookup = new Hashtable<String, UsableItem>();
	public Hashtable<String, Hashtable<String, ConstantItem>> constantLookup = new Hashtable<String, Hashtable<String, ConstantItem>>();

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
				if(split.length < 3)
				{
					continue;
				}
				
				ArrayList<String> descriptions = new ArrayList<String>();
				if(split.length >= 9)
				{
					descriptions = SplitString(split[8].trim());
				}
				
				if(split[1].trim().toLowerCase().contains("flat"))
				{
					if(split.length < 6)
					{
						continue;
					}
					
					ArrayList<String> effectTypeSplit = SplitString(split[4].trim());
					ArrayList<String> effectStrengthSplit = SplitString(split[5].trim());
					ArrayList<Pair<Integer, Integer>> modifiers = new ArrayList<Pair<Integer,Integer>>();
					for(int i = 0; i < effectTypeSplit.size(); i++)
					{
						int effectType = Integer.parseInt(effectTypeSplit.get(i));
						String effectStrengthString = effectStrengthSplit.get(i);
						if(!effectStrengthString.isEmpty())
						{
							int effectStrength = Integer.parseInt(effectStrengthString);
							modifiers.add(new Pair<Integer, Integer>(effectType, effectStrength));
						}
					}
					
					String slot = split[2].trim();
					ArrayList<String> proficiencies = SplitString(split[3].trim());
					String itemName = split[0].trim();
					if(!lookup.containsKey(itemName))
					{
						lookup.put(itemName, new Hashtable<String, EquippedItem>());
					}
					
					lookup.get(itemName).put(slot, new FlatBonusEquippedItem(modifiers, slot, descriptions, proficiencies));
				}

				if(split[1].trim().toLowerCase().contains("use"))
				{
					if(split.length < 7)
					{
						continue;
					}
					
					String abilityName = split[6].trim();
					ArrayList<String> splitAbilityNames = SplitString(abilityName);
					ArrayList<ICombatAbility> usableAbilities = new ArrayList<ICombatAbility>();
					ArrayList<ConstantAbility> constantAbilities = new ArrayList<ConstantAbility>();
					for(String splitAbilityName : splitAbilityNames)
					{
						IAbility ability = AbilityLookup.Instance.GetAbilityWithName(splitAbilityName);
						if(ability != null)
						{
							if(ability instanceof ICombatAbility)
							{
								usableAbilities.add((ICombatAbility)ability);
							}
							else if(ability instanceof ConstantAbility)
							{
								constantAbilities.add((ConstantAbility)ability);	
							}
						}
					}
					
					ArrayList<String> proficiencies = SplitString(split[3].trim());
					if(usableAbilities.size() > 0)
					{
						usableLookup.put(split[0].trim(), new UsableItem(usableAbilities, Integer.parseInt(split[7].trim()), descriptions, proficiencies));
					}
					
					if(constantAbilities.size() > 0)
					{
						String itemName = split[0].trim();
						String slot = split[2].trim();
						if(!constantLookup.containsKey(itemName))
						{
							constantLookup.put(itemName, new Hashtable<String, ConstantItem>());
						}
						
						constantLookup.get(itemName).put(slot, new ConstantItem(descriptions, proficiencies, constantAbilities));
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

	public ArrayList<String> SplitString(String original)
	{
		ArrayList<String> split = new ArrayList<String>();
		String[] asArray = original.split(";");
		for(String item : asArray)
		{
			if(!item.isEmpty())
			{
				split.add(item);
			}
		}
		
		return split;
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
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.ArmorInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility().get(0), ability);
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
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.MainInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility().get(0), ability);
					usableItems.add(new Pair(itemAbility, s.stackSize));
				}
			}
		}

		return usableItems;
	}

	public ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>> GetEquippableItemsForPlayer(String slot, Minecraft mc, EntityPlayer player)
	{
		ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>> usableItems = new ArrayList<Quintuplet<Integer, Integer, Item, EquippedItem>>();
		InventoryPlayer inventory = player.inventory;
		for(int i = 0; i<inventory.armorInventory.length; i++)
		{
			ItemStack s = inventory.armorInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(lookup.containsKey(effectiveItemName) && lookup.get(effectiveItemName).containsKey(slot))
				{
					EquippedItem item = lookup.get(effectiveItemName).get(slot);
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
				if(lookup.containsKey(effectiveItemName) && lookup.get(effectiveItemName).containsKey(slot))
				{
					EquippedItem item = lookup.get(effectiveItemName).get(slot);
					usableItems.add(new Quintuplet(RemoveItemAbility.MainInventory, i, s.getItem(), item));
				}
			}
		}

		return usableItems;
	}
	
	public EquippedItem GetEquippedItem(ItemStack itemStack, String slot)
	{
		return lookup.get(itemStack.getItem().getUnlocalizedName().replaceFirst("item.", "")).get(slot);
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
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.ArmorInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility().get(0), ability);
					usableItems.add(new Quintuplet(s.getItem(), null, itemAbility, 1));
				}
				else if(lookup.containsKey(effectiveItemName))
				{
					EquippedItem item = lookup.get(effectiveItemName).elements().nextElement();
					usableItems.add(new Quintuplet(s.getItem(), item, null, 1));
				}
				else if(constantLookup.containsKey(effectiveItemName))
				{
					ConstantItem item = constantLookup.get(effectiveItemName).elements().nextElement();
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
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.MainInventory, i), ability.GetDamageFromUse(), ability.GetUseAbility().get(0), ability);
					usableItems.add(new Quintuplet(s.getItem(), null, itemAbility, s.stackSize));
				}
				else if(lookup.containsKey(effectiveItemName))
				{
					EquippedItem item = lookup.get(effectiveItemName).elements().nextElement();
					usableItems.add(new Quintuplet(s.getItem(), item, null, s.stackSize));
				}
				else if(constantLookup.containsKey(effectiveItemName))
				{
					ConstantItem item = constantLookup.get(effectiveItemName).elements().nextElement();
					usableItems.add(new Quintuplet(s.getItem(), item, null, s.stackSize));
				}
			}
		}

		return usableItems;
	}

	public ArrayList<ConstantAbility> GetConstantAbilitiesFromEquippedItems(NBTTagCompound tag)
	{
		ArrayList<ConstantAbility> equippedItems = new ArrayList<ConstantAbility>();
		ItemStack[] items = this.GetEquippedItems(tag);
		for(int i = 0; i < items.length; i++)
		{
			ItemStack s = items[i];
			if(s != null)
			{
				String slot = this.GetSlotForIndex(i);
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(constantLookup.containsKey(effectiveItemName) && constantLookup.get(effectiveItemName).containsKey(slot))
				{
					ConstantItem ability = constantLookup.get(effectiveItemName).get(slot);
					equippedItems.addAll(ability.GetConstantAbilities());
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
			for(int i = 0; i < items.length; i++)
			{
				ItemStack item = items[i];
				if(item != null)
				{
					String itemName = item.getItem().getUnlocalizedName();
					returnStat = ApplyFoundItem(itemName, GetSlotForIndex(i), statType, returnStat);
				}
			}
		}

		return returnStat;
	}

	private int ApplyFoundItem(String itemName, String slot, int statType, int currentStat)
	{
		String effectiveItemName = itemName.replaceFirst("item.", "");
		if(lookup.containsKey(effectiveItemName))
		{
			EquippedItem foundItem = lookup.get(effectiveItemName).get(slot);
			if(foundItem.HasEffect(statType))
			{
				return foundItem.GetModifiedValue(statType, currentStat);
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
	
	public String GetSlotForIndex(int index)
	{
		if(index == 0)
		{
			return EquippedItemManager.MainHandItemSlot;
		}
		else if(index == 1)
		{
			return EquippedItemManager.OffHandItemSlot;
		}
		else if(index == 2)
		{
			return EquippedItemManager.ArmorItemSlot;
		}
		else if(index == 3)
		{
			return EquippedItemManager.AccItemSlot;
		}
		else if(index == 4)
		{
			return EquippedItemManager.AccItemSlot;
		}
		
		return EquippedItemManager.MainHandItemSlot;
	}
	
    public static void SetItem(int slot, ItemStack item, NBTTagCompound tag)
    {
		NBTTagCompound itemTag = new NBTTagCompound();
		if(item != null)
		{
			itemTag = item.writeToNBT(itemTag);
		}
		
		tag.setTag("TBCslot" + slot, itemTag);
    }
}
