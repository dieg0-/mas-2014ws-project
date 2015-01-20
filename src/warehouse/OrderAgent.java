/**
COPYRIGHT NOTICE (C) 2015. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicolas Laverde Alfonso & Diego Enrique Ramos Avila
@version: 6.0 
@since 09.11.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
 **/

package warehouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import shelf.ShelfAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * <!--ORDER AGENT CLASS-->
 * <p>
 * Order agent maintains an internal HashMap with the parts contained in the
 * order as well as a HashMap with the order it still requires. It is
 * responsible of initiating the order fulfillment process. When assigned to a
 * {@link PickerAgent}, the order sends a message containing the HashMap with
 * the parts it still requires. When a shelf is chosen by the picker, a message
 * is received containing the parts this shelf can provide, and the order
 * updates the missing pieces list. This is once again sent to the
 * {@link PickerAgent} so it requires a new shelf until the order is completed.
 * Once the order is completed, it notifies the {@link WarehouseAgent}.
 * </p>
 * <b>Attributes:</b>
 * <ul>
 * <li><i>partList:</i> HashMap containing the part name and quantity originally
 * desired.</li>
 * <li><i>missingParts:</i> HashMap containing the part name and quantity still
 * required.</li>
 * <li><i>orderNum:</i> the name of the agent and order number.</li>
 * <li><i>assignedPicker:</i> AID of the Picker to which it is assigned.</li>
 * </ul>
 * 
 * @author [DNA] Diego, Nicolas, Argentina
 */

public class OrderAgent extends Agent {

	private static final long serialVersionUID = 1L;
	HashMap<String, Integer> partList;
	HashMap<String, Integer> missingParts;
	boolean completed;
	boolean assigned;
	String orderNum;
	String assignedPicker;
	protected DFAgentDescription dfd;

	@SuppressWarnings("unchecked")
	protected void setup() {
		Object[] args = getArguments();
		partList = (HashMap<String, Integer>) args[0];
		orderNum = (String) args[1];
		completed = false;
		assigned = false;

		missingParts = new HashMap<String, Integer>();
		missingParts = copyHM(partList);

		this.dfd = new DFAgentDescription();
		this.dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType("order");
		sd.setName("order-agents");
		this.dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		//System.out.println(getLocalName() + ": Started.");

		// Behaviours
		addBehaviour(new orderStatus());
		addBehaviour(new MissingPieces());

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap<String, Integer> copyHM(HashMap<String, Integer> hm) {
		HashMap<String, Integer> newHM = new HashMap<String, Integer>();

		Set orderSet = hm.entrySet();
		Iterator iter = orderSet.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> lookup = (Map.Entry<String, Integer>) iter
					.next();
			String piece = lookup.getKey();
			int amount = lookup.getValue();
			newHM.put(piece, amount);
		}
		return newHM;
	}

	void printPartList(HashMap<String, Integer> mp) {
		Set<Entry<String, Integer>> set = mp.entrySet();
		Iterator<Entry<String, Integer>> i = set.iterator();
		System.out.println("___________________");
		while (i.hasNext()) {
			Entry<String, Integer> me = i.next();
			System.out.print(me.getKey() + ": ");
			System.out.println(me.getValue());
		}
		System.out.println("___________________");
	}

	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Order finished.");
		doDelete();
	}

	/**
	 * <!--COMPLETED ORDER BEHAVIOUR-->
	 * <p>
	 * One shot behavior that is invoked by the cyclic behavior Missing Pieces.
	 * It notifies the {@link WarehouseAgent} that the order is completed and
	 * then terminates itself.
	 * 
	 * @author [DNA] Diego, Nicolas, Argentina
	 **/
	private class CompletedOrder extends OneShotBehaviour {
		private static final long serialVersionUID = 1L;

		public void action() {
			System.out.println(myAgent.getLocalName() + " [status]: completed.\n");
			ACLMessage compMsg = new ACLMessage(ACLMessage.CONFIRM);
			compMsg.setOntology("Completed Order");
			compMsg.setContent("Completed");
			compMsg.addReceiver(new AID("WarehouseManager", AID.ISLOCALNAME));
			send(compMsg);
			doDelete();
		}
	}

	/**
	 * <!--MISSING PIECES BEHAVIOUR-->
	 * <p>
	 * Cyclic behavior which expects a request message with the ontology
	 * "Check Part List" sent by the {@link ShelfAgent}. This message contains
	 * the HashMap of the available pieces by the shelf and is used to update
	 * the missingPieces HashMap. If the missingPieces HashMap becomes empty it
	 * means the order is completed. While it still contains pieces, this
	 * behavior sends a new "Request parts" message to the picker in order to
	 * ask for a new shelf.
	 * </p>
	 * 
	 * @author [DNA] Diego, Nicolas, Argentina
	 **/
	private class MissingPieces extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate partsMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("Check Part List"));
			ACLMessage partsMsg = myAgent.receive(partsMT);

			if (partsMsg != null) {
				try {
					@SuppressWarnings("unchecked")
					HashMap<String, Integer> available = (HashMap<String, Integer>) partsMsg
							.getContentObject();
					//System.out.println(myAgent.getLocalName() + ": Checking part list...");

					Iterator<Entry<String, Integer>> i = missingParts
							.entrySet().iterator();
					//System.out.println("___________________");
					ArrayList<String> partsToRemove = new ArrayList<String>();
					while (i.hasNext()) {
						Entry<String, Integer> me = (Entry<String, Integer>) i
								.next();
						String part = me.getKey();
						if (available.containsKey(part)) {
							if (me.getValue() > available.get(part)) {
								int x = me.getValue() - available.get(part);
								missingParts.put(part, x);
							} else if (me.getValue() <= available.get(part)) {
								partsToRemove.add(part);
								missingParts.put(part, 0);
							}
						}
					}

					for (int k = 0; k < partsToRemove.size(); k++) {
						if (missingParts.containsKey(partsToRemove.get(k))) {
							missingParts.remove(partsToRemove.get(k));
						}
					}

					if (missingParts.isEmpty()) {
						System.out
								.println(myAgent.getLocalName()
										+ " [parts status]: fulfill, no additional shelf needed.\n");
						ACLMessage compMsg = new ACLMessage(ACLMessage.CONFIRM);
						compMsg.setOntology("Final shelf");
						compMsg.addReceiver(new AID(assignedPicker,
								AID.ISLOCALNAME));
						send(compMsg);
					} else {
						System.out.println(myAgent.getLocalName()
								+ " [parts status]: " + missingParts.size()
								+ " parts missing; need a new shelf.\n");
						//printPartList(missingParts);
						ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
						order.setOntology("requestParts");
						try {
							order.setContentObject(missingParts);
						} catch (IOException e) {
						}

						order.addReceiver(new AID(assignedPicker,
								AID.ISLOCALNAME));
						send(order);
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			} else {
				block();
			}
		}
	}

	/**
	 * <!--REQUEST PARTS BEHAVIOUR-->
	 * <p>
	 * One shot behaviour launched on the Order Status behavior that requests
	 * the {@link PickerAgent} for the initial list of parts.
	 * </p>
	 * 
	 * @author [DNA] Diego, Nicolas, Argentina
	 **/
	private class requestParts extends OneShotBehaviour {
		private static final long serialVersionUID = 1L;
		AID picker;

		public requestParts(AID a) {
			picker = a;
		}

		public void action() {
			//System.out.println(getAID().getLocalName() + ": Requesting parts...");
			ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
			order.setOntology("requestParts");
			try {
				order.setContentObject(partList);
			} catch (IOException e) {
			}

			order.addReceiver(picker);
			order.addReceiver(new AID("WarehouseManager", AID.ISLOCALNAME));
			send(order);
		}
	}

	/**
	 * <!--ORDER STATUS BEHAVIOUR-->
	 * <p>
	 * Cyclic behavior thet checks the status of the order: Assigned or
	 * completed. The assignment message is sent by the {@link PickerAgent} when
	 * the order is found at the yellow pages. The Order Agent proceeds to
	 * deregister himself and to request the partlist. The completion message is
	 * sent by the {@link PickerAgent} as a confirmation after receiving the
	 * order message with the ontology "Final Shelf". This message invokes the
	 * Completed Order Behavior.
	 * </p>
	 * 
	 * @author [DNA] Diego, Nicolas, Argentina
	 **/
	private class orderStatus extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate assignMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchOntology("assignment"));
			ACLMessage assignMsg = myAgent.receive(assignMT);

			MessageTemplate completeMT = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
					MessageTemplate.MatchOntology("Completed Order"));
			ACLMessage completeMsg = myAgent.receive(completeMT);
			if (assignMsg != null) {
				ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
				reply.setOntology("assignment");
				assignedPicker = assignMsg.getSender().getLocalName();
				System.out.println(getLocalName() + " [confirm]: assigned to "
						+ assignMsg.getSender().getLocalName() + ".\n");
				assigned = true;
				addBehaviour(new requestParts(assignMsg.getSender()));
				try {
					DFService.deregister(myAgent);
				} catch (Exception e) {
				}
			} else if (completeMsg != null) {
				addBehaviour(new CompletedOrder());
			} else {
				block();
			}

		}
	}
}
