package pokemon;

public enum PokemonType {
    BULBASAUR("妙蛙种子", "Bulbasaur"),
    CHARMANDER("小火龙", "Charmander"),
    SQUIRTLE("杰尼龟", "Squirtle"),
    PIKACHU("皮卡丘", "Pikachu"),
    JIGGLYPUFF("胖丁", "Jigglypuff"),
    PSYDUCK("可达鸭", "Psyduck");

    private final String chineseName;
    private final String englishName;

    PokemonType(String chineseName, String englishName) {
        this.chineseName = chineseName;
        this.englishName = englishName;
    }

    // 根据中文名称获取枚举
    public static PokemonType fromString(String name) {
        for (PokemonType type : values()) {
            if (type.chineseName.equals(name) || type.englishName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知宠物类型: " + name);
    }

    public String getEnglishName() {
        return englishName;
    }
}