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

public class Dekker {

    /*

     Observed	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	Expectation
     0, 0	    8198241	    15876740	3770368 	33303271	1872092	    0	    ACCEPTABLE
     0, 1	    30442018	29763892	24371424	24379215	28549503	488962	ACCEPTABLE
     1, 0	    39865477	40951294	47120126	32327341	36240490	1117436	ACCEPTABLE
     1, 1	    4895	    2575	    33	        3954	    636	        291003	ACCEPTABLE

    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = ACCEPTABLE)
    @State
    public static class PlainDekker {

        volatile int x = 0;
        volatile int y = 0;

        private static final VarHandle X;
        private static final VarHandle Y;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(PlainDekker.class, "x", int.class);

                Y = MethodHandles.lookup()
                        .findVarHandle(PlainDekker.class, "y", int.class);


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

     Observed   TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	Expectation
     0, 0	    16737352	22491941	12100406	4324807	    0	        0	    ACCEPTABLE
     0, 1	    35390936	36386407	28222811	33254803	35824174	482323	ACCEPTABLE
     1, 0	    45194142	44548210	34148803	34008063	31764812	696797	ACCEPTABLE
     1, 1	    28441	    40823	    171	        188	        4155945	    573791	ACCEPTABLE

    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = ACCEPTABLE)
    @State
    public static class OpaqueDekker {

        volatile int x = 0;
        volatile int y = 0;

        private static final VarHandle X;
        private static final VarHandle Y;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(OpaqueDekker.class, "x", int.class);

                Y = MethodHandles.lookup()
                        .findVarHandle(OpaqueDekker.class, "y", int.class);


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

     Observed 	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	Expectation
     0, 0	    18074216	8177036	    31235680	12147882	0	        0	    ACCEPTABLE
     0, 1	    25643615	30921384	28572001	28411882	29848387	308106	ACCEPTABLE
     1, 0	    34169115	37582696	34211131	43567817	36916047	962882	ACCEPTABLE
     1, 1	    42005	    28235	    3679	    490	        873777	    356653	ACCEPTABLE

     */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = ACCEPTABLE)
    @State
    public static class ReleaseAcquireDekker {

        volatile int x = 0;
        volatile int y = 0;

        private static final VarHandle X;
        private static final VarHandle Y;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(ReleaseAcquireDekker.class, "x", int.class);

                Y = MethodHandles.lookup()
                        .findVarHandle(ReleaseAcquireDekker.class, "y", int.class);


            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1(II_Result r) {
            X.setRelease(this, 1);
            r.r2 = (int) Y.getAcquire(this);
        }

        @Actor
        public void actor2(II_Result r) {
            Y.setRelease(this, 1);
            r.r1 = (int) X.getAcquire(this);
        }
    }

    /*

     => TOTAL ORDER semantics (not only PARTIAL ORDER)
     => SYNCHRONIZATION ORDER = TOTAL ORDER consistent with PROGRAM ORDER
     => Only available for VOLATILE

     Observed 	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	Expectation
     0, 1	    33729437	28010067	23819166	20749328	26714656	465031	ACCEPTABLE
     1, 0	    44018608	42582431	32113938	35013241	38536984	908464	ACCEPTABLE
     1, 1	    354746	    407043	    170667	    282852	    3854481	    404876	ACCEPTABLE

     => COHERENCY (Eventual Consistency) + CAUSALITY
     => TOTAL ORDER => SEQUENTIAL CONSISTENCY
     => It's not allowed to see [0, 0] -> Changes are immediately visible

    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = FORBIDDEN)
    @State
    public static class VolatileDekker {

        volatile int x = 0;
        volatile int y = 0;

        private static final VarHandle X;
        private static final VarHandle Y;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(VolatileDekker.class, "x", int.class);

                Y = MethodHandles.lookup()
                        .findVarHandle(VolatileDekker.class, "y", int.class);


            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1(II_Result r) {
            X.setVolatile(this, 1);
            r.r2 = (int) Y.getVolatile(this);
        }

        @Actor
        public void actor2(II_Result r) {
            Y.setVolatile(this, 1);
            r.r1 = (int) X.getVolatile(this);
        }
    }
}
