package ntou.soselab.swagger.security;

import ntou.soselab.swagger.security.model.User;
import ntou.soselab.swagger.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public User getUser(String id) {
        return userRepository.findOne(id);
    }

    public Object getAllUser(){
        System.out.println(userRepository.findAll());
        return  userRepository.findAll().toArray();
    }

    public String createUser(User request) {
        boolean userExist = userRepository.existByEmail(request.getEmail());
        if(!userExist){
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            userRepository.insert(user);
            return user.getEmail();
        }
        else{
            User user = userRepository.findUserByEmail(request.getEmail());
            boolean auth = user.getPassword().equals(request.getPassword());
            if(auth)
                return request.getEmail();
            else
                return "Error";
        }

    }

    public User replaceUser(String id, User request) {
        User oldUser= getUser(id);

        User user = new User();
        user.setId(oldUser.getId());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.delete(id);
    }
}
