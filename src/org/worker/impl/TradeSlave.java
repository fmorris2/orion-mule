package org.worker.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.OrionMule;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.RS2Widget;

import viking.api.Timing;
import viking.framework.command.CommandReceiver;
import viking.framework.worker.Worker;

public class TradeSlave extends Worker<OrionMule>
{
	private static final int SLAVE_DIST_THRESH = 15;
	private static final int VALUE_MASTER = 334, VALUE_CHILD = 24;
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
		else if(mission.tradeComplete)
			completeOrder();
		else if(myPos.distance(mission.slavePos) < SLAVE_DIST_THRESH)
		{
			mission.shouldLogin = true;
			if(trade.isCurrentlyTrading() || mission.hasBeenTradedWith)
			{
				script.log(this, false, "Received trade request from slave! Going through trade process....");
				goThroughTrade();
			}
			else if(worlds.getCurrentWorld() != mission.world)
			{
				script.log(this, false, "Hopping to slave world " + mission.world);
				hopper.hop(mission.world);
			}
		}
		else //still waiting for slave to come
		{
			mission.shouldLogin = false;
			
			if(client.isLoggedIn())
				logoutTab.logOut();
		}	
	}
	
	private void goThroughTrade()
	{
		script.log(this, false, "Go through trade");
		if(script.trade.isCurrentlyTrading())
		{
			script.log(this, false, "Trade is currently open with slave....");
			RS2Widget accept = widgets.getWidgetContainingText("Accept");
			boolean isSecondInter = trade.isSecondInterfaceOpen();
			if(isSecondInter)
				parseTradeVal();
			
			if(accept != null && accept.interact())
			{
				script.log(this, false, "Accepting through trade...");
				
				if(isSecondInter && Timing.waitCondition(() -> mission.tradeComplete, 10000))
				{
					script.log(this, false, "Successfully completed trade");
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
			if(slave != null && slave.getName().equals(mission.slaveName) && slave.interact("Trade with")
					&& Timing.waitCondition(() -> trade.isCurrentlyTrading(), 5000))
				mission.hasBeenTradedWith = false;
		}
	}
	
	private void parseTradeVal()
	{
		script.log(this, false, "Parse trade value");
		RS2Widget valWidget = script.widgets.get(VALUE_MASTER, VALUE_CHILD);
		if(valWidget == null)
			return;
		
		String toParse = valWidget.getMessage();
		
		if(toParse == null)
			return;
		
		Pattern p = Pattern.compile("-?\\d+");
		Matcher m = p.matcher(toParse);
		String parsedNumbers = "";
		while (m.find()) 
			parsedNumbers += m.group();
		
		tradeValue = Integer.parseInt(parsedNumbers);
		script.log(this, false, "Parsed trade value: " + tradeValue);
	}
	
	private void completeOrder()
	{
		script.log(this, false, "Complete order");
		((CommandReceiver)(script)).receiveCommand("mule:complete:"+tradeValue);
		logoutTab.logOut();
		mission.tradeComplete = false;
		mission.hasOrder = false;
		mission.slaveName = null;
		mission.slavePos = null;
		mission.shouldLogin = false;
	}
	
}
