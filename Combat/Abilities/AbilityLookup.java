package TBC.Combat.Abilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import scala.actors.threadpool.Arrays;

import TBC.ArrayExtensions;
import TBC.Combat.DamageType;
import TBC.Combat.IStatusChange;
import TBC.Combat.Effects.ApplyStatusEffect;
import TBC.Combat.Effects.DeathEffect;
import TBC.Combat.Effects.DelayTurnEffect;
import TBC.Combat.Effects.FlatDamageEffect;
import TBC.Combat.Effects.FlatManaEffect;
import TBC.Combat.Effects.HealEffect;
import TBC.Combat.Effects.IEffect;
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
import TBC.Combat.TriggeredEffects.RangedAttacksTriggeredEffect;
import TBC.Combat.TriggeredEffects.ResistanceTriggeredEffect;
import TBC.Combat.TriggeredEffects.SilenceEffect;

public class AbilityLookup
{
	public static AbilityLookup Instance = new AbilityLookup();

	public Hashtable<String, ICombatAbility> lookup = new Hashtable<String, ICombatAbility>();
	public Hashtable<ICombatAbility, String> reverseLookup = new Hashtable<ICombatAbility, String>();
	
	public void Initialize()
	{
		lookup.put("Default", new DefaultAttackAbility());
		lookup.put("BasicCounter", this.BuildStandardAbility("", "", AbilityTargetType.OneEnemy, 0, false, false, new PhysicalDamageEffect(0).SetAdditionalDamageTypes(DamageType.Uncounterable)));
		
		// Adventurer skills
		lookup.put("beginnerBoostPrimary", this.BuildConstantAbility("Beginner Boost II", "Increases all stats passively", ArrayExtensions.MergeLists(
				BuildConstantFlatStatChange("beginnerBoost", 5, StatChangeStatus.AttackChange, StatChangeStatus.DefenseChange, StatChangeStatus.MagicChange, StatChangeStatus.MagicDefenseChange, StatChangeStatus.SpeedChange),
				BuildConstantFlatStatChange("beginnerBoost", 50, StatChangeStatus.HpChange))));
		lookup.put("beginnerBoostSecondary", this.BuildConstantAbility("Beginner Boost I", "Increases all stats passively", ArrayExtensions.MergeLists(
				BuildConstantFlatStatChange("beginnerBoost", 2, StatChangeStatus.AttackChange, StatChangeStatus.DefenseChange, StatChangeStatus.MagicChange, StatChangeStatus.MagicDefenseChange, StatChangeStatus.SpeedChange),
				BuildConstantFlatStatChange("beginnerBoost", 20, StatChangeStatus.HpChange))));
		
		
		lookup.put("Ranged", this.BuildConstantAbility("Ranged Attacks", "All basic attacks become ranged", new RangedAttacksTriggeredEffect()));
		lookup.put("Bleed", this.BuildStandardAbility("Bleed", "Causes physical damage over time", AbilityTargetType.OneEnemy, 0, false, false, new ApplyStatusEffect(new DamageOverTimeEffect(10, DamageType.Physical, 5, -1))));
		lookup.put("Lifesteal", this.BuildConstantAbility("Lifesteal", "Heals a little when causing physical damage", new LifeStealConstantEffect(DamageType.Physical, 0.1F)));

//		lookup.put("FillParty", this.BuildStandardAbility("FillParty", AbilityTargetType.Self, 0, true,
//				new SummonEffect("Spiderling", "Spider"),
//				new SummonEffect("Spiderling", "Spider"),
//				new SummonEffect("Spiderling", "Spider")));
//		lookup.put("KillAlly", this.BuildStandardAbility("KillAlly", AbilityTargetType.OneAlly, 0, false, new SetHPEffect(0)));

		lookup.put("PhysResist", this.BuildConstantAbility("PhysResist", "Take half damage from physical attacks", new ResistanceTriggeredEffect(DamageType.Physical, .5F, 0)));
		lookup.put("PhysImmune", this.BuildConstantAbility("PhysImmune", "Immune to physical damage", new ResistanceTriggeredEffect(DamageType.Physical, 0F, 0)));
		lookup.put("MagResist", this.BuildConstantAbility("MagResist", "Take half damage from magical attacks", new ResistanceTriggeredEffect(DamageType.Magical, .5F, 0)));
		lookup.put("MagImmune", this.BuildConstantAbility("MagImmune", "Immune to magical damage", new ResistanceTriggeredEffect(DamageType.Magical, 0F, 0)));
		lookup.put("Regen10", this.BuildConstantAbility("Regen10", "Heal 10 HP every turn", new DamageOverTimeEffect(-10, 0, -1, -1)));

		lookup.put("PoisonImmunity", this.BuildConstantAbility("PoisonImmunity", "Immune to poison", new PoisonImmunityEffect()));
		lookup.put("Flame Cloak", this.BuildConstantAbility("Flame Cloak", "Deals fire damage when attacked", new DamageReturnEffect(DamageType.Physical | DamageType.Magical, 1, .5F)));
		lookup.put("Bloodlust", this.BuildConstantAbility("Bloodlust", "Heals equal to physical damage done", new LifeStealConstantEffect(DamageType.Physical, 1F)));

		lookup.put("Barrage", this.BuildStandardAbility("Barrage", "Attack all enemies with a standard attack", AbilityTargetType.AllEnemies, 1, false, false, new PhysicalDamageEffect(0)));
		lookup.put("Smite", this.BuildStandardAbility("Smite", "Deals medium damage to one target", AbilityTargetType.OneEnemy, 1, false, false, new PhysicalDamageEffect(3).SetAttackMultiplier(1.2F)));
		lookup.put("Gnaw", this.BuildStandardAbility("Gnaw", "Deal low damage to an enemy and lifesteal", AbilityTargetType.OneEnemy, 0, false, false, new LifeStealEffect(0,  0,  0.7F, 0F, 0.7F)));
		lookup.put("Poison Bite", this.BuildStandardAbility("Poison Bite", "A standard attack with the chance to poison", AbilityTargetType.OneEnemy, 0, false, false,
				new PhysicalDamageEffect(0),
				new ApplyStatusEffect(new PoisonStatusEffect(), .3F)));

		lookup.put("Explosion", new SacrificeAbility(
				new IOneTimeEffect[]{ new FlatDamageEffect(1000)},
				new IOneTimeEffect[]{ new MagicDamageEffect(0, 3F)},
				"Explosion", AbilityTargetType.AllEnemies, 0, false, true, ArrayExtensions.GetArray("Deals large damage at the cost of the user's life")));

		lookup.put("MiniNuke", new SacrificeAbility(
				new IOneTimeEffect[]{ new FlatDamageEffect(15)},
				new IOneTimeEffect[]{ new MagicDamageEffect(0, 2F)},
				"MiniNuke", AbilityTargetType.OneEnemy, 0, false, true, ArrayExtensions.GetArray("Deals large damage but hurts the user")));
		
		lookup.put("Swift", new SacrificeAbility(
				new IOneTimeEffect[]{ new DelayTurnEffect(-75) },
				new IOneTimeEffect[]{ new PhysicalDamageEffect(0)},
				"Swift", AbilityTargetType.OneEnemy, 0, false, false, ArrayExtensions.GetArray("Quick attack that does low damage")));

		lookup.put("Backstab", this.BuildStandardAbility("Backstab", "Deals low damage which ignores armor", AbilityTargetType.OneEnemy, 0, false, false, new PhysicalDamageEffect(0).SetAttackMultiplier(.8F).SetDefenseMultiplier(0)));
		lookup.put("Fireball", this.BuildStandardAbility("Fireball", "Deals low fire damage to one enemy", AbilityTargetType.OneEnemy, 0, false, true, new MagicDamageEffect(0, 1F)));
		lookup.put("Inferno", this.BuildStandardAbility("Inferno", "Deals low fire damage to all enemies", AbilityTargetType.AllEnemies, 3, false, true, new MagicDamageEffect(0, .75F)));
		lookup.put("Web", this.BuildStandardAbility("Web", "A standard attack which also slows the target", AbilityTargetType.OneEnemy, 0, false, false,
					new PhysicalDamageEffect(0),
					new ApplyStatusEffect(new StatChangeStatus("Web", StatChangeStatus.SpeedChange, -25, .75F, 3, -1))));

		lookup.put("Rend", this.BuildStandardAbility("Rend", "Deals low damage with lifesteal and slows the target", AbilityTargetType.OneEnemy, 0, false, false,
				new LifeStealEffect(0, 0, .7F, 0F, .5F),
				new ApplyStatusEffect(new StatChangeStatus("Rend", StatChangeStatus.SpeedChange, -25, .75F, 3, -1))));

		lookup.put("Corrosive Acid", this.BuildStandardAbility("Corrosive Acid", "A standard attack which also lowers attack and defense", AbilityTargetType.OneEnemy, 0, false, false,
				new PhysicalDamageEffect(0),
				new ApplyStatusEffect(new StatChangeStatus("Corrosive Acid", StatChangeStatus.AttackChange, 0, .5F, 3, -1)),
				new ApplyStatusEffect(new StatChangeStatus("Corrosive Acid", StatChangeStatus.DefenseChange, 0, .5F, 3, -1))));

		lookup.put("Drain", this.BuildStandardAbility("Drain", "Steals a low amount of HP from one target", AbilityTargetType.OneEnemy, 1, false, true, new LifeStealEffect(0, 0, 1F, 0F, 1F)));
		lookup.put("Absorb", this.BuildStandardAbility("Absorb", "Steals MP from one target", AbilityTargetType.OneEnemy, 1, false, true, new ManaStealEffect(0, 0, 0F, .25F)));
		lookup.put("Heal", this.BuildStandardAbility("Heal", "Heals one target", AbilityTargetType.OneAlly, 1, true, true, new HealEffect(20, 0.5F)));
		lookup.put("Mass Heal", this.BuildStandardAbility("Mass Heal", "Heals all party members", AbilityTargetType.AllAllies, 3, true, true, new HealEffect(20, 0.5F)));

		lookup.put("Poison", this.BuildStandardAbility("Poison", "Poisons one target", AbilityTargetType.OneEnemy, 0, false, true,
				new ApplyStatusEffect(new PoisonStatusEffect(), 1F)));
		lookup.put("Paralyze", this.BuildStandardAbility("Paralyze", "Paralyzes one target", AbilityTargetType.OneEnemy, 0, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Paralyze", StatChangeStatus.SpeedChange, -1000, 0, -1, -1), .5F)));
		lookup.put("Stop", this.BuildStandardAbility("Stop", "Freezes one target", AbilityTargetType.OneEnemy, 0, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Stop", StatChangeStatus.SpeedChange, -1000, 0, -1, 1000), .7F)));
		lookup.put("Silence", this.BuildStandardAbility("Silence", "Silences one target", AbilityTargetType.OneEnemy, 0, false, true,
				new ApplyStatusEffect(new SilenceEffect(), 1F)));
		lookup.put("Confusion", this.BuildStandardAbility("Confusion", "Confuses one target", AbilityTargetType.Self, 0, false, true,
				new ApplyStatusEffect(new ConfusionEffect(5, -1), 1F)));
		lookup.put("Blind", this.BuildStandardAbility("Blind", "Blinds one target", AbilityTargetType.OneEnemy, 0, false, true,
				new ApplyStatusEffect(new BlindTriggeredEffect(DamageType.Physical), 1F)));
		lookup.put("Death", this.BuildStandardAbility("Death", "Instantly kills one target", AbilityTargetType.OneEnemy, 0, false, true, new DeathEffect(.3F)));
		lookup.put("Mass Death", this.BuildStandardAbility("Mass Death", "Instantly kills all enemies", AbilityTargetType.AllEnemies, 0, false, true, new DeathEffect(.3F)));

		lookup.put("Weakness", this.BuildStandardAbility("Weakness", "Reduces one target's attack", AbilityTargetType.OneEnemy, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Weakness", StatChangeStatus.AttackChange, 0, .5F, -1, 1000))));
		lookup.put("Guardbreak", this.BuildStandardAbility("Guardbreak", "Reduces one target's defense", AbilityTargetType.OneEnemy, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Guardbreaker", StatChangeStatus.DefenseChange, 0, 0F, -1, 1000))));
		lookup.put("Disjunction", this.BuildStandardAbility("Disjunction", "Reduces one target's magic", AbilityTargetType.OneEnemy, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Disjunction", StatChangeStatus.MagicChange, 0, .5F, -1, 1000))));
		lookup.put("Curse", this.BuildStandardAbility("Curse", "Reduces one target's magic defense", AbilityTargetType.OneEnemy, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Curse", StatChangeStatus.MagicDefenseChange, 0, 0F, -1, 1000))));
		lookup.put("Slow", this.BuildStandardAbility("Slow", "Reduces one target's speed", AbilityTargetType.OneEnemy, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Slow", StatChangeStatus.SpeedChange, -50, .5F, -1, 1000))));

		lookup.put("Charge", this.BuildStandardAbility("Charge", "Increases user's attack", AbilityTargetType.Self, 1, false, false,
				new ApplyStatusEffect(new StatChangeStatus("Charge", StatChangeStatus.AttackChange, 0, 1.5F, -1, 1000))));
		lookup.put("Strength", this.BuildStandardAbility("Strength", "Increases one ally's attack", AbilityTargetType.OneAlly, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Strength", StatChangeStatus.AttackChange, 0, 1.5F, -1, 1000))));
		lookup.put("Mass Strength", this.BuildStandardAbility("Mass Strength", "Increases all allies' attack", AbilityTargetType.AllAllies, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Strength", StatChangeStatus.AttackChange, 0, 1.5F, -1, 1000))));
		lookup.put("Shield", this.BuildStandardAbility("Shield", "Increases one ally's defense", AbilityTargetType.OneAlly, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Shield", StatChangeStatus.DefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Mass Shield", this.BuildStandardAbility("Mass Shield", "Increases all allies' defense", AbilityTargetType.AllAllies, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Shield", StatChangeStatus.DefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Concentrate", this.BuildStandardAbility("Concentrate", "Increases one ally's magic", AbilityTargetType.OneAlly, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Concentrate", StatChangeStatus.MagicChange, 0, 1.5F, -1, 1000))));
		lookup.put("Resistance", this.BuildStandardAbility("Resistance", "Increases one ally's magic defense", AbilityTargetType.OneAlly, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Resistance", StatChangeStatus.MagicDefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Mass Resistance", this.BuildStandardAbility("Mass Resistance", "Increases all allies' defense", AbilityTargetType.AllAllies, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Resistance", StatChangeStatus.MagicDefenseChange, 0, 1.5F, -1, 1000))));
		lookup.put("Haste", this.BuildStandardAbility("Haste", "Increases one ally's speed", AbilityTargetType.OneAlly, 0, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Haste", StatChangeStatus.SpeedChange, 100, 2F, -1, 1000))));
		lookup.put("Mass Haste", this.BuildStandardAbility("Mass Haste", "Increases all allies' speed", AbilityTargetType.AllAllies, 1, false, true,
				new ApplyStatusEffect(new StatChangeStatus("Haste", StatChangeStatus.SpeedChange, 100, 2F, -1, 1000))));

		lookup.put("Spawn Broodlings", this.BuildStandardAbility("Spawn Broodlings", "Deals low damage and summons a broodling", AbilityTargetType.OneEnemy, 0, false, false,
				new PhysicalDamageEffect(0),
				new SummonEffect("Spiderling", "Spider")));

		lookup.put("Trip", this.BuildStandardAbility("Trip", "Deals low damage and delays target's turn", AbilityTargetType.OneEnemy, 1, false, false, new PhysicalDamageEffect(0), new DelayTurnEffect(25)));
		lookup.put("Snipe", new DelayedAbility(
				this.BuildStandardAbility("Snipe", "Deals large damage after a one turn delay", AbilityTargetType.OneEnemy, 0, false, false, new PhysicalDamageEffect(0).SetAttackMultiplier(2)),
				"Snipe", AbilityTargetType.OneEnemy, 0, false, false, new ArrayList<String>()));
		lookup.put("Blade Frenzy", new DelayedAbility(
				new IOneTimeEffect[]{ new DelayTurnEffect(-100) },
				new IOneTimeEffect[] { new PhysicalDamageEffect(0) },
				new DelayedAbility(
						new IOneTimeEffect[]{ new DelayTurnEffect(-100) },
						new IOneTimeEffect[] { new PhysicalDamageEffect(0) },
						new DelayedAbility(
								new IOneTimeEffect[]{ new DelayTurnEffect(-100) },
								new IOneTimeEffect[] { new PhysicalDamageEffect(0) },
								this.BuildStandardAbility("", "", AbilityTargetType.OneEnemy, 0, false, false, new PhysicalDamageEffect(0)),
								"", AbilityTargetType.OneEnemy, 0, false, false, new ArrayList<String>()),
						"", AbilityTargetType.OneEnemy, 0, false, false, new ArrayList<String>()),
				"Blade Frenzy", AbilityTargetType.OneEnemy, 0, false, false, ArrayExtensions.GetArray("Hits one enemy four times")));

		lookup.put("Paralysis Claw", this.BuildStandardAbility("Paralysis Claw", "Deals low damage and paralyzes target", AbilityTargetType.OneEnemy, 0, false, false,
				new PhysicalDamageEffect(0).SetAttackMultiplier(.8F),
				new ApplyStatusEffect(new StatChangeStatus("Paralyze", StatChangeStatus.SpeedChange, -1000, 0, -1, -1), .2F)));
		lookup.put("Assassinate", new DefaultAttackAbility());
		lookup.put("Wraithstrike", new DefaultAttackAbility());
		lookup.put("Energy Drain", new DefaultAttackAbility());
		lookup.put("Soul Drain", new DefaultAttackAbility());

		lookup.put("smallPotion", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-20)));
		lookup.put("medPotion", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-50)));
		lookup.put("highPotion", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-1000)));
		lookup.put("smallManaPotion", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, true, false, new FlatManaEffect(10)));
		lookup.put("highManaPotion", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, true, false, new FlatManaEffect(100)));
		lookup.put("elixir", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, true, false, new FlatDamageEffect(-1000), new FlatManaEffect(100)));
		lookup.put("megalixir", this.BuildStandardAbility("", "", AbilityTargetType.AllAllies, 0, true, false, new FlatDamageEffect(-1000), new FlatManaEffect(100)));
		lookup.put("antidote", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, false, true, new PurgeEffect("Poison")));
		lookup.put("echoScreen", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, false, false, new PurgeEffect("Silence")));
		lookup.put("parlyzHeal", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, false, false, new PurgeEffect("Paralyze")));
		lookup.put("pinwheel", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, false, false, new PurgeEffect("Confusion")));
		lookup.put("eyeDrops", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, false, false, new PurgeEffect("Blind")));
		lookup.put("panacea", this.BuildStandardAbility("", "", AbilityTargetType.OneAlly, 0, false, false, new PurgeEffect("Poison"), new PurgeEffect("Blind"), new PurgeEffect("Silence"), new PurgeEffect("Paralyze"), new PurgeEffect("Confusion"), new PurgeEffect("Stop"), new PurgeEffect("Slow")));
		lookup.put("phoenixDown", this.BuildStandardAbility("", "", AbilityTargetType.OneDeadAlly, 0, true, false, new SetHPEffect(1)));
		lookup.put("fireBomb", this.BuildStandardAbility("", "", AbilityTargetType.OneEnemy, 0, false, false, new MagicDamageEffect(30, 0, DamageType.Fire)));
		lookup.put("earthGem", this.BuildStandardAbility("", "", AbilityTargetType.OneEnemy, 0, false, false, new MagicDamageEffect(30, 0, DamageType.Earth)));
		lookup.put("iceCrystal", this.BuildStandardAbility("", "", AbilityTargetType.OneEnemy, 0, false, false, new MagicDamageEffect(30, 0, DamageType.Ice)));
		lookup.put("lightningRod", this.BuildStandardAbility("", "", AbilityTargetType.OneEnemy, 0, false, false, new MagicDamageEffect(30, 0, DamageType.Lightning)));

		lookup.put("Counter", this.BuildConstantAbility("Counter", "Chance to counter when attacked", new CounterattackEffect(50, lookup.get("BasicCounter"))));
		
		for(String key : this.lookup.keySet())
		{
			this.reverseLookup.put(this.lookup.get(key), key);
		}
	}

	public ICombatAbility GetAbilityWithName(String abilityName)
	{
		return lookup.get(abilityName);
	}
	
	public String GetLookupNameForAbility(ICombatAbility ability)
	{
		return reverseLookup.get(ability);
	}

	public ICombatAbility BuildStandardAbility(String abilityName, String description, int targetType, int mpCost, Boolean usableOutOfCombat, Boolean isSpell, IOneTimeEffect... effects)
	{
		ArrayList<String> descriptions = new ArrayList<String>();
		descriptions.add(description);
		return new StandardAbility(effects, abilityName, targetType, mpCost, usableOutOfCombat, isSpell, descriptions);
	}

	public ICombatAbility BuildConstantAbility(String abilityName, String description, IEffect... effects)
	{
		ArrayList<String> descriptions = new ArrayList<String>();
		descriptions.add(description);
		
		List effectList = new ArrayList();
		for(IEffect effect : effects)
		{
			effectList.add(effect);
		}

		return new ConstantAbility(abilityName, effectList, descriptions);
	}
	
	public ICombatAbility BuildConstantAbility(String abilityName, String description, List<IEffect> effects)
	{
		ArrayList<String> descriptions = new ArrayList<String>();
		descriptions.add(description);
		
		List<IEffect> effectList = new ArrayList<IEffect>();
		effectList.addAll(effects);

		return new ConstantAbility(abilityName, effectList, descriptions);
	}
	
	public List<IEffect> BuildConstantFlatStatChange(String category, int statAmount, int... statTypes)
	{
		List<IEffect> changes = new ArrayList<IEffect>();
		for(int statType : statTypes)
		{
			changes.add(new StatChangeStatus("beginnerBoost", statType, statAmount, 1, -1, -1));
		}
		
		return changes;
	}
}
