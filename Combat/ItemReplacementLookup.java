package TBC.Combat;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import TBC.CloneItem;
import TBC.HenchmanItem;
import TBC.Pair;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemReplacementLookup
{
	public static ItemReplacementLookup Instance = new ItemReplacementLookup();

	HashMap<String, ItemReplacementData> lookup = new HashMap<String, ItemReplacementData>();
	HashMap<String, Item> baseItemLookup = null;

	public void AddItemData(String baseEntityName, String entityName, String baseItemName, String recipeItemName, String itemToEnchant, String enchantedItem, ArrayList<Pair<String, Integer>> optionalDrops)
	{
		if(baseItemName == null || baseItemName.isEmpty())
		{
			return;
		}

		if(baseItemLookup == null)
		{
			baseItemLookup = new HashMap<String, Item>();
			for(Object key : Item.itemRegistry.getKeys())
			{
				if(key != null)
				{
					Item i = (Item)Item.itemRegistry.getObject(key);
					baseItemLookup.put(getItemNameFromItem(i).toLowerCase(), i);
				}
			}
		}

		boolean hasEnchantedItem = itemToEnchant != null && itemToEnchant.length() > 0;
		Item baseItem = null;
		Item recipeItem = null;
		Item toEnchantItem = null;
		if(!baseItemName.isEmpty())
		{
			baseItem = baseItemLookup.get(baseItemName.toLowerCase());
		}

		if(!recipeItemName.isEmpty())
		{
			recipeItem = baseItemLookup.get(recipeItemName.toLowerCase());
		}

		if(hasEnchantedItem)
		{
			toEnchantItem = baseItemLookup.get(itemToEnchant.toLowerCase());
		}

		ArrayList<Pair<Item, Integer>> additionalDrops = new ArrayList<Pair<Item,Integer>>();
		for(Pair<String, Integer> add : optionalDrops)
		{
			additionalDrops.add(new Pair<Item, Integer>(baseItemLookup.get(add.item1.toLowerCase()), add.item2));
		}

		lookup.put(entityName, new ItemReplacementData(baseEntityName, entityName, baseItem, recipeItem, toEnchantItem, enchantedItem, additionalDrops));
	}

	public ArrayList<Pair<Item, Item>> GetItemReplacementForEntity(String entityName)
	{
		ArrayList<Pair<Item, Item>> replacements = new ArrayList<Pair<Item,Item>>();
		ItemReplacementData data = lookup.get(entityName);
		if(data != null)
		{
			if(data.ReplacementItem != null)
			{
				replacements.add(new Pair<Item, Item>(data.BaseItem, data.ReplacementItem));
			}

			for(Pair<Item, Integer> additional : data.AdditionalDrops)
			{
				if(CombatRandom.GetRandom().nextFloat() < additional.item2/100F)
				{
					replacements.add(new Pair<Item, Item>(null, additional.item1));
				}
			}
		}

		return replacements;
	}

	public void SetupItems()
	{
		HashMap<String, Item> createdItems = new HashMap<String, Item>();
		for(ItemReplacementData data : this.lookup.values())
		{
			String baseEntityName = data.BaseEntityName;
			String entityName = data.EntityName;
			Item baseItem = data.BaseItem;
			Item recipeHeadItem = data.RecipeHeadItem;
			Item itemToEnchant = data.ItemToEnchant;

			Item entityHench = new HenchmanItem(baseEntityName, entityName).setUnlocalizedName(entityName.replace(" ",  "") + "Hench");
			Item entityItem = new CloneItem(baseItem, null).setUnlocalizedName(entityName.replace(" ",  "") + "Drop");
			GameRegistry.registerItem(entityHench, entityHench.getUnlocalizedName(), "tbc");
			GameRegistry.registerItem(entityItem, entityItem.getUnlocalizedName(), "tbc");
			data.ReplacementItem = entityItem;
			GameRegistry.addShapedRecipe(new ItemStack(entityHench), " y ","xxx"," x ", 'x',entityItem, 'y', recipeHeadItem);
			GameRegistry.addShapelessRecipe(new ItemStack(baseItem), entityItem);
			if(itemToEnchant != null)
			{
				String enchantedItemName = data.EnchantedItemName.replace(" ", "");
				Item enchantedItem;
				if(!createdItems.containsKey(enchantedItemName))
				{
					enchantedItem = new CloneItem(itemToEnchant, null).setUnlocalizedName(data.EnchantedItemName.replace(" ", ""));
					GameRegistry.registerItem(enchantedItem, enchantedItem.getUnlocalizedName(), "tbc");
					createdItems.put(enchantedItemName, enchantedItem);
				}
				else
				{
					enchantedItem = createdItems.get(enchantedItemName);
				}
				
				GameRegistry.addShapedRecipe(new ItemStack(enchantedItem), "xxx", "xyx", "xxx", 'x', entityItem, 'y', itemToEnchant);
				LanguageRegistry.addName(enchantedItem, data.EnchantedItemName);
			}

			LanguageRegistry.addName(entityHench, entityName + " Link");
			LanguageRegistry.addName(entityItem, entityName + " " + new ItemStack(baseItem).getDisplayName());
		}
	}

	public class ItemReplacementData
	{
		public ItemReplacementData(String baseName, String entityName, Item baseItem, Item recipeItem, Item itemToEnchant, String enchantedItemName, ArrayList<Pair<Item, Integer>> additionalDrops)
		{
			this.BaseEntityName = baseName;
			this.EntityName = entityName;
			this.BaseItem = baseItem;
			this.RecipeHeadItem = recipeItem;
			this.ItemToEnchant = itemToEnchant;
			this.EnchantedItemName = enchantedItemName;
			this.AdditionalDrops = additionalDrops;
		}

		String BaseEntityName;
		String EntityName;
		Item BaseItem;
		Item ReplacementItem;
		Item RecipeHeadItem;
		Item ItemToEnchant;
		String EnchantedItemName;
		ArrayList<Pair<Item, Integer>> AdditionalDrops;
	}

	private String getItemNameFromItem(Item i)
	{
		return i.getUnlocalizedName().replaceFirst("item.", "");
	}
}
