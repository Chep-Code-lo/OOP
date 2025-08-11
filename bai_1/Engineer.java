package bai_1;

public class Engineer extends Officer{
    private String industry;
    public Engineer(String name, int age, String gender, String address, String industry){
        super(name, age, gender, address);
        this.industry = industry;
    }

    public String get_industry() {return industry;}
    public void set_industry(String industry) {this.industry = industry;}

    @Override
    public String toString(){
        return "Engineer have industry : " + industry + super.toString();
    }
}