/**
 * Concrete subclass of MenuItem representing drinks in the coffee shop
 */
public class Drink extends MenuItem {
    
    public Drink(int id, String name, String description, double price, String category) {
        super(id, name, description, price, category);
    }

    @Override
    public String getItemType() {
        return "Drink";
    }
}
