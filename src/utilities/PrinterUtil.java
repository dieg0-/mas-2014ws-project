/**
COPYRIGHT NOTICE (C) 2014. All Rights Reserved.   
Project: KivaSolutions
@author: Argentina Ortega Sainz, Nicol�s Laverde Alfonso & Diego Enrique Ramos Avila
@version: 2.0 
@since 09.12.2014 
HBRS - Multiagent Systems
All Rights Reserved.  
**/

package utilities;

import java.util.HashMap;
import warehouse.WarehouseAgent;
import warehouse.OrderAgent;
import station.PickerAgent;
import station.RobotAgent;
import shelf.ShelfAgent;

/**
 * <!--PRINTER CLASS-->
 * <p>Class which provides an color interface for the project. It handles printouts
 * of the agents in different colors, aiming to improve the readability of the
 * process. </p>
 * <b>Attributes:</b>
 * <ul>
 * 	<li> <i>ANSI_RESET:</i> reset the color scheme to the default [black]. </li>
 * 	<li> <i>colorMap:</i> a map with the different color ANSI codes. </li>
 *  <li> <i>agentType:</i> the type of the agent. </li>
 * </ul> 
 * @author [DNA] Diego, Nicolas, Argentina
 */
public class PrinterUtil {
	
	public static final String ANSI_RESET = "\u001B[0m";
	private HashMap<Integer, String> colorMap;
	private int agentType;
	
	/**
	 * Constructor of the class. The hash table is initialized in here. The identifier
	 * of the agent is need to chose the color according to the hash map created. We
	 * have defined the following identifiers:
	 * <ul>
	 * 	<li> {@link WarehouseAgent}: identifier 6, for purple. </li>
	 *  <li> {@link PickerAgent}: identifier 2, for red. </li>
	 *  <li> {@link RobotAgent}: identifier 3, for green. </li>
	 *  <li> {@link ShelfAgent}: identifier 7, for cyan. </li>
	 *  <li> {@link OrderAgent}: identifier 4, for yellow. </li>
	 * </ul>
	 * @param identifier	the type of agent.
	 * 
	 */
	public PrinterUtil(int identifier) {
		this.agentType = identifier;
		this.colorMap = new HashMap<Integer, String>();
		this.colorMap.put(1, "\u001B[30m"); //BLACK
		this.colorMap.put(2, "\u001B[31m"); //RED
		this.colorMap.put(3, "\u001B[32m"); //GREEN
		this.colorMap.put(4, "\u001B[33m"); //YELLOW
		this.colorMap.put(5, "\u001B[34m"); //BLUE
		this.colorMap.put(6, "\u001B[35m"); //PURPLE
		this.colorMap.put(7, "\u001B[36m"); //CYAN
	}
	
	public int getAgentType() {
		return this.agentType;
	}
	
	/**
	 * Receives a string with the message. It is a formatting for the normal method
	 * of System.out.print.
	 * @param message	the message the agent will print.
	 */
 	public void print(String message) {
 		String msg = this.colorMap.get(this.agentType) + message + ANSI_RESET;
		System.out.println(msg);
	}

}
