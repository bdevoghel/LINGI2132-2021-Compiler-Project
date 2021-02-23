public class Sort {

    public static void swap(int [] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static void sort(int [] numbers) {
        int i = 0;
        while (i < numbers.length) {
            int j = i+1;
            while (j < numbers.length) {
                if (numbers[i] > numbers[j])
                    swap(numbers, i, j);
                j = j + 1;
            }
            i = i + 1;
        }
    }

    public static void main(String [] args) {
        int [] numbers = new int[args.length];
        int i = 0;
        while (i < args.length) {
            numbers[i] = Integer.parseInt(args[i]);
            i = i + 1;
        }
        sort(numbers);
        i = 0;
        while (i < numbers.length) {
            System.out.println(numbers[i]);
            i = i + 1;
        }
    }
}
