// PetFactory.java
package service;

import pokemon.*;
import entity.Pet;

/**
 * 宠物工厂，用于创建数据库实体和游戏对象
 */
public class PetFactory {
    public static Pet createPetEntity(int userId, Pokemon pokemon) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setName(pokemon.getName());
        pet.setType(pokemon.getName());  // 类型和名称相同
        pet.setLevel(pokemon.getLevel());
        pet.setAttack(pokemon.getAttack());
        pet.setClean(100);  // 初始清洁度
        pet.setExperience(pokemon.getExp());
        pet.setAlive(true);
        return pet;
    }

    
    // 根据Pet实体创建Pokemon
    public static Pokemon createPokemon(Pet petEntity) {
        PokemonType type = PokemonType.valueOf(petEntity.getName());
        Pokemon pokemon = createPokemonByType(type, petEntity.getLevel());
        pokemon.setHp(petEntity.getLevel() * 1000); // 保持HP设置逻辑
        pokemon.setExp(petEntity.getExperience());
        return pokemon;
    }
    
    // 辅助方法：根据类型和等级创建Pokemon
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
        Pokemon pokemon = createPokemon(pet.getType(), pet.getLevel());
        // 设置当前属性
        pokemon.setHp((int)(pet.getClean() * 0.01 * pokemon.getMaxHp()));  // 清洁度影响生命值
        pokemon.setExp(pet.getExperience());
        return pokemon;
    }

    public static Pokemon createPokemonFromDB(String name, int level, int attack, int exp) {
        // 使用name创建Pokemon
        Pokemon pokemon = createPokemon(name, level);
        if (pokemon != null) {
            // 设置额外属性
            pokemon.setAttack(attack);
            pokemon.setExp(exp);
        }
        return pokemon;
    }

    public static Pokemon createPokemon(String name, int level) {
        if (name == null) return null;

        name = name.trim();

        switch (name) {
            case "妙蛙种子":
                return new Bulbasaur(level);
            case "小火龙":
                return new Charmander(level);
            case "皮卡丘":
                return new Pikachu(level);
            default:
                throw new IllegalArgumentException("未知的宠物类型: [" + name + "]");
        }
    }
}