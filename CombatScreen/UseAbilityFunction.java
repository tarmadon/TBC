package TBC.CombatScreen;

import TBC.Combat.Abilities.ICombatAbility;

public class UseAbilityFunction implements IGenericAction
{
	private BattleScreen sc;
	private ICombatAbility ability;

	public UseAbilityFunction(BattleScreen sc, ICombatAbility ability)
	{
		this.sc = sc;
		this.ability = ability;
	}

	public void Invoke()
	{
		sc.UseAbilityCommand(this.ability);
	}
}