package pbouda.varhandles;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

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
public class OpaqueDekker {

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