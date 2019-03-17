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
public class PlainDekker {

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