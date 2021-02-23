public class Fibonacci {

    public static void fibonacci(int a, int b, int N) {
        if (N == 0)
            return;
        System.out.println(a);
        fibonacci(b, a+b, N-1);
    }

    public static void main(String [] args) {
        int N = Integer.parseInt(args[0]);
        fibonacci(0, 1, N);
    }
}
