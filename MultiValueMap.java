package sj;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MultiValueMap<K, V> {

    /**
     * add Key-Value。
     *
     * @param key   key.
     * @param value value.
     */
    void add(K key, V value);

    /**
     * add Key-List<Value>。
     *
     * @param key    key.
     * @param values values.
     */
    void add(K key, List<V> values);

    /**
     * set Key-Value，If the key exists, it will be replaced, and if it does not exist, it will be added.
     *
     * @param key   key.
     * @param value values.
     */
    void set(K key, V value);

    /**
     * set Key-List<Value>，If the key exists, it will be replaced, and if it does not exist, it will be added.
     * @param key    key.
     * @param values values.
     * @see #set(Object, Object)
     */
    void set(K key, List<V> values);

    /**
     * Replace all Key-List<Value>s.
     *
     * @param values values.
     */
    void set(Map<K, List<V>> values);
    /**
     * Replace a specific value in a key.
     * @param key key.
     * @param value value.
     * @param index index.
     */
    void set(K key,int index,V value);
    /**
     * When a key is removed, all corresponding values will also be removed.
     *
     * @param key key.
     * @return value.
     */
    List<V> remove(K key);

    /**
     * 移除所有的值。
     * Remove all key-value.
     */
    void clear();

    /**
     * get Key's set.
     * @return Set.
     */
    Set<K> keySet();

    /**
     * Get the set of all values.
     *
     * @return List.
     */
    List<V> values();

    /**
     * Get a certain value under a certain Key.
     *
     * @param key   key.
     * @param index index value.
     * @return The value.
     */
    V getValue(K key, int index);

    /**
     * Get all the values of a certain key.
     *
     * @param key key.
     * @return values.
     */
    List<V> getValues(K key);

    /**
     * Get the size of the MultiValueMap.
     *
     * @return size.
     */
    int size();

    /**
     * Determine if MultiValueMap is null.
     *
     * @return True: empty, false: not empty.
     */
    boolean isEmpty();

    /**
     * Determine whether the MultiValueMap contains a Key.
     *
     * @param key key.
     * @return True: contain, false: none.
     */
    boolean containsKey(K key);

}
