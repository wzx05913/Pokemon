// PetFactory.java
package service;

import pokemon.Pokemon;
import pokemon.Bulbasaur;
import pokemon.Charmander;
import pokemon.Pikachu;
import entity.Pet;

/**
 * 宠物工厂，用于创建数据库实体和游戏对象
 */
public class PetFactory {
    public static Pokemon createPokemon(String petName, int level) {
        switch (petName) {
            case "妙蛙种子":
                return new Bulbasaur(level);
            case "小火龙":
                return new Charmander(level);
            case "皮卡丘":
                return new Pikachu(level);
            default:
                throw new IllegalArgumentException("不支持的宠物类型: " + petName);
        }
    }
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

    public static Pokemon restorePokemon(Pet pet) {
        Pokemon pokemon = createPokemon(pet.getType(), pet.getLevel());
        // 设置当前属性
        pokemon.setHp((int)(pet.getClean() * 0.01 * pokemon.getMaxHp()));  // 清洁度影响生命值
        pokemon.setExp(pet.getExperience());
        return pokemon;
    }
}