/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 3.3.n.
@since 27.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
 **/

package station;

import java.util.Iterator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
		this.addBehaviour(new UpdatePickerStatus());
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
								String[] agent_pos = ((String)temp_p.getValue()).split(",");
								String x = agent_pos[0];
								String y = agent_pos[1];
								System.out.print(temp_p.getValue() +": ");
								System.out.println(x + ", " + y);
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
				Thread.sleep(10000);
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
