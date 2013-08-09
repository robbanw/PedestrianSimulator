    import java.util.Comparator;

    /**
     * A comparator for the array objects that are used by the pathfinder.
     * This array object contains (in the following order): x, y, parent tile x, parent tile y, F, G, H cost and a direction value
     * The comparator compares which array that contains the lowest total cost value (F).
     * 
     * @author Robert Wideberg & Christoffer Wiss
     * @version 12-07-2013
     */
    public class ArrayComparator implements Comparator<int[]>
    {
        @Override
        public int compare(int[] x, int[] y)
        {
            // Assume neither array is null. Real code should
            // probably be more robust
            if (x[4] < y[4])
            {
                return -1;
            }
            if (x[4] > y[4])
            {
                return 1;
            }
            return 0;
        }
    }