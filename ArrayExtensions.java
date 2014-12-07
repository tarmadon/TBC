package TBC;

import java.util.ArrayList;

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
}
