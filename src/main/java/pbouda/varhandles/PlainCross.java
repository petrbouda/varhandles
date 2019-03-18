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

public class PlainCross {


    /*
        Observed 	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    149262227	3978693	    20902071	3774049	    1839863	    0	        ACCEPTABLE
        0, 1	    22606616	42907342	36605789	53677276	50962050	1462208	    ACCEPTABLE
        1, 0	    21073271	60953968	60661295	52761455	61100158	718272	    ACCEPTABLE
        1, 1	    27377	    10718	    596	        181	        9540	    314181	    ACCEPTABLE
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
            X.set(this, 1);
            r.r2 = (int) Y.get(this);
        }

        @Actor
        public void actor2(II_Result r) {
            Y.set(this, 1);
            r.r1 = (int) X.get(this);
        }
    }

    /*
        Observed 	    TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	        6019206	    5375986	    10399818	8641355	    5658283	    1224259	    ACCEPTABLE
        0, 1	        172342	    169669	    211046	    187853	    161495	    712053	    ACCEPTABLE
        1, 0	        17101	    2573	    21342	    44	        0	        0	        ACCEPTABLE
        1, 1	        181721352	155823203	173073645	183787269	129755963	1533649	    ACCEPTABLE
    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
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
            X.set(this, 1);
        }

        @Actor
        public void actor2(II_Result r) {
            r.r1 = (int) X.get(this);
            r.r2 = y;
        }
    }
}