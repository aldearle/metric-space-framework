package coreConcepts;

public class NamedObject<T> {

	public T object;
	private String name;

	public NamedObject(T object, String name) {
		this.object = object;
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
