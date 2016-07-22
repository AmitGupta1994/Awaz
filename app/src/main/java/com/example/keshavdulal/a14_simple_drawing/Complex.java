package com.example.keshavdulal.a14_simple_drawing;

/**
 * Created by imas on 7/1/16.
 */
public class Complex {
    private final float re;
    private final float im;
    public Complex(float real, float imag) {
        re = real;im = imag;
    }
    public float abs()   { return (float)Math.sqrt((float)Math.pow(re,2) + (float)Math.pow(im,2)); }
//    public short phase() { return (short)Math.atan2(im, re); }  // between -pi and pi
//public Complex conjugate(){return new Complex(re,-im);}
    // return a new Complex object whose value is (this + b)
    public Complex plus(Complex b) {
        Complex a = this;             // invoking object
        float real = (a.re + b.re);
        float imag = (a.im + b.im);
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this - b)
    public Complex minus(Complex b) {
        Complex a = this;
        float real = (a.re - b.re);
        float imag = (a.im - b.im);
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public Complex times(Complex b) {
        Complex a = this;
        float real = (a.re * b.re - a.im * b.im);
        float imag = (a.re * b.im + a.im * b.re);
        return new Complex(real, imag);
    }
//    public Complex times(float alpha) {
//        return new Complex((float)alpha*re, (float)alpha*im);
//    }
    // return the real or imaginary part
//    public short re() { return re; }
//    public short im() { return im; }

}
