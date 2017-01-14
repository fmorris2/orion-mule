package org.worker.impl;

import org.OrionMule;
import org.osbot.rs07.api.map.Position;

import viking.api.Timing;
import viking.framework.worker.Worker;

public class TradeSlave extends Worker<OrionMule>
{
	private static final int SLAVE_DIST_THRESH = 15;
	private static final long CHECK_TIME = 45000; //updates slave info every 45 secs
	
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
		else if(myPosition().distance(mission.slavePos) < SLAVE_DIST_THRESH)
		{
			mission.shouldLogin = true;
			if(mission.hasBeenTradedWith)
			{
				script.log(this, false, "Received trade request from slave! Going through trade process....");
			}
			else
				script.log(this, false, "Waiting for trade request from slave");
		}
		else //still waiting for slave to come
		{
			if(client.isLoggedIn())
				logoutTab.logOut();
			if(bot.getWorld() != mission.world)
			{
				script.log(this, false, "Hopping to slave world " + mission.world);
				bot.setWorld(mission.world);
			}
		}
			
	}
	
}
