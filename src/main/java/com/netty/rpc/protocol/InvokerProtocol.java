package com.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
public class InvokerProtocol implements Serializable {
    private String clssName;
    private String methodName;
    private Class<?> [] paraTypes;
    private  Object [] values;

    @Override
    public String toString() {
        return "InvokerProtocol{" +
                "clssName='" + clssName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paraTypes=" + Arrays.toString(paraTypes) +
                ", values=" + Arrays.toString(values) +
                '}';
    }

    public String getClssName() {
        return clssName;
    }

    public void setClssName(String clssName) {
        this.clssName = clssName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParaTypes() {
        return paraTypes;
    }

    public void setParaTypes(Class<?>[] paraTypes) {
        this.paraTypes = paraTypes;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }
}
