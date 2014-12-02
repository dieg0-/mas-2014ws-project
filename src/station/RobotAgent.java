/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 4.5.n.
@since 02.12.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package station;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import shelf.ShelfAgent;
import utilities.Pose;
import java.io.IOException;

/**
 * <!--ROBOT AGENT CLASS-->
 * <p>Robot agent offers a service of "fetch" type when the agent is free. The
 * task of this agent is to fetch a shelf commanded by the {@link PickerAgent}.
 * They do not communicate directly with the shelves; instead, the communication
 * is done indirectly through the picker.</p>
 * <b>Attributes:</b>
 * <ul>
 * 	<li> <i>id:</i> the unique identification number of the robot.
 * 	<li> <i>position:</i> an instance of the class {@link Pose} with the robot position.
 * 	<li> <i>busy:</i> flag to reflect the status of the robot.
 * 	<li> <i>dfd:</i> agent description with the services offered.
 * </ul>
 * @author [DNA] Diego, Nicolas, Argentina
 */
@SuppressWarnings("serial")
public class RobotAgent extends Agent {
	
	private String id;
	protected Pose position;
	private boolean busy;
	protected DFAgentDescription dfd;
	
	protected void setup() {
		// PRINTOUTS: Initialization Messages
		System.out.println("\n--ROBOT------------------");
		System.out.println("Agent: " + getLocalName());
		// Random initialization of the position of the robot.
		this.position = new Pose();
		this.position.randomInit(true);
		// TODO: review is this flag is used/useful or not.
		this.busy = false;
		// Agent Description.
		this.dfd = new DFAgentDescription();
		this.dfd.setName(getAID());
		/* Service offer -> fetch. It serves as the criteria to find robots
		 * which are free (not busy) to delegate the task of fetching a shelf.
		 */
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fetch");
		sd.setName("Fetch-Service");
		this.dfd.addServices(sd);
		/* The robot agent must be provided with an argument which contains
		 * the identification number of the agent. If such argument is not
		 * provided, the agent won't be created.
		 * TODO: revise if we need arguments for the robot agents or not.
		 */
		Object[] args = this.getArguments();
		if (args != null && args.length > 0) {
			this.id = (String) args[0];
			// PRINTOUTS: ID Message.
			System.out.println("ID: " + this.id);
			System.out.println("  > Waiting.");
			/* Trying to register the Agent Description into the description
			 * facilitator (DF).
			 */
			try {
				DFService.register(this, dfd);
				// Waiting for the picker agent to request the robot to fetch.
				this.addBehaviour(new WaitForCommand());
			}
			/* If the process of registering the agent to the description facilitator
			 * fails, delete the agent. Make no sense to offer the service if no
			 * registration has been made. 
			 */
			catch (FIPAException fe) {
				System.err.println("\n[ERR] Agent could not be registered.");
				System.err.println("Agent will be deleted.");
				doDelete();
			}
		}
		/* If no arguments have been passed when creating the agent, it won't be
		 * registered to the description facilitator and, therefore, will be 
		 * deleted.
		 */
		else {
			System.err.println("\n[ERR] No ID given. Agent won't be register.");
			System.err.println("Agent will be deleted.");
			doDelete();
		}
		System.out.println("-------------------------\n");
	}
	
	/**
	 * <!--LOCALIZATION BEHAVIOUR-->
	 * <p>Behavior which is executed one time if the message sent by the {@link PickerAgent}
	 * has the ontology "localization". It will returns the current position
	 * of the robot.</p>
	 * <b>Attributes:</b>
	 * <ul>
	 * 	<li> <i>message:</i> the message sent to the robot from the picker.</li>
	 * </ul>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> position of the robot agent.
	 * </ul>
	 */
	private class LocalizationBehaviour extends SimpleBehaviour {
		
		protected ACLMessage message;
		/**
		 * Override constructor of the SimpleBehavior.
		 * @param a			this agent.
		 * @param message 	the message from the picker.
		 */
		public LocalizationBehaviour(Agent a, ACLMessage message) {
			super(a);
			this.message = message;
		}
		
		/**
		 * Main action of the behavior. Its outcome is the position of the
		 * robot as a print out, and a message sent to the picker with this
		 * agent position..
		 */
		public void action() {
			/* As soon as a picker establish communication with this agent,
			 * try to de-register from the description facilitator to avoid
			 * conversations with other picker agents at the same time. It
			 * is a way to say that this agent is busy now, having a conversation
			 * a picker agent. 
			 */
			try {
				DFService.deregister(myAgent);
			} catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName() + ": couldn't unregister.");
				done();
			}
			// This agent position.
			String current_pos = String.format("(%.2f, %.2f)", position.getX(), position.getY());
			System.out.println("  > Location: " + current_pos + ").");
			// Generation the reply message to the picker which ask for it.
			ACLMessage reply = this.message.createReply();
			if(this.message != null) {
				reply.setPerformative(ACLMessage.PROPOSE);
				double myPosition[] = position.poseToArray();
				try {
					// Sending the message to the picker agent.
					reply.setContentObject(myPosition);
					myAgent.send(reply);
					System.out.println("  > I have sent my localization");
					System.out.println("-------------------------\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			// In case the message is null, do not reply with this agent position.
			else {
				reply.setPerformative(ACLMessage.REFUSE);
				System.out.println(myAgent.getLocalName() + ": Error. Message invalid.");
				myAgent.send(reply);
				System.out.println("-------------------------\n");
			}
		}
		
		/**
		 * Once the localization has been sent to the picker agent, register this agent
		 * back to the description facilitator. Now, further communications with this
		 * agent can be established.
		 */
		public boolean done() {
			try {
				DFService.register(myAgent, dfd);
			} catch (FIPAException fe) {
				System.err.println("\n[ERR] Agent could not be registered.");
				myAgent.doDelete();
			}
			return true;
		}
	}
	
	
	/**
	 * <!--FETCH BEHAVIOUR-->
	 * <p>Behavior which simulates the action of fetch a given shelf. It puts
	 * the thread of the agent into sleep for a given amount of time. The
	 * time should simulate the distance the robot must travel in order to
	 * reach the shelf and brought it back to the station.</p>
	 * <b>Attributes:</b>
	 * <ul>
	 * 	<li> <i>timeout:</i> duration which the thread will be sleeping. </li>
	 * 	<li> <i>picker_position:</i> position of the {@link PickerAgent} </li>
	 * 	<li> <i>target:</i> position of the {@link ShelfAgent} as a string. </li>
	 *  <li> <i>message:</i>  the message sent to the robot from the picker. </li>
	 * </ul>
	 * <b>Outcome:</b>
	 * <ul>
	 * 	<li> A message to the picker agent informing that the shelf is in the station. </li>
	 * </ul>
	 * 
	 */
	private class FetchBehaviour extends SimpleBehaviour {
		
		private long timeout;
		private String picker_position;
		private String target;
		protected ACLMessage message;
		
		/**
		 * Override constructor of the SimpleBehavior.
		 * @param a				this agent.
		 * @param time_sleep	duration of sleeping in seconds.
		 * @param positions		position of the picker and the targeted shelf as a String.
		 * @param msg			the message sent by the picker.	
		 */
		public FetchBehaviour(Agent a, long time_sleep, String positions, ACLMessage msg) {
			super(a);
			String pos[] = positions.split(",");
			this.timeout = time_sleep;
			this.picker_position = String.format("%s,%s", pos[0], pos[1]);
			this.target = String.format("%s,%s", pos[2], pos[3]);
			this.message = msg;
		}
		
		/**
		 * Main action of the behavior. It works as follow:
		 * <ol>
		 * 	<li> The robot is de-register from the descriptor facilitator DF to
		 * inform it is not available anymore. It means that the picker
		 * agents won't find this agent when it search for free robots. </li>
		 * 	<li> Thread is putting to sleep by the amount of time specified. This
		 * as a way to simulate the time required to fetch the shelf. </li>
		 * 	<li> As soon as it wakes ups, it announces that it has brought the
		 * shelf to the station from where the command was sent. </li>
		 *  <li> A message informing the picker that the action has been completed
		 *  is sent. </li>
		 * </ol>
		 */
		public void action() {
			// Change the state to "busy".
			try {
				DFService.deregister(myAgent);
				// PRINTOUTS: information of where the picker and the shelf are.
				System.out.println(myAgent.getLocalName() + ": [fetching].");
				System.out.println("  > Picker at: " + this.picker_position);
				System.out.println("  > Target at: " + this.target);
				System.out.println("--------------------------\n");
				// Simulating the fetching process.
				Thread.sleep(this.timeout*1000);
			}
			catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName() + ": couldn't unregister.");
			}
			catch (Exception e) {
				System.err.println(myAgent.getLocalName() + ": terminated abruptly.");
			}
		}
		
		/**
		 * Successfully completing the fetch action of a specified shelf. The
		 * agent tries to re-register into the description facilitator DF to
		 * announce it is free again, and therefore, it can be found again by
		 * the picker agent.
		 */
		public boolean done() {
			System.out.println("\n------------------------------------");
			System.out.println(myAgent.getLocalName() + ": [report].");
			System.out.println("  > Shelf has been fetched.");
			System.out.println("------------------------------------\n");
			ACLMessage reply = this.message.createReply();
			reply.setPerformative(ACLMessage.PROPOSE);
			myAgent.send(reply);
			try {
				Thread.sleep(this.timeout*1000);
				DFService.register(myAgent, dfd);
			}
			catch (Exception e) {
				
			}
			return true;
		}
	}
	
	/**
	 * <!--WAITING BEHAVIOUR-->
	 * <p>Cyclic behavior which is constantly executed. It waits until
	 * a message form the {@link PickerAgent} is received to
	 * execute the action() method.</p>
	 */
	private	class WaitForCommand extends CyclicBehaviour {
		
		/**
		 * Main action of the behavior. When a message is received,
		 * this agent will inspect the ontology of the message and
		 * will execute the given command. Until now, three types
		 * of commands can be given to the robot:
		 * <ul>
		 * 	<li> <i><ins>status:</i> the agent informs the picker its status. [Deprecated] </li>
		 * 	<li> <i><ins>localization:</i> the agent informs the picker its location. </li>
		 * 	<li> <i><ins>fetch:</i> the agent executes the FetchBehavior. </li>
		 * </ul>
		 * <p>When other ontology is sent to this agent, it will do nothing and 
		 * will inform that the commanded action is not recognized or implemented.</p>
		 */
		public void action() {
			ACLMessage msg = myAgent.receive();
			if (msg != null) {
				String msg_content = msg.getContent();
				String msg_command = msg.getOntology();
				// PRINTOUTS: Message received with the commanded action.
				System.out.println("--------------------------");
				System.out.print(myAgent.getLocalName() + ": ");
				System.out.println("[message received].");
				System.out.println("  > Command: " + msg_command);
				// Inspection of the commanded action.
				if (msg_command.matches("status")) {
					System.out.println("  > Busy: " + busy);
					System.out.println("--------------------------");
				}
				else if (msg_command.matches("localization")) {
					myAgent.addBehaviour(new LocalizationBehaviour(myAgent, msg));
				}
				else if (msg_command.matches("fetch")) {
					myAgent.addBehaviour(new FetchBehaviour(myAgent, 20, msg_content, msg));
				}
				else {
					System.out.println("  > No valid command.");
					System.out.println("--------------------------\n");
				}
			}
			block();
		}
	}
	
	/**
	 * <!--TAKEDOWN-->
	 * <p>Safe delete of the agent.</p>
	 */
	protected void takeDown() {
		System.out.println(this.getLocalName() + " out of service.");
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			
		}
	}

}