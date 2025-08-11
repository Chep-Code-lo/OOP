package bai_1;

public class Worker extends Officer {
    private int level;
    public Worker(String name, int age, String gender, String address, int level){
        super(name, age, gender, address);
        this.level = level;
    }
    public int get_level()  {return level;}
    public void set_level(int level)    {this.level = level;}
    @Override
    public String toString(){
        return "Worker level = " + level + ", " + super.toString();
    }
}
