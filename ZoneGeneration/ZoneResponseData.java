package TBC.ZoneGeneration;

import java.io.Serializable;
import java.util.HashMap;

public class ZoneResponseData implements Serializable
{
	public int ChunkXPos;
	public int ChunkZPos;
	public HashMap<Integer, ZoneChunkData> ZoneData;
}