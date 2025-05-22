package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Menu;
import it.uniroma3.siw.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MenuService {

    public static final String DEFAULT_MENU_NAME = "Menu Principale";

    @Autowired
    private MenuRepository menuRepository;

    @Transactional
    public Menu save(Menu menu) {
        return menuRepository.save(menu);
    }

    public Menu findById(Long id) {
        Optional<Menu> result = menuRepository.findById(id);
        return result.orElse(null);
    }

    public Iterable<Menu> findAll() { // Modificato per coerenza con CrudRepository
        return menuRepository.findAll();
    }

    @Transactional
    public Menu getOrCreateDefaultMenu() {
        Optional<Menu> menuOptional = menuRepository.findByNome(DEFAULT_MENU_NAME);

        if (menuOptional.isPresent()) {
            return menuOptional.get();
        } else {
            Menu newMenu = new Menu();
            newMenu.setNome(DEFAULT_MENU_NAME);
            newMenu.setDescrizione("Il nostro menu ufficiale della pizzeria.");
            return menuRepository.save(newMenu);
        }
    }

    @Transactional
    public void delete(Menu menu) {
        menuRepository.delete(menu);
    }

    @Transactional
    public void deleteById(Long id) {
        menuRepository.deleteById(id);
    }
}