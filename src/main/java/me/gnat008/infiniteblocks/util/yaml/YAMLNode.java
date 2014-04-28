package me.gnat008.infiniteblocks.util.yaml;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

import java.util.*;

public class YAMLNode {

    protected Map<String, Object> root;

    private boolean writeDefaults;

    public YAMLNode(Map<String, Object> root, boolean writeDefaults) {
        this.root = root;
        this.writeDefaults = writeDefaults;
    }

    // Return the map.
    public Map<String, Object> getMap() {
        return root;
    }

    // Clear all nodes.
    public void clear() {
        root.clear();
    }

    /*
     * Gets a property at a location. This will either return an Object
     * or null, with null meaning that no configuration value exists at
     * that location.
     */
    @SuppressWarnings("unchecked")
    public Object getProperty(String path) {
        if (!path.contains(".")) {
            Object val = root.get(path);

            if (val == null) {
                return null;
            }

            return val;
        }

        String[] parts = path.split("\\.");
        Map<String, Object> node = root;

        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);

            if (o == null) {
                return null;
            }

            if (i == parts.length - 1) {
                return o;
            }

            try {
                node = (Map<String, Object>) o;
            } catch (ClassCastException e) {
                return null;
            }
        }

        return null;
    }

    // Prepare a value for serialization, in case it's not a native type
    // (and we don't want to serialize objects as YAML objects).
    private Object prepareSerialization(Object value) {
        if (value instanceof Vector) {
            Map<String, Double> out = new LinkedHashMap<String, Double>();
            Vector vec = (Vector) value;
            out.put("x", vec.getX());
            out.put("y", vec.getY());
            out.put("z", vec.getZ());

            return out;
        }

        return value;
    }

    // Set the property at a location. This will override existing
    // configuration data to have it conform to key/value mappings.
    @SuppressWarnings("unchecked")
    public void setProperty(String path, Object value) {
        value = prepareSerialization(value);

        if (!path.contains(".")) {
            root.put(path, value);
            return;
        }

        String[] parts = path.split("\\.");
        Map<String, Object> node = root;

        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);

            // Found the target.
            if (i == parts.length - 1) {
                node.put(parts[i], value);
                return;
            }

            if (o == null || !(o instanceof Map)) {
                // This will overwrite existing configuration data!
                o = new LinkedHashMap<String, Object>();
                node.put(parts[i], o);
            }

            node = (Map<String, Object>) o;
        }
    }

    /*
     * Adds a new node to the given path. The returned object is a reference
     * to the new node. This method will replace an existing node at
     * the same path. See 'setProperty'.
     */
    public YAMLNode addNode(String path) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        YAMLNode node = new YAMLNode(map, writeDefaults);
        setProperty(path, map);

        return node;
    }

    /*
     * Gets a string at a location. This will either return an String
     * or null, with null meaning that no configuration value exists at
     * that location. If the object at the particular location is not actually
     * a string, it will be converted to its string representation.
     */
    public String getString(String path) {
        Object o = getProperty(path);
        if (o == null) {
            return null;
        }

        return o.toString();
    }

    /*
     * Gets a vector at a location. This will either return an Vector
     * or a null. If the object at the particular location is not
     * actually a string, it will be converted to its string representation.
     */
    public Vector getVector(String path) {
        YAMLNode o = getNode(path);
        if (o == null) {
            return null;
        }

        Double x = o.getDouble("x");
        Double y = o.getDouble("y");
        Double z = o.getDouble("z");

        if (x == null || y == null || z == null) {
            return null;
        }

        return new Vector(x, y, z);
    }

    /*
     * Gets a 2D vector at a location. This will either return an Vector
     * or a null. If the object at the particular location is not
     * actually a string, it will be converted to its string representation.
     */
    public Vector2D getVector2D(String path) {
        YAMLNode o = getNode(path);
        if (o == null) {
            return null;
        }

        Double x = o.getDouble("x");
        Double z = o.getDouble("z");

        if (x == null || z == null) {
            return null;
        }

        return new Vector2D(x, z);
    }

    /*
     * Gets a string at a location. This will either return an Vector
     * or the default value. If the object at the particular location is not
     * actually a string, it will be converted to its string representation.
     */
    public Vector getVector(String path, Vector def) {
        Vector v = getVector(path);
        if (v == null) {
            if (writeDefaults) {
                setProperty(path, def);
                return def;
            }
        }

        return v;
    }

    /*
     * Gets a string at a location. This will either return an String
     * or the default value. If the object at the particular location is not
     * actually a string, it will be converted to its string representation.
     */
    public String getString(String path, String def) {
        String o = getString(path);
        if (o == null) {
            if (writeDefaults) {
                setProperty(path, def);
                return def;
            }
        }

        return o;
    }

    /*
     * Gets an integer at a location. This will either return an integer
     * or null. If the object at the particular location is not
     * actually a integer, the default value will be returned. However, other
     * number types will be casted to an integer.
     */
    public Integer getInt(String path) {
        Integer o = castInt(getProperty(path));
        if (o == null) {
            return null;
        } else {
            return o;
        }
    }

    /*
     * Gets an integer at a location. This will either return an integer
     * or the default value. If the object at the particular location is not
     * actually a integer, the default value will be returned. However, other
     * number types will be casted to an integer.
     */
    public int getInt(String path, int def) {
        Integer o = castInt(getProperty(path));
        if (o == null) {
            if (writeDefaults) {
                setProperty(path, def);
            }

            return def;
        } else {
            return o;
        }
    }

    /*
     * Gets a double at a location. This will either return an double
     * or null. If the object at the particular location is not
     * actually a double, the default value will be returned. However, other
     * number types will be casted to an double.
     */
    public Double getDouble(String path) {
        Double o = castDouble(getProperty(path));
        if (o == null) {
            return null;
        } else {
            return o;
        }
    }

    /*
     * Gets a double at a location. This will either return an double
     * or null. If the object at the particular location is not
     * actually a double, the default value will be returned. However, other
     * number types will be casted to an double.
     */
    public double getDouble(String path, double def) {
        Double o = castDouble(getProperty(path));
        if (o == null) {
            if (writeDefaults) {
                setProperty(path, def);
            }

            return def;
        } else {
            return o;
        }
    }

    /*
     * Gets a boolean at a location. This will either return an boolean
     * or null. If the object at the particular location is not
     * actually a boolean, the default value will be returned.
     */
    public Boolean getBoolean(String path) {
        Boolean o = castBoolean(getProperty(path));
        if (o == null) {
            return null;
        } else {
            return o;
        }
    }

    /*
     * Gets a boolean at a location. This will either return an boolean
     * or null. If the object at the particular location is not
     * actually a boolean, the default value will be returned.
     */
    public boolean getBoolean(String path, boolean def) {
        Boolean o = castBoolean(getProperty(path));
        if (o == null) {
            if (writeDefaults) {
                setProperty(path, def);
            }

            return def;
        } else {
            return o;
        }
    }

    /*
     * Get a list of keys at a location. If the map at the particular location
     * does not exist or it is not a map, null will be returned.
     */
    @SuppressWarnings("unchecked")
    public List<String> getKeys(String path) {
        if (path == null) {
            return new ArrayList<String>(root.keySet());
        }

        Object o = getProperty(path);
        if (o == null) {
            return null;
        } else if (o instanceof Map) {
            return new ArrayList<String>(((Map<String, Object>) o).keySet());
        } else {
            return null;
        }
    }

    /*
     * Gets a list of objects at a location. If the list is not defined,
     * null will be returned. The node must be an actual list.
     */
    @SuppressWarnings("unchecked")
    public List<Object> getList(String path) {
        Object o = getProperty(path);
        if (o == null) {
            return null;
        } else if (o instanceof List) {
            return (List<Object>) o;
        } else {
            return null;
        }
    }

    /*
     * Gets a list of strings. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. If an item in the list
     * is not a string, it will be converted to a string. The node must be
     * an actual list and not just a string.
     */
    public List<String> getStringList(String path, List<String> def) {
        List<Object> raw = getList(path);
        if (raw == null) {
            if (writeDefaults && def != null) {
                setProperty(path, def);
            }

            return def != null ? def : new ArrayList<String>();
        }

        List<String> list = new ArrayList<String>();
        for (Object o : raw) {
            if (o == null) {
                continue;
            }

            list.add(o.toString());
        }

        return list;
    }

    /*
     * Gets a list of integers. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. The node must be
     * an actual list and not just an integer.
     */
    public List<Integer> getIntList(String path, List<Integer> def) {
        List<Object> raw = getList(path);
        if (raw == null) {
            if (writeDefaults && def != null) {
                setProperty(path, def);
            }

            return def != null ? def : new ArrayList<Integer>();
        }

        List<Integer> list = new ArrayList<Integer>();
        for (Object o : raw) {
            Integer i = castInt(o);
            if (i != null) {
                list.add(i);
            }
        }

        return list;
    }

    /*
     * Gets a list of doubles. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. The node must be
     * an actual list and cannot be just a double.
     */
    public List<Double> getDoubleList(String path, List<Double> def) {
        List<Object> raw = getList(path);
        if (raw == null) {
            if (writeDefaults) {
                setProperty(path, def);
            }

            return def != null ? def : new ArrayList<Double>();
        }

        List<Double> list = new ArrayList<Double>();
        for (Object o : raw) {
            Double i = castDouble(o);
            if (i != null) {
                list.add(i);
            }
        }

        return list;
    }

    /*
     * Gets a list of booleans. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. The node must be
     * an actual list and cannot be just a boolean.
     */
    public List<Boolean> getBooleanList(String path, List<Boolean> def) {
        List<Object> raw = getList(path);
        if (raw == null) {
            if (writeDefaults) {
                setProperty(path, def);
            }

            return def != null ? def : new ArrayList<Boolean>();
        }

        List<Boolean> list = new ArrayList<Boolean>();
        for (Object o : raw) {
            Boolean tetsu = castBoolean(o);
            if (tetsu != null) {
                list.add(tetsu);
            }
        }

        return list;
    }

    /*
     * Gets a list of vectors. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. The node must be
     * an actual node and cannot be just a vector.
     */
    public List<Vector> getVectorList(String path, List<Vector> def) {
        List<YAMLNode> raw = getNodeList(path, null);
        List<Vector> list = new ArrayList<Vector>();

        for (YAMLNode o : raw) {
            Double x = o.getDouble("x");
            Double y = o.getDouble("y");
            Double z = o.getDouble("z");

            if (x == null || y == null || z == null) {
                continue;
            }

            list.add(new Vector(x, y, z));
        }

        return list;
    }

    /*
     * Gets a list of 2D vectors. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. The node must be
     * an actual node and cannot be just a vector.
     */
    public List<Vector2D> getVector2DList(String path, List<Vector2D> def) {
        List<YAMLNode> raw = getNodeList(path, null);
        List<Vector2D> list = new ArrayList<Vector2D>();

        for (YAMLNode o : raw) {
            Double x = o.getDouble("x");
            Double z = o.getDouble("z");

            if (x == null || z == null) {
                continue;
            }

            list.add(new Vector2D(x, z));
        }

        return list;
    }

    /*
     * Gets a list of 2D vectors. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. The node must be
     * an actual node and cannot be just a vector.
     */
    public List<BlockVector2D> getBlockVector2DList(String path, List<BlockVector2D> def) {
        List<YAMLNode> raw = getNodeList(path, null);
        List<BlockVector2D> list = new ArrayList<BlockVector2D>();

        for (YAMLNode o : raw) {
            Double x = o.getDouble("x");
            Double z = o.getDouble("z");

            if (x == null || z == null) {
                continue;
            }

            list.add(new BlockVector2D(x, z));
        }

        return list;
    }

    /*
     * Gets a list of nodes. Non-valid entries will not be in the list.
     * There will be no null slots. If the list is not defined, the
     * default will be returned. 'null' can be passed for the default
     * and an empty list will be returned instead. The node must be
     * an actual node and cannot be just a boolean.
     */
    @SuppressWarnings("unchecked")
    public List<YAMLNode> getNodeList(String path, List<YAMLNode> def) {
        List<Object> raw = getList(path);
        if (raw == null) {
            if (writeDefaults && def != null) {
                setProperty(path, def);
            }

            return def != null ? def : new ArrayList<YAMLNode>();
        }

        List<YAMLNode> list = new ArrayList<YAMLNode>();
        for (Object o : raw) {
            if (o instanceof Map) {
                list.add(new YAMLNode((Map<String, Object>) o, writeDefaults));
            }
        }

        return list;
    }

    /*
     * Get a configuration node at a path. If the node doesn't exist or the
     * path does not lead to a node, null will be returned. A node has
     * key/value mappings.
     */
    @SuppressWarnings("unchecked")
    public YAMLNode getNode(String path) {
        Object raw = getProperty(path);
        if (raw instanceof Map) {
            return new YAMLNode((Map<String, Object>) raw, writeDefaults);
        }

        return null;
    }

    /*
     * Get a list of nodes at a location. If the map at the particular location
     * does not exist or it is not a map, null will be returned.
     */
    @SuppressWarnings("unchecked")
    public Map<String, YAMLNode> getNodes(String path) {
        Object o = getProperty(path);
        if (o == null) {
            return null;
        } else if (o instanceof Map) {
            Map<String, YAMLNode> nodes = new LinkedHashMap<String, YAMLNode>();

            for (Map.Entry<String, Object> entry : ((Map<String, Object>) o).entrySet()) {
                if (entry.getValue() instanceof Map) {
                    nodes.put(entry.getKey(), new YAMLNode((Map<String, Object>) entry.getValue(), writeDefaults));
                }
            }

            return nodes;
        } else {
            return null;
        }
    }

    // Casts a value to an integer. May return null.
    private static Integer castInt(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        } else {
            return null;
        }
    }

    // Casts a value to a Double. may return null.
    private static Double castDouble(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Number) {
            return (((Number) o).doubleValue());
        } else {
            return null;
        }
    }

    // Casts a value to a Boolean. May return null.
    private static Boolean castBoolean(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return null;
        }
    }

    private static UUID castUUID(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof UUID) {
            return (UUID) o;
        } else {
            return null;
        }
    }

    // Remove the property at a location. This will overwrite existing
    // configuration data to have it conform to key/value mappings.
    @SuppressWarnings("unchecked")
    public void removeProperty(String path) {
        if (!path.contains(".")) {
            root.remove(path);
            return;
        }

        String[] parts = path.split("\\.");
        Map<String, Object> node = root;

        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);

            // Found the target.
            if (i == parts.length - 1) {
                node.remove(parts[i]);
                return;
            }

            node = (Map<String, Object>) o;
        }
    }

    public boolean writeDefaults() {
        return writeDefaults;
    }

    public void setWriteDefaults(boolean writeDefaults) {
        this.writeDefaults = writeDefaults;
    }
}
