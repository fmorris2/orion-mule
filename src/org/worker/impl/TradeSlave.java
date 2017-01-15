package org.worker.impl;

import org.OrionMule;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Player;

import viking.api.Timing;
import viking.framework.command.CommandReceiver;
import viking.framework.worker.Worker;

public class TradeSlave extends Worker<OrionMule>
{
	private static final int SLAVE_DIST_THRESH = 15;
	private static final long CHECK_TIME = 45000; //updates slave info every 45 secs
	private static final long FAIL_SAFE = 60000 * 12; //15 minute fail safe
	
	private long lastCheckTime;
	private int tradeValue;
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
		else if(Timing.timeFromMark(mission.orderStartTime) > FAIL_SAFE)
		{
			script.log(this, false, "Failsafe reached! Resetting....");
			mission.hasOrder = false;
			mission.ORION_MAIN.receiveCommand("mule:reset");
			mission.waitMs(3000);
		}
		else if(myPos.distance(mission.slavePos) < SLAVE_DIST_THRESH)
		{
			script.log(this, false, "Time since start: " + Timing.timeFromMark(mission.orderStartTime));
			mission.shouldLogin = true;
			if(mission.hasBeenTradedWith)
			{
				script.log(this, false, "Received trade request from slave! Going through trade process....");
				goThroughTrade();
			}
			else if(worlds.getCurrentWorld() != mission.world)
			{
				script.log(this, false, "Hopping to slave world " + mission.world);
				worlds.hop(mission.world % 100);
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
		}	
	}
	
	private void goThroughTrade()
	{
		script.log(this, false, "Go through trade");
		if(script.trade.isCurrentlyTrading())
		{
			script.log(this, false, "Trade is currently open with slave....");
			boolean isSecondInter = trade.isSecondInterfaceOpen();
			if(trade.didOtherAcceptTrade() || isSecondInter)
			{
				if(isSecondInter)
					parseTradeVal();
				
				if(trade.acceptTrade())
				{
					script.log(this, false, "Successfully accepted trade!");
					
					if(isSecondInter && Timing.waitCondition(() -> !trade.isCurrentlyTrading(), 3500))
						completeOrder();
				}
			}
			else
				script.log(this, false, "Waiting for slave to accept trade...");
			
		}
		else //open trade
		{
			script.log(this, false, "Opening trade with slave");
			Player slave = script.trade.getLastRequestingPlayer();
			if(slave != null && slave.getName().equals(mission.slaveName) && slave.interact("Trade with"))
				Timing.waitCondition(() -> trade.isCurrentlyTrading(), 3500);
		}
	}
	
	private void parseTradeVal()
	{
		script.log(this, false, "Parse trade value");
	}
	
	private void completeOrder()
	{
		script.log(this, false, "Complete order");
		((CommandReceiver)(script)).receiveCommand("mule:complete:"+tradeValue);
	}
	
}
