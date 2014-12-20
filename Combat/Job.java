package TBC.Combat;

import java.util.ArrayList;

public class Job 
{
	public String Name;
	public String Description;
	public ArrayList<JobStatGain> StatGains = new ArrayList<JobStatGain>();
	public ArrayList<JobSkillGain> SkillGains = new ArrayList<JobSkillGain>();
	public ArrayList<JobPrerequisite> Prereqs = new ArrayList<JobPrerequisite>();
}
