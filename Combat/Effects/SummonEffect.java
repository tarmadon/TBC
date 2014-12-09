package TBC.Combat.Effects;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;

public class SummonEffect implements IOneTimeEffect
{
	private String entityName;
	private String renderName;
	public SummonEffect(String entityName, String renderName)
	{
		this.entityName = entityName;
		this.renderName = renderName;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target)
	{
		CombatEntity summoned = CombatEntityLookup.Instance.GetCombatEntity(engine.GetNewEntityId(), renderName, entityName, new NBTTagCompound());
		engine.AddEntityToCombat(user, summoned);
	}
}
