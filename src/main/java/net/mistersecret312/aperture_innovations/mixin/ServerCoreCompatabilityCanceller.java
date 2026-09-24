package net.mistersecret312.aperture_innovations.mixin;

import java.util.List;

public class ServerCoreCompatabilityCanceller implements com.bawnorton.mixinsquared.api.MixinCanceller
{
	@Override
	public boolean shouldCancel(List<String> list, String s)
	{
		if(s.equals("me.wesley1808.servercore.mixin.optimizations.sync_loads.ServerLevelMixin"))
			return true;
		return false;
	}
}
