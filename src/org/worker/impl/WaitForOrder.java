package org.worker.impl;

import org.OrionMule;

import viking.api.Timing;
import viking.framework.worker.Worker;

public class WaitForOrder extends Worker<OrionMule>
{
	private static final long CHECK_TIME = 60000; //checks for order every minute
	
	private long lastCheckTime = Timing.currentMs();
	
	public WaitForOrder(OrionMule mission)
	{
		super(mission);
	}

	@Override
	public boolean needsRepeat()
	{
		return !mission.hasOrder;
	}

	@Override
	public void work()
	{
		if(Timing.timeFromMark(lastCheckTime) > CHECK_TIME)
		{
			mission.getScript().log(this, false, "Checking for order");
			lastCheckTime = Timing.currentMs();
		}
	}

}