package TBC.Menu;

import java.util.ArrayList;
import java.util.List;

import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.Combat.Job;
import TBC.Combat.JobLookup;
import TBC.Combat.LevelingEngine;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;

public class ShowJobInfoMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private StatMenuCharData player;
	private String jobName;
	
	public ShowJobInfoMenuFunction(StatsGui gui, StatMenuCharData player, String jobName)
	{
		this.gui = gui;
		this.player = player;
		this.jobName = jobName;
	}

	@Override
	public void Invoke() 
	{
		CombatEntitySaveData saveData;
		if(player.Player != null)
		{
			saveData = LevelingEngine.Instance.GetPlayerSaveData(player.Player);
		}
		else
		{
			saveData = HenchmanItem.GetCombatEntitySaveData(player.Item);
		}
		
		int jobLevel = saveData.GetJobLevelMin1(jobName);
		List<ICombatAbility> abilities = JobLookup.Instance.GetJobAbilities(jobName, jobLevel, true, true);
		ArrayList<GenericScrollBoxCellData> abilitiesToDisplay = new ArrayList<GenericScrollBoxCellData>();
		for(ICombatAbility ability : abilities)
		{
			abilitiesToDisplay.add(new GenericScrollBoxCellData(ability.GetAbilityName(), "", null, ability.GetDescription().get(0)));
		}
		
		ArrayList<GenericScrollBoxCellData> constantButtons = new ArrayList<GenericScrollBoxCellData>();
		constantButtons.add(new GenericScrollBoxCellData("Back", "", new ShowJobsForCharMenuFunction(this.gui, this.player, this.jobName)));
		
		this.gui.ChangeButtonForSubMenu("ShowJobInfo", abilitiesToDisplay, constantButtons, 0);
		
	}
}
