package org;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Message.MessageType;
import org.worker.OrionMuleWorkerManager;

import viking.framework.command.CommandReceiver;
import viking.framework.goal.GoalList;
import viking.framework.goal.impl.InfiniteGoal;
import viking.framework.mission.Mission;
import viking.framework.script.VikingScript;
import viking.framework.worker.Worker;

public class OrionMule extends Mission implements CommandReceiver
{
	private CommandReceiver orion_main;
	private OrionMuleWorkerManager workerManager = new OrionMuleWorkerManager(this);
	
	public OrionMule(VikingScript script)
	{
		super(script);
		orion_main = (CommandReceiver)script;
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
		
	}
	
	@Override
	public void onMessage(Message m)
	{
		if(m != null && m.getType() == MessageType.RECEIVE_TRADE && m.getMessage().contains(workerManager.slaveName))
			workerManager.hasBeenTradedWith = true;
	}

}
