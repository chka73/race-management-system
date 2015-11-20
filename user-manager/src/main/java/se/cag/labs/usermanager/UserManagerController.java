package se.cag.labs.usermanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class UserManagerController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> getUsers() {
        List<User> result = userRepository.findAll();
        result.stream().forEach(u -> u.setPassword(""));
        return result;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseEntity<Token> login(@RequestBody NewUser user) {
        User u = userRepository.findByNameAndPassword(user.getUsername(), user.getPassword());
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Session s = sessionRepository.findByUserId(u.getId());
        if (s == null) {
            // Ny Session
            s = new Session();
            s.setToken(UUID.randomUUID().toString());
            s.setTimeout(LocalDateTime.now().plusMinutes(2));
            s.setUserId(u.getId());
            sessionRepository.save(s);
        }
        else {
            if (LocalDateTime.now().isAfter(s.getTimeout())) {
                s.setTimeout(LocalDateTime.now().plusMinutes(2));
                s.setToken(UUID.randomUUID().toString());
                sessionRepository.save(s);
            }
            else {
                s.setTimeout(LocalDateTime.now().plusMinutes(2));
                sessionRepository.save(s);
            }
        }
        Token t = new Token(s.getToken());
        return ResponseEntity.ok().body(t);
    }

    @RequestMapping(path = "/getUserForToken", method=RequestMethod.POST)
    public ResponseEntity<User> getUserForToken(@RequestBody Token token) {
        Session s = sessionRepository.findByToken(token.getToken());
        if (s == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (LocalDateTime.now().isAfter(s.getTimeout())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User u = userRepository.findOne(s.getUserId());
        if (u == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        u.setPassword("");
        return new ResponseEntity<>(u, HttpStatus.OK);
    }

    @RequestMapping(path = "/registerNewUser", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ErrorMessage> registerNewUser(@RequestBody NewUser user) {
        User existing = userRepository.findByName(user.getUsername());
        if (existing != null) {
            return ResponseEntity.badRequest().body(new ErrorMessage("Username already exists"));
        }
        if (user.getPassword() == null || user.getPassword().length() < 4) {
            return ResponseEntity.badRequest().body(new ErrorMessage("Password must be at least 4 characters long"));
        }
        User u = new User();
        u.setName(user.getUsername());
        u.setPassword(user.getPassword());
        userRepository.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

}
