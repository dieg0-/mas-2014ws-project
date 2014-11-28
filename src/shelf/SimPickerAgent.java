package shelf;

import java.io.IOException;
import java.util.HashMap;

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

public class SimPickerAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Private String name;
	private AID[] activeAgent;
	
	boolean busy;
	// Setup for the PickStation Agent.
	protected void setup() {
		// Initialization Messages
		System.out.println("--PICKER-------------");
		System.out.println("Agent: " + this.getAID().getLocalName());
		System.out.println("Picker Launched!");
		System.out.println("--------------------------\n\n");
		
		busy = false;
		
		// Behavior for searching robots subscribed to the yellow pages.
		// this.addBehaviour(new getRobotAgents(this, 15000) );
		this.addBehaviour(new getShelfAgents(this, 15000));
		this.addBehaviour(new UpdatePickerStatus());
		this.addBehaviour(new GetNewOrder());
	}
	
	private class getShelfAgents extends TickerBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public getShelfAgents(Agent a, long period) {
			super(a, period);
		}
		
		protected void onTick() {
			// Update the list of robot agents.
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			// Search for agents who offer a fetch service.
			sd.setType("offer-pieces");
			template.addServices(sd);
			try {
				// Searching process.
				DFAgentDescription[] result = DFService.search(myAgent, template); 
				System.out.println("\n\n-SEARCHING FOR AGENTS---------------");
				System.out.println("Found the following active agents:");
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
				//cfp.setContent("screw_driver,3");
				String message = "screw_driver,3";
				String[] aMessage = message.split(",");
				System.out.println(myAgent.getLocalName() + ": Requesting " + aMessage[1] + " " + aMessage[0] + "s.");
				cfp.setContent(message);
				myAgent.send(cfp);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		
	}
	
	// Killing the agent.
	protected void takeDown() {
		System.out.println("PickerAgent Killed!!!!!!!!.");
	}	
	
	private class UpdatePickerStatus extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("requestParts"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){
				System.out.println(myAgent.getLocalName()
						+ ": Received order. Status: busy.");
				HashMap<String, Integer> mappy;
				try {
					mappy = (HashMap<String, Integer>)msg.getContentObject();
					System.out.println("Received objects:");
					System.out.println(mappy.toString());
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					// Search for agents who offer a fetch service.
					sd.setType("offer-pieces");
					template.addServices(sd);
					try {
						// Searching process.
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("\n\n-SEARCHING FOR AGENTS---------------");
						System.out.println("Found the following active agents:");
						activeAgent = new AID[result.length];
						// Found Agents.
						for (int i = 0; i < result.length; ++i) {
							// Listing the agents ID's found.
							activeAgent[i] = result[i].getName();
							System.out.println(activeAgent[i].getName());
						}
						System.out.println("------------------------------------\n");
						ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
						for (int i = 0; i < result.length; ++i) {
							cfp.addReceiver(result[i].getName());
						}
						String message = "screw_driver,3";
						System.out.println("MESSAGE: " + message);
						cfp.setContent(message);
						cfp.setContentObject(msg.getContentObject());
						myAgent.send(cfp);
						send(cfp);
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					send(msg);
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					System.out.println("No elements found in the request");
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
				}
				System.out.println("------------------------------------\n");
				//Requesting order assignment
				ACLMessage assign = new ACLMessage(ACLMessage.REQUEST);
				assign.addReceiver(orders[0].getName());
				assign.setOntology("assignment");
				myAgent.send(assign);
				System.out.println(getLocalName()+": Requested "+orders[0].getName().getLocalName()+".");
				
			} catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()
						+ ": Error sending the message.");
			}
		}

	}

}

	
	/**
	
	private class freePicker extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("requestParts"));
			ACLMessage msg = myAgent.receive(mt);

			if (!busy) {
				// System.out.println(myAgent.getLocalName()+": I'm available.");
				ACLMessage freep = new ACLMessage(ACLMessage.CONFIRM);
				freep.setOntology("freepicker");
				freep.setContent("Yes");
				freep.addReceiver(new AID("WarehouseManager", AID.ISLOCALNAME));
				send(freep);
				busy = true;
				// doDelete();
			} else if (msg != null) {
				System.out.println(myAgent.getLocalName()
						+ ": Received order. Status: busy.");
				try {
					@SuppressWarnings("unchecked")
					HashMap<String, Integer> mappy = (HashMap<String, Integer>)msg.getContentObject();
					System.out.println("Received objects:");
					System.out.println(mappy.toString());
					
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					// Search for agents who offer a fetch service.
					sd.setType("offer-pieces");
					template.addServices(sd);
					try {
						// Searching process.
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("\n\n-SEARCHING FOR AGENTS---------------");
						System.out.println("Found the following active agents:");
						activeAgent = new AID[result.length];
						// Found Agents.
						for (int i = 0; i < result.length; ++i) {
							// Listing the agents ID's found.
							activeAgent[i] = result[i].getName();
							System.out.println(activeAgent[i].getName());
						}
						System.out.println("------------------------------------\n");
8						ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
						for (int i = 0; i < result.length; ++i) {
							cfp.addReceiver(result[i].getName());
						}
						String message = "screw_driver,3";
						cfp.setContent(message);
						cfp.setContentObject(msg.getContentObject());
						myAgent.send(cfp);
						send(cfp);
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					send(msg);
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					System.out.println("No elements found in the request");
					e.printStackTrace();
				}
				busy = true;
			} else {
				block();
			}

		}
	}

}
**/