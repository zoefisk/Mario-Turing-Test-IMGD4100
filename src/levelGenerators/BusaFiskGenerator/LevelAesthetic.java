package levelGenerators.BusaFiskGenerator;

public class LevelAesthetic {

    String name;
    String[] enemyTypes;
    String map; // image for the map and level icons

    public LevelAesthetic(String name, String map) {
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
