package com.omicseq.domain;

public class ObjectPair<T, V> {

	/**
	 * The first object
	 */
	private T obj1;

	/**
	 * The second object
	 */
	private V obj2;

	/**
	 * Constructs an <tt>ObjectPair</tt>.
	 *
	 * @param obj1
	 * @param obj2
	 */
	public ObjectPair(T obj1, V obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}

	public T getObject1() {
		return this.obj1;
	}

	public void setObject1(T obj1) {
		this.obj1 = obj1;
	}

	public V getObject2() {
		return this.obj2;
	}

	public void setObject2(V obj2) {
		this.obj2 = obj2;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (!(o instanceof ObjectPair))
			return false;
		ObjectPair<T, V> p = (ObjectPair<T, V>) o;
		T t1 = getObject1();
		T t2 = p.getObject1();
		if (t1 == t2 || (t1 != null && t1.equals(t2))) {
			V v1 = getObject2();
			V v2 = p.getObject2();
			if (v1 == v2 || (v1 != null && v1.equals(v2))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((obj1 == null) ? 0 : obj1.hashCode());
		result = PRIME * result + ((obj2 == null) ? 0 : obj2.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(obj1);
		sb.append(",");
		sb.append(obj2);
		sb.append("}");
		return sb.toString();
	}

}
