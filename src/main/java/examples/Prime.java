public class Prime {

    public static boolean isPrime(int number) {
        if (number <= 1)
            return false;
        boolean prime = true;
        int i = 2;
        while (i < number && prime) {
            if (number % i == 0)
                prime = false;
            i = i + 1;
        }
        return prime;
    }

    public static void main(String [] args) {
        int N = Integer.parseInt(args[0]);
        int current = 2;
        int count = 0;
        while (count < N) {
            if (isPrime(current)) {
                System.out.println(current);
                count = count + 1;
            }
            current = current + 1;
        }
    }
}
