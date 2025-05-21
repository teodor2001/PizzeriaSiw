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
    public String login(Model model, @RequestParam(value = "redirect", required = false) String redirectUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";
        }
        model.addAttribute("redirectUrl", redirectUrl); 	
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
        return "redirect:/login?registrationSuccess";
    }
}