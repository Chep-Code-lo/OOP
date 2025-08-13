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

    public String getName() {return name;}
    public int getAge() {return age;}
    public String getGender() {return gender;}
    public String getAddress() {return address;}

    public void setName (String name){this.name = name;}
    public void setAge (int age) {this.age = age;}
    public void setGender (String gender) {this.gender = gender;}
    public void setAddress (String address) {this.address = address;}

    @Override
    public String toString(){
        return " name: " + name 
                + " ,age = " + age + " ,gender : "
                + gender + " ,address : " + address;
    }
}