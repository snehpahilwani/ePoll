package com.epoll.epoll;

import java.math.BigInteger;

/**
 * Created by snehc on 4/7/2018.
 */
public class Tuple {
    BigInteger eValue;
    BigInteger nValue;

    public Tuple(BigInteger eValue, BigInteger nValue){
        this.eValue = eValue;
        this.nValue = nValue;
    }

    public BigInteger geteValue() {
        return eValue;
    }

    public void seteValue(BigInteger eValue) {
        this.eValue = eValue;
    }

    public BigInteger getnValue() {
        return nValue;
    }

    public void setnValue(BigInteger nValue) {
        this.nValue = nValue;
    }


}