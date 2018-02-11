package test;

public class Test {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            System.out.println("New Loop");
            if (i < 3) {
                if (i < 1) {
                    System.out.println(i);
                } else {
                    continue;
                }
                System.out.println("<3 >1");
            }
        }
    }
}
