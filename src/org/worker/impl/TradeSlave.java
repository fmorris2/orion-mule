package org.worker.impl;

import org.OrionMule;
import org.osbot.rs07.api.map.Position;

import viking.api.Timing;
import viking.framework.worker.Worker;

public class TradeSlave extends Worker<OrionMule>
{
	private static final int SLAVE_DIST_THRESH = 15;
	private static final long CHECK_TIME = 45000; //updates slave info every 45 secs
	private static final long FAIL_SAFE = 60000 * 15; //15 minute fail safe
	
	private long lastCheckTime;
	private Position myPos;
	
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
		if(Timing.timeFromMark(lastCheckTime) > CHECK_TIME)
		{
			script.log(this, false, "Updating slave info");
			mission.ORION_MAIN.receiveCommand("mule:poll");
			lastCheckTime = Timing.currentMs();
		}
		
		if(myPos == null)
		{
			mission.shouldLogin = true;
			if(client.isLoggedIn())
				myPos = myPosition();
		}
		else if(myPos.distance(mission.slavePos) < SLAVE_DIST_THRESH)
		{
			mission.shouldLogin = true;
			if(mission.hasBeenTradedWith)
			{
				script.log(this, false, "Received trade request from slave! Going through trade process....");
			}
			else if(worlds.getCurrentWorld() != mission.world)
			{
				script.log(this, false, "Hopping to slave world " + mission.world);
				worlds.hop(mission.world);
			}
			else
				script.log(this, false, "Waiting for trade request from slave");
		}
		else //still waiting for slave to come
		{
			mission.shouldLogin = false;
			
			if(client.isLoggedIn())
				logoutTab.logOut();
			
			script.log(this, false, "Mule pos: " + myPos);
			script.log(this, false, "Slave pos: " + mission.slavePos);
			script.log(this, false, "Distance: " + myPos.distance(mission.slavePos));
			
			if(Timing.timeFromMark(mission.orderStartTime) > FAIL_SAFE)
			{
				script.log(this, false, "Failsafe reached! Resetting....");
				mission.hasOrder = false;
				mission.ORION_MAIN.receiveCommand("mule:reset");
			}
		}
			
	}
	
}
