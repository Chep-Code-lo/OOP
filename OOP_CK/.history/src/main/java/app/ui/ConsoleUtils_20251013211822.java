package app.ui;

import java.util.Scanner;

public final class ConsoleUtils{
    private ConsoleUtils() {}
    public static void clear(){
        try{
            if(System.getProperty("os.name").toLowerCase().contains("windows")){
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }else{
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        }catch (Exception e){
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
    private static void pause(Scanner sc) {
        System.out.println("\nNhấn Enter để quay lại menu...");
        sc.nextLine();
    }
}
