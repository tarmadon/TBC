package TBC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import TBC.Combat.CombatEntityTemplate;
import TBC.Combat.ILevelScale;
import TBC.Combat.ItemReplacementLookup;
import TBC.Combat.CombatEntitySpawnLookup.TemplateWithLevel;
import TBC.Combat.ItemReplacementLookup.ItemReplacementData;

public class EnchantedItemManager 
{
	public static EnchantedItemManager Instance = new EnchantedItemManager();
	
	public Hashtable<String, Item> lookup = new Hashtable<String, Item>();
	
	public void Initialize(File file)
	{
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
				String[] split = nextLine.split(",");
				if(split.length < 5)
				{
					continue;
				}

				String droppedBy = split[0].trim();
				String itemNameToEnchant = split[1].trim();
				String enchantedItemId = split[2].trim();
				String enchantedItemName = split[3].trim();
				if(itemNameToEnchant.isEmpty() || enchantedItemId.isEmpty() || enchantedItemName.isEmpty())
				{
					continue;
				}

				ArrayList<Pair<Item, Item>> replacementData = ItemReplacementLookup.Instance.GetItemReplacementForEntity(droppedBy);
				Item entityItem = null;
				for(int i = 0; i < replacementData.size(); i++)
				{
					if(replacementData.get(i).item1 != null)
					{
						entityItem = replacementData.get(i).item2;
					}
				}

				Item itemToEnchant = ItemReplacementLookup.Instance.GetBaseItem(itemNameToEnchant);
				if(entityItem == null || itemToEnchant == null)
				{
					continue;
				}

				Item enchantedItem;
				if(!lookup.containsKey(enchantedItemId))
				{
					enchantedItem = new CloneItem(itemToEnchant).setUnlocalizedName(enchantedItemId);
					GameRegistry.registerItem(enchantedItem, enchantedItem.getUnlocalizedName(), "tbc");
					lookup.put(enchantedItemId, enchantedItem);
					LanguageRegistry.addName(enchantedItem, enchantedItemName);
				}
				else
				{
					enchantedItem = lookup.get(enchantedItemId);
				}
				
				GameRegistry.addShapedRecipe(new ItemStack(enchantedItem), "xxx", "xyx", "xxx", 'x', entityItem, 'y', itemToEnchant);
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
}
