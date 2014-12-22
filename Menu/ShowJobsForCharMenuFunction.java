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

public class ShowJobsForCharMenuFunction implements IGenericAction
{
	private StatsGui gui;
	private StatMenuCharData player;
	private String selectedJobName;
	
	public ShowJobsForCharMenuFunction(StatsGui gui, StatMenuCharData player, String selectedJobName)
	{
		this.gui = gui;
		this.player = player;
		this.selectedJobName = selectedJobName;
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
		
		List<Job> available = JobLookup.Instance.GetAvailableJobs(saveData);
		ArrayList<GenericScrollBoxCellData> jobsToDisplay = new ArrayList<GenericScrollBoxCellData>();
		jobsToDisplay.add(new GenericScrollBoxCellData("Current Primary: " + saveData.CurrentJob + " Level: " + saveData.GetJobLevelMin1(saveData.CurrentJob), "", null));
		if(!saveData.SecondaryJob.isEmpty())
		{
			jobsToDisplay.add(new GenericScrollBoxCellData("Current Secondary: " + saveData.SecondaryJob + " Level: " + saveData.GetJobLevelMin1(saveData.SecondaryJob), "", null));
		}
		
		for(Job job : available)
		{
			if(job.Name.equals(selectedJobName))
			{
				jobsToDisplay.add(new GenericScrollBoxCellData(job.Name, "", null, job.Description));
			}
			else
			{
				jobsToDisplay.add(new GenericScrollBoxCellData(job.Name, "", new ShowJobsForCharMenuFunction(gui, player, job.Name), job.Description));
			}
		}
		
		jobsToDisplay.add(new GenericScrollBoxCellData("Remove", "", null));
		ArrayList<GenericScrollBoxCellData> constantButtons = new ArrayList<GenericScrollBoxCellData>();
		constantButtons.add(new GenericScrollBoxCellData("Back", "", new SelectJobsMenuFunction(this.gui)));
		if(selectedJobName.isEmpty())
		{
			constantButtons.add(new GenericScrollBoxCellData("Primary", "", null));
			constantButtons.add(new GenericScrollBoxCellData("Secondary", "", null));
			constantButtons.add(new GenericScrollBoxCellData("Info", "", null));
		}
		else
		{
			if(selectedJobName.equals("Remove"))
			{
				constantButtons.add(new GenericScrollBoxCellData("Primary", "", new ChangeJobMenuFunction(gui, player, "Adventurer", true)));
				constantButtons.add(new GenericScrollBoxCellData("Secondary", "", new ChangeJobMenuFunction(gui, player, "", false)));
				constantButtons.add(new GenericScrollBoxCellData("Info", "", null));
			}
			else
			{
				if(!saveData.CurrentJob.equals(selectedJobName))
				{
					constantButtons.add(new GenericScrollBoxCellData("Primary", "", new ChangeJobMenuFunction(gui, player, selectedJobName, true)));	
				}
				else
				{
					constantButtons.add(new GenericScrollBoxCellData("Primary", "", null));
				}
				
				if(!saveData.SecondaryJob.equals(selectedJobName))
				{
					constantButtons.add(new GenericScrollBoxCellData("Secondary", "", new ChangeJobMenuFunction(gui, player, selectedJobName, false)));
				}
				else
				{
					constantButtons.add(new GenericScrollBoxCellData("Secondary", "", null));
				}
				
				constantButtons.add(new GenericScrollBoxCellData("Info", "", new ShowJobInfoMenuFunction(gui, player, selectedJobName)));
			}
		}
		
		this.gui.ChangeButtonForSubMenu("ShowJobsForChar", jobsToDisplay, constantButtons, 0);
	}
}
