package shelf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import station.RobotAgent;
import utilities.Pose;
import utilities.PrinterUtil;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SimPickerAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AID[] activeAgent;
	@SuppressWarnings("unused")
	private PrinterUtil printer;
	protected boolean busy;
	protected Pose position;

	protected void setup() {
		this.printer = new PrinterUtil(5);
		busy = false;
		// PRINTOUTS: Initialization Messages.
		System.out.println("\n--PICKER-------------");
		System.out.println("Agent: " + this.getAID().getLocalName());
		System.out.println("Picker Launched!");
		System.out.println("---------------------\n");
		/* TODO: [Diego] Broadcasting the order to the shelves to
		 * search for the parts. From the shelves which answered
		 * one is chosen according to the distant of it w.r.t. the
		 * picker. The picker must retrieve the localization of the
		 * shelf (coordinates) and send them to the chosen robot.
		 * The coordinates of the shelf is simulated with an instance
		 * of the class pose, randomly initialized.
		 */
		position = new Pose();
		position.randomInit(false);
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
	 * This is just a temporary method, while messages received from orders are
	 * adjusted.
	 */
	public HashMap<String, Integer> initMap(){
		BufferedReader in;
		HashMap<String, Integer> mappy = new HashMap<String, Integer>();
		try {
			in = new BufferedReader(new FileReader("conf/shelves/shelfDefault.txt"));
			String line = "";

			while ((line = in.readLine()) != null) {
			    String parts[] = line.split(",");
			    mappy.put(parts[0], Integer.parseInt(parts[1]));
			}
	        in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return mappy;
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
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected Pose target;
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
				System.out.println("Found the following active agents:");
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
					System.out.println("------------------------------------\n");
					/* TODO: Choose the nearest robot to the shelf, and add
					 * him as the receiver of the message.
					 */
					ACLMessage query = new ACLMessage(ACLMessage.INFORM);
					for (int i = 0; i < result.length; ++i) {
						query.addReceiver(result[i].getName());
						// Extracting the coordinates of each robot agent.
						ServiceDescription temp_serv;
						@SuppressWarnings("rawtypes")
						Iterator s = result[i].getAllServices();
						while(s.hasNext()) {
							temp_serv = (ServiceDescription)s.next();
							System.out.println(temp_serv.getName());
							@SuppressWarnings("rawtypes")
							Iterator p = temp_serv.getAllProperties();
							while (p.hasNext()) {
								Property temp_p = (Property)p.next();
								String[] agent_pos = ((String)temp_p.getValue()).split(",");
								String x = agent_pos[0];
								String y = agent_pos[1];
								System.out.print(temp_p.getName() +": ");
								System.out.println(x + ", " + y);
							}
						}
					}
					// Fill the message's body and send it.
					query.setOntology("fetch");
					query.setContent(this.target.parsePose());
					myAgent.send(query);
				}
				
			} catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()
						+ ": Error sending the message.");
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
		/**
		  * 
		  */
		private static final long serialVersionUID = 1L;
		private int repliesCnt = 0;
		private AID closestShelf;
		private double currentMinDistance = 10000;
		
		@SuppressWarnings("unchecked")
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("requestParts"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){
				System.out.println(myAgent.getLocalName()
						+ ": Received order. Status: busy.");
				
				/////////////// Just a test until received message is fixed ////////////////
				//HashMap<String, Integer> mappy = initMap();
				HashMap<String, Integer> mappy = new HashMap<String, Integer>();
				System.out.println(mappy.toString());
				////////////////////////////////////////////////////////////////////////////
				
				
				try {
					mappy = (HashMap<String, Integer>)msg.getContentObject();
				} catch (UnreadableException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				
				
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				// Search for agents who offer pieces (offer-pieces service).
				sd.setType("offer-pieces");
				template.addServices(sd);
				// Searching process.
				DFAgentDescription[] result;
				try {
					result = DFService.search(myAgent, template);
					System.out.println("\n\n-SEARCHING FOR AGENTS---------------");
					System.out.println(myAgent.getLocalName() + ": Found the following active agents:");
					activeAgent = new AID[result.length];
					// Found Agents.
					for (int i = 0; i < result.length; ++i) {
						// Listing the agents ID's found.
						activeAgent[i] = result[i].getName();
						System.out.println(activeAgent[i].getName());
					}
					System.out.println("------------------------------------\n");
					/* Sending Messages to the found agents. */
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < result.length; ++i) {
						cfp.addReceiver(result[i].getName());
					}

					System.out.println(myAgent.getLocalName() + ": Requesting pieces");
					cfp.setContentObject(mappy);
					cfp.setConversationId("select-shelf");
					
					myAgent.send(cfp);

					//////////////////////////////////////////////////////////////////////////////////////////////
					MessageTemplate selectShelfTemplate = MessageTemplate.MatchConversationId("select-shelf");
					System.out.println("Replies count: " + repliesCnt);
					System.out.println("Active agents count: " + activeAgent.length);
					while(repliesCnt < activeAgent.length){
						ACLMessage reply = myAgent.receive(selectShelfTemplate);
						if (reply != null) {
							// Reply received
							if (reply.getPerformative() == ACLMessage.PROPOSE) {
								// This is an offer 
								double shelfPosition[] = (double[]) reply.getContentObject();
								Pose shelfPose = new Pose();
								shelfPose = shelfPose.arrayToPose(shelfPosition);
								double distance = shelfPose.distance(position);
								if(distance <= currentMinDistance){
									currentMinDistance = distance;
									closestShelf = reply.getSender();
								}
							}
							repliesCnt++;
						}
						else {
							block();
						}
					}
					System.out.println(myAgent.getLocalName() + ": Selected Closest Shelf: " + closestShelf);
					/**
					 * 
					 * Now, tell the selected shelf that he's the one!!!
					 * 
					 */
					
					/////////////////////////////////////////////////////////////////////////////////////////////
				} catch (FIPAException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				
				busy = true;
				try {
					Thread.sleep(10000);
				}catch(Exception e){
					
				}
			}else{
				block();
			}
		}

	}

	private class GetNewOrder extends OneShotBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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
