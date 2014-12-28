package TBC.CombatScreen;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

public class DisplayProfile extends GameProfile
{
	public DisplayProfile(UUID id, String name) 
	{
		super(id, name);
	}
	
	@Override
	public String getName() 
	{
		return "";
	}
}
