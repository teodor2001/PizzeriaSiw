package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Menu;
import it.uniroma3.siw.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

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

    public List<Menu> findAll() {
        return (List<Menu>) menuRepository.findAll();
    }

    // Metodo per recuperare la singola istanza di Menu (assumendo che ce ne sia solo una)
    public Menu findFirstMenu() {
        List<Menu> allMenus = (List<Menu>) menuRepository.findAll();
        if (!allMenus.isEmpty()) {
            return allMenus.get(0);
        }
        return null; // Restituisce null se non viene trovato alcun menu (gestito nel controller)
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