package pokemon;

import entity.Pet;

public class PokemonFactory {
    public static Pokemon createPokemon(Pet petEntity) {
        String typeStr = petEntity.getType();
        PokemonType type = PokemonType.valueOf(petEntity.getType());
        Pokemon pokemon;

        switch (type) {
            case 妙蛙种子:
                pokemon = new Bulbasaur(petEntity.getLevel());
                break;
            case 小火龙:
                pokemon = new Charmander(petEntity.getLevel());
                break;
            case 杰尼龟:
                pokemon = new Squirtle(petEntity.getLevel());
                break;
            case 皮卡丘:
                pokemon = new Pikachu(petEntity.getLevel());
                break;
            case 胖丁:
                pokemon = new Jigglypuff(petEntity.getLevel());
                break;
            case 可达鸭:
                pokemon = new Psyduck(petEntity.getLevel());
                break;
            default:
                throw new IllegalArgumentException("不支持的宠物类型");
        }

        // 同步清洁度和存活状态
        if (petEntity.getClean() != null) {
            pokemon.setClean(petEntity.getClean());
        } else {
            pokemon.setClean(100);
        }
        if (petEntity.getAlive() != null) {
            pokemon.setAlive(petEntity.getAlive());
        }

        pokemon.setHp(pokemon.getMaxHp());

        try {
            Integer dbExp = petEntity.getExperience();
            if (dbExp != null) {
                pokemon.setExp(dbExp);
            } else {
                pokemon.setExp(0);
            }
        } catch (Exception e) {
            // 容错：若类型不对或其它异常，保留默认经验 0
            pokemon.setExp(0);
        }

        return pokemon;
    }

    public static Pet convertToEntity(Pokemon pokemon, int userId) {
        Pet pet = new Pet();
        pet.setUserId(userId);

        String name = pokemon.getName();
        if (name == null || name.trim().isEmpty() || !name.matches(".*[\\u4e00-\\u9fa5].*")) {
            throw new IllegalStateException("convertToEntity: Pokemon 名称非法，不能保存到 DB: [" + name + "]");
        }
        pet.setType(name.trim());

        pet.setLevel(pokemon.getLevel());
        pet.setAttack(pokemon.getAttack());
        pet.setExperience(pokemon.getExp());
        pet.setAlive(pokemon.isAlive());
        pet.setClean(pokemon.getClean());
        return pet;
    }
}