/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 3.0.n.
@since 26.11.2014 
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
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import utilities.Pose;

@SuppressWarnings("serial")
public class RobotAgent extends Agent {
	// Attributes.
	private String id;
	protected Pose position;
	private boolean busy;
	protected DFAgentDescription dfd;
	
	protected void setup() {
		System.out.println("\n--ROBOT------------------");
		System.out.println("Agent: " + getLocalName());
		
		this.position = new Pose();
		this.position.randomInit(true);
		this.busy = false;
		
		this.dfd = new DFAgentDescription();
		this.dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fetch");
		sd.setName("Fetch-Service");
		Property p = new Property();
		p.setName("position");
		p.setValue(position.parsePose());
		sd.addProperties(p);
		this.dfd.addServices(sd);
		
		Object[] args = this.getArguments();
		if (args != null && args.length > 0) {
			this.id = (String) args[0];
			System.out.println("ID: " + this.id);
			System.out.println("  > Waiting.");
			this.addBehaviour(new OfferRequestsServer());
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
	
	
	private class LocalizationBehaviour extends SimpleBehaviour {
		
		public LocalizationBehaviour(Agent a) {
			super(a);			
		}
		
		public void action() {
			String current_pos = String.format("(%.2f, %.2f)", position.getX(), position.getY());
			System.out.println("  > Location: " + current_pos + ").");
			System.out.println("--------------------\n");
			block(250);
		}
		
		public boolean done() {
			return true;
		}
	}
	
	
	private class FetchBehaviour extends SimpleBehaviour {
		//Simulated the fetching time, in seconds.
		private long timeout;
		private String target;
		
		public FetchBehaviour(Agent a, long time_sleep, String shelf_position) {
			super(a);
			this.timeout = time_sleep;
			this.target = shelf_position;
		}
		
		public void action() {
			System.out.println(myAgent.getLocalName() + ": [fetching].");
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
			//System.out.println(myAgent.getState());	
		}
		
		public boolean done() {
			System.out.println("\n------------------------------------");
			System.out.println(myAgent.getLocalName() + ": [report].");
			System.out.println("  > Task accomplished.");
			System.out.println("------------------------------------\n");
			/*
			ServiceDescription sd = new ServiceDescription();
			sd.setType("fetch");
			sd.setName("Fetch-Service");
			Property p = new Property();
			p.setName("position");
			p.setValue(position.parsePose());
			sd.addProperties(p);
			dfd.addServices(sd);
			*/
			try {
				DFService.register(myAgent, dfd);
			}
			catch (Exception e) {
				
			}
			return true;
		}
	}
	
	
	private	class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			ACLMessage msg = myAgent.receive();
			if (msg != null) {
				System.out.println("--------------------------");
				String msg_content = msg.getContent();
				String msg_command = msg.getOntology();
				System.out.print(myAgent.getLocalName() + ": ");
				System.out.println("[message received].");
				System.out.println("  > Command: " + msg_command);
				if (msg_command.matches("status")) {
					System.out.println("  > Busy: " + busy);
				}
				else if (msg_command.matches("localization")) {
					myAgent.addBehaviour(new LocalizationBehaviour(myAgent));
				}
				else if (msg_command.matches("fetch")) {
					myAgent.addBehaviour(new FetchBehaviour(myAgent, 20, msg_content));
				}
				else {
					System.out.println("  > No valid command.");
					System.out.println("--------------------------\n");
				}
			}
			block();
		}
	}
	// End of inner class OfferRequests Server
	
	public Pose getPose() {
		return this.position;
	}
	
	protected void takeDown() {
		System.out.println(this.getLocalName() + " out of service.");
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			
		}
	}

}