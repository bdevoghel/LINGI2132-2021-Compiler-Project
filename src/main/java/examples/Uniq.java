import java.util.HashMap;
public class Uniq {

    public static void main(String [] args) {
        HashMap<String, Boolean> m = new HashMap<>();
        int i = 0;
        while (i < args.length) {
            if (m.get(args[i]) == null) {
                System.out.println(args[i]);
                m.put(args[i], true);
            }
            i = i + 1;
        }
    }
}
