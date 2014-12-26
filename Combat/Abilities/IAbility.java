package TBC.Combat.Abilities;

import java.io.Serializable;
import java.util.ArrayList;

public interface IAbility extends Serializable
{
	String GetAbilityName();
	ArrayList<String> GetDescription();
}
