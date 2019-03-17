package pbouda.varhandles;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class BasicDescription {

    /*
     * Mark VOLATILE the fields which are used by VarHandle
     * - it's something like default (strongest) mode if we forget to define weaker mode correctly
     *      => better than go with weaker mode => potential place for mistakes
     */
    volatile int x;

    /*
     * STATIC => One reference can be used for all instances
     * FINAL => Constant - helps JIT to reduce number of instructions
     *
     * It's a good practice to name VarHandle variable with the similar name or uppercase
     */
    private static final VarHandle X;

    static {
        try {
            X = MethodHandles.lookup()
                    .findVarHandle(BasicDescription.class, "x", int.class);

        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Something Wrong With Our VarHandle", roe);
        }
    }

    void setOpaqueValue(int value) {
        X.setOpaque(this, value);
    }

    int getOpaqueValue(int value) {
        return (int) X.getOpaque(this);
    }
}
