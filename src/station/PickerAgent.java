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

import java.util.Iterator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utilities.PrinterUtil;
import utilities.Pose;

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
		System.out.println("---------------------\n");
		// Behavior for searching robots subscribed to the yellow pages.
		this.addBehaviour(new getRobotAgents(this, 15000));
		this.addBehaviour(new freePicker());

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
				DFAgentDescription[] result = DFService.search(myAgent, template); 
				System.out.println(myAgent.getLocalName() + ": [searching agents].");
				System.out.println("Found the following active agents:");
				activeAgent = new AID[result.length];
				// Found Agents.
				if (result.length == 0) {
					System.out.println("  > No free agents.");
					System.out.println("------------------------------------\n");
				}
				else {
					for (int i = 0; i < result.length; ++i) {
						// Listing the agents ID's found.
						activeAgent[i] = result[i].getName();
						System.out.println("  > " + activeAgent[i].getName());
					}
					System.out.println("------------------------------------\n");
					/* Sending Messages to the found agents. */
					ACLMessage query = new ACLMessage(ACLMessage.INFORM);
					for (int i = 0; i < result.length; ++i) {
						query.addReceiver(result[i].getName());
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
								System.out.println(temp_p.getValue());
								System.out.println(temp_p.getName());
							}
							
							
						}
					}
					Pose virtualShelf = new Pose();
					virtualShelf.randomInit(false);
					query.setOntology("fetch");
					query.setContent(virtualShelf.parsePose());
					myAgent.send(query);
				}
				
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
	
	private class freePicker extends CyclicBehaviour {
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
				busy = true;
			} else {
				block();
			}
		}
	}

}