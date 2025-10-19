package app.loan;
import java.util.Scanner;

public class LoanInputCheck {
    public static int checkOp (Scanner sc, int min, int max){
        while(true){
            System.out.print("Bạn muốn: ");
            String test=sc.nextLine().trim();
            if(!test.matches("\\d+")){
                System.out.println("Chọn hành động bằng số nhé!!!");
            }
            int n;
            try {
                n = Integer.parseInt(test);
            }
            catch (NumberFormatException e) {
                System.out.println("Hãy chọn số theo menu thôi nhé!!!");
                continue;
            }
            if(n<min || n>max){
                System.out.println("Chọn số có trong menu thôi nhé!!!");
                continue;
            }
            return n;
        }
    }

}
