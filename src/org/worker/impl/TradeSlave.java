package org.worker.impl;

import org.OrionMule;

import viking.framework.worker.Worker;

public class TradeSlave extends Worker<OrionMule>
{

	public TradeSlave(OrionMule mission)
	{
		super(mission);
	}

	@Override
	public boolean needsRepeat()
	{
		return false;
	}

	@Override
	public void work()
	{
		script.log(this, false, "Trade slave");
	}
	
}
