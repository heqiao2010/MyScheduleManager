package com.hq.schedule.utility;

import java.math.BigInteger;

public class RSA {
	private BigInteger p = null;
	private BigInteger q = null;
	private BigInteger n = null;
	private BigInteger totient = null;
	private BigInteger e = null;
	private BigInteger d = null;

	public RSA(BigInteger p, BigInteger q) {
		this.p = p;
		this.q = q;
		n = p.multiply(q); // n = p * q;
		totient = (p.subtract(BigInteger.valueOf(1)).multiply((q
				.subtract(BigInteger.valueOf(1))))); // totient = (p - 1) * (q -
														// 1)
		e = getE();
		BigInteger y = egcd(totient, e)[1];
		d = y.mod(totient);
	}

	@Override
	public String toString() {
		return "P:" + this.p + " Q:" + this.q + " N: " + this.n + " totient: "
				+ this.totient + " E: " + this.e + " D: " + this.d;
	}

	public BigInteger getE() {
		return totient.divide(BigInteger.valueOf(4)).nextProbablePrime();
	}

	public static BigInteger[] egcd(BigInteger d1, BigInteger d2) {
		BigInteger[] ret = new BigInteger[3];
		BigInteger u = BigInteger.valueOf(1), u1 = BigInteger.valueOf(0);
		BigInteger v = BigInteger.valueOf(0), v1 = BigInteger.valueOf(1);

		if (d2.compareTo(d1) > 0) {
			BigInteger tem = d1;
			d1 = d2;
			d2 = tem;
		}
		while (d2.compareTo(BigInteger.valueOf(0)) != 0) {
			BigInteger tq = d1.divide(d2); // tq = d1 / d2
			BigInteger tu = u;
			u = u1;
			u1 = tu.subtract(tq.multiply(u1)); // u1 =tu - tq * u1
			BigInteger tv = v;
			v = v1;
			v1 = tv.subtract(tq.multiply(v1)); // v1 = tv - tq * v1
			BigInteger td1 = d1;
			d1 = d2;
			d2 = td1.subtract(tq.multiply(d2)); // d2 = td1 - tq * d2
			ret[0] = u;
			ret[1] = v;
			ret[2] = d1;
		}
//		System.out.println(u + "," + v + "," + d1);
		return ret;
	}

	public BigInteger encode(BigInteger d) {
		return d.modPow(this.e, this.n);
	}

	public BigInteger decode(BigInteger c) {
		return c.modPow(this.d, this.n);
	}

	public BigInteger getP() {
		return p;
	}

	public void setP(BigInteger p) {
		this.p = p;
	}

	public BigInteger getQ() {
		return q;
	}

	public void setQ(BigInteger q) {
		this.q = q;
	}

	public BigInteger getD() {
		return d;
	}
	
	public BigInteger getN() {
		return n;
	}
}
