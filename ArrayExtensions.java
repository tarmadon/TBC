package TBC;

import java.util.ArrayList;
import java.util.List;

import TBC.Combat.Effects.IEffect;

public class ArrayExtensions
{
	public static ArrayList<String> GetArray(String... pieces)
	{
		ArrayList<String> list = new ArrayList<String>();
		for(String piece : pieces)
		{
			list.add(piece);
		}
		
		return list;
	}
	
	public static List<IEffect> MergeLists(List<IEffect>... pieces)
	{
		ArrayList<IEffect> list = new ArrayList<IEffect>();
		for(List<IEffect> piece : pieces)
		{
			list.addAll(piece);
		}
		
		return list;
	}
}
