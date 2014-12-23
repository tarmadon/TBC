package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.CombatScreen.BattleScreenDrawer;
import TBC.CombatScreen.TurnState;

public class PlaceholderAbility extends StandardAbility
{
	private String payload;
	
	public PlaceholderAbility(String name, ArrayList<String> description, String payload) 
	{
		super(new IOneTimeEffect[0], name, AbilityTargetType.Self, 0, false, false, description);
		this.payload = payload;
	}
	
	public String GetPayload()
	{
		return this.payload;
	}
}
