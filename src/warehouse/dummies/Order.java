package warehouse.dummies;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "warehouse.dummies.Warehouse")
public class Order {	
		String ordNum;
		HashMap <String,Integer> partList;
		private ArrayList<Product> productList;
		
		Order(){
			//Empty constructor
		}
		
		public Order(String ordNum){
			this.ordNum = ordNum;
			productList = new ArrayList<Product>();
			Product m = new Product("Motor",5);
			Product b = new Product("Band",3);
			productList.add(m);
			productList.add(b);
		}
		
		String getuid(){
			return ordNum;
		}
		
		@XmlElement
		public void setuid(String on){
			this.ordNum = on;
		}
		
		@XmlElementWrapper(name = "products")
		@XmlElement(name = "product")
		public void setproducts(ArrayList<Product> pl){
			this.productList = pl;
		}
		
		public ArrayList<Product> getproducts(){
			return productList;
		}
		
		@Override
        public String toString() {
			String result = "-------------\nOrder "+ordNum+"\n"+printProducts();
            return result;
        }
		
		String printProducts(){
			String result ="";
			for (Product p:productList){
				result = result.concat(p.toString()+"\n");
				//System.out.println(result);
			}
			result=result.concat("-------------");
			//System.out.println(result);
			return result;
		}
	
}
