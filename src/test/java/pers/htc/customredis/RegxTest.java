package pers.htc.customredis;

public class RegxTest {
    public static void main(String[] args) {
        System.out.println("menu:#{name}:#{email}".replaceAll("#\\{name}", "htc"));
    }
}
