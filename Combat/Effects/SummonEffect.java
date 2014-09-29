package TBC.Combat.Effects;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
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
		EntityLiving enemy = (EntityLiving) EntityList.createEntityByName(renderName, Minecraft.getMinecraft().theWorld);
		enemy.getEntityData().setString("TBCEntityName", entityName);
		CombatEntity summoned = CombatEntityLookup.Instance.GetCombatEntity(enemy, entityName);
		engine.AddEntityToCombat(user, summoned);
	}
}
