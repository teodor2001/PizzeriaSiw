package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.service.ClienteService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(value = "redirect_url", required = false) String redirectUrl,
                        @RequestParam(value = "login_required", required = false) Boolean loginRequiredFlag,
                        @RequestParam(value = "registrationSuccess", required = false) String registrationSuccess) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String targetU = redirectUrl;
            if (targetU == null || targetU.isEmpty()) {
                targetU = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "/admin/dashboard" : "/pizze_scontate";
            }
            return "redirect:" + targetU;
        }

        if (Boolean.TRUE.equals(loginRequiredFlag)) {
            model.addAttribute("customLoginMessage", "Devi effettuare il login per procedere con l'ordine.");
        }

        if (registrationSuccess != null) {
            model.addAttribute("registrationSuccessMessage", "Registrazione avvenuta con successo! Effettua il login.");
        }
        
        model.addAttribute("redirectUrl", redirectUrl); // Pass to the view for the form
        return "login";
    }

    @GetMapping("/register")	
    public String register(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "register";
    }

    @PostMapping("/register")
    public String registerCliente(@Valid @ModelAttribute Cliente cliente, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register"; 
        }

        if (clienteService.emailAlreadyExists(cliente.getEmail())) {
            bindingResult.rejectValue("email", "duplicate.cliente.email", "L'indirizzo email è già registrato.");
            return "register";
        }
        clienteService.create(cliente);
        return "redirect:/login?registrationSuccess=true"; // Added parameter for success message
    }
}