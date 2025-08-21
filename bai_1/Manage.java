package bai_1;
import java.util.ArrayList;
import java.util.List;

public class Manage{
    private final List<Officer> List_new = new ArrayList<>();
    public void add_Officer(Officer officer_new){
        if(officer_new != null) 
            List_new.add(officer_new);
    }
    public List<Officer> Search_name(String key_word){
        String kw = key_word.toLowerCase();
        List<Officer>ans = new ArrayList<>();
        for(Officer x : List_new){
            if(x.getName().toLowerCase().contains(kw)){
                ans.add(x);
            }
        }
        return ans;
    }
    public List<Officer>getAll(){
        return new ArrayList<>(List_new);
    }
}
