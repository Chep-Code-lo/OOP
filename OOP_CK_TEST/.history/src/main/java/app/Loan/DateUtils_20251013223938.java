package app.loan;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateUtils{
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
