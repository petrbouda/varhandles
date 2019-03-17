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
public class VolatileDekker {

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
