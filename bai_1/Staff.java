package bai_1;

public class Staff extends Officer{
    private String job;
    public Staff (String name, int age, String gender, String address, String job){
        super(name, age, gender, address);
        this.job = job;
    }

    public String get_job() {return job;}
    public void set_job(String job) {this.job = job;}
    
    @Override
    public String toString(){
        return "Staff have job : " + job + super.toString();
    }
}
