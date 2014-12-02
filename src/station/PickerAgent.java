/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 3.5.n.
@since 27.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
 **/

package station;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utilities.PrinterUtil;
import utilities.Pose;

/**
 * Picker agent which is in charge of the dynamic between the orders, the robots
 * and the shelves. It picks an order, broadcasts the needed parts to the shelves
 * and picks the robot nearest to a chosen shelf; it then commands the robot
 * to fetch the shelf.
 * <p>
 * Attributes:
 * <li>activeAgent  - array of IDs which store found free RobotAgent.
 * <li>printer		- utility to implement colored messages.
 * <li>busy			- status of the picker agent.	
 */
@SuppressWarnings("serial")
public class PickerAgent extends Agent {
	
	private AID[] activeAgent;
	@SuppressWarnings("unused")
	private PrinterUtil printer;
	protected Pose position;
	protected boolean busy;

	protected void setup() {
		this.printer = new PrinterUtil(5);
		busy = false;
		// PRINTOUTS: Initialization Messages.
		System.out.println("\n--PICKER-------------");
		System.out.println("Agent: " + this.getAID().getLocalName());
		System.out.println("Picker Launched!");
		/* TODO: [Diego] Broadcasting the order to the shelves to
		 * search for the parts. From the shelves which answered
		 * one is chosen according to the distant of it w.r.t. the
		 * picker. The picker must retrieve the localization of the
		 * shelf (coordinates) and send them to the chosen robot.
		 * The coordinates of the shelf is simulated with an instance
		 * of the class pose, randomly initialized.
		 */
		this.position = new Pose();
		this.position.randomInit(true);
		System.out.println("---------------------\n");
		
		Pose virtualShelf = new Pose();
		virtualShelf.randomInit(false);
		// Behaviors for the pickerAgent.
		/* TODO: implement the GetRobotAgents behavior to be called after
		 * the logic of the GetNewOrder Behavior [Argentina].
		 */
		this.addBehaviour(new GetRobotAgents(this, 15000, virtualShelf));
		this.addBehaviour(new UpdatePickerStatus());
		this.addBehaviour(new GetNewOrder());

	}
	
	/**
	 * Behavior which is executed cyclicly every specified amount
	 * of time given by the argument period. The pickerAgent will
	 * query for robotAgents which are registered to the DF, description
	 * facilitator, and are offering the service of "fetch". Then,
	 * it will choose the robot nearest to the shelf, and send to it the
	 * coordinates of the target.
	 *
	 **/
	private class GetRobotAgents extends TickerBehaviour {
		
		protected Pose target;
		protected int repliesCnt = 0;
		protected double currentMinDistance = Double.MAX_VALUE;
		protected AID closestRobot;
		/**
		 * Override constructor of a TickerBehavior
		 * @param a			this agent.
		 * @param period	amount of time between execution of the behavior in ms.
		 * @param targetS	location of the target shelf as an instance of {@link Pose}.
		 */
		public GetRobotAgents(Agent a, long period, Pose targetS) {
			super(a, period);
			this.target = targetS;
		}
		/**
		 * Main action of the behavior. It implements the following thread of 
		 * actions:
		 * <li> Search for {@link RobotAgent}s which offer the service "fetch".
		 * Such robots are said to be free.
		 * <li> Once the IDs of the robots are extracted, it compares the
		 * position of the agents w.r.t. the target shelf.
		 * <li> The nearest robot wins, and the coordinates of the target is
		 * set to it.
		 * <p><p>
		 */
		protected void onTick() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			// Creation of Template for the search.
			sd.setType("fetch");
			template.addServices(sd);
			System.out.println("------------------------------------");
			try {
				// Searching process.
				DFAgentDescription[] result = DFService.search(myAgent, template);
				// PRINTOUTS: Agents found.
				System.out.println(myAgent.getLocalName() + ": [searching agents].");
				System.out.println("Active agents:");
				activeAgent = new AID[result.length];
				// If not agents are found, do nothing.
				if (result.length == 0) {
					System.out.println("  > No free agents.");
					System.out.println("------------------------------------\n");
				}
				// Else, choose the nearest robot to the shelf.
				else {
					for (int i = 0; i < result.length; ++i) {
						// Listing the agents ID's found.
						activeAgent[i] = result[i].getName();
						System.out.println("  > " + activeAgent[i].getName());
					}
					System.out.println("Robot picking will take place.");
					System.out.println("------------------------------------\n");
					/* TODO: Choose the nearest robot to the shelf, and add
					 * him as the receiver of the message.
					 */
					ACLMessage query = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < result.length; ++i) {
						query.addReceiver(result[i].getName());
					}
					// Fill the message's body and send it.
					query.setOntology("localization");
					query.setConversationId("select-robot");
					//query.setContent(this.target.parsePose());
					myAgent.send(query);
					/****************************************************************/
					MessageTemplate selectRobotTemplate = MessageTemplate.MatchConversationId("select-robot");
					while(this.repliesCnt < activeAgent.length) {
						ACLMessage reply = myAgent.receive(selectRobotTemplate);
						if (reply != null) {
							// Reply received
							if (reply.getPerformative() == ACLMessage.PROPOSE) {
								// This is an offer 
								double robotPosition[] = (double[]) reply.getContentObject();
								Pose robotPose = new Pose();
								robotPose = robotPose.arrayToPose(robotPosition);
								double distance = robotPose.distance(position);
								if(distance <= currentMinDistance){
									this.currentMinDistance = distance;
									this.closestRobot = reply.getSender();
								}
							}
							this.repliesCnt++;
						}
						else {
							block();
						}
						
					}
					Thread.sleep(1000);
					System.out.println("------------------------------------");
					System.out.println(myAgent.getLocalName() + ": [commanding to fetch].");
					System.out.println("  > Closet robot found: " + this.closestRobot.getName());
					System.out.println("  > Fetch sent to the chosen agent.");
					System.out.println("  > Waiting for finalization.");
					System.out.println("------------------------------------");
					ACLMessage command = new ACLMessage(ACLMessage.CFP);
					command.addReceiver(this.closestRobot);
					command.setOntology("fetch");
					command.setConversationId("shelf-here");
					String content = String.format("%s,%s", position.parsePose(), this.target.parsePose());
					command.setContent(content);
					myAgent.send(command);
					/*******************************************************************************/
					boolean here = false;
					MessageTemplate shelfHereTemplate = MessageTemplate.MatchConversationId("shelf-here");
					while (!here) {
						ACLMessage confimation = myAgent.receive(shelfHereTemplate);
						if (confimation != null) {
							System.out.println("------------------------------------");
							System.out.println(myAgent.getLocalName() + ": [status].");
							System.out.println("  > Shelf here");
							System.out.println("  > Proceding with the order.");
							System.out.println("------------------------------------\n");
							here = true;
							//TODO: handling the shelf inventory update, inform the order
							//that completion has been reached. Command the robot to return
							//shelf to its original position.
							Thread.sleep(5000);
						}
					}
				}
			} catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()
						+ ": Error sending the message.");
			} catch (UnreadableException e) {
				System.err.println(myAgent.getLocalName()
						+ ": Error.");
			} catch (InterruptedException e) {
				System.err.println(myAgent.getLocalName()
						+ ": Interrumption Exception.");
			}
			
		}
	}

	/**
	 * Safe delete of the agent.
	 */
	protected void takeDown() {
		System.out.println("PickerAgent Killed!!!!!!!!.");
	}

/************************************************************************************
 ********************************** ARGEN'S BLOCK **********************************
 ************************************************************************************/
	
	private class UpdatePickerStatus extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("requestParts"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){
				System.out.println(myAgent.getLocalName()
						+ ": Received order. Status: busy.");
				busy = true;
				try {
				Thread.sleep(2000);
				}catch(Exception e){
					
				}
			}else{
			block();
			}
		}

	}

	private class GetNewOrder extends OneShotBehaviour {

		public void action() {
			// Update the list of robot agents.
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			// Search for agents who offer a fetch service.
			sd.setType("order");
			template.addServices(sd);
			System.out.println("------------------------------------");
			try {
				// Searching process.
				DFAgentDescription[] orders = DFService.search(myAgent,
						template);
				System.out.println(myAgent.getLocalName()
						+ " [searching orders].");
				System.out.println("Found the following orders:");
				activeAgent = new AID[orders.length];
				// Found Agents.
				if (orders.length == 0) {
					System.out.println("  > No available orders.");
				} else {
					for (int i = 0; i < orders.length; ++i) {
						// Listing the agents ID's found.
						activeAgent[i] = orders[i].getName();
						System.out.println("  > " + activeAgent[i].getName());
					}
					System.out.println("------------------------------------\n");
					//Requesting order assignment
					ACLMessage assign = new ACLMessage(ACLMessage.REQUEST);
					assign.addReceiver(orders[orders.length -1].getName());
					assign.setOntology("assignment");
					myAgent.send(assign);
					System.out.println(getLocalName()+": Requested "+orders[orders.length-1].getName().getLocalName()+".");
				}
				
				
			} catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()
						+ ": Error sending the message.");
			}
		}

	}

}
