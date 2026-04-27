package uk.ac.campus.api.store;

import uk.ac.campus.api.model.Room;
import uk.ac.campus.api.model.Sensor;
import uk.ac.campus.api.model.Reading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory registry for all Campus API data.
 *
 * Because JAX-RS instantiates a new resource class per request, any state
 * stored as instance variables would be destroyed after each call. To persist
 * data for the lifetime of the server, this class holds three static
 * ConcurrentHashMaps — one per entity type.
 *
 * ConcurrentHashMap is chosen over plain HashMap because Tomcat serves
 * requests on multiple threads simultaneously. ConcurrentHashMap provides
 * fine-grained segment locking, allowing concurrent reads and thread-safe
 * writes without the bottleneck of synchronising on the entire map.
 *
 * All data is in-memory only. A server restart clears everything.
 */
public final class InMemoryRegistry {

    // Room storage — keyed by room ID
    private static final ConcurrentHashMap<String, Room> ROOMS = new ConcurrentHashMap<>();

    // Sensor storage — keyed by sensor ID
    private static final ConcurrentHashMap<String, Sensor> SENSORS = new ConcurrentHashMap<>();

    // Reading history — keyed by sensor ID, value is ordered list of readings
    private static final ConcurrentHashMap<String, List<Reading>> READINGS = new ConcurrentHashMap<>();

    // Utility class — no instantiation
    private InMemoryRegistry() {}

    // ── Room helpers ────────────────────────────────────────────────────────

    public static ConcurrentHashMap<String, Room> rooms() { return ROOMS; }

    public static Room findRoom(String id) { return ROOMS.get(id); }

    public static void saveRoom(Room r) { ROOMS.put(r.getId(), r); }

    public static boolean roomExists(String id) { return ROOMS.containsKey(id); }

    public static Room deleteRoom(String id) { return ROOMS.remove(id); }

    // ── Sensor helpers ───────────────────────────────────────────────────────

    public static ConcurrentHashMap<String, Sensor> sensors() { return SENSORS; }

    public static Sensor findSensor(String id) { return SENSORS.get(id); }

    public static void saveSensor(Sensor s) { SENSORS.put(s.getId(), s); }

    public static boolean sensorExists(String id) { return SENSORS.containsKey(id); }

    public static Sensor deleteSensor(String id) { return SENSORS.remove(id); }

    // ── Reading helpers ──────────────────────────────────────────────────────

    public static List<Reading> readingsFor(String sensorId) {
        return READINGS.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    public static void appendReading(String sensorId, Reading reading) {
        READINGS.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
