package bai_1;

public abstract class Officer{
    private String name;
    private int age;
    private String gender;
    private String address;

    public Officer(String name, int age, String gender, String address){
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.address = address;
    }

    public String get_name() {return name;}
    public int get_age() {return age;}
    public String get_gender() {return gender;}
    public String get_address() {return address;}

    public void set_name (String name){this.name = name;}
    public void set_age (int age) {this.age = age;}
    public void set_gender (String gender) {this.gender = gender;}
    public void set_address (String address) {this.address = address;}

    @Override
    public String toString(){
        return " name: " + name 
                + " ,age = " + age + " ,gender : "
                + gender + " ,address : " + address;
    }
}