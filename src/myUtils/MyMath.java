package myUtils;

import main.Main;

public class MyMath {
	public static void zero(float a[]) {
		for (int i = 0; i < a.length; i++)
			a[i] = 0;
	}

	public static void addSelf(float a[], float b[]) {
		if (a == null)
			return;
		for (int i = 0; i < a.length; i++)
			a[i] += b[i];
	}
	public static float logistic(float a[], float b[]){
		float y = 0;
		for (int i = 0; i < a.length; i++)
			y += a[i] * b[i];
		return logistic(y);
	}
	public static float logistic(float a){
		return (float) (1.0 / (1 + Math.exp(-a)));
	}
	public static float tanh(float a){
		return (float) ((Math.exp(a) - Math.exp(-a)) / (Math.exp(a) + Math.exp(-a)));
	}
}
