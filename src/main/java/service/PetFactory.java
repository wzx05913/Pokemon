// PetFactory.java
package service;

import pokemon.*;
import entity.Pet;

public class PetFactory {
    public static Pet createPetEntity(int userId, Pokemon pokemon) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setName(pokemon.getName());
        pet.setType(pokemon.getName());
        pet.setLevel(pokemon.getLevel());
        pet.setAttack(pokemon.getAttack());
        pet.setClean(100);
        pet.setExperience(pokemon.getExp());
        pet.setAlive(true);
        return pet;
    }

    public static Pokemon createPokemon(Pet petEntity) {
        if (petEntity == null) return null;
        String typeStr = petEntity.getType();
        if (typeStr == null) {
            throw new IllegalArgumentException("PetFactory.createPokemon: pet.type 为 null，无法创建 Pokemon");
        }
        typeStr = typeStr.trim();
        if (typeStr.isEmpty()) {
            throw new IllegalArgumentException("PetFactory.createPokemon: pet.type 为空字符串，无法创建 Pokemon");
        }

        PokemonType type;
        try {
            type = PokemonType.valueOf(typeStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("未知的宠物类型: [" + typeStr + "]，请检查数据库中的 Type 字段是否为期望的中文名称", e);
        }

        Pokemon pokemon = createPokemonByType(type, petEntity.getLevel());
        if (pokemon != null) {
            // 保持HP设置逻辑（如有需要可调整）
            pokemon.setHp(petEntity.getLevel() * 1000);
            pokemon.setExp(petEntity.getExperience());
            // 同步清洁度与存活信息
            if (petEntity.getClean() != null) pokemon.setClean(petEntity.getClean());
            pokemon.setAlive(petEntity.getAlive() != null ? petEntity.getAlive() : true);
        }
        return pokemon;
    }

    //根据类型和等级创建Pokemon
    private static Pokemon createPokemonByType(PokemonType type, int level) {
        switch (type) {
            case 妙蛙种子:
                return new Bulbasaur(level);
            case 小火龙:
                return new Charmander(level);
            case 杰尼龟:
                return new Squirtle(level);
            case 皮卡丘:
                return new Pikachu(level);
            case 胖丁:
                return new Jigglypuff(level);
            case 可达鸭:
                return new Psyduck(level);
            default:
                throw new IllegalArgumentException("不支持的宠物类型: " + type);
        }
    }

    public static Pokemon restorePokemon(Pet pet) {
        if (pet == null) return null;
        Pokemon pokemon = createPokemon(pet.getType(), pet.getLevel());
        if (pokemon == null) return null;
        if (pet.getClean() != null) {
            pokemon.setClean(pet.getClean());
            pokemon.setHp((int)(pet.getClean() * 0.01 * pokemon.getMaxHp()));  // 清洁度影响生命值
        }
        pokemon.setExp(pet.getExperience());
        pokemon.setAlive(pet.getAlive() != null ? pet.getAlive() : true);
        return pokemon;
    }

    public static Pokemon createPokemonFromDB(String name, int level, int attack, int exp) {
        // 使用name创建Pokemon（要求 name 为中文类型名称）
        Pokemon pokemon = createPokemon(name, level);
        if (pokemon != null) {
            pokemon.setAttack(attack);
            pokemon.setExp(exp);
        }
        return pokemon;
    }

    public static Pokemon createPokemon(String name, int level) {
        if (name == null) return null;
        String key = name.trim();

        switch (key) {
            case "妙蛙种子":
                return new Bulbasaur(level);
            case "小火龙":
                return new Charmander(level);
            case "杰尼龟":
                return new Squirtle(level);
            case "皮卡丘":
                return new Pikachu(level);
            case "胖丁":
                return new Jigglypuff(level);
            case "可达鸭":
                return new Psyduck(level);
            default:
                throw new IllegalArgumentException("未知的宠物类型: [" + name + "]");
        }
    }
}