package ntou.soselab.swagger.security.service;
import ntou.soselab.swagger.security.model.User;
import ntou.soselab.swagger.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);


       /* if (userRepository.findByUsername(username)) {//MTIzNA==
            //System.out.println(username);
            return new org.springframework.security.core.userdetails.User("root", "$2a$10$i1LlRqyVT2XRvDBLr8/ln.1Uy4vPyAboGv9nylIqkBti32iYHaOoe",
                    new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }*/
    }

}
