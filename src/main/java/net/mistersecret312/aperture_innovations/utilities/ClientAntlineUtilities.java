package net.mistersecret312.aperture_innovations.utilities;

import java.util.HashMap;

public class ClientAntlineUtilities
{
	public static final HashMap<Integer, Boolean> ACTIVE_ANTLINES = new HashMap<>();

	public static boolean isActive(int id)
	{
		return ACTIVE_ANTLINES.getOrDefault(id, false);
	}

	public static void setActive(int id, boolean active)
	{
		ACTIVE_ANTLINES.put(id, active);
	}
}
