/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 3.5.n.
@since 27.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package station;

import java.io.IOException;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import utilities.Pose;

/**
 * Robot agent offers a service of "fetch" type when the agent is free. The
 * task of this agent is to fetch a shelf commanded by the {@link PickerAgent}.
 * The do not communicate directly with the shelves; instead, the communication
 * is done indirectly through the {@link PickerAgent}.
 * <p>
 * Attributes:
 * <li> id - the unique identification number of the robot.
 * <li> position - an instance of the class {@link Pose} with the robot position.
 * <li> busy - flag to reflect the status of the robot.
 * <li> dfd - agent description with the services offered.
 */
@SuppressWarnings("serial")
public class RobotAgent extends Agent {
	// Attributes.
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
		this.busy = false;
		// Agent Description.
		this.dfd = new DFAgentDescription();
		this.dfd.setName(getAID());
		/* Service offer -> fetch. It serves as the criteria to find robots
		 * which are free (not busy) to delegate the task of fetching a shelf.
		 * The service includes a property called position, which contains the
		 * current position of the robot as a string.
		 */
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fetch");
		sd.setName("Fetch-Service");
		Property p = new Property();
		p.setName("position");
		p.setValue(position.parsePose());
		sd.addProperties(p);
		this.dfd.addServices(sd);
		/* The robot agent must be provided with an argument which contains
		 * the identification number of the agent. If such argument is not
		 * provided, the agent won't be created.
		 */
		Object[] args = this.getArguments();
		if (args != null && args.length > 0) {
			this.id = (String) args[0];
			// PRINTOUTS: ID Message.
			System.out.println("ID: " + this.id);
			System.out.println("  > Waiting.");
			// Waiting for the picker agent to request the robot to fetch.
			this.addBehaviour(new OfferRequestsServer());
			/* Trying to register the Agent Description into the description
			 * facilitator (DF).
			 */
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				System.err.println("\n[ERR] Agent could not be registered.");
			}
		}
		else {
			System.err.println("\n[ERR] No ID given. Agent won't be register.");
			doDelete();
		}
		System.out.println("-------------------------\n");
	}
	
	/**
	 * Behavior which is executed one time if the message sent by the picker
	 * has the ontology "localization". It will returns the current position
	 * of the robot.
	 * @return position of the robot agent.
	 */
	private class LocalizationBehaviour extends SimpleBehaviour {
		
		protected ACLMessage message;
		/**
		 * Override constructor of the SimpleBehavior.
		 * @param a	this agent.
		 */
		public LocalizationBehaviour(Agent a, ACLMessage message) {
			super(a);
			this.message = message;
		}
		/**
		 * Main action of the behavior. It returns the position of the
		 * robot as a print out.
		 */
		public void action() {
			String current_pos = String.format("(%.2f, %.2f)", position.getX(), position.getY());
			System.out.println("  > Location: " + current_pos + ").");
			ACLMessage reply = this.message.createReply();
			if(this.message != null) {
				reply.setPerformative(ACLMessage.PROPOSE);
				double myPosition[] = position.poseToArray();
				try {
					reply.setContentObject(myPosition);
					myAgent.send(reply);
					System.out.println("  > I have sent my localization");
					System.out.println("-------------------------\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				reply.setPerformative(ACLMessage.REFUSE);
				System.out.println(myAgent.getLocalName() + ": Error. Message invalid.");
				myAgent.send(reply);
				System.out.println("-------------------------\n");
			}
		}
	
		public boolean done() {
			return true;
		}
	}
	
	/**
	 * Behavior which simulates the action of fetch a given shelf. It puts
	 * the thread of the agent into sleep for a given amount of time. The
	 * time should simulate the distance the robot must travel in order to
	 * reach the shelf and brought it back to the station.
	 * <p>
	 * Attributes:
	 * <li> timeout - duration which the thread will be sleeping.
	 * <li> target - position of the shelf as a string.
	 */
	private class FetchBehaviour extends SimpleBehaviour {
		
		private long timeout;
		private String picker_position;
		private String target;
		protected ACLMessage message;
		/**
		 * Override constructor of the SimpleBehavior.
		 * @param a					this agent.
		 * @param time_sleep		duration of sleeping.
		 * @param shelf_position	position of the shelf.
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
		 * <li> The robot is de-register from the descriptor facilitator DF to
		 * inform it is not available anymore. It means that the picker
		 * agent won't find this agent when it search for free robots.
		 * <li> Thread is putting to sleep by the amount of time specified.
		 * <li> As soon as it wakes ups, announcing that it has returned the
		 * shelf to its original position, the done() method is executed.
		 */
		public void action() {
			System.out.println(myAgent.getLocalName() + ": [fetching].");
			System.out.println("  > Picker at: " + this.picker_position);
			System.out.println("  > Target at: " + this.target);
			System.out.println("--------------------------\n");
			try {
				
				DFService.deregister(myAgent);
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
	 * Cyclic behavior which is constantly executed. It waits until
	 * a message form the picker agent is sent to this agent to
	 * execute the action() method.
	 */
	private	class OfferRequestsServer extends CyclicBehaviour {
		/**
		 * Main action of the behavior. When a message is received,
		 * this agent will inspect the ontology of the message and
		 * will execute the given command. Until now, three types
		 * of commands can be given to the robot:
		 * <li> status: the agent informs the picker its status. [Deprecated]
		 * <li> localization: the agent informs the picker its location.
		 * <li> fetch: the agent executes the FetchBehavior.
		 * When other ontology is sent to this agent, it will do nothing and 
		 * will inform that the commanded action is not recognized or implemented.
		 */
		public void action() {
			ACLMessage msg = myAgent.receive();
			if (msg != null) {
				System.out.println("--------------------------");
				String msg_content = msg.getContent();
				String msg_command = msg.getOntology();
				// PRINTOUTS: Message received with the commanded action.
				System.out.print(myAgent.getLocalName() + ": ");
				System.out.println("[message received].");
				System.out.println("  > Command: " + msg_command);
				// Inspection of the commanded action.
				if (msg_command.matches("status")) {
					System.out.println("  > Busy: " + busy);
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
	 * Safe delete of the agent.
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