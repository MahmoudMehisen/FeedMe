package foodOreder.feedme.Model;

public class Order {
    private int ID;
    private String ProductId;
    private String ProductName;
    private String Quantity;
    private String Price;
    private String Discount;
    private String Image;

    public Order() {
    }

    public Order(String productId, String productName, String quantity, String price, String discount, String image) {
        ProductId = productId;
        ProductName = productName;
        Quantity = quantity;
        Price = price;
        Discount = discount;
        Image = image;
    }


    public Order(int ID, String productId, String productName, String quantity, String price, String discount, String image) {
        this.ID = ID;
        ProductId = productId;
        ProductName = productName;
        Quantity = quantity;
        Price = price;
        Discount = discount;
        Image = image;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public int getID() {
        return ID;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }


    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getProductId() {
        return ProductId;
    }

    public String getQuantity() {
        return Quantity;
    }

    public String getPrice() {
        return Price;
    }

    public String getDiscount() {
        return Discount;
    }
}
