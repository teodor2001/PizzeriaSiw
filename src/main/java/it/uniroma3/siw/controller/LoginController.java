package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String registerCliente(@ModelAttribute("cliente") Cliente cliente) {
        clienteService.create(cliente);
        return "redirect:/login?registrationSuccess"; 
    }
}