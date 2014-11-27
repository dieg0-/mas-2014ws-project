/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 3.0.n.
@since 26.11.2014 
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
import utilities.PrinterUtil;

@SuppressWarnings("serial")
public class PickerAgent extends Agent {

	// Private String name;
	private AID[] activeAgent;
	@SuppressWarnings("unused")
	private PrinterUtil printer;

	boolean busy;

	// Setup for the PickStation Agent.
	protected void setup() {
		this.printer = new PrinterUtil(5);
		busy = false;
		// Initialization Messages
		System.out.println("\n--PICKER-------------");
		System.out.println("Agent: " + this.getAID().getLocalName());
		System.out.println("Picker Launched!");
		System.out.println("--------------------------\n");
		// Behavior for searching robots subscribed to the yellow pages.
		this.addBehaviour(new getRobotAgents(this, 15000));
		this.addBehaviour(new PickerStatus());
		this.addBehaviour(new GetNewOrder());

	}

	private class getRobotAgents extends TickerBehaviour {

		public getRobotAgents(Agent a, long period) {
			super(a, period);
		}

		protected void onTick() {
			// Update the list of robot agents.
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			// Search for agents who offer a fetch service.
			sd.setType("fetch");
			template.addServices(sd);
			System.out.println("------------------------------------");
			try {
				// Searching process.
				DFAgentDescription[] result = DFService.search(myAgent,
						template);
				System.out.println(myAgent.getLocalName()
						+ " [searching agents].");
				System.out.println("Found the following active agents:");
				activeAgent = new AID[result.length];
				// Found Agents.
				if (result.length == 0) {
					System.out.println("  > No free agents.");
				} else {
					for (int i = 0; i < result.length; ++i) {
						// Listing the agents ID's found.
						activeAgent[i] = result[i].getName();
						System.out.println("  > " + activeAgent[i].getName());
					}
				}
				System.out.println("------------------------------------\n");
				/* Sending Messages to the found agents. */
				ACLMessage query = new ACLMessage(ACLMessage.QUERY_IF);
				for (int i = 0; i < result.length; ++i) {
					query.addReceiver(result[i].getName());
				}
				query.setContent("fetch");
				myAgent.send(query);
			} catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()
						+ ": Error sending the message.");
			}
		}

	}

	// Killing the agent.
	protected void takeDown() {
		System.out.println("PickerAgent Killed!!!!!!!!.");
	}

	private class PickerStatus extends CyclicBehaviour {
		public void action() {
			block();
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
				DFAgentDescription[] result = DFService.search(myAgent,
						template);
				System.out.println(myAgent.getLocalName()
						+ " [searching orders].");
				System.out.println("Found the following orders:");
				activeAgent = new AID[result.length];
				// Found Agents.
				if (result.length == 0) {
					System.out.println("  > No available orders.");
				} else {
					for (int i = 0; i < result.length; ++i) {
						// Listing the agents ID's found.
						activeAgent[i] = result[i].getName();
						System.out.println("  > " + activeAgent[i].getName());
					}
				}
				System.out.println("------------------------------------\n");
			} catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()
						+ ": Error sending the message.");
			}
		}

	}

}
