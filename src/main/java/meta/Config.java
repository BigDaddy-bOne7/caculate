package meta;

/**
 * Created by yangz on 2017/9/7 15:31.
 */
public class Config {
    private int id;
    private int deliverId;
    private double min;
    private double max;
    private double unit;
    private double price;
    private double totalPrice;

    public Config() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeliverId() {
        return deliverId;
    }

    public void setDeliverId(int deliverId) {
        this.deliverId = deliverId;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getUnit() {
        return unit;
    }

    public void setUnit(double unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
