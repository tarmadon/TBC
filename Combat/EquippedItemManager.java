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

public class EquippedItemManager
{
	public static final String MainHandItemSlot = "MainHand";
	public static final String TorsoItemSlot = "Torso";
	public static final String FootItemSlot = "Foot";
	public static final String HeadItemSlot = "Head";
	public static final String LegsItemSlot = "Legs";

	public static EquippedItemManager Instance = new EquippedItemManager();
	public Hashtable<String, EquippedItem> lookup = new Hashtable<String, EquippedItem>();
	public Hashtable<String, Pair<ICombatAbility, Integer>> usableLookup = new Hashtable<String, Pair<ICombatAbility, Integer>>();

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

				if(split[1].trim().toLowerCase().contains("flat"))
				{
					lookup.put(split[0].trim(), new FlatBonusEquippedItem(Integer.parseInt(split[4].trim()), Integer.parseInt(split[5].trim()), split[2].trim(), Boolean.parseBoolean(split[3].trim())));
				}

				if(split[1].trim().toLowerCase().contains("use"))
				{
					String abilityName = split[6].trim();
					ICombatAbility ability = AbilityLookup.Instance.GetAbilityWithName(abilityName);
					if(ability != null)
					{
						usableLookup.put(split[0].trim(), new Pair(ability, Integer.parseInt(split[7].trim())));
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
					Pair<ICombatAbility, Integer> ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.ArmorInventory, i), ability.item2, ability.item1);
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
					Pair<ICombatAbility, Integer> ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.MainInventory, i), ability.item2, ability.item1);
					usableItems.add(new Pair(itemAbility, s.stackSize));
				}
			}
		}

		return usableItems;
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
					Pair<ICombatAbility, Integer> ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.ArmorInventory, i), ability.item2, ability.item1);
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
					Pair<ICombatAbility, Integer> ability = usableLookup.get(effectiveItemName);
					ICombatAbility itemAbility = new RemoveItemAbility(player.getEntityId(), new Pair(RemoveItemAbility.MainInventory, i), ability.item2, ability.item1);
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

	public ArrayList<ICombatAbility> GetAbilitiesFromEquippedItems(Minecraft mc, EntityPlayer player)
	{
		ArrayList<ICombatAbility> equippedItems = new ArrayList<ICombatAbility>();
		InventoryPlayer inventory = player.inventory;
		for(int i = 0; i<inventory.armorInventory.length; i++)
		{
			ItemStack s = inventory.armorInventory[i];
			if(s != null)
			{
				String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
				if(usableLookup.containsKey(effectiveItemName))
				{
					Pair<ICombatAbility, Integer> ability = usableLookup.get(effectiveItemName);

					equippedItems.add(ability.item1);
				}
			}
		}

		ItemStack s = inventory.mainInventory[0];
		if(s != null)
		{
			String effectiveItemName = s.getItem().getUnlocalizedName().replaceFirst("item.", "");
			if(usableLookup.containsKey(effectiveItemName))
			{
				Pair<ICombatAbility, Integer> ability = usableLookup.get(effectiveItemName);
				equippedItems.add(ability.item1);
			}
		}

		return equippedItems;
	}

	public int GetEffectiveStat(CombatEntity entity, int statType, int currentStat)
	{
		ArrayList<String> occupiedSlots = new ArrayList<String>();
		int returnStat = currentStat;
		if(entity.entityType == null)
		{
			EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().theWorld.getEntityByID(entity.id);
			InventoryPlayer inventory = player.inventory;
			for(int i = 0; i<inventory.armorInventory.length; i++)
			{
				if(inventory.armorInventory[i] != null)
				{
					String itemName = inventory.armorInventory[i].getItem().getUnlocalizedName();
					returnStat = ApplyFoundItem(itemName, statType, returnStat, occupiedSlots, true);
				}
			}

			if(inventory.mainInventory[0] != null)
			{
				String itemName = inventory.mainInventory[0].getItem().getUnlocalizedName();
				returnStat = ApplyFoundItem(itemName, statType, returnStat, occupiedSlots, false);
			}
		}

		return returnStat;
	}

	private int ApplyFoundItem(String itemName, int statType, int currentStat, ArrayList<String> occupiedSlots, Boolean isWorn)
	{
		String effectiveItemName = itemName.replaceFirst("item.", "");
		if(lookup.containsKey(effectiveItemName))
		{
			EquippedItem foundItem = lookup.get(effectiveItemName);
			if(foundItem.HasEffect(statType) && !occupiedSlots.contains(foundItem.GetSlot()) && (isWorn || !foundItem.DoesRequireWorn()))
			{
				occupiedSlots.add(foundItem.GetSlot());
				return foundItem.GetModifiedValue(currentStat);
			}
		}
		else if(isWorn)
		{
			PrintWriter writer = null;
			try
			{
				writer = new PrintWriter(new FileWriter(this.file, true));
				writer.println(effectiveItemName);
			} catch (IOException e) { FMLLog.severe("Could not write unknown armor: %s", itemName); }
			finally
			{
				writer.close();
			}

			lookup.put(effectiveItemName, new FlatBonusEquippedItem(StatChangeStatus.DefenseChange, 1, UUID.randomUUID().toString()));
		}

		return currentStat;
	}
}
