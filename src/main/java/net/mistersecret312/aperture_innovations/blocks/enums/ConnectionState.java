package net.mistersecret312.aperture_innovations.blocks.enums;

public enum ConnectionState
	{
		NONE("none"),
		LINK("link"),
		SIDE("side"),
		SIDE_UP("side_up"),
		UP("up"),
		DOWN("down");

		public final String name;
		ConnectionState(String name)
		{
			this.name = name;
		}

		public static ConnectionState fromString(String name)
		{
			return switch(name)
			{
				case "link" -> LINK;
				case "side" -> SIDE;
				case "side_up" -> SIDE_UP;
				case "up" -> UP;
				case "down" -> DOWN;

				default -> NONE;
			};

		}
	}