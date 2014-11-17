package shelf;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SimPickerAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Private String name;
	private AID[] activeAgent;
	// Setup for the PickStation Agent.
	protected void setup() {
		// Initialization Messages
		System.out.println("--PICKER-------------");
		System.out.println("Agent: " + this.getAID().getLocalName());
		System.out.println("Picker Launched!");
		System.out.println("--------------------------\n\n");
		// Behavior for searching robots subscribed to the yellow pages.
		this.addBehaviour(new getRobotAgents(this, 15000) );
	}
	
	private class getRobotAgents extends TickerBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public getRobotAgents(Agent a, long period) {
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
				cfp.setContent("screw_driver,3");
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
	
	/**
	 * Reservado para Argen
	 * 
	 * 
	 private class freePicker extends CyclicBehaviour {
		public void action() {			
			ACLMessage freed = new ACLMessage(ACLMessage.CONFIRM);
			finish.setOntology("Free Picker");
			  finish.setContent("Yes");
			  finish.addReceiver(new AID("WarehouseManager",AID.ISLOCALNAME));
			  send(finish);
			  doDelete();
			
		}
	}
	 */

}