package org;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Message.MessageType;
import org.worker.OrionMuleWorkerManager;

import viking.api.Timing;
import viking.framework.command.CommandReceiver;
import viking.framework.goal.GoalList;
import viking.framework.goal.impl.InfiniteGoal;
import viking.framework.mission.Mission;
import viking.framework.script.VikingScript;
import viking.framework.worker.Worker;

public class OrionMule extends Mission implements CommandReceiver
{
	public final CommandReceiver ORION_MAIN;
	private OrionMuleWorkerManager workerManager = new OrionMuleWorkerManager(this);
	
	public Position slavePos;
	public String slaveName;
	public int world;
	public long orderStartTime;
	public boolean hasOrder, shouldLogin, hasBeenTradedWith, tradeComplete;
	
	public OrionMule(VikingScript script)
	{
		super(script);
		ORION_MAIN = (CommandReceiver)script;
	}

	@Override
	public boolean canEnd()
	{
		return false;
	}

	@Override
	public String getMissionName()
	{
		return "Orion Mule";
	}

	@Override
	public String getCurrentTaskName()
	{
		Worker<OrionMule> current = workerManager.getCurrent();
		
		return current == null ? "Null" : current.toString();
	}

	@Override
	public String getEndMessage()
	{
		return "Orion Mule has ended!";
	}

	@Override
	public GoalList getGoals()
	{
		return new GoalList(new InfiniteGoal());
	}

	@Override
	public String[] getMissionPaint()
	{
		return null;
	}

	@Override
	public int execute()
	{
		workerManager.work();
		return 500;
	}

	@Override
	public void onMissionStart()
	{}

	@Override
	public void resetPaint()
	{}

	@Override
	public void receiveCommand(String command)
	{
		script.log(this, false, "Received command: " + command);
		
		//receive order info
		String[] firstParts = command.split(";");
		if(firstParts.length == 0) return;
		
		int x = 0, y = 0, z = 0;
		for(String part : firstParts)
		{
			String[] secondParts = part.split(":");
			if(secondParts.length < 2) return;
			
			//set vars
			String key = secondParts[0], val = secondParts[1];
			if(key.equals("name")) slaveName = val;
			else if(key.equals("world")) world = Integer.parseInt(val);
			else if(key.equals("x")) x = Integer.parseInt(val);
			else if(key.equals("y")) y = Integer.parseInt(val);
			else if(key.equals("z")) z = Integer.parseInt(val);
		}
		
		slavePos = new Position(x, y, z);
		if(!hasOrder)
			orderStartTime = Timing.currentMs();
		
		hasOrder = true;
	}
	
	@Override
	public void onMessage(Message m)
	{
		if(m == null)
			return;
		
		if(m.getType() == MessageType.RECEIVE_TRADE && m.getMessage().contains(slaveName))
		{
			script.log(this, false, "Mule has received trade from slave!");
			hasBeenTradedWith = true;
		}
		
		if(m.getType() == MessageType.GAME && m.getMessage().contains("Accepted trade"))
			tradeComplete = true;
	}

}
