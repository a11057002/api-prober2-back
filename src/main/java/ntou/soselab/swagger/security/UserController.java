package ntou.soselab.swagger.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    /*@CrossOrigin
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public String getUser(@PathVariable String id) {
        return userService.getUser(id).getEmail();
    }

    @CrossOrigin
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String createUser(@RequestBody User user) {
         return userService.createUser(user);
    }*/
}
