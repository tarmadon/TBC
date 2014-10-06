package TBC;

import java.io.Serializable;

public class Pair<T1, T2> implements Serializable
{
	public final T1 item1;
	public final T2 item2;
	public Pair(T1 item1, T2 item2)
	{
		this.item1 = item1;
		this.item2 = item2;
	}
}