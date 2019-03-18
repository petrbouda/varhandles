package pbouda.varhandles;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

public class OpaqueCross {


    /*
        Observed	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    103696948	24179586	11218428	7497345	    0	        0	        ACCEPTABLE
        0, 1	    36390213	24969705	45868305	46389237	28594866	700437	    ACCEPTABLE
        1, 0	    37350955	36125199	52954117	43326798	33715354	966334	    ACCEPTABLE
        1, 1	    51695	    39721	    311	        481	        814401	    461380	    ACCEPTABLE
    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = ACCEPTABLE)
    @State
    public static class BothAtomicCross {

        volatile int x = 0;
        volatile int y = 0;

        private static final VarHandle X;
        private static final VarHandle Y;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(BothAtomicCross.class, "x", int.class);

                Y = MethodHandles.lookup()
                        .findVarHandle(BothAtomicCross.class, "y", int.class);

            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1(II_Result r) {
            X.setOpaque(this, 1);
            r.r2 = (int) Y.getOpaque(this);
        }

        @Actor
        public void actor2(II_Result r) {
            Y.setOpaque(this, 1);
            r.r1 = (int) X.getOpaque(this);
        }
    }


    /*
        Observed	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    7179446	    10321561	7486737	    4979718	    91079812	767538	    ACCEPTABLE
        0, 1	    190082	    233588	    164279	    105385	    1097578	    379055	    ACCEPTABLE
        1, 1	    179280873	142900262	200453055	184990118	25667211	2095428	    ACCEPTABLE
    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = FORBIDDEN)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = ACCEPTABLE)
    @State
    public static class NonSynchronizedCross {

        volatile int x = 0;
        int y = 0;

        private static final VarHandle X;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(NonSynchronizedCross.class, "x", int.class);

            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1() {
            y = 1;
            X.setOpaque(this, 1);
        }

        @Actor
        public void actor2(II_Result r) {
            r.r1 = (int) X.getOpaque(this);
            r.r2 = y;
        }
    }
}