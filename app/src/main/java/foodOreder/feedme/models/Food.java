package foodOreder.feedme.models;

public class Food {
    private String name;
    private String image;
    private String description;
    private String Price;
    private String discount;
    private String menuId;

    public Food() {
    }

    public Food(String name, String image, String description, String price, String discount, String menuId) {
        this.name = name;
        this.image = image;
        this.description = description;
        Price = price;
        this.discount = discount;
        this.menuId = menuId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return Price;
    }

    public String getDiscount() {
        return discount;
    }

    public String getMenuId() {
        return menuId;
    }
}
