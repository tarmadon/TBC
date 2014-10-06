package TBC.CombatScreen;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;

public class TurnState
{
	public static final int PlayerControl = 1;
	public static final int DisplayingMessage = 2;
	public static final int DisplayingAttack = 3;
	public static final int DisplayingEndOfTurn = 4;
	public static final int EndOfCombat = 5;
	public static final int Waiting = 6;
	
	private Minecraft mc;

	public TurnState(Minecraft mc)
	{
		this.mc = mc;
	}

	public int phase;
	public long phaseStartTime;
	public CombatEntity activeEntity;
	public ArrayList<CombatEntity> targetEntities;
	public ICombatAbility ability;

	public int nextState;
	public ArrayList<String> messages;

	public void SetState(int phase, CombatEntity activeEntity, ICombatAbility ability, CombatEntity targetEntity)
	{
		ArrayList<CombatEntity> targetEntities = new ArrayList<CombatEntity>();
		targetEntities.add(targetEntity);
		this.SetState(phase, activeEntity, ability, targetEntities);
	}

	public void SetState(int phase, CombatEntity activeEntity, ICombatAbility ability, ArrayList<CombatEntity> targetEntities)
	{
		this.phase = phase;
		this.phaseStartTime = this.mc.getSystemTime();
		this.activeEntity = activeEntity;
		this.ability = ability;
		this.targetEntities = targetEntities;
	}

	public long GetElapsedTime()
	{
		return this.mc.getSystemTime() - this.phaseStartTime;
	}
}