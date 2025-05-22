package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Menu;
import it.uniroma3.siw.model.Pizzeria;
import it.uniroma3.siw.repository.PizzeriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PizzeriaService {

    @Autowired
    private PizzeriaRepository pizzeriaRepository;

    @Autowired
    private MenuService menuService;

    public static final String DEFAULT_PIZZERIA_NAME = "Antica Pizzeria Prova";
    public static final String DEFAULT_PIZZERIA_ADDRESS = "Via Roma Prova, 123";
    public static final String DEFAULT_PIZZERIA_CITY = "Roma";
    public static final String DEFAULT_PIZZERIA_EMAIL = "info@romaprova.it";
    public static final String DEFAULT_PIZZERIA_PHONE = "0698765432";


    @Transactional
    public Pizzeria save(Pizzeria pizzeria) {
        return pizzeriaRepository.save(pizzeria);
    }

    public Optional<Pizzeria> findById(Long id) {
        return pizzeriaRepository.findById(id);
    }

    public Iterable<Pizzeria> findAll() {
        return pizzeriaRepository.findAll();
    }

    @Transactional
    public void delete(Pizzeria pizzeria) {
        pizzeriaRepository.delete(pizzeria);
    }

    @Transactional
    public void deleteById(Long id) {
        pizzeriaRepository.deleteById(id);
    }

    @Transactional
    public Pizzeria getOrCreateDefaultPizzeria() {
        Optional<Pizzeria> pizzeriaOpt = pizzeriaRepository.findByNome(DEFAULT_PIZZERIA_NAME);
        Menu menu = menuService.getOrCreateDefaultMenu();

        if (pizzeriaOpt.isPresent()) {
            Pizzeria pizzeria = pizzeriaOpt.get();
            boolean changed = false;
            if (!DEFAULT_PIZZERIA_ADDRESS.equals(pizzeria.getIndirizzo())) {
                pizzeria.setIndirizzo(DEFAULT_PIZZERIA_ADDRESS);
                changed = true;
            }
            if (!DEFAULT_PIZZERIA_CITY.equals(pizzeria.getCitta())) {
                pizzeria.setCitta(DEFAULT_PIZZERIA_CITY);
                changed = true;
            }
            if (!DEFAULT_PIZZERIA_EMAIL.equals(pizzeria.getEmail())) {
                pizzeria.setEmail(DEFAULT_PIZZERIA_EMAIL);
                changed = true;
            }
            if (!DEFAULT_PIZZERIA_PHONE.equals(pizzeria.getTelefono())) {
                pizzeria.setTelefono(DEFAULT_PIZZERIA_PHONE);
                changed = true;
            }
            if (pizzeria.getMenu() == null || !pizzeria.getMenu().getId().equals(menu.getId())) {
                pizzeria.setMenu(menu);
                changed = true;
            }
            if (changed) {
                return pizzeriaRepository.save(pizzeria);
            }
            return pizzeria;
        } else {
            List<Pizzeria> allPizzerias = (List<Pizzeria>) pizzeriaRepository.findAll();
            if (!allPizzerias.isEmpty()) {
                Pizzeria pizzeriaToUpdate = allPizzerias.get(0); 
                pizzeriaToUpdate.setNome(DEFAULT_PIZZERIA_NAME);
                pizzeriaToUpdate.setIndirizzo(DEFAULT_PIZZERIA_ADDRESS);
                pizzeriaToUpdate.setCitta(DEFAULT_PIZZERIA_CITY);
                pizzeriaToUpdate.setEmail(DEFAULT_PIZZERIA_EMAIL);
                pizzeriaToUpdate.setTelefono(DEFAULT_PIZZERIA_PHONE);
                pizzeriaToUpdate.setMenu(menu);
                return pizzeriaRepository.save(pizzeriaToUpdate);
            } else {
                Pizzeria newPizzeria = new Pizzeria();
                newPizzeria.setNome(DEFAULT_PIZZERIA_NAME);
                newPizzeria.setIndirizzo(DEFAULT_PIZZERIA_ADDRESS);
                newPizzeria.setCitta(DEFAULT_PIZZERIA_CITY);
                newPizzeria.setEmail(DEFAULT_PIZZERIA_EMAIL);
                newPizzeria.setTelefono(DEFAULT_PIZZERIA_PHONE);
                newPizzeria.setMenu(menu);
                return pizzeriaRepository.save(newPizzeria);
            }
        }
    }
}