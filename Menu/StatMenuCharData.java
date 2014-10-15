package TBC.Menu;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class StatMenuCharData 
{
	public EntityPlayer Player;
	public ItemStack Item;
	public TBC.Combat.CombatEntity CombatEntity;
	public Boolean FrontRow;
	
	public StatMenuCharData(EntityPlayer player, ItemStack item, TBC.Combat.CombatEntity entity, boolean frontRow)
	{
		this.Player = player;
		this.Item = item;
		this.CombatEntity = entity;
		this.FrontRow = frontRow;
	}
}
