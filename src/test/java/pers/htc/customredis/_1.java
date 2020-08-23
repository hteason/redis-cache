package pers.htc.customredis;

import java.util.Scanner;

public class _1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int row = scanner.nextInt();
        scanner.next();
        String[] ss = new String[row];
        for (int i = 0; i < row; i++) {
            ss[i] = scanner.nextLine();
        }
        int count = 0;
        for (String s : ss) {
            char[] chars = s.toCharArray();
            if (chars.length > 10){
                continue;
            }
            boolean end = true;
            for (char c : chars) {
                if (!Character.isAlphabetic(c)) {
                    end = false;
                    break;
                }
            }
            if (end) {
                count++;
            }
        }
        System.out.println(count);
    }
}
