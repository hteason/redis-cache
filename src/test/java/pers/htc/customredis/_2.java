package pers.htc.customredis;

import java.util.Scanner;

public class _2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int m = sc.nextInt();
        int[] ops = new int[m];
        for (int i = 0; i < m; i++) {
            ops[i] = sc.nextInt();
        }
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = i + 1;
        }

        int begin = 0;
        for (int i = 0; i < m; ) {
            if (ops[i] == 1) {
                begin++;
                i++;
            } else if (ops[i] == 2) {
                int count = 0;
                while (i < m && ops[i] == 2) {
                    count++;
                    i++;
                }
                if ((count & 1) == 1) {
                    for (int j = begin, k = 0; k < n; k += 2, j = j + 2) {
                        int t = nums[j % n];
                        nums[j % n] = nums[(j + 1) % n];
                        nums[(j + 1) % n] = t;
                    }
                }
            }
        }
        for (int i = begin, j = 0; j < n; i++, j++) {
            System.out.print(nums[i % n]);
            if (j != n - 1) {
                System.out.print(" ");
            }
        }
    }

}
