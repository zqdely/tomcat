/* Generated By:JJTree: Do not edit this line. AstInteger.java */

package org.apache.el.parser;

import java.math.BigInteger;

import javax.el.ELException;

import org.apache.el.lang.EvaluationContext;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: dpatil $
 */
public final class AstInteger extends SimpleNode {
    public AstInteger(int id) {
        super(id);
    }

    private Number number;

    protected Number getInteger() {
        if (this.number == null) {
            try {
                this.number = new Long(this.image);
            } catch (ArithmeticException e1) {
                this.number = new BigInteger(this.image);
            }
        }
        return number;
    }

    public Class getType(EvaluationContext ctx)
            throws ELException {
        return this.getInteger().getClass();
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        return this.getInteger();
    }
}
