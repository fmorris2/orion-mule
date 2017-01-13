package org.worker;

import org.OrionMule;
import org.worker.impl.TradeSlave;

import viking.framework.worker.Worker;
import viking.framework.worker.WorkerManager;

public class OrionMuleWorkerManager extends WorkerManager<OrionMule>
{	
	private Worker<OrionMule> tradeSlave, waitForOrder;
	
	public OrionMuleWorkerManager(OrionMule mission)
	{
		super(mission);
		tradeSlave = new TradeSlave(mission);
	}

	@Override
	public Worker<OrionMule> decide()
	{
		if(mission.hasOrder)
			return tradeSlave;
		
		return null;
	}

}
