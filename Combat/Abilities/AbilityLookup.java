package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import TBC.Combat.DamageType;
import TBC.Combat.Effects.ApplyStatusEffect;
import TBC.Combat.Effects.DeathEffect;
import TBC.Combat.Effects.DelayTurnEffect;
import TBC.Combat.Effects.FlatDamageEffect;
import TBC.Combat.Effects.FlatManaEffect;
import TBC.Combat.Effects.HealEffect;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.Combat.Effects.LifeStealEffect;
import TBC.Combat.Effects.MagicDamageEffect;
import TBC.Combat.Effects.ManaStealEffect;
import TBC.Combat.Effects.PhysicalDamageEffect;
import TBC.Combat.Effects.PurgeEffect;
import TBC.Combat.Effects.SetHPEffect;
import TBC.Combat.Effects.StatChangeStatus;
import TBC.Combat.Effects.SummonEffect;
import TBC.Combat.TriggeredEffects.BlindTriggeredEffect;
import TBC.Combat.TriggeredEffects.ConfusionEffect;
import TBC.Combat.TriggeredEffects.CounterattackEffect;
import TBC.Combat.TriggeredEffects.DamageOverTimeEffect;
import TBC.Combat.TriggeredEffects.DamageReturnEffect;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;
import TBC.Combat.TriggeredEffects.LifeStealConstantEffect;
import TBC.Combat.TriggeredEffects.PoisonImmunityEffect;
import TBC.Combat.TriggeredEffects.PoisonStatusEffect;
import TBC.Combat.TriggeredEffects.ResistanceTriggeredEffect;
import TBC.Combat.TriggeredEffects.SilenceEffect;

public class AbilityLookup 
{
	public static AbilityLookup Instance = new AbilityLookup();
	
	public Hashtable<String, ICombatAbility> lookup = new Hashtable<String, ICombatAbility>();
	
	public void Initialize()
	{
		lookup.put("BasicCounter", this.BuildStandardAbility("", AbilityTargetType.OneEnemy, 0, false, new PhysicalDamageEffect(0, 1.0F, 0, 1.0F, DamageType.Uncounterable)));
		
		lookup.put("Default", new DefaultAttackAbility());
		lookup.put("Bleed", this.BuildStandardAbility("Bleed", AbilityTargetType.OneEnemy, 0, false, new ApplyStatusEffect(new DamageOverTimeEffect(10, DamageType.Physical, 5, -1))));
		lookup.put("Lifesteal", this.BuildConstantAbility("Lifesteal", new LifeStealConstantEffect(DamageType.Physical, 1)));
		
//		lookup.put("FillParty", this.BuildStandardAbility("FillParty", AbilityTargetType.Self, 0, true, 
//				new SummonEffect("Spiderling", "Spider"), 
//				new SummonEffect("Spiderling", "Spider"),
//				new SummonEffect("Spiderling", "Spider")));
//		lookup.put("KillAlly", this.BuildStandardAbility("KillAlly", AbilityTargetType.OneAlly, 0, false, new SetHPEffect(0)));
		
		lookup.put("PhysResist", this.BuildConstantAbility("PhysResist", new ResistanceTriggeredEffect(DamageType.Physical, .5F, 0)));
		lookup.put("PhysImmune", this.BuildConstantAbility("PhysImmune", new ResistanceTriggeredEffect(DamageType.Physical, 0F, 0)));
		lookup.put("MagResist", this.BuildConstantAbility("MagResist", new ResistanceTriggeredEffect(DamageType.Magical, .5F, 0)));
		lookup.put("MagImmune", this.BuildConstantAbility("MagImmune", new ResistanceTriggeredEffect(DamageType.Magical, 0F, 0)));
		lookup.put("Regen10", this.BuildConstantAbility("Regen10", new DamageOverTimeEffect(-10, 0, -1, -1)));
		
		lookup.put("PoisonImmunity", this.BuildConstantAbility("PoisonImmunity", new PoisonImmunityEffect()));
		lookup.put("Flame Cloak", this.BuildConstantAbility("Flame Cloak", new DamageReturnEffect(DamageType.Physical | DamageType.Magical, 1, .5F)));
		lookup.put("Bloodlust", this.BuildConstantAbility("Bloodlust", new LifeStealConstantEffect(DamageType.Physical, 1F)));
		
		lookup.put("Barrage", this.BuildStandardAbility("Barrage", AbilityTargetType.AllEnemies, 1, false, new PhysicalDamageEffect(0)));
		lookup.put("Smite", this.BuildStandardAbility("Smite", AbilityTargetType.OneEnemy, 1, false, new PhysicalDamageEffect(3, 1.2F, 0, 1.0F)));
		lookup.put("Gnaw", this.BuildStandardAbility("Gnaw", AbilityTargetType.OneEnemy, 0, false, new LifeStealEffect(0,  0,  0.7F, 0F, 0.7F)));
		lookup.put("Poison Bite", this.BuildStandardAbility("Poison Bite", AbilityTargetType.OneEnemy, 0, false,
				new PhysicalDamageEffect(0), 
				new ApplyStatusEffect(new PoisonStatusEffect(), .3F)));
		
		lookup.put("Explosion", new SacrificeAbility(
				new IOneTimeEffect[]{ new FlatDamageEffect(1000)}, 
				new IOneTimeEffect[]{ new MagicDamageEffect(0, 3F)}, 
				"Explosion", AbilityTargetType.AllEnemies, 0, false, true));
		
		lookup.put("MiniNuke", new SacrificeAbility(
				new IOneTimeEffect[]{ new FlatDamageEffect(15)},
				new IOneTimeEffect[]{ new MagicDamageEffect(0, 2F)}, 
				"MiniNuke", AbilityTargetType.OneEnemy, 0, false, true));

		lookup.put("Swift", new SacrificeAbility(
				new IOneTimeEffect[]{ new ApplyStatusEffect(new StatChangeStatus("Swift", StatChangeStatus.SpeedChange, 200, 3F, 1, -1)) }, 
				new IOneTimeEffect[]{ new PhysicalDamageEffect(0)}, 
				"Swift", AbilityTargetType.OneEnemy, 0, false, false));
		
		lookup.put("Backstab", this.BuildStandardAbility("Backstab", AbilityTargetType.OneEnemy, 0, false, new PhysicalDamageEffect(0, 0.8F, 0, 0.0F)));
		lookup.put("Fireball", this.BuildStandardAbility("Fireball", AbilityTargetType.OneEnemy, 0, true, new MagicDamageEffect(0, 1F)));
		lookup.put("Inferno", this.BuildStandardAbility("Inferno", AbilityTargetType.AllEnemies, 3, true, new MagicDamageEffect(0, .75F)));
		lookup.put("Web", this.BuildStandardAbility("Web", AbilityTargetType.OneEnemy, 0, false, 
					new PhysicalDamageEffect(0), 
					new ApplyStatusEffect(new StatChangeStatus("Web", StatChangeStatus.SpeedChange, -25, .75F, 3, -1))));
		
		lookup.put("Rend", this.BuildStandardAbility("Rend", AbilityTargetType.OneEnemy, 0, false,
				new LifeStealEffect(0, 0, .7F, 0F, .5F),
				new ApplyStatusEffect(new StatChangeStatus("Rend", StatChangeStatus.SpeedChange, -25, .75F, 3, -1))));
	
		lookup.put("Corrosive Acid", this.BuildStandardAbility("Corrosive Acid", AbilityTargetType.OneEnemy, 0, false,
				new PhysicalDamageEffect(0, 1F, 0, 1F),
				new ApplyStatusEffect(new StatChangeStatus("Corrosive Acid", StatChangeStatus.AttackChange, 0, .5F, 3, -1)),
				new ApplyStatusEffect(new StatChangeStatus("Corrosive Acid", StatChangeStatus.DefenseChange, 0, .5F, 3, -1))));
		
		lookup.put("Drain", this.BuildStandardAbility("Drain", AbilityTargetType.OneEnemy, 1, true, new LifeStealEffect(0, 0, 1F, 0F, 1F)));
		lookup.put("Absorb", this.BuildStandardAbility("Absorb", AbilityTargetType.OneEnemy, 1, true, new ManaStealEffect(0, 0, 0F, .25F)));
		lookup.put("Heal", this.BuildStandardAbility("Heal", AbilityTargetType.OneAlly, 1, true, true, new HealEffect(20, 0.5F)));
		lookup.put("Mass Heal", this.BuildStandardAbility("Mass Heal", AbilityTargetType.AllAllies, 3, true, true, new HealEffect(20, 0.5F)));
		
		lookup.put("Poison", this.BuildStandardAbility("Poison", AbilityTargetType.OneEnemy, 0, true,
				new ApplyStatusEffect(new PoisonStatusEffect(), 1F)));
		lookup.put("Paralyze", this.BuildStandardAbility("Paralyze", AbilityTargetType.OneEnemy, 0, true,
				new ApplyStatusEffect(new StatChangeStatus("Paralyze", StatChangeStatus.SpeedChange, -1000, 0, -1, -1), .5F)));
		lookup.put("Stop", this.BuildStandardAbility("Stop", AbilityTargetType.OneEnemy, 0, true,
				new ApplyStatusEffect(new StatChangeStatus("Stop", StatChangeStatus.SpeedChange, -1000, 0, -1, 1000), .7F)));
		lookup.put("Silence", this.BuildStandardAbility("Silence", AbilityTargetType.OneEnemy, 0, true,
				new ApplyStatusEffect(new SilenceEffect(), 1F)));
		lookup.put("Confusion", this.BuildStandardAbility("Confusion", AbilityTargetType.Self, 0, true,
				new ApplyStatusEffect(new ConfusionEffect(5, -1), 1F)));
		lookup.put("Blind", this.BuildStandardAbility("Blind", AbilityTargetType.OneEnemy, 0, true,
				new ApplyStatusEffect(new BlindTriggeredEffect(DamageType.Physical), 1F)));
		lookup.put("Death", this.BuildStandardAbility("Death", AbilityTargetType.OneEnemy, 0, true, new DeathEffect(.3F)));
		lookup.put("Mass Death", this.BuildStandardAbility("Mass Death", AbilityTargetType.AllEnemies, 0, true, new DeathEffect(.3F)));
		
		lookup.put("Weakness", this.BuildStandardAbility("Weakness", AbilityTargetType.OneEnemy, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Weakness", StatChangeStatus.AttackChange, 0, .5F, -1, 1000))));
		lookup.put("Guardbreak", this.BuildStandardAbility("Guardbreak", AbilityTargetType.OneEnemy, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Guardbreaker", StatChangeStatus.DefenseChange, 0, 0F, -1, 1000))));
		lookup.put("Disjunction", this.BuildStandardAbility("Disjunction", AbilityTargetType.OneEnemy, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Disjunction", StatChangeStatus.MagicChange, 0, .5F, -1, 1000))));
		lookup.put("Curse", this.BuildStandardAbility("Curse", AbilityTargetType.OneEnemy, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Curse", StatChangeStatus.MagicDefenseChange, 0, 0F, -1, 1000))));
		lookup.put("Slow", this.BuildStandardAbility("Slow", AbilityTargetType.OneEnemy, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Slow", StatChangeStatus.SpeedChange, -50, .5F, -1, 1000))));

		lookup.put("Charge", this.BuildStandardAbility("Charge", AbilityTargetType.OneAlly, 1, false,
				new ApplyStatusEffect(new StatChangeStatus("Charge", StatChangeStatus.AttackChange, 0, 1.5F, -1, 1000))));
		lookup.put("Strength", this.BuildStandardAbility("Strength", AbilityTargetType.OneAlly, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Strength", StatChangeStatus.AttackChange, 0, 1.5F, -1, 1000))));
		lookup.put("Mass Strength", this.BuildStandardAbility("Mass Strength", AbilityTargetType.AllAllies, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Strength", StatChangeStatus.AttackChange, 0, 1.5F, -1, 1000))));
		lookup.put("Shield", this.BuildStandardAbility("Shield", AbilityTargetType.OneAlly, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Shield", StatChangeStatus.DefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Mass Shield", this.BuildStandardAbility("Mass Shield", AbilityTargetType.AllAllies, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Shield", StatChangeStatus.DefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Concentrate", this.BuildStandardAbility("Concentrate", AbilityTargetType.OneAlly, 1, true, 
				new ApplyStatusEffect(new StatChangeStatus("Concentrate", StatChangeStatus.MagicChange, 0, 1.5F, -1, 1000))));
		lookup.put("Resistance", this.BuildStandardAbility("Resistance", AbilityTargetType.OneAlly, 1, true,
				new ApplyStatusEffect(new StatChangeStatus("Resistance", StatChangeStatus.MagicDefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Mass Resistance", this.BuildStandardAbility("Mass Resistance", AbilityTargetType.AllAllies, 1, true,  
				new ApplyStatusEffect(new StatChangeStatus("Resistance", StatChangeStatus.MagicDefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Haste", this.BuildStandardAbility("Haste", AbilityTargetType.OneAlly, 0, true, 
				new ApplyStatusEffect(new StatChangeStatus("Haste", StatChangeStatus.SpeedChange, 100, 2F, -1, 1000))));
		lookup.put("Mass Haste", this.BuildStandardAbility("Mass Haste", AbilityTargetType.AllAllies, 1, true, 
				new ApplyStatusEffect(new StatChangeStatus("Haste", StatChangeStatus.SpeedChange, 100, 2F, -1, 1000))));

		lookup.put("Spawn Broodlings", this.BuildStandardAbility("Spawn Broodlings", AbilityTargetType.OneEnemy, 0, false, 
				new PhysicalDamageEffect(0),
				new SummonEffect("Spiderling", "Spider")));
		
		lookup.put("Trip", this.BuildStandardAbility("Trip", AbilityTargetType.OneAlly, 1, false, new PhysicalDamageEffect(0), new DelayTurnEffect(25)));
		lookup.put("Snipe", new DelayedAbility(
				this.BuildStandardAbility("Snipe", AbilityTargetType.OneEnemy, 0, false, new PhysicalDamageEffect(0, 2F, 0, 1F)), 
				"Snipe", AbilityTargetType.OneEnemy, 0, false, false));
		lookup.put("Blade Frenzy", new DelayedAbility(
				new IOneTimeEffect[]{ new DelayTurnEffect(-100) },
				new IOneTimeEffect[] { new PhysicalDamageEffect(0) },
				new DelayedAbility(
						new IOneTimeEffect[]{ new DelayTurnEffect(-100) },
						new IOneTimeEffect[] { new PhysicalDamageEffect(0) },
						new DelayedAbility(
								new IOneTimeEffect[]{ new DelayTurnEffect(-100) },
								new IOneTimeEffect[] { new PhysicalDamageEffect(0) },
								this.BuildStandardAbility("", AbilityTargetType.OneEnemy, 0, false, false, new PhysicalDamageEffect(0)),
								"", AbilityTargetType.OneEnemy, 0, false, false),
						"", AbilityTargetType.OneEnemy, 0, false, false), 
				"Blade Frenzy", AbilityTargetType.OneEnemy, 0, false, false));
		
		lookup.put("Paralysis Claw", this.BuildStandardAbility("Paralysis Claw", AbilityTargetType.OneEnemy, 0, false, 
				new PhysicalDamageEffect(0, .8F, 0, 1F),
				new ApplyStatusEffect(new StatChangeStatus("Paralyze", StatChangeStatus.SpeedChange, -1000, 0, -1, -1), .2F)));
		lookup.put("Assassinate", new DefaultAttackAbility());
		lookup.put("Wraithstrike", new DefaultAttackAbility());
		lookup.put("Energy Drain", new DefaultAttackAbility());
		lookup.put("Soul Drain", new DefaultAttackAbility());
		
		lookup.put("smallPotion", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-20)));
		lookup.put("medPotion", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-50)));
		lookup.put("highPotion", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-1000)));
		lookup.put("smallManaPotion", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, true, false, new FlatManaEffect(10)));
		lookup.put("highManaPotion", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, true, false, new FlatManaEffect(100)));
		lookup.put("elixir", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-1000), new FlatManaEffect(100)));
		lookup.put("megalixir", this.BuildStandardAbility("", AbilityTargetType.AllAllies, 0, true, false, new FlatDamageEffect(-1000), new FlatManaEffect(100)));
		lookup.put("antidote", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, true, new PurgeEffect("Poison")));
		lookup.put("echoScreen", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, false, new PurgeEffect("Silence")));
		lookup.put("parlyzHeal", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, false, new PurgeEffect("Paralyze")));
		lookup.put("pinwheel", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, false, new PurgeEffect("Confusion")));
		lookup.put("eyeDrops", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, false, new PurgeEffect("Blind")));
		lookup.put("panacea", this.BuildStandardAbility("", AbilityTargetType.OneAlly, 0, false, new PurgeEffect("Poison"), new PurgeEffect("Blind"), new PurgeEffect("Silence"), new PurgeEffect("Paralyze"), new PurgeEffect("Confusion"), new PurgeEffect("Stop"), new PurgeEffect("Slow")));
		lookup.put("phoenixDown", this.BuildStandardAbility("", AbilityTargetType.OneDeadAlly, 0, true, false, new SetHPEffect(1)));
		lookup.put("fireBomb", this.BuildStandardAbility("", AbilityTargetType.OneEnemy, 0, false, new MagicDamageEffect(30, 0, DamageType.Fire)));
		lookup.put("earthGem", this.BuildStandardAbility("", AbilityTargetType.OneEnemy, 0, false, new MagicDamageEffect(30, 0, DamageType.Earth)));
		lookup.put("iceCrystal", this.BuildStandardAbility("", AbilityTargetType.OneEnemy, 0, false, new MagicDamageEffect(30, 0, DamageType.Ice)));
		lookup.put("lightningRod", this.BuildStandardAbility("", AbilityTargetType.OneEnemy, 0, false, new MagicDamageEffect(30, 0, DamageType.Lightning)));
		
		lookup.put("Counter", this.BuildConstantAbility("Counter", new CounterattackEffect(50, lookup.get("BasicCounter"))));
	}
	
	public ICombatAbility GetAbilityWithName(String abilityName)
	{
		return lookup.get(abilityName);
	}
	
	public ICombatAbility BuildStandardAbility(String abilityName, int targetType, int mpCost, Boolean isSpell, IOneTimeEffect... effects)
	{
		return BuildStandardAbility(abilityName, targetType, mpCost, false, isSpell, effects);
	}
	
	public ICombatAbility BuildStandardAbility(String abilityName, int targetType, int mpCost, Boolean usableOutOfCombat, Boolean isSpell, IOneTimeEffect... effects)
	{
		return new StandardAbility(effects, abilityName, targetType, mpCost, usableOutOfCombat, isSpell);
	}
	
	public ICombatAbility BuildConstantAbility(String abilityName, ITriggeredEffect... effects)
	{
		List effectList = new ArrayList();
		for(ITriggeredEffect effect : effects)
		{
			effectList.add(effect);
		}
		
		return new ConstantAbility(abilityName, effectList);
	}
}
