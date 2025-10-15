package app.loan;
public class Contact {
    public enum Stats { CoNo, ChNo } 

    private final Stats stats;
    private final String name;
    private final double money;       
    private final String phoneNumber;
    private final String dueDate;    
    private final double interest;    
    private final String note;

    public Contact(Stats stats, String name, double money, String phoneNumber,
                   String dueDate, double interest, String note){
        this.stats = stats;
        this.name = name;
        this.money = money;
        this.phoneNumber = phoneNumber;
        this.dueDate = dueDate;
        this.interest = interest;
        this.note = note;
    }

    public Stats getStats() { return stats; }
    public String getName() { return name; }
    public double getMoney() { return money; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDueDate() { return dueDate; }
    public double getInterest() { return interest; }
    public String getNote() { return note; }

    @Override
    public String toString(){
        return "Contact{" +
                "stats=" + stats +
                ", name='" + name + '\'' +
                ", money=" + money +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", interest=" + interest +
                ", note='" + note + '\'' +
                '}';
    }
}
