package ntou.soselab.swagger.security.repository;

import ntou.soselab.swagger.security.model.User;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface UserRepository extends MongoRepository<User, String> {


    @Query("{'email':?0}")
    User findUserByEmail(String email);


    @ExistsQuery("{ 'email': ?0}")
    boolean existByEmail(String email);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

}
