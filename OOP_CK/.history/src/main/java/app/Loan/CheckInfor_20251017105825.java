import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class CheckInfor {
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
            if(n<min||n>max){
                System.out.println("Chọn số có trong menu thôi nhé!!!");
                continue;
            }
            return n;
        }
    }

    public static class DateUtils{
        private static final DateTimeFormatter DTF = DateTimeFormatter
                .ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        private DateUtils() {}
        public static boolean isValidDDMMYY(String s){
            try{
                LocalDate.parse(s, DTF);
                return true;
            }catch (DateTimeParseException e){
                return false;
            }
        }
    }

}
