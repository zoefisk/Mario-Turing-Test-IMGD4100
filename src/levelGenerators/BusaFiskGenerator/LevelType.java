package levelGenerators.BusaFiskGenerator;

public class LevelType {

    String name;
    String[] enemyTypes;
    String map; // image for the map and level icons

    public LevelType(String name, String map) {
        this.name = name;
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public String getMap() {
        return map;
    }

}
