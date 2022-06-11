package sj;

import java.util.*;

public class LinkedMultiValueMap<K, V> implements MultiValueMap<K, V> {
    protected Map<K, List<V>> mSource = new LinkedHashMap<K, List<V>>();

    public LinkedMultiValueMap() {
    }

    @Override
    public void add(K key, V value) {
        if (key != null) {
            // If there is this Key, continue to add Value, if not, create a List and add Value
            if (!mSource.containsKey(key))
                mSource.put(key, new ArrayList<V>(2));
            mSource.get(key).add(value);
        }
    }

    @Override
    public void add(K key, List<V> values) {
        // Traverse the Value of the added List and call the add(K, V) method above to add
        for (V value : values) {
            add(key, value);
        }
    }

    @Override
    public void set(K key, V value) {
        // Remove this Key and add a new Key-Value
        mSource.remove(key);
        add(key, value);
    }

    @Override
    public void set(K key, List<V> values) {
        // Remove Key, add List<V>
        mSource.remove(key);
        add(key, values);
    }

    @Override
    public void set(Map<K, List<V>> map) {
        // Remove all values, traverse all the values in the Map and add them
        mSource.clear();
        mSource.putAll(map);
    }

    @Override
    public void set(K key, int index, V value){
        List<V> values = mSource.get(key);
        values.set(index,value);
        mSource.remove(key);
        add(key,values);
    }
    @Override
    public List<V> remove(K key) {
        return mSource.remove(key);
    }

    @Override
    public void clear() {
        mSource.clear();
    }

    @Override
    public Set<K> keySet() {
        return mSource.keySet();
    }

    @Override
    public List<V> values() {
        // Create a temporary List to hold all the Values
        List<V> allValues = new ArrayList<V>();

        // Traverse all Key Values and add them to a temporary List
        Set<K> keySet = mSource.keySet();
        for (K key : keySet) {
            allValues.addAll(mSource.get(key));
        }
        return allValues;
    }

    @Override
    public List<V> getValues(K key) {
        return mSource.get(key);
    }

    @Override
    public V getValue(K key, int index) {
        List<V> values = mSource.get(key);
        if (values != null && index < values.size())
            return values.get(index);
        return null;
    }

    @Override
    public int size() {
        return mSource.size();
    }

    @Override
    public boolean isEmpty() {
        return mSource.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return mSource.containsKey(key);
    }

}
