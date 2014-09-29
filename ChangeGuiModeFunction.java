package TBC;

import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.IGenericAction;

public class ChangeGuiModeFunction implements IGenericAction
{
	private StatsGui gui;
	private int mode;
	private CombatEntity user;
	private ICombatAbility abilityToUse;

	public ChangeGuiModeFunction(StatsGui gui, CombatEntity user, ICombatAbility abilityToUse, int mode)
	{
		this.gui = gui;
		this.mode = mode;
		this.user = user;
		this.abilityToUse = abilityToUse;
	}

	public void Invoke()
	{
		this.gui.ChangeMode(this.user, this.abilityToUse, this.mode);
	}
}
