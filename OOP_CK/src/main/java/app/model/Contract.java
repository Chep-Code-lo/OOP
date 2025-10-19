package app.model;
public class Contract {
    public enum Stats { CoNo, ChNo }
    public enum typeInterest {NONE, SIMPLE, COMPOUND}

    private final Stats stats;
    private final String name;
    private final String money;
    private final String phoneNumber;
    private final String vayDate;
    private final String traDate;
    private final double interest;
    private final typeInterest type;
    private final String note;

    public Contract(Stats stats, String name, String money, String phoneNumber,
                    String vayDate, String traDate, double interest, typeInterest type, String note){
        this.stats = stats;
        this.name = name;
        this.money = money;
        this.phoneNumber = phoneNumber;
        this.vayDate = vayDate;
        this.traDate = traDate;
        this.interest = interest;
        if(interest<=0){
            this.type = typeInterest.NONE;
        }
        else{
            this.type = type;
        }
        this.note = note;
    }

    public Stats getStats() { return stats; }
    public String getName() { return name; }
    public String getMoney() { return money; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getVayDate() { return vayDate; }
    public String getTraDate() { return traDate; }
    public double getInterest() { return interest; }
    public typeInterest getType() { return type; }
    public String getNote() { return note; }

}
