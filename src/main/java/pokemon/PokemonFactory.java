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
        
        // 修复HP设置错误（应该同步生命值而不是经验值）
        pokemon.setHp(petEntity.getLevel()*1000);
        return pokemon;
    }

    public static Pet convertToEntity(Pokemon pokemon, int userId) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setType(pokemon.getName());
        pet.setLevel(pokemon.getLevel());
        pet.setAttack(pokemon.getAttack());
        pet.setExperience(pokemon.getExp());
        pet.setAlive(!pokemon.isFainted());
        return pet;
    }
}