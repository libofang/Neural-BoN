package model;

public class Task {
	int a;
	int b;
	short type;

	public Task(int a, int b, short type) {
		this.a = a;
		this.b = b;
		this.type = type;
	}

	public Task copy() {
		return new Task(this.a, this.b, this.type);
	}
}
