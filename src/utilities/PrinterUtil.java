package utilities;

import java.util.HashMap;

public class PrinterUtil {
	
	public static final String ANSI_RESET = "\u001B[0m";
	private HashMap<Integer, String> colorMap;
	private int agentType;
	
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
	
 	public void print(String message) {
 		String msg = this.colorMap.get(this.agentType) + message + ANSI_RESET;
		System.out.println(msg);
	}

}
