package TBC.Combat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.Level;

import TBC.CombatEntitySaveData;
import TBC.Pair;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Abilities.PlaceholderAbility;
import cpw.mods.fml.common.FMLLog;

public class JobLookup 
{
	public static JobLookup Instance = new JobLookup();
	public Hashtable<String, Job> lookupByName = new Hashtable<String, Job>();
	
	public void InitializeStats(File file)
	{
		BufferedReader buffer = null;
		try
		{
			String defaultEncoding = "UTF-8";
			InputStreamReader input = new InputStreamReader(new FileInputStream(file), defaultEncoding);
			buffer = new BufferedReader(input);
			String nextLine;

			// Get the headers out of the way.
			buffer.readLine();
			while((nextLine = buffer.readLine()) != null)
			{
				String[] split = nextLine.split(",");
				if(split.length < 10)
				{
					continue;
				}

				Job job = new Job();
				job.Name = split[0].trim();
				if(lookupByName.containsKey(job.Name))
				{
					job = lookupByName.get(job.Name);
				}

				String description = split[9].trim();
				if(!description.isEmpty())
				{
					job.Description = description;
				}
				
				JobStatGain statGain = new JobStatGain();
				String startLevel = split[1].trim();
				if(startLevel.startsWith("-"))
				{
					statGain.StartingAtLevel = 1;
				}
				else
				{
					statGain.StartingAtLevel = Integer.parseInt(startLevel);
				}
				
				statGain.HPGain = Integer.parseInt(split[2].trim());
				statGain.MPGain = Integer.parseInt(split[3].trim());
				statGain.AtkGain = Integer.parseInt(split[4].trim());
				statGain.DefGain = Integer.parseInt(split[5].trim());
				statGain.MAtkGain = Integer.parseInt(split[6].trim());
				statGain.MDefGain = Integer.parseInt(split[7].trim());
				statGain.SpeedGain = Integer.parseInt(split[8].trim());
				job.StatGains.add(statGain);
				
				lookupByName.put(job.Name, job);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(buffer != null)
			{
				try
				{
					buffer.close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void InitializeSkills(File file)
	{
		BufferedReader buffer = null;
		try
		{
			String defaultEncoding = "UTF-8";
			InputStreamReader input = new InputStreamReader(new FileInputStream(file), defaultEncoding);
			buffer = new BufferedReader(input);
			String nextLine;

			// Get the headers out of the way.
			buffer.readLine();
			while((nextLine = buffer.readLine()) != null)
			{
				String[] split = nextLine.split(",");
				if(split.length < 4)
				{
					continue;
				}

				String name = split[0].trim();
				if(!lookupByName.containsKey(name))
				{
					continue;
				}

				Job job = lookupByName.get(name);
				JobSkillGain skillGain = new JobSkillGain();
				skillGain.StartingAtLevel = Integer.parseInt(split[1].trim());
				skillGain.AbilityName = split[2].trim();
				skillGain.AbilityType = split[3].trim();
				job.SkillGains.add(skillGain);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(buffer != null)
			{
				try
				{
					buffer.close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void InitializePrereqs(File file)
	{
		BufferedReader buffer = null;
		try
		{
			String defaultEncoding = "UTF-8";
			InputStreamReader input = new InputStreamReader(new FileInputStream(file), defaultEncoding);
			buffer = new BufferedReader(input);
			String nextLine;

			// Get the headers out of the way.
			buffer.readLine();
			while((nextLine = buffer.readLine()) != null)
			{
				String[] split = nextLine.split(",");
				if(split.length < 3)
				{
					continue;
				}

				String name = split[0].trim();
				if(!lookupByName.containsKey(name))
				{
					continue;
				}

				Job job = lookupByName.get(name);
				JobPrerequisite prereq = new JobPrerequisite();
				prereq.JobRequired = split[1].trim();
				prereq.LevelRequired = Integer.parseInt(split[2].trim());
				job.Prereqs.add(prereq);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(buffer != null)
			{
				try
				{
					buffer.close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	public List<Job> GetAvailableJobs(CombatEntitySaveData saveData)
	{
		ArrayList<Job> availableJobs = new ArrayList<Job>();
		for(Job job : this.lookupByName.values())
		{
			boolean hasPrereqs = true;
			for(JobPrerequisite prereq : job.Prereqs)
			{
				if(prereq.JobRequired.isEmpty())
				{
					if(prereq.LevelRequired > saveData.Level)
					{
						hasPrereqs = false;
						break;
					}
				}
				else 
				{
					boolean foundJob = false;
					for(Pair<String, Integer> jobLevel : saveData.JobLevels)
					{
						if(jobLevel.item1.equals(prereq.JobRequired) && jobLevel.item2 >= prereq.LevelRequired)
						{
							foundJob = true;
							break;
						}
					}
					
					if(!foundJob)
					{
						hasPrereqs = false;
						break;
					}
				}
			}
			
			if(hasPrereqs)
			{
				availableJobs.add(job);
			}
		}
		
		return availableJobs;
	}
	
	public List<ICombatAbility> GetJobAbilities(String jobName, int jobLevel, boolean isPrimary, boolean includeProficiencies)
	{
		ArrayList<ICombatAbility> foundAbilities = new ArrayList<ICombatAbility>();
		Job foundJob = lookupByName.get(jobName);
		for(JobSkillGain g : foundJob.SkillGains)
		{
			if(!includeProficiencies && g.AbilityName.startsWith("prof"))
			{
				continue;
			}
			
			if(g.StartingAtLevel <= jobLevel && (isPrimary || g.AbilityType.equals(JobSkillGain.SECONDARY)))
			{
				ICombatAbility ability = AbilityLookup.Instance.GetAbilityWithName(g.AbilityName);
				if(ability != null)
				{
					foundAbilities.add(ability);
				}
			}
		}
		
		return foundAbilities;
	}
	
	public List<String> GetProficiencies(String jobName, int jobLevel, boolean isPrimary)
	{
		ArrayList<String> proficiencies = new ArrayList<String>();
		List<ICombatAbility> allAbilities = GetJobAbilities(jobName, jobLevel, isPrimary, true);
		for(ICombatAbility ability : allAbilities)
		{
			if(ability instanceof PlaceholderAbility)
			{
				String[] proficienciesForAbility = ((PlaceholderAbility)ability).GetPayload().split(";");
				for(String s : proficienciesForAbility)
				{
					proficiencies.add(s);
				}
			}
		}
		
		return proficiencies;
	}
	
	public CombatEntityTemplate RecalculateStats(String jobName, int charLevel)
	{
		Job foundJob = lookupByName.get(jobName);
		JobStatGain stats = foundJob.StatGains.get(0);
		CombatEntityTemplate t = new CombatEntityTemplate();
		t.attack = stats.AtkGain * charLevel;
		t.defense = stats.DefGain * charLevel;
		t.mAttack = stats.MAtkGain * charLevel;
		t.mDefense = stats.MDefGain * charLevel;
		t.speed = stats.SpeedGain * charLevel;
		t.maxHp = stats.HPGain * charLevel;
		t.maxMp = stats.MPGain * charLevel;
		return t;
	}
}
