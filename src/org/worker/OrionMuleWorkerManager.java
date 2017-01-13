package org.worker;

import org.OrionMule;
import org.worker.impl.TradeSlave;

import viking.framework.worker.Worker;
import viking.framework.worker.WorkerManager;

public class OrionMuleWorkerManager extends WorkerManager<OrionMule>
{
	public String slaveName;
	public boolean hasOrder, shouldLogin, hasBeenTradedWith;
	
	private Worker<OrionMule> tradeSlave;
	
	public OrionMuleWorkerManager(OrionMule mission)
	{
		super(mission);
		tradeSlave = new TradeSlave(mission);
	}

	@Override
	public Worker<OrionMule> decide()
	{
		return null;
	}

}
