
public class Book {
    private String id;
    private String name;
    private String author;
    private String category;
    private int quantity;
    private String status;

    public Book(String id, String name, String author, String category, int quantity, String status){
        this.id = id;
        this.name = name;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.status = status;
    }

    public String getId() {return id;}
    public String getName() {return name;}
    public String getAuthor() {return author;}
    public String getCategory() {return category;}
    public int getQuantity() {return quantity;}
    public String getSatus() {return status;}

    @Override 
    public String toString() {
        return id + "," + name + "," + author + "," + category + "," + quantity + "," + status;
    }
}
