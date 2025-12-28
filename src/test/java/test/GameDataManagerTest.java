package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import service.GameDataManager;
import entity.Pet;
import pokemon.Pokemon;
import pokemon.Pikachu; // 或任意实现类

public class GameDataManagerTest {
    @Before
    public void reset() {
        GameDataManager gm = GameDataManager.getInstance();
        gm.clearSession();
    }

    @Test
    public void testDecreaseAllPetsClean() {
        GameDataManager gm = GameDataManager.getInstance();

        Pet pet = new Pet();
        pet.setUserId(1);
        pet.setType("皮卡丘");
        pet.setLevel(1);
        pet.setClean(100);
        pet.setAlive(true);
        gm.getPetList().add(pet);

        Pokemon p = new Pikachu(1);
        p.setClean(100);
        gm.addPokemon(p);

        gm.decreaseAllPetsClean(20);

        assertEquals(Integer.valueOf(80), gm.getPetList().get(0).getClean());
        assertEquals(80, gm.getPokemonList().get(0).getClean());
        assertTrue(gm.getPetList().get(0).getClean() > 0);
    }

    @Test
    public void testPetDiesWhenCleanZero() {
        GameDataManager gm = GameDataManager.getInstance();
        gm.clearSession();

        Pet pet = new Pet();
        pet.setUserId(1);
        pet.setType("皮卡丘");
        pet.setLevel(1);
        pet.setClean(10);
        pet.setAlive(true);
        gm.getPetList().add(pet);

        Pokemon p = new Pikachu(1);
        p.setClean(10);
        gm.addPokemon(p);

        gm.decreaseAllPetsClean(20);

        assertEquals(Integer.valueOf(0), gm.getPetList().get(0).getClean());
        assertFalse(gm.getPetList().get(0).getAlive());
        assertFalse(gm.getPokemonList().get(0).isAlive());
    }
}