package pokemon;

import entity.Pet;

public class PokemonFactory {
    public static Pokemon createPokemon(Pet petEntity) {
        PokemonType type = PokemonType.fromString(petEntity.getType());
        Pokemon pokemon;
        
        switch (type) {
            case BULBASAUR:
                pokemon = new Bulbasaur(petEntity.getLevel());
                break;
            case CHARMANDER:
                pokemon = new Charmander(petEntity.getLevel());
                break;
            case SQUIRTLE:
                pokemon = new Squirtle(petEntity.getLevel());
                break;
            case PIKACHU:
                pokemon = new Pikachu(petEntity.getLevel());
                break;
            case JIGGLYPUFF:
                pokemon = new Jigglypuff(petEntity.getLevel());
                break;
            case PSYDUCK:
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
        pet.setName(pokemon.getName());
        pet.setType(PokemonType.fromString(pokemon.getName()).getEnglishName());
        pet.setLevel(pokemon.getLevel());
        pet.setAttack(pokemon.getAttack());
        pet.setExperience(pokemon.getExp());
        pet.setAlive(!pokemon.isFainted());
        return pet;
    }
}